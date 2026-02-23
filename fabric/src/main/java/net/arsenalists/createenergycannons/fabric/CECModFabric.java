package net.arsenalists.createenergycannons.fabric;

import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.content.energy.EnergyCapHelper;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import net.arsenalists.createenergycannons.registry.CECDefaultCannonMountPropertiesSerializers;
import net.fabricmc.api.ModInitializer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Map;

public final class CECModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Set up Fabric energy provider before common init
        EnergyCapHelper.setProvider((be, side) -> {
            if (be instanceof EnergyCannonMountBlockEntity mount) {
                return mount.getEnergyStorage();
            }
            if (be instanceof CreativeBatteryBlockEntity battery) {
                return battery.getEnergyStorage();
            }
            return EnergyCapHelper.EMPTY;
        });

        CECMod.init();
        // On Fabric, registrate_fabric requires an explicit register() call (equivalent to
        // Forge's registerEventListeners + RegisterEvent). Without this, RegistryEntry.get()
        // returns null because entries are never submitted to the Minecraft registry.
        CECMod.REGISTRATE.register();
        CECMod.postBusRegister();

        // Register cannon mount properties (equivalent to Forge's onCommonSetup).
        // Must be called after REGISTRATE.register() so that RegistryEntry.get() works.
        CECDefaultCannonMountPropertiesSerializers.init();

        // Register configs with Fabric (forge-config-api-port)
        for (Map.Entry<ModConfig.Type, ForgeConfigSpec> pair : CECConfig.getSpecs().entrySet()) {
            new ModConfig(pair.getKey(), pair.getValue(), CECMod.MODID);
        }
    }
}
