package net.arsenalists.createenergycannons.neoforge;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.config.ConfigType;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.content.energy.EnergyCapHelper;
import net.arsenalists.createenergycannons.content.energy.IModEnergyStorage;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import net.arsenalists.createenergycannons.neoforge.loot.SledLootModifier;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.arsenalists.createenergycannons.registry.CECCreateRegistries;
import net.arsenalists.createenergycannons.registry.CECDefaultCannonMountPropertiesSerializers;
import net.createmod.catnip.config.ConfigBase;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Map;

@Mod(CECMod.MODID)
public final class CECModNeoForge {

    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM_REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CECMod.MODID);

    static {
        GLM_REGISTRY.register("sled_persistence", () -> SledLootModifier.CODEC.get());
    }

    public CECModNeoForge(IEventBus modEventBus, ModContainer container) {
        new CECMod();

        GLM_REGISTRY.register(modEventBus);

        CECMod.REGISTRATE.registerEventListeners(modEventBus);
        CECMod.postBusRegister();

        modEventBus.addListener(this::onRegister);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onRegisterCapabilities);

        // Keep WaterTemp's "now" current so water temperature decays on read anywhere it's stored.
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(CECModNeoForge::onServerTick);
        // Tab population is handled by Registrate's own BuildCreativeModeTabContentsEvent
        modEventBus.addListener(CECNeoForgeEvents::onConfigLoad);
        modEventBus.addListener(CECNeoForgeEvents::onConfigReload);

        if (FMLEnvironment.dist.isClient()) {
            CECNeoForgeClientEvents.register(modEventBus);
        }

        EnergyCapHelper.setProvider((be, side) -> {
            if (be.getLevel() == null) return EnergyCapHelper.EMPTY;
            IEnergyStorage storage = be.getLevel().getCapability(
                    Capabilities.EnergyStorage.BLOCK, be.getBlockPos(), side);
            return storage == null ? EnergyCapHelper.EMPTY : wrapNeoForgeEnergy(storage);
        });

        for (Map.Entry<ConfigType, ConfigBase> pair : CECConfig.CONFIGS.entrySet()) {
            container.registerConfig(toNeoForgeType(pair.getKey()), pair.getValue().specification);
        }
    }

    private static net.neoforged.fml.config.ModConfig.Type toNeoForgeType(ConfigType t) {
        return switch (t) {
            case CLIENT -> net.neoforged.fml.config.ModConfig.Type.CLIENT;
            case COMMON -> net.neoforged.fml.config.ModConfig.Type.COMMON;
            case SERVER -> net.neoforged.fml.config.ModConfig.Type.SERVER;
        };
    }

    private void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(CreateBuiltInRegistries.CONTRAPTION_TYPE.key())) {
            event.register(CreateBuiltInRegistries.CONTRAPTION_TYPE.key(), helper ->
                    CECCreateRegistries.registerContraptionTypes(helper::register));
        }
        if (event.getRegistryKey().equals(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key())) {
            event.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.key(), helper ->
                    CECCreateRegistries.registerArmInteractionPointTypes(helper::register));
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CECMod.init();
            CECDefaultCannonMountPropertiesSerializers.init();
        });
    }

    private static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        net.minecraft.server.level.ServerLevel overworld = event.getServer().overworld();
        if (overworld != null) {
            net.arsenalists.createenergycannons.content.cooling.WaterTemp.currentTick = overworld.getGameTime();
        }
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                CECBlockEntity.CREATIVE_BATTERY.get(),
                (be, side) -> wrapModEnergy(be.getEnergyStorage())
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                CECBlockEntity.ENERGY_CANNON_MOUNT.get(),
                (be, side) -> wrapModEnergy(be.getEnergyStorage())
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                CECBlockEntity.FIXED_ENERGY_CANNON_MOUNT.get(),
                (be, side) -> wrapModEnergy(be.getEnergyStorage())
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CECBlockEntity.COOLING_UNIT.get(),
                (be, side) -> be.getCoolantHandler()
        );
    }

    private static IEnergyStorage wrapModEnergy(IModEnergyStorage storage) {
        return new IEnergyStorage() {
            @Override public int receiveEnergy(int max, boolean sim) { return storage.receiveEnergy(max, sim); }
            @Override public int extractEnergy(int max, boolean sim) { return storage.extractEnergy(max, sim); }
            @Override public int getEnergyStored() { return storage.getEnergyStored(); }
            @Override public int getMaxEnergyStored() { return storage.getMaxEnergyStored(); }
            @Override public boolean canExtract() { return storage.canExtract(); }
            @Override public boolean canReceive() { return storage.canReceive(); }
        };
    }

    private static IModEnergyStorage wrapNeoForgeEnergy(IEnergyStorage storage) {
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
