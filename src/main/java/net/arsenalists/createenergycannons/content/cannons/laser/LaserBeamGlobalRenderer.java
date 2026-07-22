package net.arsenalists.createenergycannons.content.cannons.laser;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.client.CECClientShaders;
import net.arsenalists.createenergycannons.client.CECRenderTypes;
import net.arsenalists.createenergycannons.content.particle.CECVertexFormats;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


@Environment(EnvType.CLIENT)
public class LaserBeamGlobalRenderer {

    private static final ResourceLocation GRADIENT_TEXTURE =
            CECMod.resource("textures/beam/laser_beam_gradient.png");

    private static final int LAYER_CORE = 0;
    private static final int LAYER_INNER = 1;
    private static final int LAYER_OUTER = 2;
    private static final int LAYER_TENDRIL = 3;
    private static final int LAYER_FLICKER = 4;

    private static final float CORE_RADIUS = 0.10f;
    private static final float INNER_RADIUS = 0.28f;
    private static final float OUTER_RADIUS = 0.65f;
    private static final float FLICKER_RADIUS = 1.05f;

    private static final float CORE_ALPHA = 0.65f;
    private static final float INNER_ALPHA = 0.40f;
    private static final float OUTER_ALPHA = 0.10f;
    private static final float TENDRIL_ALPHA = 0.80f;
    private static final float FLICKER_ALPHA = 0.06f;

    // Fallback alphas (lower because no per-pixel noise modulation)
    private static final float FB_CORE_ALPHA = 0.45f;
    private static final float FB_INNER_ALPHA = 0.25f;
    private static final float FB_OUTER_ALPHA = 0.12f;
    private static final float FB_TENDRIL_ALPHA = 0.65f;
    private static final float FB_FLICKER_ALPHA = 0.05f;

    private static final double[] CROSS_ANGLES = {
            0.0, Math.PI / 5.0, 2.0 * Math.PI / 5.0, 3.0 * Math.PI / 5.0, 4.0 * Math.PI / 5.0
    };

    public record BeamData(
            Vec3 origin, Vec3 direction, int range, int power,
            boolean isMounted, long lastUpdateTick, int colorTint
    ) {}

    private static final Map<Integer, BeamData> ACTIVE_BEAMS = new ConcurrentHashMap<>();

    // CPU-side gradient cache
    private static float[][][] gradientColors = null;

    private static void ensureGradientLoaded() {
        if (gradientColors != null) return;
        try {
            Optional<Resource> opt = Minecraft.getInstance().getResourceManager().getResource(GRADIENT_TEXTURE);
            if (opt.isPresent()) {
                try (var is = opt.get().open()) {
                    NativeImage img = NativeImage.read(is);
                    int w = img.getWidth();
                    int h = img.getHeight();
                    gradientColors = new float[w][h][3];
                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            int pixel = img.getPixelRGBA(x, y);
                            gradientColors[x][y][0] = (pixel & 0xFF) / 255.0f;
                            gradientColors[x][y][1] = ((pixel >> 8) & 0xFF) / 255.0f;
                            gradientColors[x][y][2] = ((pixel >> 16) & 0xFF) / 255.0f;
                        }
                    }
                    img.close();
                }
            }
        } catch (Exception e) {
            CECMod.getLogger().warn("Failed to load laser beam gradient texture", e);
        }
        if (gradientColors == null) {
            gradientColors = new float[5][16][3];
            for (int x = 0; x < 5; x++)
                for (int y = 0; y < 16; y++)
                    gradientColors[x][y] = new float[]{1.0f, 1.0f, 1.0f};
        }
    }

    private static float[] getGradientColor(int layerIndex, int power) {
        ensureGradientLoaded();
        int x = Math.max(0, Math.min(layerIndex, gradientColors.length - 1));
        int y = Math.max(0, Math.min(power - 1, gradientColors[0].length - 1));
        return gradientColors[x][y];
    }

    public static void clearGradientCache() { gradientColors = null; }

    public static void registerWorldBeam(LaserBlockEntity be) {
        if (be.getLevel() == null) return;
        if (be.getFireRate() <= 0 || be.getRange() <= 0) {
            ACTIVE_BEAMS.remove(be.getBlockPos().hashCode());
            return;
        }
        Direction dir = be.getBlockState().getValue(LaserBlock.FACING);
        ACTIVE_BEAMS.put(be.getBlockPos().hashCode(), new BeamData(
                Vec3.atCenterOf(be.getBlockPos()), Vec3.atLowerCornerOf(dir.getNormal()),
                be.getRange(), be.getFireRate(), false,
                be.getLevel().getGameTime(), be.getLensTint()));
    }

    public static void registerMountedBeam(int entityId, Vec3 origin, Vec3 direction,
                                           int range, int power, long gameTick, int colorTint) {
        if (range <= 0 || origin == null || direction == null || origin.equals(Vec3.ZERO)) {
            ACTIVE_BEAMS.remove(entityId);
            return;
        }
        ACTIVE_BEAMS.put(entityId, new BeamData(origin, direction, range, power, true, gameTick, colorTint));
    }

    public static void remove(int key) { ACTIVE_BEAMS.remove(key); }
    public static void clear() { ACTIVE_BEAMS.clear(); }

    private static Boolean shaderModPresent = null;


    private static boolean isShaderModPresent() {
        if (shaderModPresent != null) return shaderModPresent;
        shaderModPresent = false;
        // Iris (Fabric)
        try { Class.forName("net.irisshaders.iris.api.v0.IrisApi"); shaderModPresent = true; } catch (Exception ignored) {}
        // Oculus (Forge port of Iris)
        if (!shaderModPresent) try { Class.forName("net.irisshaders.iris.Iris"); shaderModPresent = true; } catch (Exception ignored) {}
        // OptiFine
        if (!shaderModPresent) try { Class.forName("net.optifine.shaders.Shaders"); shaderModPresent = true; } catch (Exception ignored) {}
        // Embeddium/Rubidium shaders extension
        if (!shaderModPresent) try { Class.forName("org.embeddedt.embeddium.api.EmbeddiumApi"); shaderModPresent = true; } catch (Exception ignored) {}
        return shaderModPresent;
    }

    /**
     * Checks if a shader pack is actually active (not just mod installed).
     * Falls back to mod-presence check if API isn't available.
     */
    private static boolean isShaderPackActive() {
        try {
            // Iris/Oculus API: check if shader pack is in use
            Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = irisApi.getMethod("getInstance").invoke(null);
            return (boolean) irisApi.getMethod("isShaderPackInUse").invoke(instance);
        } catch (Exception e) {
            // No Iris API available - fall back to OptiFine check
            try {
                Class<?> shaders = Class.forName("net.optifine.shaders.Shaders");
                Object currentPack = shaders.getField("currentShaderName").get(null);
                return currentPack != null && !"OFF".equals(currentPack) && !"".equals(currentPack);
            } catch (Exception ignored) {}
            // Can't determine - if shader mod is present, assume active
            return isShaderModPresent();
        }
    }

    // ==================== ENTRY POINT ====================

    /**
     * Main render entry point. Uses custom shader when no shader pack is active,
     * falls back to Create's pipeline when Iris/Oculus has shaders enabled.
     */
    public static void renderFrame(PoseStack poseStack, Vec3 cam, float partialTick, long gameTime) {
        if (ACTIVE_BEAMS.isEmpty()) return;

        boolean shaderLoaded = CECClientShaders.getLaserBeamShader() != null;
        boolean shaderPackOn = isShaderPackActive();
        boolean useCustomShader = shaderLoaded && !shaderPackOn;

        var it = ACTIVE_BEAMS.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            BeamData beam = entry.getValue();
            if (gameTime - beam.lastUpdateTick > 5) { it.remove(); continue; }

            if (useCustomShader) {
                shaderRenderBeam(poseStack, cam, beam, partialTick, gameTime, entry.getKey());
            } else {
                // Accumulate into Create's buffer - drawn after loop
                fallbackRenderBeam(poseStack, cam, beam, partialTick, gameTime, entry.getKey());
            }
        }

        if (!useCustomShader) {
            // Flush Create's buffer
            fallbackFlush();
        }
    }


    private static void shaderRenderBeam(PoseStack poseStack, Vec3 camera, BeamData beam,
                                          float partialTick, long gameTime, int beamHash) {
        Vec3 beamDir = beam.direction.normalize();
        Vec3 beamMid = beam.origin.add(beamDir.scale(beam.range * 0.5));
        Vec3 toCamera = camera.subtract(beamMid).normalize();
        Vec3 right = beamDir.cross(toCamera).normalize();
        if (right.lengthSqr() < 0.001) {
            right = beamDir.cross(new Vec3(0, 1, 0)).normalize();
            if (right.lengthSqr() < 0.001)
                right = beamDir.cross(new Vec3(1, 0, 0)).normalize();
        }
        Vec3 up = right.cross(beamDir).normalize();

        ShaderInstance shader = CECClientShaders.getLaserBeamShader();
        if (shader == null) return;

        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);

        poseStack.pushPose();
        poseStack.translate(beam.origin.x - camera.x, beam.origin.y - camera.y, beam.origin.z - camera.z);
        //? if <1.21 {
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().mulPoseMatrix(poseStack.last().pose());
        //?} else {
        /*// 1.21+: getModelViewStack() returns a joml Matrix4fStack, but the shader
        // reads from a SEPARATE modelViewMatrix field. Without applyModelViewMatrix()
        // after the stack mutation, the shader binds the previous (camera-only) matrix
        // and the beam vertices end up in wildly wrong screen positions.
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().mul(poseStack.last().pose());
        *///?}
        RenderSystem.applyModelViewMatrix();
        poseStack.popPose();

        try {
            if (shader.GAME_TIME != null)
                shader.GAME_TIME.set(((float)(gameTime % 24000) + partialTick) / 24000.0f);

            int power = Math.max(1, Math.min(16, beam.power));
            float powerScale = 0.65f + 0.55f * (power / 16.0f);
            float tintR = 1, tintG = 1, tintB = 1;
            if (beam.colorTint >= 0) {
                tintR = ((beam.colorTint >> 16) & 0xFF) / 255.0f;
                tintG = ((beam.colorTint >> 8) & 0xFF) / 255.0f;
                tintB = (beam.colorTint & 0xFF) / 255.0f;
            }

            Vec3 o = Vec3.ZERO, e = beamDir.scale(beam.range);
            float[] gO = getGradientColor(LAYER_OUTER, power), gF = getGradientColor(LAYER_FLICKER, power);
            float[] gT = getGradientColor(LAYER_TENDRIL, power), gI = getGradientColor(LAYER_INNER, power);
            float[] gC = getGradientColor(LAYER_CORE, power);

            shaderLayer(o, e, right, up, beamDir, LAYER_OUTER, OUTER_RADIUS*powerScale, OUTER_ALPHA, power, gO[0]*tintR, gO[1]*tintG, gO[2]*tintB, shader);
            shaderFlicker(o, e, right, up, FLICKER_RADIUS*powerScale, FLICKER_ALPHA, power, gF[0]*tintR, gF[1]*tintG, gF[2]*tintB, shader);
            shaderTendrils(o, e, right, up, beamDir, TENDRIL_ALPHA, power, powerScale, gameTime, beamHash, gT[0]*tintR, gT[1]*tintG, gT[2]*tintB, shader);
            shaderLayer(o, e, right, up, beamDir, LAYER_INNER, INNER_RADIUS*powerScale, INNER_ALPHA, power, gI[0]*tintR, gI[1]*tintG, gI[2]*tintB, shader);
            shaderLayer(o, e, right, up, beamDir, LAYER_CORE, CORE_RADIUS*powerScale, CORE_ALPHA, power, gC[0]*tintR, gC[1]*tintG, gC[2]*tintB, shader);
        } catch (Exception ex) {
            CECMod.getLogger().error("Error rendering laser beam (shader path)", ex);
        } finally {
            //? if <1.21 {
            RenderSystem.getModelViewStack().popPose();
            //?} else
            /*RenderSystem.getModelViewStack().popMatrix();*/
            // Pair the pop with applyModelViewMatrix() so the shader sees the restored
            // matrix on subsequent draws (see push/mul comment above for the 1.21 reason).
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
        }
    }

    private static void shaderLayer(Vec3 o, Vec3 e, Vec3 right, Vec3 up, Vec3 beamDir,
                                     int layerIndex, float radius, float alpha, int power,
                                     float r, float g, float b, ShaderInstance shader) {
        Tesselator tess = Tesselator.getInstance();
        //? if <1.21 {
        BufferBuilder builder = tess.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, CECVertexFormats.PARTICLE_WITH_OVERLAY);
        //?} else
        /*BufferBuilder builder = tess.begin(VertexFormat.Mode.QUADS, CECVertexFormats.PARTICLE_WITH_OVERLAY);*/
        try {
            for (double angle : CROSS_ANGLES) {
                Vec3 off = right.scale(Math.cos(angle)*radius).add(up.scale(Math.sin(angle)*radius));
                shaderVtx(builder, o.subtract(off), 0, 0, layerIndex, power, alpha, r, g, b);
                shaderVtx(builder, o.add(off),      0, 1, layerIndex, power, alpha, r, g, b);
                shaderVtx(builder, e.add(off),      1, 1, layerIndex, power, alpha, r, g, b);
                shaderVtx(builder, e.subtract(off), 1, 0, layerIndex, power, alpha, r, g, b);
            }
            shader.apply();
            //? if <1.21 {
            BufferUploader.drawWithShader(builder.end());
            //?} else
            /*BufferUploader.drawWithShader(builder.buildOrThrow());*/
            shader.clear();
        } catch (Exception ex) {
            //? if <1.21 {
            try { builder.end(); } catch (Exception ignored) {}
            //?} else
            /*try { builder.buildOrThrow(); } catch (Exception ignored) {}*/
            throw ex;
        }
    }

    private static void shaderFlicker(Vec3 o, Vec3 e, Vec3 right, Vec3 up,
                                       float radius, float alpha, int power,
                                       float r, float g, float b, ShaderInstance shader) {
        Tesselator tess = Tesselator.getInstance();
        //? if <1.21 {
        BufferBuilder builder = tess.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, CECVertexFormats.PARTICLE_WITH_OVERLAY);
        //?} else
        /*BufferBuilder builder = tess.begin(VertexFormat.Mode.QUADS, CECVertexFormats.PARTICLE_WITH_OVERLAY);*/
        try {
            for (Vec3 off : new Vec3[]{right.scale(radius), up.scale(radius)}) {
                shaderVtx(builder, o.subtract(off), 0, 0, LAYER_FLICKER, power, alpha, r, g, b);
                shaderVtx(builder, o.add(off),      0, 1, LAYER_FLICKER, power, alpha, r, g, b);
                shaderVtx(builder, e.add(off),      1, 1, LAYER_FLICKER, power, alpha, r, g, b);
                shaderVtx(builder, e.subtract(off), 1, 0, LAYER_FLICKER, power, alpha, r, g, b);
            }
            shader.apply();
            //? if <1.21 {
            BufferUploader.drawWithShader(builder.end());
            //?} else
            /*BufferUploader.drawWithShader(builder.buildOrThrow());*/
            shader.clear();
        } catch (Exception ex) {
            //? if <1.21 {
            try { builder.end(); } catch (Exception ignored) {}
            //?} else
            /*try { builder.buildOrThrow(); } catch (Exception ignored) {}*/
            throw ex;
        }
    }

    private static void shaderTendrils(Vec3 o, Vec3 e, Vec3 right, Vec3 up, Vec3 beamDir,
                                        float alpha, int power, float powerScale,
                                        long gameTime, int beamHash,
                                        float r, float g, float b, ShaderInstance shader) {
        long seed = beamHash ^ ((gameTime / 3) * 31L);
        Random rand = new Random(seed);
        int count = 6 + (int)((power - 1) / 15.0f * 10);
        Vec3 beamVec = e.subtract(o);

        Tesselator tess = Tesselator.getInstance();
        //? if <1.21 {
        BufferBuilder builder = tess.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, CECVertexFormats.PARTICLE_WITH_OVERLAY);
        //?} else
        /*BufferBuilder builder = tess.begin(VertexFormat.Mode.QUADS, CECVertexFormats.PARTICLE_WITH_OVERLAY);*/
        try {
            for (int i = 0; i < count; i++) {
                float tS = rand.nextFloat() * 0.7f;
                float tE = Math.min(1.0f, tS + 0.1f + rand.nextFloat() * 0.2f);
                Vec3 aS = o.add(beamVec.scale(tS)), aE = o.add(beamVec.scale(tE));
                float ang = rand.nextFloat() * (float)(Math.PI * 2);
                float rd = (0.12f + rand.nextFloat() * 0.22f) * powerScale;
                Vec3 off = right.scale(Math.cos(ang)*rd).add(up.scale(Math.sin(ang)*rd));
                float sw = 0.018f + rand.nextFloat() * 0.025f;
                Vec3 tan = beamDir.cross(off.normalize()).normalize().scale(sw);

                shaderVtx(builder, aS.add(off).subtract(tan), tS, 0, LAYER_TENDRIL, power, alpha, r, g, b);
                shaderVtx(builder, aS.add(off).add(tan),      tS, 1, LAYER_TENDRIL, power, alpha, r, g, b);
                shaderVtx(builder, aE.add(off).add(tan),      tE, 1, LAYER_TENDRIL, power, alpha, r, g, b);
                shaderVtx(builder, aE.add(off).subtract(tan), tE, 0, LAYER_TENDRIL, power, alpha, r, g, b);
            }
            shader.apply();
            //? if <1.21 {
            BufferUploader.drawWithShader(builder.end());
            //?} else
            /*BufferUploader.drawWithShader(builder.buildOrThrow());*/
            shader.clear();
        } catch (Exception ex) {
            //? if <1.21 {
            try { builder.end(); } catch (Exception ignored) {}
            //?} else
            /*try { builder.buildOrThrow(); } catch (Exception ignored) {}*/
            throw ex;
        }
    }

    private static void shaderVtx(BufferBuilder b, Vec3 pos, float u, float v,
                                   int layerIndex, int power, float alpha,
                                   float r, float g, float bx) {
        //? if <1.21 {
        b.vertex(pos.x, pos.y, pos.z).uv(u, v).overlayCoords(layerIndex, power)
                .color(r, g, bx, alpha).uv2(15728880).endVertex();
        //?} else {
        /*b.addVertex((float)pos.x, (float)pos.y, (float)pos.z).setUv(u, v).setOverlay((power << 16) | (layerIndex & 0xFFFF))
                .setColor(r, g, bx, alpha).setLight(15728880);
        *///?}
    }

    private static SuperRenderTypeBuffer fallbackBuffer;
    private static VertexConsumer fallbackConsumer;

    private static void fallbackRenderBeam(PoseStack poseStack, Vec3 camera, BeamData beam,
                                            float partialTick, long gameTime, int beamHash) {
        Vec3 dir = beam.direction.normalize();

        poseStack.pushPose();
        poseStack.translate(
                beam.origin.x - camera.x,
                beam.origin.y - camera.y,
                beam.origin.z - camera.z
        );

        // Rotate beam from vertical (Y-up) to beam direction using yaw/pitch
        float yaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
        float pitch = (float) Math.toDegrees(Math.asin(dir.y));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F - pitch));
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        // Get beam color from gradient + tint
        int power = Math.max(1, Math.min(16, beam.power));
        float[] gC = getGradientColor(LAYER_CORE, power);
        float tintR = 1, tintG = 1, tintB = 1;
        if (beam.colorTint >= 0) {
            tintR = ((beam.colorTint >> 16) & 0xFF) / 255.0f;
            tintG = ((beam.colorTint >> 8) & 0xFF) / 255.0f;
            tintB = (beam.colorTint & 0xFF) / 255.0f;
        }
        float[] colors = {gC[0] * tintR, gC[1] * tintG, gC[2] * tintB, 1.0f};

        // Use mc.renderBuffers().bufferSource() - same as master branch
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // Render using the exact same beacon beam method from master branch
        float powerScale = 0.65f + 0.55f * (power / 16.0f);
        LaserRenderer.renderBeaconBeam(poseStack, bufferSource, partialTick, gameTime,
                0, beam.range + 1, colors, 0.2F * powerScale, 0.25F * powerScale);

        poseStack.popPose();
    }

    //? if <1.21 {
    private static final ResourceLocation BEAM_TEX = new ResourceLocation("textures/entity/beacon_beam.png");
    //?} else
    /*private static final ResourceLocation BEAM_TEX = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");*/

    private static void fallbackFlush() {
        // Flush only the beacon beam RenderTypes (not all batches which breaks Iris)
        MultiBufferSource.BufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();
        buf.endBatch(RenderType.beaconBeam(BEAM_TEX, false));
        buf.endBatch(RenderType.beaconBeam(BEAM_TEX, true));
    }
}
