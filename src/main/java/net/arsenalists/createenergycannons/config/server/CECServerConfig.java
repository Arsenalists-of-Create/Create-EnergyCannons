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
    public final ConfigInt laserPowerConsumption = i (500,1,"laserPowerConsumption", "The energy consumption of the laser contraption");
    public final ConfigInt laserRange = i(256,1, "laserRange", "The range of the laser's beam (in blocks)");
    public final ConfigInt laserDamage = i(1,1,"laserDamage", "The per-hit damage of the laser");
    public final ConfigInt laserBurnTime = i(2,1,"laserSecondsOfFire", "The fire burn time (in seconds) of entities hit by the laser");
    public final ConfigInt laserBlockBreakThreshold = i(10,1,"laserBlockBreakThreshold", "How long a block takes to break when hit by the beam of the laser");

    public final ConfigInt coolingHeatPerUse = i(100, 0, "coolingHeatPerUse", "Temperature added to circulated water each cooling tick");
    public final ConfigInt coolingMaxTemperature = i(200, 1, "coolingMaxTemperature", "Maximum temperature circulated water can reach");
    public final ConfigInt coolingDecayTicksPerDegree = i(100, 1, "coolingDecayTicksPerDegree", "Ticks for hot water to passively shed one degree of heat");
    public final ConfigInt coolingCooldownTicksPerBarrel = i(2, 0, "coolingCooldownTicksPerBarrel", "Cannon cooldown ticks pulled forward per regenerative barrel each tick");
    public final ConfigInt coolingWaterPerBarrel = i(5, 0, "coolingWaterPerBarrel", "Water (mB) consumed per regenerative barrel each cooling tick");
    public final ConfigInt coolingEnergyPerBarrel = i(250, 0, "coolingEnergyPerBarrel", "Energy drawn from the mount per regenerative barrel each cooling tick");
    public final ConfigInt coolingUnitCapacity = i(8000, 1, "coolingUnitCapacity", "Capacity (mB) of each of the Cooling Unit's tanks");

}
