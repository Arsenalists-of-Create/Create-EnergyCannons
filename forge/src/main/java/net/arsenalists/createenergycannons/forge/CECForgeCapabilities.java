package net.arsenalists.createenergycannons.forge;

import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlockEntity;
import net.arsenalists.createenergycannons.content.energy.IModEnergyStorage;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = CECMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CECForgeCapabilities {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity be = event.getObject();

        if (be instanceof CreativeBatteryBlockEntity batteryBE) {
            event.addCapability(
                new ResourceLocation(CECMod.MODID, "creative_battery_energy"),
                createEnergyProvider(batteryBE::getEnergyStorage)
            );
        } else if (be instanceof EnergyCannonMountBlockEntity mountBE) {
            event.addCapability(
                new ResourceLocation(CECMod.MODID, "energy_mount_energy"),
                createEnergyProvider(mountBE::getEnergyStorage)
            );
        }
    }

    private static ICapabilityProvider createEnergyProvider(Supplier<IModEnergyStorage> storageSupplier) {
        IEnergyStorage forgeStorage = new IEnergyStorage() {
            @Override public int receiveEnergy(int max, boolean sim) { return storageSupplier.get().receiveEnergy(max, sim); }
            @Override public int extractEnergy(int max, boolean sim) { return storageSupplier.get().extractEnergy(max, sim); }
            @Override public int getEnergyStored() { return storageSupplier.get().getEnergyStored(); }
            @Override public int getMaxEnergyStored() { return storageSupplier.get().getMaxEnergyStored(); }
            @Override public boolean canExtract() { return storageSupplier.get().canExtract(); }
            @Override public boolean canReceive() { return storageSupplier.get().canReceive(); }
        };
        LazyOptional<IEnergyStorage> lazyOptional = LazyOptional.of(() -> forgeStorage);

        return new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ForgeCapabilities.ENERGY)
                    return lazyOptional.cast();
                return LazyOptional.empty();
            }
        };
    }
}
