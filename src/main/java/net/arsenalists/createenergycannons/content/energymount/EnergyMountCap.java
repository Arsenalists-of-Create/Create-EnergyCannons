package net.arsenalists.createenergycannons.content.energymount;

import net.arsenalists.createenergycannons.content.energy.IModEnergyStorage;

public class EnergyMountCap implements IModEnergyStorage {
    private int energy;
    private final int capacity;
    private final Runnable onChange;

    public EnergyMountCap(int capacity, Runnable onChange) {
        this.energy = 0;
        this.capacity = capacity;
        this.onChange = onChange;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = Math.min(maxReceive, this.capacity - this.energy);
        if (!simulate) {
            this.energy += received;
            if (received > 0) onChange.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(maxExtract, this.energy);
        if (!simulate) {
            this.energy -= extracted;
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }

    @Override
    public boolean canExtract() {
        return this.energy > 0;
    }

    @Override
    public boolean canReceive() {
        return this.energy < this.capacity;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(energy, this.capacity));
    }
}
