package net.arsenalists.createenergycannons.config;

import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.config.server.CECServerConfig;
import net.createmod.catnip.config.ConfigBase;
//? if <1.21 {
/*import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
*///?}
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class CECConfig {
    public static final Map<ConfigType, ConfigBase> CONFIGS = new EnumMap<>(ConfigType.class);
    private static CECServerConfig server;

    public static CECServerConfig server() {
        return server;
    }

    public static ConfigBase byType(ConfigType type) {
        return CONFIGS.get(type);
    }

    //? if <1.21 {
    /*public static <T extends ConfigBase> T register(Supplier<T> factory, ConfigType side) {
        Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
            T config = factory.get();
            config.registerAll(builder);
            return config;
        });

        T config = specPair.getLeft();
        config.specification = specPair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    public static Map<ConfigType, ForgeConfigSpec> getSpecs() {
        Map<ConfigType, ForgeConfigSpec> specs = new EnumMap<>(ConfigType.class);
        CONFIGS.forEach((type, config) -> specs.put(type, config.specification));
        return specs;
    }
    *///?} else {
    public static <T extends ConfigBase> T register(Supplier<T> factory, ConfigType side) {
        Pair<T, net.neoforged.neoforge.common.ModConfigSpec> specPair =
            new net.neoforged.neoforge.common.ModConfigSpec.Builder().configure(builder -> {
                T config = factory.get();
                config.registerAll(builder);
                return config;
            });
        T config = specPair.getLeft();
        config.specification = specPair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    public static Map<ConfigType, net.neoforged.neoforge.common.ModConfigSpec> getSpecs() {
        Map<ConfigType, net.neoforged.neoforge.common.ModConfigSpec> specs = new EnumMap<>(ConfigType.class);
        CONFIGS.forEach((type, config) -> specs.put(type, config.specification));
        return specs;
    }
    //?}

    public static void register() {
        server = register(CECServerConfig::new, ConfigType.SERVER);
    }

    //? if <1.21 {
    /*public static BaseConfigScreen createConfigScreen(Minecraft mc, Screen parent) {
        BaseConfigScreen.setDefaultActionFor(CECMod.MODID, (base) -> base
                .withSpecs(null,
                        null,
                        CECConfig.server().specification));

        return new BaseConfigScreen(parent, CECMod.MODID);
    }
    *///?}
}
