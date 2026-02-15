package net.arsenalists.createenergycannons.config.server;

import net.createmod.catnip.config.ConfigBase;

public class CECServerConfig extends ConfigBase {
    @Override
    public String getName() {
        return "CEC Server";
    }



    public final ConfigInt mountCoolDownTime = i(500,1,"mountCoolDownTime", "The amount of time (in ticks) it takes for the energy mount to cool down");
    public final ConfigInt mountEnergyCapacity = i(500000,1,"mountEnergyCapacity","The energy capacity of the energy mount");
    public final ConfigInt mountChargeTime = i(20,1,"mountChargeTime", "The amount of time (in ticks) it takes for the energy mount to charge");
    public final ConfigInt railgunCostPerBlock = i(20000,1,"energyCostPerRailBlock", "The energy cost per Railgun Barrel block");
    public final ConfigInt coilgunCostPerBlock = i(10000,1,"energyCostPerCoilBlock", "The energy cost per Coilgun Barrel block");
    public final ConfigFloat railgunPowerMultiplier = f(1.1f,0,"railgunPowerMultiplier","The power multiplier of railgun contraptions");
    public final ConfigInt laserPowerConsumption = i (5,1,"laserPowerConsumption", "The energy consumption of the laser contraption");
    public final ConfigInt laserRange = i(256,1, "laserRange", "The range of the laser's beam (in blocks)");
    public final ConfigInt laserDamage = i(1,1,"laserDamage", "The per-hit damage of the laser");
    public final ConfigInt laserBurnTime = i(2,1,"laserSecondsOfFire", "The fire burn time (in seconds) of entities hit by the laser");
    public final ConfigInt laserBlockBreakThreshold = i(10,1,"laserBlockBreakThreshold", "How long a block takes to break when hit by the beam of the laser");

}
