package net.arsenalists.createenergycannons.content.battery;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.arsenalists.createenergycannons.content.energy.EnergyCapHelper;
import net.arsenalists.createenergycannons.content.energy.IModEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CreativeBatteryBlockEntity extends SmartBlockEntity {
    public static final CreativeEnergyCap ENERGY_CAP = new CreativeEnergyCap();

    public CreativeBatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    public IModEnergyStorage getEnergyStorage() {
        return ENERGY_CAP;
    }

    public void tick() {
        for (Direction direction : Direction.values()) {
            BlockPos offset = worldPosition.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(offset);
            if (blockEntity != null) {
                IModEnergyStorage storage = EnergyCapHelper.getEnergy(blockEntity, direction.getOpposite());
                if (storage.canReceive()) {
                    storage.receiveEnergy(15000, false);
                }
            }
        }
    }
}
