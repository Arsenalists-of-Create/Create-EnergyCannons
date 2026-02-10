package net.arsenalists.createenergycannons.registry;

import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CECSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CECMod.MODID);

\    public static final RegistryObject<SoundEvent> COILGUN_FIRE = register("coilgun_fire");

    public static final RegistryObject<SoundEvent> RAILGUN_FIRE = register("railgun_fire");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = CECMod.resource(name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
        SOUNDS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
    }
}
