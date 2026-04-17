package net.arsenalists.createenergycannons;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.network.PacketHandler;
import net.arsenalists.createenergycannons.ponder.CECPonderPlugin;
import net.arsenalists.createenergycannons.registry.*;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class CECMod {
    public static final String MODID = "createenergycannons";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

    public CECMod() {
        getLogger().info("Initializing Create Energy Cannons!");

        // Registrate / normal common setup only
        CECItems.register();
        CECBlocks.register();
        CECBlockEntity.register();
        CECLang.register();
        CECCannonContraptionTypes.register();
        CECPartials.register();
        CECConfig.register();
    }

    /**
     * Common non-registry setup only.
     * Safe on both Forge and Fabric.
     */
    public static void init() {
        PacketHandler.register();
    }

    /**
     * Called once loader-specific event/bus registration is available.
     */
    public static void postBusRegister() {
        CECCreativeModeTabs.register();
        CECParticles.register();
        CECSoundEvents.register();
    }

    public static void clientInit() {
        PonderIndex.addPlugin(new CECPonderPlugin());
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static ResourceLocation resource(String id) {
        return new ResourceLocation(MODID, id);
    }
}