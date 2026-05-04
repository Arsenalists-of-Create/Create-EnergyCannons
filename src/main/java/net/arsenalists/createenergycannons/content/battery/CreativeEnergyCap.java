package net.arsenalists.createenergycannons.content.battery;

import net.arsenalists.createenergycannons.content.energy.IModEnergyStorage;

public class CreativeEnergyCap implements IModEnergyStorage {
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return maxReceive;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return maxExtract;
    }

    @Override
    public int getEnergyStored() {
        return getMaxEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
