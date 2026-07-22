package net.arsenalists.createenergycannons.content.energymount;

import net.arsenalists.createenergycannons.content.energy.IModEnergyStorage;

/** Shared contract for energy cannon mounts (rotating and fixed) so a contraption can drive either. */
public interface IEnergyCannonMount {
    IModEnergyStorage getEnergyStorage();

    void setCannonCooldown(long endTime);

    void accelerateCooldown(long currentTime, int ticks);
}
