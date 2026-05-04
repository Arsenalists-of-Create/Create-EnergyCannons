package net.arsenalists.createenergycannons.content.energy;

/**
 * Cross-platform energy storage interface.
 * On Forge this maps to IEnergyStorage, on Fabric to team.reborn.energy.api.EnergyStorage.
 */
public interface IModEnergyStorage {
    int receiveEnergy(int maxReceive, boolean simulate);
    int extractEnergy(int maxExtract, boolean simulate);
    int getEnergyStored();
    int getMaxEnergyStored();
    boolean canExtract();
    boolean canReceive();
}
