package net.arsenalists.createenergycannons.content.energymount;

import net.minecraftforge.energy.EnergyStorage;

public class EnergyMountCap extends EnergyStorage {
    Runnable onChange;

    public EnergyMountCap(int capacity, Runnable onChange) {
        super(capacity);
        this.onChange = onChange;
    }


    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0)
            onChange.run();
        return received;
    }

    @Override
    public boolean canExtract() {
        return false;
    }
}
