package net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.sliding;

import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.MountedRailCannonContrpation.MountedRailCannonContraption;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.RailCannonBlock;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import rbasamoyai.createbigcannons.cannons.big_cannons.cannon_end.BigCannonEnd;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;

import javax.annotation.Nullable;

public class SlidingBreechBlock extends DirectionalAxisKineticBlock implements IBE<SlidingBreechBlockEntity>, RailCannonBlock {

    private final BigCannonMaterial cannonMaterial;
    private final NonNullSupplier<? extends Block> quickfiringConversion;

    public SlidingBreechBlock(Properties properties, BigCannonMaterial cannonMaterial, NonNullSupplier<? extends Block> quickfiringConversion) {
        super(properties.pushReaction(PushReaction.BLOCK));
        this.cannonMaterial = cannonMaterial;
        this.quickfiringConversion = quickfiringConversion;
    }

    @Override
    public MountedRailCannonContraption.TYPE getType() {
        return MountedRailCannonContraption.TYPE.DEFAULT;
    }

    @Override
    public BigCannonMaterial getCannonMaterial() {
        return this.cannonMaterial;
    }

    @Override
    public CannonCastShape getCannonShape() {
        return CannonCastShape.SLIDING_BREECH;
    }

    @Override
    public Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    public BigCannonEnd getOpeningType(@Nullable Level level, BlockState state, BlockPos pos) {
        return level != null && level.getBlockEntity(pos) instanceof SlidingBreechBlockEntity breech ? breech.getOpeningType() : BigCannonEnd.OPEN;
    }


    @Override
    public BigCannonEnd getDefaultOpeningType() {
        return BigCannonEnd.CLOSED;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        Direction horizontal = context.getHorizontalDirection();
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(AXIS_ALONG_FIRST_COORDINATE, horizontal.getAxis() == Direction.Axis.Z);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide) this.onRemoveCannon(state, level, pos, newState, isMoving);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) this.playerWillDestroyBigCannon(level, pos, state, player);
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public Class<SlidingBreechBlockEntity> getBlockEntityClass() {
        return SlidingBreechBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SlidingBreechBlockEntity> getBlockEntityType() {
        return CECBlockEntity.SLIDING_BREECH.get();
    }

    @Override
    public boolean isComplete(BlockState state) {
        return true;
    }

    public BlockState getConversion(BlockState old) {
        return this.quickfiringConversion.get().defaultBlockState()
                .setValue(FACING, old.getValue(FACING))
                .setValue(AXIS_ALONG_FIRST_COORDINATE, old.getValue(AXIS_ALONG_FIRST_COORDINATE));
    }

}
