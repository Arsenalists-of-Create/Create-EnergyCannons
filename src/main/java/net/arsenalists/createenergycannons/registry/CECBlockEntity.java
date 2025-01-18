package net.arsenalists.createenergycannons.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.LayeredRailCannonBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.RailCannonBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.quickfire.QuickfiringBreechBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.quickfire.QuickfiringBreechBlockEntityRenderer;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.quickfire.QuickfiringBreechInstance;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.screwbreech.ScrewBreechBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.screwbreech.ScrewBreechBlockEntityRenderer;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.screwbreech.ScrewBreechInstance;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.sliding.SlidingBreechBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.sliding.SlidingBreechBlockEntityRenderer;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.sliding.SlidingBreechInstance;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntityRenderer;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountInstance;


public class CECBlockEntity {

    public static final BlockEntityEntry<CreativeBatteryBlockEntity> CREATIVE_BATTERY = CECMod.REGISTRATE
            .blockEntity("creative_battery", CreativeBatteryBlockEntity::new)
            .validBlock(CECBlocks.BATTERY_BLOCK)
            .register();

    public static final BlockEntityEntry<EnergyCannonMountBlockEntity> ENERGY_CANNON_MOUNT = CECMod.REGISTRATE
            .blockEntity("energy_cannon_mount", EnergyCannonMountBlockEntity::new)
            .instance(() -> CannonMountInstance::new)
            .validBlock(CECBlocks.ENERGY_CANNON_MOUNT)
            .renderer(() -> CannonMountBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<RailCannonBlockEntity> CANNON = CECMod.REGISTRATE
            .blockEntity("cannon", RailCannonBlockEntity::new)
            .validBlocks(CECBlocks.RAILGUN_BARREL, CECBlocks.STEEL_COILGUN_BARREL, CECBlocks.NETHERSTEEL_COILGUN_BARREL)
            .register();

    public static final BlockEntityEntry<ScrewBreechBlockEntity> SCREW_BREECH = CECMod.REGISTRATE
            .blockEntity("screw_breech", ScrewBreechBlockEntity::new)
            .instance(() -> ScrewBreechInstance::new, false)
            .renderer(() -> ScrewBreechBlockEntityRenderer::new)
            .validBlocks(CECBlocks.NETHERSTEEL_SCREW_BREECH, CECBlocks.STEEL_RAIL_SCREW_BREECH)
            .register();

    public static final BlockEntityEntry<QuickfiringBreechBlockEntity> QUICKFIRING_BREECH = CECMod.REGISTRATE
            .blockEntity("quickfiring_breech", QuickfiringBreechBlockEntity::new)
            .instance(() -> QuickfiringBreechInstance::new)
            .renderer(() -> QuickfiringBreechBlockEntityRenderer::new)
            .validBlocks(CECBlocks.STEEL_RAIL_QUICKFIRING_BREECH)
            .register();

    public static final BlockEntityEntry<SlidingBreechBlockEntity> SLIDING_BREECH = CECMod.REGISTRATE
            .blockEntity("sliding_breech", SlidingBreechBlockEntity::new)
            .instance(() -> SlidingBreechInstance::new, false)
            .renderer(() -> SlidingBreechBlockEntityRenderer::new)
            .validBlocks(CECBlocks.STEEL_SLIDING_BREECH)
            .register();

    public static final BlockEntityEntry<LaserBlockEntity> LASER = CECMod.REGISTRATE
            .blockEntity("laser", LaserBlockEntity::new)
            .validBlock(CECBlocks.LASER)
            .register();

    public static final BlockEntityEntry<LayeredRailCannonBlockEntity> LAYERED_CANNON = CECMod.REGISTRATE
            .blockEntity("layered_cannon", LayeredRailCannonBlockEntity::new)
            .register();

    public static void register() {
        CECMod.getLogger().info("Registering Block Entities");
    }
}
