package net.arsenalists.createenergycannons.config;

import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.config.server.CECServerConfig;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class CECConfig {
    public static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);
    private static CECServerConfig server;

    public static CECServerConfig server() {
        return server;
    }

    public static ConfigBase byType(ModConfig.Type type) {
        return CONFIGS.get(type);
    }

    public static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
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

    public static Map<ModConfig.Type, ForgeConfigSpec> getSpecs() {
        Map<ModConfig.Type, ForgeConfigSpec> specs = new EnumMap<>(ModConfig.Type.class);
        CONFIGS.forEach((type, config) -> specs.put(type, config.specification));
        return specs;
    }

    public static void register() {
        server = register(CECServerConfig::new, ModConfig.Type.SERVER);
    }

    public static BaseConfigScreen createConfigScreen(Minecraft mc, Screen parent) {
        BaseConfigScreen.setDefaultActionFor(CECMod.MODID, (base) -> base
                .withSpecs(null,
                        null,
                        CECConfig.server().specification));

        return new BaseConfigScreen(parent, CECMod.MODID);
    }
}
