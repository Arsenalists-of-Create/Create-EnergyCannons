package net.arsenalists.createenergycannons;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.arsenalists.createenergycannons.registry.CECBlocks;
import net.arsenalists.createenergycannons.registry.CECCreativeModeTabs;
import net.arsenalists.createenergycannons.registry.CECLang;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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
        CECBlocks.register();
        CECBlockEntity.register();
        CECCreativeModeTabs.register(modEventBus);
        CECLang.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static Logger getLogger() {
        return LOGGER;
    }

}
