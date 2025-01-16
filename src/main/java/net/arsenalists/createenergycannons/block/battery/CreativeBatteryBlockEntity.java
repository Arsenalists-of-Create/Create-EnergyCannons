package net.arsenalists.createenergycannons.block.battery;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreativeBatteryBlockEntity extends SmartBlockEntity {
    public static final CreativeEnergyCap ENERGY_CAP = new CreativeEnergyCap();

    public CreativeBatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }


    public void tick() {
        for (Direction direction : Direction.values()) {
            BlockPos offset = worldPosition.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(offset);
            if (blockEntity != null) {
                LazyOptional<IEnergyStorage> energyStorage = blockEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite());
                energyStorage.ifPresent(storage -> {
                    if (storage.canReceive()) {
                        storage.receiveEnergy(15000, false);
                    }
                });
            }

        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY)
            return LazyOptional.of(() -> ENERGY_CAP).cast();
        return super.getCapability(cap, side);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ENERGY)
            return LazyOptional.of(() -> ENERGY_CAP).cast();
        return super.getCapability(cap);
    }
}
