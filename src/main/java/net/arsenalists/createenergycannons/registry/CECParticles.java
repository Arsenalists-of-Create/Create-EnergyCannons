package net.arsenalists.createenergycannons.registry;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.particle.EnergyCannonPlumeParticleData;
import net.arsenalists.createenergycannons.content.particle.EnergyMuzzleParticleData;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class CECParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
        DeferredRegister.create(CECMod.MODID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<SimpleParticleType> LASER_GLARE = PARTICLES.register("laser_glare",
            () -> new SimpleParticleType(false) {});

    public static final RegistrySupplier<ParticleType<EnergyMuzzleParticleData>> ENERGY_MUZZLE =
        PARTICLES.register("energy_muzzle", () ->
            //? if <1.21 {
            new ParticleType<EnergyMuzzleParticleData>(false, EnergyMuzzleParticleData.DESERIALIZER) {
                @Override
                public Codec<EnergyMuzzleParticleData> codec() {
                    return EnergyMuzzleParticleData.CODEC;
                }
            }
            //?} else {
            /*new ParticleType<EnergyMuzzleParticleData>(false) {
                public com.mojang.serialization.MapCodec<EnergyMuzzleParticleData> codec() { return EnergyMuzzleParticleData.MAP_CODEC; }
                public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, EnergyMuzzleParticleData> streamCodec() { return EnergyMuzzleParticleData.STREAM_CODEC; }
            }
            *///?}
        );

    public static final RegistrySupplier<ParticleType<EnergyCannonPlumeParticleData>> ENERGY_CANNON_PLUME =
        PARTICLES.register("energy_cannon_plume", () ->
            //? if <1.21 {
            new ParticleType<EnergyCannonPlumeParticleData>(false, EnergyCannonPlumeParticleData.DESERIALIZER) {
                @Override
                public Codec<EnergyCannonPlumeParticleData> codec() {
                    return EnergyCannonPlumeParticleData.CODEC;
                }
            }
            //?} else {
            /*new ParticleType<EnergyCannonPlumeParticleData>(false) {
                public com.mojang.serialization.MapCodec<EnergyCannonPlumeParticleData> codec() { return EnergyCannonPlumeParticleData.MAP_CODEC; }
                public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, EnergyCannonPlumeParticleData> streamCodec() { return EnergyCannonPlumeParticleData.STREAM_CODEC; }
            }
            *///?}
        );

    public static void register() {
        PARTICLES.register();
    }
}
