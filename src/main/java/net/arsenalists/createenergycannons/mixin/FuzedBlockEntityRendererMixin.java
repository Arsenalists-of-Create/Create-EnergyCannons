package net.arsenalists.createenergycannons.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.arsenalists.createenergycannons.content.cannons.magnetic.sled.IMagneticSled;
import net.arsenalists.createenergycannons.registry.CECPartials;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntityRenderer;

@Mixin(FuzedBlockEntityRenderer.class)
public abstract class FuzedBlockEntityRendererMixin {
    //adds sled model to the fuzed shell
    @Inject(method = "renderSafe(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"), remap = false)
    private void renderSled(BlockEntity blockEntity, float partialTicks, PoseStack posestack, MultiBufferSource buffers, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (blockEntity instanceof IMagneticSled sledBlockEntity && sledBlockEntity.isSled()) {
            BlockState state = blockEntity.getBlockState();
            Direction facing = state.getValue(BlockStateProperties.FACING);
            SuperByteBuffer sledRender = CachedBufferer.partialFacing(CECPartials.MAGNETIC_SLED, blockEntity.getBlockState(), facing);
            sledRender.renderInto(posestack, buffers.getBuffer(RenderType.cutout()));
        }
    }
}
