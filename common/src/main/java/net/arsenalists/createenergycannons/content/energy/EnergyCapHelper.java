package net.arsenalists.createenergycannons.content.energy;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class EnergyCapHelper {

    /** No-op energy storage that always returns zero. */
    public static final IModEnergyStorage EMPTY = new IModEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() { return 0; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return false; }
    };

    private static BiFunction<BlockEntity, @Nullable Direction, IModEnergyStorage> provider = (be, side) -> EMPTY;

    /**
     * Set the platform-specific energy provider. Called once during mod init.
     */
    public static void setProvider(BiFunction<BlockEntity, @Nullable Direction, IModEnergyStorage> provider) {
        EnergyCapHelper.provider = provider;
    }

    /**
     * Get the energy storage from a block entity on the given side.
     * Returns EMPTY if no energy storage is available.
     */
    public static IModEnergyStorage getEnergy(BlockEntity be, @Nullable Direction side) {
        return provider.apply(be, side);
    }
}
