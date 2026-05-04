package net.arsenalists.createenergycannons.content.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EnergyCannonPlumeParticle extends NoRenderParticle {

    private final Vec3 direction;
    private final float size;
    private final float power;
    private final int cannonType;

    EnergyCannonPlumeParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                              float size, float power, int cannonType, int lifetime) {
        super(level, x, y, z);
        this.direction = new Vec3(dx, dy, dz);
        this.size = size;
        this.power = power;
        this.cannonType = cannonType;
        this.gravity = 0;
        this.friction = 0.90f;
        this.lifetime = lifetime;

        float f = this.power / 4;
        this.setParticleSpeed(dx * f, dy * f, dz * f);
    }

    @Override
    public void tick() {
        Vec3 right = this.direction.cross(new Vec3(Direction.UP.step()));
        Vec3 up = this.direction.cross(right);
        double progress = this.lifetime == 0 ? 1 : Mth.clamp((float) this.age / (float) this.lifetime, 0, 1);

        float smallScale = this.size * 0.25f;
        int count = Math.min(3, Mth.ceil(smallScale * 5));

        for (int i = 0; i < count; ++i) {
            double dirScale = 0.3 * progress + 0.8 + this.random.nextDouble() * 0.25;
            double dirPerpScale = smallScale * 0.25f;

            Vec3 vel = this.direction.scale(dirScale)
                .add(right.scale((this.random.nextDouble() - this.random.nextDouble()) * dirPerpScale))
                .add(up.scale((this.random.nextDouble() - this.random.nextDouble()) * dirPerpScale));

            int smokeLifetime = 30 + this.random.nextInt(10) + Mth.ceil(10 * progress) + (int) Math.ceil(this.power * this.power) / 2;

            this.level.addParticle(new EnergyMuzzleParticleData((int) this.power, this.cannonType, smallScale),
                true, this.x, this.y, this.z, vel.x, vel.y, vel.z);
        }

        super.tick();
    }

    public static class Provider implements ParticleProvider<EnergyCannonPlumeParticleData> {
        @Override
        public Particle createParticle(EnergyCannonPlumeParticleData data, ClientLevel level,
                                       double x, double y, double z, double dx, double dy, double dz) {
            return new EnergyCannonPlumeParticle(level, x, y, z, dx, dy, dz,
                data.size(), data.power(), data.cannonType(), data.lifetime());
        }
    }
}
