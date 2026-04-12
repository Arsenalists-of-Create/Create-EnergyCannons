package net.arsenalists.createenergycannons.fabric;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.content.energy.EnergyCapHelper;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import net.arsenalists.createenergycannons.registry.CECCreateRegistries;
import net.arsenalists.createenergycannons.registry.CECDefaultCannonMountPropertiesSerializers;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraftforge.common.ForgeConfigSpec;
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

        for (Map.Entry<ModConfig.Type, ForgeConfigSpec> pair : CECConfig.getSpecs().entrySet()) {
            new ModConfig(pair.getKey(), pair.getValue(), CECMod.MODID);
        }
    }
}