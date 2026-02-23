package net.arsenalists.createenergycannons.content.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arsenalists.createenergycannons.registry.CECParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public record EnergyMuzzleParticleData(int power, int cannonType, float size) implements ParticleOptions {

    public static final int TYPE_GREEN = 0;
    public static final int TYPE_RED = 1;

    // Legacy aliases
    public static final int TYPE_RAIL = TYPE_GREEN;
    public static final int TYPE_COIL = TYPE_RED;

    public static final Codec<EnergyMuzzleParticleData> CODEC = RecordCodecBuilder.create(i ->
        i.group(
            Codec.INT.fieldOf("power").forGetter(EnergyMuzzleParticleData::power),
            Codec.INT.fieldOf("cannonType").forGetter(EnergyMuzzleParticleData::cannonType),
            Codec.FLOAT.fieldOf("size").forGetter(EnergyMuzzleParticleData::size)
        ).apply(i, EnergyMuzzleParticleData::new)
    );

    @SuppressWarnings("deprecation")
    public static final Deserializer<EnergyMuzzleParticleData> DESERIALIZER =
        new Deserializer<>() {
            @Override
            public EnergyMuzzleParticleData fromCommand(ParticleType<EnergyMuzzleParticleData> type,
                                                         StringReader reader) {
                return new EnergyMuzzleParticleData(1, TYPE_RAIL, 1.0f);
            }

            @Override
            public EnergyMuzzleParticleData fromNetwork(ParticleType<EnergyMuzzleParticleData> type,
                                                         FriendlyByteBuf buf) {
                return new EnergyMuzzleParticleData(buf.readVarInt(), buf.readVarInt(), buf.readFloat());
            }
        };

    @Override
    public ParticleType<?> getType() {
        return CECParticles.ENERGY_MUZZLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(power);
        buf.writeVarInt(cannonType);
        buf.writeFloat(size);
    }

    @Override
    public String writeToString() {
        return "energy_muzzle " + power + " " + cannonType + " " + size;
    }
}
