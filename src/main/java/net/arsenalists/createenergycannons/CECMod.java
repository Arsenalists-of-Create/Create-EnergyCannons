package net.arsenalists.createenergycannons;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.arsenalists.createenergycannons.content.particle.CECVertexFormats;
import net.arsenalists.createenergycannons.content.particle.EnergyCannonPlumeParticle;
import net.arsenalists.createenergycannons.content.particle.EnergyMuzzleParticle;
import net.arsenalists.createenergycannons.content.particle.LaserGlareParticle;
import net.arsenalists.createenergycannons.network.PacketHandler;
import net.arsenalists.createenergycannons.registry.*;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.io.IOException;

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

        CECCannonContraptionTypes.register();
        CECPartials.register();
        CECParticles.register();

        modEventBus.addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        CECDefaultCannonMountPropertiesSerializers.init();
        event.enqueueWork(PacketHandler::register);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        private static ShaderInstance energyMuzzleParticleShader;

        public static ShaderInstance getEnergyMuzzleParticleShader() {
            return energyMuzzleParticleShader;
        }

        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(
                new ShaderInstance(
                    event.getResourceProvider(),
                    "createenergycannons:energy_muzzle_particle",
                    CECVertexFormats.PARTICLE_WITH_OVERLAY
                ),
                shader -> energyMuzzleParticleShader = shader
            );
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(CECParticles.LASER_GLARE.get(), LaserGlareParticle.Factory::new);
            event.registerSpriteSet(CECParticles.ENERGY_MUZZLE.get(), EnergyMuzzleParticle.Provider::new);
            event.registerSpecial(CECParticles.ENERGY_CANNON_PLUME.get(), new EnergyCannonPlumeParticle.Provider());
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static ResourceLocation resource(String id) {
        return new ResourceLocation(MODID, id);
    }
}
