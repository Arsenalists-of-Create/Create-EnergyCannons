package net.arsenalists.createenergycannons.block.energymount;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;

import java.util.Iterator;

public class EnergyCannonMount extends KineticBlock implements IBE<EnergyCannonMountBlockEntity> {
    public static final DirectionProperty HORIZONTAL_FACING;
    public static final BooleanProperty ASSEMBLY_POWERED;
    public static final BooleanProperty FIRE_POWERED;
    public static final DirectionProperty VERTICAL_DIRECTION;

    public EnergyCannonMount(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any()).setValue(HORIZONTAL_FACING, Direction.NORTH)).setValue(ASSEMBLY_POWERED, false)).setValue(FIRE_POWERED, false)).setValue(VERTICAL_DIRECTION, Direction.DOWN));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{HORIZONTAL_FACING, ASSEMBLY_POWERED, FIRE_POWERED, VERTICAL_DIRECTION});
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState) ((BlockState) this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection())).setValue(VERTICAL_DIRECTION, context.getNearestLookingVerticalDirection());
    }

    public Direction.Axis getRotationAxis(BlockState state) {
        return ((Direction) state.getValue(HORIZONTAL_FACING)).getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
    }

    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == this.getRotationAxis(state) || face == state.getValue(VERTICAL_DIRECTION);
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return (BlockState) state.setValue(HORIZONTAL_FACING, rotation.rotate((Direction) state.getValue(HORIZONTAL_FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return (BlockState) state.setValue(HORIZONTAL_FACING, mirror.mirror((Direction) state.getValue(HORIZONTAL_FACING)));
    }

    public Class<EnergyCannonMountBlockEntity> getBlockEntityClass() {
        return EnergyCannonMountBlockEntity.class;
    }

    public BlockEntityType<? extends EnergyCannonMountBlockEntity> getBlockEntityType() {
        return CECBlockEntity.ENERGY_CANNON_MOUNT.get();
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, 0);
        }

    }

    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        boolean prevAssemblyPowered = (Boolean) state.getValue(ASSEMBLY_POWERED);
        boolean prevFirePowered = (Boolean) state.getValue(FIRE_POWERED);
        boolean assemblyPowered = this.hasNeighborSignal(level, state, pos, ASSEMBLY_POWERED);
        boolean firePowered = this.hasNeighborSignal(level, state, pos, FIRE_POWERED);
        Direction fireDirection = (Direction) state.getValue(HORIZONTAL_FACING);
        int firePower = level.getSignal(pos.relative(fireDirection), fireDirection);
        this.withBlockEntityDo(level, pos, (cmbe) -> {
            cmbe.onRedstoneUpdate(assemblyPowered, prevAssemblyPowered, firePowered, prevFirePowered, firePower);
        });
    }

    private boolean hasNeighborSignal(Level level, BlockState state, BlockPos pos, BooleanProperty property) {
        Direction assemblyDirection;
        if (property == FIRE_POWERED) {
            assemblyDirection = (Direction) state.getValue(HORIZONTAL_FACING);
            return level.getSignal(pos.relative(assemblyDirection), assemblyDirection) > 0;
        } else if (property == ASSEMBLY_POWERED) {
            assemblyDirection = ((Direction) state.getValue(HORIZONTAL_FACING)).getOpposite();
            return level.getSignal(pos.relative(assemblyDirection), assemblyDirection) > 0;
        } else {
            return false;
        }
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        BlockEntity var7 = level.getBlockEntity(pos);
        if (var7 instanceof CannonMountBlockEntity mount) {
            Iterator var9 = mount.getAllKineticBlockEntities().iterator();

            while (var9.hasNext()) {
                KineticBlockEntity kbe = (KineticBlockEntity) var9.next();
                kbe.preventSpeedUpdate = 0;
                if (oldState.getBlock() == state.getBlock() && state.hasBlockEntity() == oldState.hasBlockEntity() && this.areStatesKineticallyEquivalent(oldState, state)) {
                    kbe.preventSpeedUpdate = 2;
                }
            }
        }

    }

    public void updateIndirectNeighbourShapes(BlockState stateIn, LevelAccessor level, BlockPos pos, int flags, int count) {
        if (!level.isClientSide()) {
            BlockEntity var7 = level.getBlockEntity(pos);
            if (var7 instanceof CannonMountBlockEntity) {
                CannonMountBlockEntity mount = (CannonMountBlockEntity) var7;
                mount.tryUpdatingSpeed();
            }
        }

    }

    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        InteractionResult resultType = super.onWrenched(state, context);
        if (!context.getLevel().isClientSide && resultType.consumesAction()) {
            BlockEntity var5 = context.getLevel().getBlockEntity(context.getClickedPos());
            if (var5 instanceof CannonMountBlockEntity) {
                CannonMountBlockEntity mount = (CannonMountBlockEntity) var5;
                mount.disassemble();
            }
        }

        return resultType;
    }

    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        BlockState newState = super.getRotatedBlockState(originalState, targetedFace);
        if (newState != originalState) {
            return newState;
        } else {
            return targetedFace.getAxis().isHorizontal() ? (BlockState) originalState.cycle(VERTICAL_DIRECTION) : originalState;
        }
    }

    static {
        HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
        ASSEMBLY_POWERED = BooleanProperty.create("assembly_powered");
        FIRE_POWERED = BooleanProperty.create("fire_powered");
        VERTICAL_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    }
}
