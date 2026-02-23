package net.arsenalists.createenergycannons;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.network.PacketHandler;
import net.arsenalists.createenergycannons.ponder.CECPonderPlugin;
import net.arsenalists.createenergycannons.registry.*;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.lang.reflect.Field;

public class CECMod {
    public static final String MODID = "createenergycannons";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

    /**
     * Basic startup logic that is safe to run before the mod event bus is available.
     * This contains all of the Registrate-driven registrations; those fire immediately
     * and do not need {@link EventBuses#registerModEventBus}.
     */
    public static void init() {
        getLogger().info("Initializing Create Energy Cannons!");

        CECItems.register();
        CECBlocks.register();
        CECBlockEntity.register();
        // anything requiring the mod event bus must go in postBusRegister()
        CECLang.register();

        CECCannonContraptionTypes.register();
        CECPartials.register();
        // Create may already have frozen its contraption registry by the time our
        // constructor runs. when that happens calls to new ContraptionType throw
        // IllegalStateException.  we temporarily unfreeze the registry via
        // reflection so our entries can be added, then let Create re-freeze it
        // when it next does so.
        // Note: the field is named "frozen" in Mojang mappings, "f_205845_" in SRG,
        // and "field_36463" in intermediary. Find it by type to work across all.
        try {
            Registry<?> reg = CreateBuiltInRegistries.CONTRAPTION_TYPE;
            Field frozen = null;
            for (Field f : reg.getClass().getDeclaredFields()) {
                if (f.getType() == boolean.class) {
                    f.setAccessible(true);
                    if (f.getBoolean(reg)) {
                        frozen = f;
                        break;
                    }
                }
            }
            if (frozen != null) {
                frozen.setBoolean(reg, false);
            }
        } catch (Throwable t) {
            getLogger().warn("Failed to unfreeze contraption registry", t);
        }
        CECContraptionTypes.register();
        // Particle and sound registrations use DeferredRegister and require a bus
        // NOTE: CECDefaultCannonMountPropertiesSerializers.init() must be called
        // during common setup (after registries fire), not here.
        CECConfig.register();

        PacketHandler.register();
    }

    /**
     * Called once the mod event bus is available (Forge constructor after
     * EventBuses.registerModEventBus, or inside Fabric's onInitialize).
     * This handles anything using {@link DeferredRegister}.
     */
    public static void postBusRegister() {
        CECCreativeModeTabs.register();
        CECParticles.register();
        CECSoundEvents.register();
    }

    /**
     * Must be called after the mod event bus is registered (Forge) or during
     * fabric init. Registers tabs which rely on {@link CECCreativeModeTabs} and
     * its deferred register.
     */
    public static void registerCreativeTabs() {
        CECCreativeModeTabs.register();
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
