package net.arsenalists.createenergycannons.fabric;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.config.ConfigType;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.content.energy.EnergyCapHelper;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import net.arsenalists.createenergycannons.registry.CECCreateRegistries;
import net.arsenalists.createenergycannons.registry.CECDefaultCannonMountPropertiesSerializers;
import net.createmod.catnip.config.ConfigBase;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Map;

public final class CECModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new CECMod();

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

        CECCreateRegistries.registerContraptionTypes(
                (id, type) -> Registry.register(CreateBuiltInRegistries.CONTRAPTION_TYPE, id, type)
        );
        CECCreateRegistries.registerArmInteractionPointTypes(
                (id, type) -> Registry.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE, id, type)
        );

        CECMod.REGISTRATE.register();
        CECMod.postBusRegister();

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
        for (Map.Entry<ConfigType, ConfigBase> pair : CECConfig.CONFIGS.entrySet()) {
            ForgeConfigRegistry.INSTANCE.register(CECMod.MODID, toModConfigType(pair.getKey()), pair.getValue().specification);
        }
    }

    private static ModConfig.Type toModConfigType(ConfigType t) {
        return switch (t) {
            case CLIENT -> ModConfig.Type.CLIENT;
            case COMMON -> ModConfig.Type.COMMON;
            case SERVER -> ModConfig.Type.SERVER;
        };
    }
}
