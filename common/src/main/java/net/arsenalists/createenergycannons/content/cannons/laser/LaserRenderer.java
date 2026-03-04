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

        ps.pushPose();

        // Move to center of block, then to front face
        ps.translate(0.5, 0.5, 0.5);
        applyFacingRotation(ps, facing);
        ps.translate(0, 0, 0.5625); //avoid z fighting

        float half = 6.0f / 16.0f;

        Matrix4f pose = ps.last().pose();
        Matrix3f normal = ps.last().normal();

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(getStainedGlassTexture(lensColor)));
        float alpha = 1.0f;

        vc.vertex(pose, -half, -half, 0).color(1.0f, 1.0f, 1.0f, alpha)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0, 0, 1).endVertex();
        vc.vertex(pose, half, -half, 0).color(1.0f, 1.0f, 1.0f, alpha)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0, 0, 1).endVertex();
        vc.vertex(pose, half, half, 0).color(1.0f, 1.0f, 1.0f, alpha)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0, 0, 1).endVertex();
        vc.vertex(pose, -half, half, 0).color(1.0f, 1.0f, 1.0f, alpha)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0, 0, 1).endVertex();

        ps.popPose();
    }

    private static void applyFacingRotation(PoseStack ps, Direction facing) {
        switch (facing) {
            case SOUTH -> {}
            case NORTH -> ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
            case EAST -> ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
            case WEST -> ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
            case UP -> ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
            case DOWN -> ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
        }
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
