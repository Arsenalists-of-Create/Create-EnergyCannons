package net.arsenalists.createenergycannons.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class CECSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(CECMod.MODID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> COILGUN_FIRE = register("coilgun_fire");

    public static final RegistrySupplier<SoundEvent> RAILGUN_FIRE = register("railgun_fire");

    private static RegistrySupplier<SoundEvent> register(String name) {
        ResourceLocation id = CECMod.resource(name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
        SOUNDS.register();
    }
}
