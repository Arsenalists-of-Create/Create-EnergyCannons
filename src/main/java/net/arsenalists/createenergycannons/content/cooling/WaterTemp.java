package net.arsenalists.createenergycannons.content.cooling;

//? if >=1.21 {
/*import net.arsenalists.createenergycannons.registry.CECDataComponents;
import net.neoforged.neoforge.fluids.FluidStack;
*///?}

/**
 * Temperature bookkeeping for water. The stored temperature is stamped with the game time it was set;
 * the current (decayed) temperature is derived from elapsed time whenever it's read - so water cools
 * passively wherever it sits, without any tank needing to tick.
 * <p>
 * The decay math and shared state live here for every version; how the base temperature is stored on a
 * fluid is loader-specific (data components on 1.21+, NBT on 1.20.1) and lives with each loader's tanks.
 */
public final class WaterTemp {
    public static final int AMBIENT = 20;
    public static int TICKS_PER_DEGREE = 100; // ~5s per degree shed - refreshed from config on load

    /** Newest game time seen by a client/server tick; used as "now" for decay on read. */
    public static volatile long currentTick = 0L;

    private WaterTemp() {
    }

    /** Current temperature of water stamped at {@code base} degrees as of {@code heatedAt}, seen at {@code now}. */
    public static int effective(int base, long heatedAt, long now) {
        long elapsed = Math.max(0L, now - heatedAt);
        return Math.max(AMBIENT, base - (int) (elapsed / TICKS_PER_DEGREE));
    }

    //? if >=1.21 {
    /*public static int effective(FluidStack stack, long now) {
        if (!stack.has(CECDataComponents.TEMPERATURE.get())) return AMBIENT;
        int base = stack.getOrDefault(CECDataComponents.TEMPERATURE.get(), AMBIENT);
        long heatedAt = stack.getOrDefault(CECDataComponents.HEATED_AT.get(), now);
        return effective(base, heatedAt, now);
    }

    public static int effective(FluidStack stack) {
        return effective(stack, currentTick);
    }

    /^* Stamp the water with a temperature as of the given time; its decay clock restarts. ^/
    public static void heat(FluidStack stack, int temperature, long now) {
        stack.set(CECDataComponents.TEMPERATURE.get(), temperature);
        stack.set(CECDataComponents.HEATED_AT.get(), now);
    }
    *///?}
}
