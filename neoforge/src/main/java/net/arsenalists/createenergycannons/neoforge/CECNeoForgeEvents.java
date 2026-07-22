package net.arsenalists.createenergycannons.neoforge;

import net.arsenalists.createenergycannons.config.CECConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

public class CECNeoForgeEvents {

    public static void onConfigLoad(ModConfigEvent.Loading event) {
        for (net.createmod.catnip.config.ConfigBase config : CECConfig.CONFIGS.values())
            if (config.specification == event.getConfig().getSpec())
                config.onLoad();
        refreshCoolingTuning();
    }

    public static void onConfigReload(ModConfigEvent.Reloading event) {
        for (net.createmod.catnip.config.ConfigBase config : CECConfig.CONFIGS.values())
            if (config.specification == event.getConfig().getSpec())
                config.onReload();
        refreshCoolingTuning();
    }

    /** Push config-driven cooling tuning that's read on both sides (the water decay rate). */
    private static void refreshCoolingTuning() {
        try {
            net.arsenalists.createenergycannons.content.cooling.WaterTemp.TICKS_PER_DEGREE =
                    CECConfig.server().coolingDecayTicksPerDegree.get();
        } catch (Exception ignored) {
        }
    }
}
