package net.arsenalists.createenergycannons.content.cannons.laser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix3f;
import org.joml.Matrix4f;


@Environment(EnvType.CLIENT)
public class LaserRenderer extends SmartBlockEntityRenderer<LaserBlockEntity> {

    private static ResourceLocation getStainedGlassTexture(DyeColor color) {
        return new ResourceLocation("minecraft", "textures/block/" + color.getName() + "_stained_glass.png");
    }

    public LaserRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(LaserBlockEntity be, float partialTicks, PoseStack ps, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ps, buffer, light, overlay);

        DyeColor lensColor = be.getLensColor();
        if (lensColor == null) return;

        Direction facing = be.getBlockState().getValue(LaserBlock.FACING);

        float half = 6.0f / 16.0f;
        float offset = 0.5625f; // 9/16, avoid z-fighting

        // Compute quad position directly from facing vectors (platform-independent)
        Vec3 n = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 rightVec, upVec;
        if (facing.getAxis() == Direction.Axis.Y) {
            rightVec = new Vec3(1, 0, 0);
            upVec = new Vec3(0, 0, facing == Direction.UP ? -1 : 1);
        } else {
            rightVec = new Vec3(-facing.getClockWise().getStepX(), 0, -facing.getClockWise().getStepZ());
            upVec = new Vec3(0, 1, 0);
        }

        // Center of front face
        Vec3 center = new Vec3(0.5 + n.x * offset, 0.5 + n.y * offset, 0.5 + n.z * offset);

        Vec3 v0 = center.add(rightVec.scale(-half)).add(upVec.scale(-half));
        Vec3 v1 = center.add(rightVec.scale(half)).add(upVec.scale(-half));
        Vec3 v2 = center.add(rightVec.scale(half)).add(upVec.scale(half));
        Vec3 v3 = center.add(rightVec.scale(-half)).add(upVec.scale(half));

        ps.pushPose();
        Matrix4f pose = ps.last().pose();
        Matrix3f normal = ps.last().normal();

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(getStainedGlassTexture(lensColor)));

        float nx = (float) n.x, ny = (float) n.y, nz = (float) n.z;
        vc.vertex(pose, (float) v0.x, (float) v0.y, (float) v0.z).color(1f, 1f, 1f, 1f)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, nx, ny, nz).endVertex();
        vc.vertex(pose, (float) v1.x, (float) v1.y, (float) v1.z).color(1f, 1f, 1f, 1f)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, nx, ny, nz).endVertex();
        vc.vertex(pose, (float) v2.x, (float) v2.y, (float) v2.z).color(1f, 1f, 1f, 1f)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, nx, ny, nz).endVertex();
        vc.vertex(pose, (float) v3.x, (float) v3.y, (float) v3.z).color(1f, 1f, 1f, 1f)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, nx, ny, nz).endVertex();

        ps.popPose();
    }


    public static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");

    public static void renderBeaconBeam(PoseStack ps, MultiBufferSource buffer,
                                        float partialTick, long gameTime,
                                        int yOffset, int height, float[] colors) {
        renderBeaconBeam(ps, buffer, BEAM_LOCATION, partialTick, 1.0F,
                gameTime, yOffset, height, colors, 0.2F, 0.25F);
    }

    public static void renderBeaconBeam(PoseStack ps, MultiBufferSource buffer,
                                        float partialTick, long gameTime,
                                        int yOffset, int height, float[] colors,
                                        float beamRadius, float glowRadius) {
        renderBeaconBeam(ps, buffer, BEAM_LOCATION, partialTick, 1.0F,
                gameTime, yOffset, height, colors, beamRadius, glowRadius);
    }

    public static void renderBeaconBeam(PoseStack ps, MultiBufferSource buffer,
                                        ResourceLocation beamLocation, float partialTick,
                                        float textureScale, long gameTime,
                                        int yOffset, int height, float[] colors,
                                        float beamRadius, float glowRadius) {
        int maxY = yOffset + height;
        ps.pushPose();
        ps.translate(0.5D, 0.0D, 0.5D);
        float f = (float) Math.floorMod(gameTime, 40) + partialTick;
        float f1 = height < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
        float cr = colors[0], cg = colors[1], cb = colors[2];

        // Solid rotating core
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f15 = -1.0F + f2;
        float f16 = (float) height * textureScale * (0.5F / beamRadius) + f15;
        renderPart(ps, buffer.getBuffer(RenderType.beaconBeam(beamLocation, false)),
                cr, cg, cb, 1.0F, yOffset, maxY,
                0.0F, beamRadius, beamRadius, 0.0F,
                -beamRadius, 0.0F, 0.0F, -beamRadius,
                0.0F, 1.0F, f16, f15);
        ps.popPose();

        // Translucent glow
        f15 = -1.0F + f2;
        f16 = (float) height * textureScale + f15;
        renderPart(ps, buffer.getBuffer(RenderType.beaconBeam(beamLocation, true)),
                cr, cg, cb, 0.125F, yOffset, maxY,
                -glowRadius, -glowRadius, glowRadius, -glowRadius,
                -glowRadius, glowRadius, glowRadius, glowRadius,
                0.0F, 1.0F, f16, f15);
        ps.popPose();
    }

    private static void renderPart(PoseStack ps, VertexConsumer c,
                                   float r, float g, float b, float a,
                                   int minY, int maxY,
                                   float x0, float z0, float x1, float z1,
                                   float x2, float z2, float x3, float z3,
                                   float minU, float maxU, float minV, float maxV) {
        PoseStack.Pose pose = ps.last();
        Matrix4f mat = pose.pose();
        Matrix3f norm = pose.normal();
        renderBeamQuad(mat, norm, c, r, g, b, a, minY, maxY, x0, z0, x1, z1, minU, maxU, minV, maxV);
        renderBeamQuad(mat, norm, c, r, g, b, a, minY, maxY, x3, z3, x2, z2, minU, maxU, minV, maxV);
        renderBeamQuad(mat, norm, c, r, g, b, a, minY, maxY, x1, z1, x3, z3, minU, maxU, minV, maxV);
        renderBeamQuad(mat, norm, c, r, g, b, a, minY, maxY, x2, z2, x0, z0, minU, maxU, minV, maxV);
    }

    private static void renderBeamQuad(Matrix4f mat, Matrix3f norm, VertexConsumer c,
                                        float r, float g, float b, float a,
                                        int minY, int maxY,
                                        float minX, float minZ, float maxX, float maxZ,
                                        float minU, float maxU, float minV, float maxV) {
        beamVertex(mat, norm, c, r, g, b, a, maxY, minX, minZ, maxU, minV);
        beamVertex(mat, norm, c, r, g, b, a, minY, minX, minZ, maxU, maxV);
        beamVertex(mat, norm, c, r, g, b, a, minY, maxX, maxZ, minU, maxV);
        beamVertex(mat, norm, c, r, g, b, a, maxY, maxX, maxZ, minU, minV);
    }

    private static void beamVertex(Matrix4f mat, Matrix3f norm, VertexConsumer c,
                                    float r, float g, float b, float a,
                                    int y, float x, float z, float u, float v) {
        c.vertex(mat, x, (float) y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(norm, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(LaserBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(LaserBlockEntity pBlockEntity, Vec3 pCameraPos) {
        return true;
    }
}
