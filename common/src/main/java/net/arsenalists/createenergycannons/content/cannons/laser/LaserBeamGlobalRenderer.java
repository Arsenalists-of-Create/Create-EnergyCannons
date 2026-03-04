package net.arsenalists.createenergycannons.content.cannons.laser;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.client.CECClientShaders;
import net.arsenalists.createenergycannons.content.particle.CECVertexFormats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class LaserBeamGlobalRenderer {

    private static final ResourceLocation GRADIENT_TEXTURE =
            new ResourceLocation(CECMod.MODID, "textures/beam/laser_beam_gradient.png");

    private static final int LAYER_CORE = 0;
    private static final int LAYER_INNER = 1;
    private static final int LAYER_OUTER = 2;
    private static final int LAYER_TENDRIL = 3;
    private static final int LAYER_FLICKER = 4;

    private static final float CORE_RADIUS = 0.10f;
    private static final float INNER_RADIUS = 0.28f;
    private static final float OUTER_RADIUS = 0.65f;
    private static final float FLICKER_RADIUS = 1.05f;

    // Alpha values per layer (using addiditive blending)
    private static final float CORE_ALPHA = 0.65f;
    private static final float INNER_ALPHA = 0.40f;
    private static final float OUTER_ALPHA = 0.10f;
    private static final float TENDRIL_ALPHA = 0.80f;
    private static final float FLICKER_ALPHA = 0.06f;

    // circular sorta shape
    private static final double[] CROSS_ANGLES = {
            0.0, Math.PI / 5.0, 2.0 * Math.PI / 5.0, 3.0 * Math.PI / 5.0, 4.0 * Math.PI / 5.0
    };

    public record BeamData(
            Vec3 origin,
            Vec3 direction,
            int range,
            int power,
            boolean isMounted,
            long lastUpdateTick,
            int colorTint
    ) {}

    private static final Map<Integer, BeamData> ACTIVE_BEAMS = new ConcurrentHashMap<>();

    public static int dyeColorToTint(@Nullable DyeColor color) {
        if (color == null) return -1;
        float[] rgb = color.getTextureDiffuseColors();
        int r = (int) (rgb[0] * 255);
        int g = (int) (rgb[1] * 255);
        int b = (int) (rgb[2] * 255);
        return (r << 16) | (g << 8) | b;
    }

    public static void registerWorldBeam(LaserBlockEntity be) {
        if (be.getLevel() == null) return;
        if (be.getFireRate() <= 0 || be.getRange() <= 0) {
            ACTIVE_BEAMS.remove(be.getBlockPos().hashCode());
            return;
        }
        Direction dir = be.getBlockState().getValue(LaserBlock.FACING);
        Vec3 direction = Vec3.atLowerCornerOf(dir.getNormal());

        ACTIVE_BEAMS.put(be.getBlockPos().hashCode(), new BeamData(
                Vec3.atCenterOf(be.getBlockPos()),
                direction,
                be.getRange(),
                be.getFireRate(),
                false,
                be.getLevel().getGameTime(),
                dyeColorToTint(be.getLensColor())
        ));
    }

    public static void registerMountedBeam(int entityId, Vec3 origin, Vec3 direction,
                                           int range, int power, long gameTick, int colorTint) {
        if (range <= 0 || origin == null || direction == null || origin.equals(Vec3.ZERO)) {
            ACTIVE_BEAMS.remove(entityId);
            return;
        }
        ACTIVE_BEAMS.put(entityId, new BeamData(origin, direction, range, power, true, gameTick, colorTint));
    }

    public static void remove(int key) {
        ACTIVE_BEAMS.remove(key);
    }

    public static void clear() {
        ACTIVE_BEAMS.clear();
    }

    public static void renderFrame(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                   Vec3 cam, float partialTick, long gameTime) {
        if (ACTIVE_BEAMS.isEmpty()) return;

        var it = ACTIVE_BEAMS.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            BeamData beam = entry.getValue();

            if (gameTime - beam.lastUpdateTick > 5) {
                it.remove();
                continue;
            }

            renderBeamLayers(poseStack, cam, beam, partialTick, gameTime, entry.getKey());
        }
    }

    private static void renderBeamLayers(PoseStack poseStack, Vec3 camera, BeamData beam,
                                         float partialTick, long gameTime, int beamHash) {
        Vec3 beamDir = beam.direction.normalize();

        // Billboard basis vectors
        Vec3 beamMid = beam.origin.add(beamDir.scale(beam.range * 0.5));
        Vec3 toCamera = camera.subtract(beamMid).normalize();
        Vec3 right = beamDir.cross(toCamera).normalize();

        if (right.lengthSqr() < 0.001) {
            right = beamDir.cross(new Vec3(0, 1, 0)).normalize();
            if (right.lengthSqr() < 0.001) {
                right = beamDir.cross(new Vec3(1, 0, 0)).normalize();
            }
        }

        Vec3 up = right.cross(beamDir).normalize();

        ShaderInstance shader = CECClientShaders.getLaserBeamShader();
        if (shader == null) return;

        // state setup
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(3, GRADIENT_TEXTURE);

        poseStack.pushPose();
        poseStack.translate(
                beam.origin.x - camera.x,
                beam.origin.y - camera.y,
                beam.origin.z - camera.z
        );
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().mulPoseMatrix(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
        poseStack.popPose();

        if (shader.GAME_TIME != null) {
            shader.GAME_TIME.set(((float) (gameTime % 24000) + partialTick) / 24000.0f);
        }

        int power = Math.max(1, Math.min(16, beam.power));
        // Power scaling to size
        float powerScale = 0.65f + 0.55f * (power / 16.0f);

        // Get tint color
        float tintR, tintG, tintB;
        if (beam.colorTint >= 0) {
            tintR = ((beam.colorTint >> 16) & 0xFF) / 255.0f;
            tintG = ((beam.colorTint >> 8) & 0xFF) / 255.0f;
            tintB = (beam.colorTint & 0xFF) / 255.0f;
        } else {
            tintR = 1.0f;
            tintG = 1.0f;
            tintB = 1.0f;
        }

        Vec3 originRel = Vec3.ZERO;
        Vec3 endRel = beamDir.scale(beam.range);

        renderCrossBillboardLayer(originRel, endRel, right, up, beamDir,
                LAYER_OUTER, OUTER_RADIUS * powerScale, OUTER_ALPHA, power, tintR, tintG, tintB, shader);
        renderFlickerLayer(originRel, endRel, right, up,
                FLICKER_RADIUS * powerScale, FLICKER_ALPHA, power, tintR, tintG, tintB, shader);
        renderTendrils(originRel, endRel, right, up, beamDir,
                TENDRIL_ALPHA, power, powerScale, gameTime, beamHash, tintR, tintG, tintB, shader);
        renderCrossBillboardLayer(originRel, endRel, right, up, beamDir,
                LAYER_INNER, INNER_RADIUS * powerScale, INNER_ALPHA, power, tintR, tintG, tintB, shader);
        renderCrossBillboardLayer(originRel, endRel, right, up, beamDir,
                LAYER_CORE, CORE_RADIUS * powerScale, CORE_ALPHA, power, tintR, tintG, tintB, shader);

        // Restore state
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }


    private static void renderCrossBillboardLayer(Vec3 originRel, Vec3 endRel, Vec3 right, Vec3 up,
                                                   Vec3 beamDir, int layerIndex, float radius, float alpha,
                                                   int power, float tintR, float tintG, float tintB, ShaderInstance shader) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, CECVertexFormats.PARTICLE_WITH_OVERLAY);

        for (double angle : CROSS_ANGLES) {
            // Rotate the offset vector around the beam axis
            Vec3 offset = right.scale(Math.cos(angle) * radius)
                    .add(up.scale(Math.sin(angle) * radius));

            Vec3 v0 = originRel.subtract(offset);
            Vec3 v1 = originRel.add(offset);
            Vec3 v2 = endRel.add(offset);
            Vec3 v3 = endRel.subtract(offset);

            addVertex(builder, v0, 0.0f, 0.0f, layerIndex, power, alpha, tintR, tintG, tintB);
            addVertex(builder, v1, 0.0f, 1.0f, layerIndex, power, alpha, tintR, tintG, tintB);
            addVertex(builder, v2, 1.0f, 1.0f, layerIndex, power, alpha, tintR, tintG, tintB);
            addVertex(builder, v3, 1.0f, 0.0f, layerIndex, power, alpha, tintR, tintG, tintB);
        }

        shader.apply();
        BufferUploader.drawWithShader(builder.end());
        shader.clear();
    }

    /**
     * Two cross-billboards (0/90 deg) for ambient flicker glow — more volumetric than a single quad.
     */
    private static void renderFlickerLayer(Vec3 originRel, Vec3 endRel, Vec3 right, Vec3 up,
                                           float radius, float alpha, int power, float tintR, float tintG, float tintB, ShaderInstance shader) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, CECVertexFormats.PARTICLE_WITH_OVERLAY);

        // Two quads at 0 and 90 degrees
        Vec3[] offsets = {right.scale(radius), up.scale(radius)};
        for (Vec3 offset : offsets) {
            Vec3 v0 = originRel.subtract(offset);
            Vec3 v1 = originRel.add(offset);
            Vec3 v2 = endRel.add(offset);
            Vec3 v3 = endRel.subtract(offset);

            addVertex(builder, v0, 0.0f, 0.0f, LAYER_FLICKER, power, alpha, tintR, tintG, tintB);
            addVertex(builder, v1, 0.0f, 1.0f, LAYER_FLICKER, power, alpha, tintR, tintG, tintB);
            addVertex(builder, v2, 1.0f, 1.0f, LAYER_FLICKER, power, alpha, tintR, tintG, tintB);
            addVertex(builder, v3, 1.0f, 0.0f, LAYER_FLICKER, power, alpha, tintR, tintG, tintB);
        }

        shader.apply();
        BufferUploader.drawWithShader(builder.end());
        shader.clear();
    }

    /**
     * Elongated arc strips that crawl along the beam surface like electricity.
     * Seeds persist for 3 ticks so arcs are visible rather than per-frame noise.
     */
    private static void renderTendrils(Vec3 originRel, Vec3 endRel, Vec3 right, Vec3 up,
                                       Vec3 beamDir, float alpha, int power, float powerScale,
                                       long gameTime, int beamHash, float tintR, float tintG, float tintB, ShaderInstance shader) {
        // Seed changes every 3 ticks for persistent arc shapes
        long seed = beamHash ^ ((gameTime / 3) * 31L);
        Random rand = new Random(seed);

        // Power-scaled count: 6 at power 1, 16 at power 16
        int tendrilCount = 6 + (int) ((power - 1) / 15.0f * 10);

        Vec3 beamVec = endRel.subtract(originRel);
        float beamLength = (float) beamVec.length();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, CECVertexFormats.PARTICLE_WITH_OVERLAY);

        for (int i = 0; i < tendrilCount; i++) {
            // Arc start position along beam (0.0 to 0.7)
            float tStart = rand.nextFloat() * 0.7f;
            // Arc length: 10-30% of beam length
            float arcFraction = 0.1f + rand.nextFloat() * 0.2f;
            float tEnd = Math.min(1.0f, tStart + arcFraction);

            Vec3 arcStart = originRel.add(beamVec.scale(tStart));
            Vec3 arcEnd = originRel.add(beamVec.scale(tEnd));

            // Radial position on beam surface
            float angle = rand.nextFloat() * (float) (Math.PI * 2.0);
            float radialDist = (0.12f + rand.nextFloat() * 0.22f) * powerScale;
            Vec3 offset = right.scale(Math.cos(angle) * radialDist)
                    .add(up.scale(Math.sin(angle) * radialDist));

            // Strip width
            float stripWidth = 0.018f + rand.nextFloat() * 0.025f;
            // Use a tangent perpendicular to both beam and offset direction
            Vec3 offsetNorm = offset.normalize();
            Vec3 tangent = beamDir.cross(offsetNorm).normalize().scale(stripWidth);

            Vec3 v0 = arcStart.add(offset).subtract(tangent);
            Vec3 v1 = arcStart.add(offset).add(tangent);
            Vec3 v2 = arcEnd.add(offset).add(tangent);
            Vec3 v3 = arcEnd.add(offset).subtract(tangent);

            // UV: u maps along arc length, v maps across strip width
            float uStart = tStart;
            float uEnd = tEnd;

            addVertex(builder, v0, uStart, 0.0f, LAYER_TENDRIL, power, alpha, tintR, tintG, tintB);
            addVertex(builder, v1, uStart, 1.0f, LAYER_TENDRIL, power, alpha, tintR, tintG, tintB);
            addVertex(builder, v2, uEnd, 1.0f, LAYER_TENDRIL, power, alpha, tintR, tintG, tintB);
            addVertex(builder, v3, uEnd, 0.0f, LAYER_TENDRIL, power, alpha, tintR, tintG, tintB);
        }

        shader.apply();
        BufferUploader.drawWithShader(builder.end());
        shader.clear();
    }

    private static void addVertex(BufferBuilder builder, Vec3 pos, float u, float v,
                                  int layerIndex, int power, float alpha,
                                  float tintR, float tintG, float tintB) {
        builder.vertex(pos.x, pos.y, pos.z)
                .uv(u, v)
                .overlayCoords(layerIndex, power)
                .color(tintR, tintG, tintB, alpha)
                .uv2(15728880)
                .endVertex();
    }
}
