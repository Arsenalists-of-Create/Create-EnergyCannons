package net.arsenalists.createenergycannons;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.arsenalists.createenergycannons.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CECMod.MODID)
public class CECMod {
    public static final String MODID = "createenergycannons";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);

    public CECMod() {
        getLogger().info("Initializing Create Energy Cannons!");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        REGISTRATE.registerEventListeners(modEventBus);
        CECItems.register();
        CECBlocks.register();
        CECBlockEntity.register();
        CECCreativeModeTabs.register(modEventBus);
        CECLang.register();
        CECContraptionTypes.register();
        CECCannonContraptionTypes.register();
        CECPartials.register();
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(CECMod::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        CECDefaultCannonMountPropertiesSerializers.init();
    }

    public static void onClientSetup(FMLClientSetupEvent event) {

    }


    public static Logger getLogger() {
        return LOGGER;
    }

    public static ResourceLocation resource(String id) {
        return new ResourceLocation(MODID, id);
    }
}
