package net.arsenalists.createenergycannons.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserRenderer;
import net.arsenalists.createenergycannons.content.cannons.magnetic.coilgun.CoilGunBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.RailGunBlockEntity;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntityRenderer;

import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountVisual;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.cannon_end.BigCannonEndBlockEntity;


public class CECBlockEntity {

    public static final BlockEntityEntry<CreativeBatteryBlockEntity> CREATIVE_BATTERY = CECMod.REGISTRATE
            .blockEntity("creative_battery", CreativeBatteryBlockEntity::new)
            .validBlock(CECBlocks.BATTERY_BLOCK)
            .register();

    public static final BlockEntityEntry<EnergyCannonMountBlockEntity> ENERGY_CANNON_MOUNT = CECMod.REGISTRATE
            .blockEntity("energy_cannon_mount", EnergyCannonMountBlockEntity::new)
            .visual(() -> CannonMountVisual::new)
            .validBlock(CECBlocks.ENERGY_CANNON_MOUNT)
            .renderer(() -> CannonMountBlockEntityRenderer::new)
            .register();


    public static final BlockEntityEntry<CoilGunBlockEntity> COILGUN = CECMod.REGISTRATE
            .blockEntity("coilgun", CoilGunBlockEntity::new)
            .validBlocks(CECBlocks.STEEL_COILGUN_BARREL, CECBlocks.NETHERSTEEL_COILGUN_BARREL)
            .register();

    public static final BlockEntityEntry<RailGunBlockEntity> RAILGUN = CECMod.REGISTRATE
            .blockEntity("railgun", RailGunBlockEntity::new)
            .validBlocks(CECBlocks.NETHERSTEEL_RAILGUN_BARREL, CECBlocks.STEEL_RAILGUN_BARREL)
            .register();



    public static final BlockEntityEntry<LaserBlockEntity> LASER = CECMod.REGISTRATE
            .blockEntity("laser", LaserBlockEntity::new)
            .renderer(() -> LaserRenderer::new)
            .validBlock(CECBlocks.LASER)
            .register();


    public static void register() {
        CECMod.getLogger().info("Registering Block Entities");
    }
}
