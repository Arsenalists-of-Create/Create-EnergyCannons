package net.arsenalists.createenergycannons.neoforge;

import net.arsenalists.createenergycannons.config.CECConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

public class CECNeoForgeEvents {

    public static void onConfigLoad(ModConfigEvent.Loading event) {
        for (net.createmod.catnip.config.ConfigBase config : CECConfig.CONFIGS.values())
            if (config.specification == event.getConfig().getSpec())
                config.onLoad();
    }

    public static void onConfigReload(ModConfigEvent.Reloading event) {
        for (net.createmod.catnip.config.ConfigBase config : CECConfig.CONFIGS.values())
            if (config.specification == event.getConfig().getSpec())
                config.onReload();
    }
}
