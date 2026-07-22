package net.arsenalists.createenergycannons.content.cooling;

/**
 * Loader-injected factory for the Cooling Unit's coolant tanks. Each 1.20.1 loader sets its
 * implementation at init (see CECModForge / CECModFabric); NeoForge uses inline tanks and never sets it.
 * <p>
 * Deliberately not {@code @ExpectPlatform}: that convention needs the impl in this same package, which
 * splits the module under Forge's JPMS. This mirrors the repo's existing {@code EnergyCapHelper.setProvider}.
 */
public final class CoolantTanksFactory {

    @FunctionalInterface
    public interface Factory {
        CoolantTanks create(int capacity, Runnable onChange);
    }

    private static Factory factory;

    private CoolantTanksFactory() {
    }

    public static void setFactory(Factory f) {
        factory = f;
    }

    public static CoolantTanks create(int capacity, Runnable onChange) {
        if (factory == null)
            throw new IllegalStateException("No CoolantTanks factory registered for this platform");
        return factory.create(capacity, onChange);
    }
}
