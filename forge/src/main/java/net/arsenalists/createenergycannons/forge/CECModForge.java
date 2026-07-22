package net.arsenalists.createenergycannons.forge;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import dev.architectury.platform.forge.EventBuses;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.config.ConfigType;
import net.arsenalists.createenergycannons.content.energy.EnergyCapHelper;
import net.arsenalists.createenergycannons.content.energy.IModEnergyStorage;
import net.arsenalists.createenergycannons.forge.loot.SledLootModifier;
import net.arsenalists.createenergycannons.registry.CECCreateRegistries;
import net.arsenalists.createenergycannons.registry.CECDefaultCannonMountPropertiesSerializers;
import net.createmod.catnip.config.ConfigBase;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.api.distmarker.Dist;
import net.createmod.catnip.platform.CatnipServices;

import java.util.Map;

@Mod(CECMod.MODID)
public final class CECModForge {

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM_REGISTRY =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CECMod.MODID);

    private static final RegistryObject<Codec<SledLootModifier>> SLED_LOOT_MODIFIER =
            GLM_REGISTRY.register("sled_persistence", SledLootModifier.CODEC);

    public CECModForge() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        new CECMod();

        net.arsenalists.createenergycannons.content.cooling.CoolantTanksFactory.setFactory(
                net.arsenalists.createenergycannons.forge.cooling.ForgeCoolantTanks::new);

        GLM_REGISTRY.register(modEventBus);

        CECMod.REGISTRATE.registerEventListeners(modEventBus);
        EventBuses.registerModEventBus(CECMod.MODID, modEventBus);
        CECMod.postBusRegister();

        modEventBus.addListener(this::onRegister);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(CECForgeEvents::onConfigLoad);
        modEventBus.addListener(CECForgeEvents::onConfigReload);

        if (FMLEnvironment.dist.isClient()) {
            registerClientListeners(modEventBus);
        }

        EnergyCapHelper.setProvider((be, side) ->
                be.getCapability(ForgeCapabilities.ENERGY, side)
                        .map(CECModForge::wrapForgeEnergy)
                        .orElse(EnergyCapHelper.EMPTY)
        );

        ModLoadingContext context = ModLoadingContext.get();
        for (Map.Entry<ConfigType, ConfigBase> pair : CECConfig.CONFIGS.entrySet()) {
            context.registerConfig(toForgeType(pair.getKey()), pair.getValue().specification);
        }
    }

    private static ModConfig.Type toForgeType(ConfigType t) {
        return switch (t) {
            case CLIENT -> ModConfig.Type.CLIENT;
            case COMMON -> ModConfig.Type.COMMON;
            case SERVER -> ModConfig.Type.SERVER;
        };
    }

    private void onRegister(RegisterEvent event) {
        // Depending on mappings/Create wrapper, you may need to replace `.key()`
        // with the appropriate registry-key accessor for these built-in registries.

        if (event.getRegistryKey().equals(CreateBuiltInRegistries.CONTRAPTION_TYPE.key())) {
            event.register(CreateBuiltInRegistries.CONTRAPTION_TYPE.key(), helper ->
                    CECCreateRegistries.registerContraptionTypes(helper::register)
            );
        }

        if (event.getRegistryKey().equals(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key())) {
            event.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key(), helper ->
                    CECCreateRegistries.registerArmInteractionPointTypes(helper::register)
            );
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CECMod.init();
            CECDefaultCannonMountPropertiesSerializers.init();
        });
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