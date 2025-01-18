package net.arsenalists.createenergycannons.content.cannons.laser;

import com.simibubi.create.foundation.block.IBE;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannons.CannonContraptionProviderBlock;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;

public class LaserBlock extends DirectionalBlock implements CannonContraptionProviderBlock, IBE<LaserBlockEntity> {
    public LaserBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<LaserBlockEntity> getBlockEntityClass() {
        return LaserBlockEntity.class;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    @Override
    public BlockEntityType<? extends LaserBlockEntity> getBlockEntityType() {
        return CECBlockEntity.LASER.get();
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public @NotNull AbstractMountedCannonContraption getCannonContraption() {
        return new MountedLaserCannonContraption();
    }

    @Override
    public Direction getFacing(BlockState blockState) {
        return blockState.getValue(FACING);
    }

    @Override
    public CannonCastShape getCannonShape() {
        return CannonCastShape.CANNON_END;
    }

    @Override
    public boolean isComplete(BlockState blockState) {
        return true;
    }
}
