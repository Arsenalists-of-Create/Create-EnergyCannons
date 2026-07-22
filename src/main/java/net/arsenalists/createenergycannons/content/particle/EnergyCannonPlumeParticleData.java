package net.arsenalists.createenergycannons.content.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arsenalists.createenergycannons.registry.CECParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public class EnergyCannonPlumeParticleData implements ParticleOptions {

    public static final Codec<EnergyCannonPlumeParticleData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("size").forGetter(d -> d.size),
            Codec.FLOAT.fieldOf("power").forGetter(d -> d.power),
            Codec.INT.fieldOf("cannonType").forGetter(d -> d.cannonType),
            Codec.INT.fieldOf("lifetime").forGetter(d -> d.lifetime)
        ).apply(instance, EnergyCannonPlumeParticleData::new)
    );

    //? if <1.21 {
    @SuppressWarnings("deprecation")
    public static final Deserializer<EnergyCannonPlumeParticleData> DESERIALIZER = new Deserializer<>() {
        @Override
        public EnergyCannonPlumeParticleData fromCommand(ParticleType<EnergyCannonPlumeParticleData> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float size = reader.readFloat();
            reader.expect(' ');
            float power = reader.readFloat();
            reader.expect(' ');
            int cannonType = reader.readInt();
            reader.expect(' ');
            int lifetime = reader.readInt();
            return new EnergyCannonPlumeParticleData(size, power, cannonType, lifetime);
        }

        @Override
        public EnergyCannonPlumeParticleData fromNetwork(ParticleType<EnergyCannonPlumeParticleData> type, FriendlyByteBuf buffer) {
            return new EnergyCannonPlumeParticleData(buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readInt());
        }
    };
    //?}

    //? if >=1.21 {
    /*public static final com.mojang.serialization.MapCodec<EnergyCannonPlumeParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("size").forGetter(d -> d.size),
            Codec.FLOAT.fieldOf("power").forGetter(d -> d.power),
            Codec.INT.fieldOf("cannonType").forGetter(d -> d.cannonType),
            Codec.INT.fieldOf("lifetime").forGetter(d -> d.lifetime)
        ).apply(instance, EnergyCannonPlumeParticleData::new)
    );

    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, EnergyCannonPlumeParticleData> STREAM_CODEC =
        net.minecraft.network.codec.StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.FLOAT, EnergyCannonPlumeParticleData::size,
            net.minecraft.network.codec.ByteBufCodecs.FLOAT, EnergyCannonPlumeParticleData::power,
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, EnergyCannonPlumeParticleData::cannonType,
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, EnergyCannonPlumeParticleData::lifetime,
            EnergyCannonPlumeParticleData::new
        );
    *///?}

    private final float size;
    private final float power;
    private final int cannonType;
    private final int lifetime;

    public EnergyCannonPlumeParticleData(float size, float power, int cannonType, int lifetime) {
        this.size = size;
        this.power = power;
        this.cannonType = cannonType;
        this.lifetime = lifetime;
    }

    public float size() { return this.size; }
    public float power() { return this.power; }
    public int cannonType() { return this.cannonType; }
    public int lifetime() { return this.lifetime; }

    @Override
    public ParticleType<?> getType() {
        return CECParticles.ENERGY_CANNON_PLUME.get();
    }

    //? if <1.21 {
    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.size);
        buffer.writeFloat(this.power);
        buffer.writeInt(this.cannonType);
        buffer.writeInt(this.lifetime);
    }

    @Override
    public String writeToString() {
        return String.format("%f %f %d %d", this.size, this.power, this.cannonType, this.lifetime);
    }
    //?}
}
