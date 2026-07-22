package net.arsenalists.createenergycannons.content.cooling;

/**
 * Loader-agnostic view of a cooling unit's coolant, in millibuckets.
 * <p>
 * Each loader's fluid API differs - NeoForge and Forge count int millibuckets, Fabric counts
 * long droplets through transactions - so the cannon's cooling logic talks to this instead of
 * to any {@code FluidStack}. Implementations convert units and handle transactions internally.
 */
public interface CoolantUnit {

    int getColdAmount();

    int getHotAmount();

    int getHotCapacity();

    /** Move up to {@code mb} of coolant from cold to hot, heating it by heatPerUse capped at maxTemp; returns mB moved. */
    int circulate(int mb, int heatPerUse, int maxTemp, long now);

    void markCoolantChanged();
}
