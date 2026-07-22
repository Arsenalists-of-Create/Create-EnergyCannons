package net.arsenalists.createenergycannons.registry;

//? if >=1.21 {
/*import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;

/^*
 * Data components carried on coolant FluidStacks. Temperature travels with the fluid
 * through pipes and single-variant tanks; our own tanks average it when fluids merge.
 ^/
public class CECDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(CECMod.MODID, Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<Integer>> TEMPERATURE =
            COMPONENTS.register("temperature", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    // Game time the temperature was last set - passive decay is computed from elapsed time on read.
    public static final RegistrySupplier<DataComponentType<Long>> HEATED_AT =
            COMPONENTS.register("heated_at", () -> DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());

    public static void register() {
        COMPONENTS.register();
    }
}
*///?} else {
public class CECDataComponents {
    public static void register() {}
}
//?}
