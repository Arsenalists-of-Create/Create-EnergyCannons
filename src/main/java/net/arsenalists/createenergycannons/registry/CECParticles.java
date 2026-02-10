package net.arsenalists.createenergycannons.registry;

import com.mojang.serialization.Codec;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.particle.EnergyCannonPlumeParticleData;
import net.arsenalists.createenergycannons.content.particle.EnergyMuzzleParticleData;
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

    public static final RegistryObject<ParticleType<EnergyMuzzleParticleData>> ENERGY_MUZZLE =
        PARTICLES.register("energy_muzzle", () -> new ParticleType<>(false, EnergyMuzzleParticleData.DESERIALIZER) {
            @Override
            public Codec<EnergyMuzzleParticleData> codec() {
                return EnergyMuzzleParticleData.CODEC;
            }
        });

    public static final RegistryObject<ParticleType<EnergyCannonPlumeParticleData>> ENERGY_CANNON_PLUME =
        PARTICLES.register("energy_cannon_plume", () -> new ParticleType<>(false, EnergyCannonPlumeParticleData.DESERIALIZER) {
            @Override
            public Codec<EnergyCannonPlumeParticleData> codec() {
                return EnergyCannonPlumeParticleData.CODEC;
            }
        });

    public static void register() {
        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
