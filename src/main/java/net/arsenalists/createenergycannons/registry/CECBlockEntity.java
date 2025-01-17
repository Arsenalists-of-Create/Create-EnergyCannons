package net.arsenalists.createenergycannons.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.block.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.block.energymount.EnergyCannonMountBlockEntity;
import net.arsenalists.createenergycannons.block.laser.LaserBlockEntity;
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

    public static final BlockEntityEntry<LaserBlockEntity> LASER = CECMod.REGISTRATE
            .blockEntity("laser", LaserBlockEntity::new)
            .validBlock(CECBlocks.LASER)
            .register();

    public static void register() {
        CECMod.getLogger().info("Registering Block Entities");
    }
}
