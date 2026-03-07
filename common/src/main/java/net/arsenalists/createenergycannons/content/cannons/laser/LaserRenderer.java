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
