package net.arsenalists.createenergycannons.fabric;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.config.ConfigType;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.content.cooling.CoolingUnitBlockEntity;
import net.arsenalists.createenergycannons.content.cooling.WaterTemp;
import net.arsenalists.createenergycannons.content.energy.EnergyCapHelper;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.arsenalists.createenergycannons.registry.CECCreateRegistries;
import net.arsenalists.createenergycannons.registry.CECDefaultCannonMountPropertiesSerializers;
import net.createmod.catnip.config.ConfigBase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Map;

public final class CECModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new CECMod();

        net.arsenalists.createenergycannons.content.cooling.CoolantTanksFactory.setFactory(
                net.arsenalists.createenergycannons.fabric.cooling.FabricCoolantTanks::new);

        EnergyCapHelper.setProvider((be, side) -> {
            if (be instanceof EnergyCannonMountBlockEntity mount) {
                return mount.getEnergyStorage();
            }
            if (be instanceof net.arsenalists.createenergycannons.content.energymount.FixedEnergyCannonMountBlockEntity fixedMount) {
                return fixedMount.getEnergyStorage();
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

        // Expose the Cooling Unit's coolant to Create pipes (Transfer API), and keep the decay clock fresh.
        FluidStorage.SIDED.registerForBlockEntity(
                (be, dir) -> (Storage<FluidVariant>) be.coolantFluidHandler(),
                CECBlockEntity.COOLING_UNIT.get());
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerLevel overworld = server.overworld();
            if (overworld != null) WaterTemp.currentTick = overworld.getGameTime();
        });

        // Wire up config load/reload callbacks - mirrors CECForgeEvents on Forge side
        ModConfigEvents.loading(CECMod.MODID).register(config -> {
            for (ConfigBase cfg : CECConfig.CONFIGS.values()) {
                if (cfg.specification == config.getSpec()) cfg.onLoad();
            }
            refreshCoolingTuning();
        });
        ModConfigEvents.reloading(CECMod.MODID).register(config -> {
            for (ConfigBase cfg : CECConfig.CONFIGS.values()) {
                if (cfg.specification == config.getSpec()) cfg.onReload();
            }
            refreshCoolingTuning();
        });

        // Register configs using the public forge-config-api-port API
        for (Map.Entry<ConfigType, ConfigBase> pair : CECConfig.CONFIGS.entrySet()) {
            ForgeConfigRegistry.INSTANCE.register(CECMod.MODID, toModConfigType(pair.getKey()), pair.getValue().specification);
        }
    }

    private static void refreshCoolingTuning() {
        try {
            WaterTemp.TICKS_PER_DEGREE = CECConfig.server().coolingDecayTicksPerDegree.get();
        } catch (Exception ignored) {
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
