package net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.sliding;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.quickfire.QuickfiringBreechBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import rbasamoyai.createbigcannons.CBCClientCommon;
import rbasamoyai.createbigcannons.index.CBCBigCannonMaterials;
import rbasamoyai.createbigcannons.index.CBCBlockPartials;

public class SlidingBreechBlockEntityRenderer extends KineticBlockEntityRenderer<SlidingBreechBlockEntity> {

    public SlidingBreechBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SlidingBreechBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
        if (Backend.canUseInstancing(te.getLevel())) return;

        BlockState blockState = te.getBlockState();


        Direction facing = blockState.getValue(BlockStateProperties.FACING);
        Direction.Axis axis = CBCClientCommon.getRotationAxis(blockState);
        Direction blockRotation = facing.getCounterClockWise(axis);
        if (blockRotation == Direction.DOWN) blockRotation = Direction.UP;

        Quaternionf qrot;

        boolean alongFirst = blockState.getValue(QuickfiringBreechBlock.AXIS);
        if (facing.getAxis().isHorizontal() && !alongFirst) {
            Direction rotDir = facing.getAxis() == Direction.Axis.X ? Direction.UP : Direction.EAST;
            qrot = Axis.of(rotDir.step()).rotationDegrees(90f);
        } else if (facing.getAxis() == Direction.Axis.X && alongFirst) {
            qrot = Axis.of(blockRotation.step()).rotationDegrees(90f);
        } else {
            qrot = Axis.of(blockRotation.step()).rotationDegrees(0);
        }

        float renderedBreechblockOffset = te.getRenderedBlockOffset(partialTicks);
        renderedBreechblockOffset = renderedBreechblockOffset / 16.0f * 13.0f;
        Vector3f normal = blockRotation.step();
        normal.mul(renderedBreechblockOffset);

        ms.pushPose();

        CachedBufferer.partialFacing(CBCBlockPartials.breechblockFor(CBCBigCannonMaterials.STEEL), blockState, blockRotation)
                .translate(normal.x(), normal.y(), normal.z())
                .rotateCentered(qrot)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));

        ms.popPose();
    }

    @Override
    protected BlockState getRenderedBlockState(SlidingBreechBlockEntity te) {
        return shaft(getRotationAxisOf(te));
    }

}
