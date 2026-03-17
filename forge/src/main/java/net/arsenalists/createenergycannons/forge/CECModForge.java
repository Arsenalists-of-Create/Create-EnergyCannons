package net.arsenalists.createenergycannons.forge;

import com.mojang.serialization.Codec;
import dev.architectury.platform.forge.EventBuses;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.content.energy.EnergyCapHelper;
import net.arsenalists.createenergycannons.content.energy.IModEnergyStorage;
import net.arsenalists.createenergycannons.forge.loot.SledLootModifier;
import net.arsenalists.createenergycannons.registry.CECContraptionTypes;
import net.arsenalists.createenergycannons.registry.CECDefaultCannonMountPropertiesSerializers;
import net.createmod.catnip.config.ConfigBase;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

@Mod(CECMod.MODID)
public final class CECModForge {

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM_REGISTRY =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CECMod.MODID);

    private static final RegistryObject<Codec<SledLootModifier>> SLED_LOOT_MODIFIER =
            GLM_REGISTRY.register("sled_persistence", SledLootModifier.CODEC);

    public CECModForge() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        GLM_REGISTRY.register(modEventBus);

        // basic initialization that doesn't depend on a bus
        CECMod.init();

        // Register Registrate's event listeners so RegisterEvent populates entries
        CECMod.REGISTRATE.registerEventListeners(modEventBus);

        // Submit our event bus so the deferred registers can do their work
        EventBuses.registerModEventBus(CECMod.MODID, modEventBus);

        // post-bus registrations (tabs, particles, sounds)
        CECMod.postBusRegister();

        // Register common mod event bus listeners
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(CECForgeEvents::onConfigLoad);
        modEventBus.addListener(CECForgeEvents::onConfigReload);

        // Register client-only mod event bus listeners only on client side
        if (FMLEnvironment.dist.isClient()) {
            registerClientListeners(modEventBus);
        }

        // Set up Forge energy provider
        EnergyCapHelper.setProvider((be, side) ->
                be.getCapability(ForgeCapabilities.ENERGY, side)
                        .map(CECModForge::wrapForgeEnergy)
                        .orElse(EnergyCapHelper.EMPTY)
        );

        // Register configs with Forge
        ModLoadingContext context = ModLoadingContext.get();
        for (Map.Entry<ModConfig.Type, ConfigBase> pair : CECConfig.CONFIGS.entrySet()) {
            context.registerConfig(pair.getKey(), pair.getValue().specification);
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        CECDefaultCannonMountPropertiesSerializers.init();
    }

    private static void registerClientListeners(net.minecraftforge.eventbus.api.IEventBus modEventBus) {
        modEventBus.addListener(CECForgeClientEvents::registerParticleFactories);
        modEventBus.addListener(CECForgeClientEvents::registerShaders);
        modEventBus.addListener(CECForgeClientEvents::onClientSetup);
        modEventBus.addListener(net.arsenalists.createenergycannons.forge.client.SledModelHandler::onRegisterAdditional);
        modEventBus.addListener(net.arsenalists.createenergycannons.forge.client.SledModelHandler::onModifyBakingResult);
    }

    private static IModEnergyStorage wrapForgeEnergy(IEnergyStorage storage) {
        return new IModEnergyStorage() {
            @Override public int receiveEnergy(int max, boolean sim) { return storage.receiveEnergy(max, sim); }
            @Override public int extractEnergy(int max, boolean sim) { return storage.extractEnergy(max, sim); }
            @Override public int getEnergyStored() { return storage.getEnergyStored(); }
            @Override public int getMaxEnergyStored() { return storage.getMaxEnergyStored(); }
            @Override public boolean canExtract() { return storage.canExtract(); }
            @Override public boolean canReceive() { return storage.canReceive(); }
        };
    }
}
