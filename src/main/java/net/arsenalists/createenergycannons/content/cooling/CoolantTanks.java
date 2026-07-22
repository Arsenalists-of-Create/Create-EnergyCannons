package net.arsenalists.createenergycannons.content.cooling;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Loader-specific coolant plumbing behind a plain-Java seam. The common
 * {@link CoolingUnitBlockEntity} holds one of these and never touches a {@code FluidStack} directly,
 * so each loader (Forge millibuckets, Fabric droplets + transactions) implements its own tanks.
 * Amounts are millibuckets.
 */
public interface CoolantTanks {

    int getColdAmount();

    int getHotAmount();

    int getHotCapacity();

    /** Drain up to {@code mb} from cold, heat it (capped at {@code maxTemp}), fill hot; returns mB moved. */
    int circulate(int mb, int heatPerUse, int maxTemp, long now);

    void save(CompoundTag tag);

    void load(CompoundTag tag);

    boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking);

    /** The platform fluid-handler/storage object, for the loader's capability registration. */
    Object fluidHandler();
}
