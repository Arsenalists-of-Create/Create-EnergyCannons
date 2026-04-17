package net.arsenalists.createenergycannons.fabric;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.content.energy.EnergyCapHelper;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import net.arsenalists.createenergycannons.registry.CECDefaultCannonMountPropertiesSerializers;
import net.createmod.catnip.config.ConfigBase;
import net.fabricmc.api.ModInitializer;
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

        // Wire up config load/reload callbacks — mirrors CECForgeEvents on Forge side
        ModConfigEvents.loading(CECMod.MODID).register(config -> {
            for (ConfigBase cfg : CECConfig.CONFIGS.values()) {
                if (cfg.specification == config.getSpec()) cfg.onLoad();
            }
        });
        ModConfigEvents.reloading(CECMod.MODID).register(config -> {
            for (ConfigBase cfg : CECConfig.CONFIGS.values()) {
                if (cfg.specification == config.getSpec()) cfg.onReload();
            }
        });

        // Register configs using the public forge-config-api-port API
        for (Map.Entry<ModConfig.Type, ConfigBase> pair : CECConfig.CONFIGS.entrySet()) {
            ForgeConfigRegistry.INSTANCE.register(CECMod.MODID, pair.getKey(), pair.getValue().specification);
        }
    }
}
