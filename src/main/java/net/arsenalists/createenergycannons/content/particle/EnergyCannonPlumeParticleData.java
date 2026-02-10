package net.arsenalists.createenergycannons.content.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.particle.ICustomParticleData;
import net.arsenalists.createenergycannons.registry.CECParticles;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnergyCannonPlumeParticleData implements ParticleOptions, ICustomParticleData<EnergyCannonPlumeParticleData> {

    public static final Codec<EnergyCannonPlumeParticleData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("size").forGetter(d -> d.size),
            Codec.FLOAT.fieldOf("power").forGetter(d -> d.power),
            Codec.INT.fieldOf("cannonType").forGetter(d -> d.cannonType),
            Codec.INT.fieldOf("lifetime").forGetter(d -> d.lifetime)
        ).apply(instance, EnergyCannonPlumeParticleData::new)
    );

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

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.size)
            .writeFloat(this.power);
        buffer.writeVarInt(this.cannonType);
        buffer.writeVarInt(this.lifetime);
    }

    @Override
    public String writeToString() {
        return String.format("%f %f %d %d", this.size, this.power, this.cannonType, this.lifetime);
    }

    @Override
    public Deserializer<EnergyCannonPlumeParticleData> getDeserializer() {
        return DESERIALIZER;
    }

    @Override
    public Codec<EnergyCannonPlumeParticleData> getCodec(ParticleType<EnergyCannonPlumeParticleData> type) {
        return CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ParticleProvider<EnergyCannonPlumeParticleData> getFactory() {
        return new EnergyCannonPlumeParticle.Provider();
    }
}
