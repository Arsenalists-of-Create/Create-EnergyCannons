package net.arsenalists.createenergycannons.forge;

import net.arsenalists.createenergycannons.config.CECConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class CECForgeEvents {

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
