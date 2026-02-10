package net.arsenalists.createenergycannons.content.cannons.magnetic.coilgun;

import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.MountedEnergyCannonContraption;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonTubeBlock;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;

import java.util.function.Supplier;

public class CoilGunBlock extends BigCannonTubeBlock {  // CHANGED: was BigCannonEndBlock

    public static final BooleanProperty OVERHEATED = BooleanProperty.create("overheated");

    public CoilGunBlock(Properties properties, BigCannonMaterial material, Supplier<CannonCastShape> cannonShape, VoxelShape base) {
        super(properties, material, cannonShape, base);  // CHANGED: added cannonShape back
        this.registerDefaultState(this.stateDefinition.any().setValue(OVERHEATED, false));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OVERHEATED);
    }

    @Override
    public @NotNull AbstractMountedCannonContraption getCannonContraption() {
        return new MountedEnergyCannonContraption();
    }

    public static CoilGunBlock mediumCoil(BlockBehaviour.Properties properties, BigCannonMaterial material) {
        return new CoilGunBlock(properties, material, () -> CannonCastShape.MEDIUM, Shapes.block());
    }

    @Override
    public BlockEntityType<? extends BigCannonBlockEntity> getBlockEntityType() {
        return CECBlockEntity.COILGUN.get();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // Schedule tick for overheated blocks
        if (state.getValue(OVERHEATED) && !level.isClientSide()) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(OVERHEATED) && level.getBlockEntity(pos) instanceof CoilGunBlockEntity be) {
            long currentTime = level.getGameTime();
            if (!be.isOverheated(currentTime)) {
                level.setBlock(pos, state.setValue(OVERHEATED, false), 3);
            } else {
                level.scheduleTick(pos, this, 20);
            }
        }
    }
}