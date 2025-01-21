package net.arsenalists.createenergycannons.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserRenderer;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.LayeredRailCannonBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.RailCannonBlockEntity;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntityRenderer;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountInstance;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockEntity;


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
            .validBlocks(CECBlocks.RAILGUN_BARREL)
            .register();

    public static final BlockEntityEntry<BigCannonBlockEntity> BIG_CANNON = CECMod.REGISTRATE
            .blockEntity("big_cannon", BigCannonBlockEntity::new)
            .validBlocks(CECBlocks.STEEL_COILGUN_BARREL, CECBlocks.NETHERSTEEL_COILGUN_BARREL)
            .register();

    public static final BlockEntityEntry<LaserBlockEntity> LASER = CECMod.REGISTRATE
            .blockEntity("laser", LaserBlockEntity::new)
            .renderer(() -> LaserRenderer::new)
            .validBlock(CECBlocks.LASER)
            .register();

    public static final BlockEntityEntry<LayeredRailCannonBlockEntity> LAYERED_CANNON = CECMod.REGISTRATE
            .blockEntity("layered_cannon", LayeredRailCannonBlockEntity::new)
            .register();

    public static void register() {
        CECMod.getLogger().info("Registering Block Entities");
    }
}
