package net.arsenalists.createenergycannons.registry;

import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CECParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CECMod.MODID);

    public static final RegistryObject<SimpleParticleType> LASER_GLARE = PARTICLES.register("laser_glare",
            () -> new SimpleParticleType(false));

    public static void register() {
        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
