package net.arsenalists.createenergycannons.content.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class LaserGlareParticle extends TextureSheetParticle {

    public enum Layer {
        //                 scale  alpha   R      G      B      sizeJitter
        MUZZLE(            1.0f,  1.0f,   0.5f,  0.7f,  1.0f,  0.0f),
        IMPACT(            3.0f,  1.0f,   0.6f,  0.75f, 1.0f,  0.0f);

        public final float scale, alpha;
        public final float r, g, b;
        public final float sizeJitter;

        Layer(float scale, float alpha, float r, float g, float b,
              float sizeJitter) {
            this.scale = scale;
            this.alpha = alpha;
            this.r = r;
            this.g = g;
            this.b = b;
            this.sizeJitter = sizeJitter;
        }
    }

    protected LaserGlareParticle(ClientLevel world, double x, double y, double z,
                                 Layer layer, SpriteSet sprites) {
        super(world, x, y, z, 0, 0, 0);

        this.lifetime = 1;
        this.gravity = 0f;
        this.hasPhysics = false;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        float jitter = 1.0f + (world.random.nextFloat() - 0.5f) * 2.0f * layer.sizeJitter;
        this.quadSize = layer.scale * jitter;

        this.alpha = layer.alpha;
        this.rCol = layer.r;
        this.gCol = layer.g;
        this.bCol = layer.b;

        this.roll = 0;
        this.oRoll = 0;

        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return LaserParticleRenderTypes.ADDITIVE_NO_DEPTH_WRITE;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            int index = Mth.clamp(Math.round((float) vx), 0,
                    Layer.values().length - 1);
            Layer layer = Layer.values()[index];
            return new LaserGlareParticle(world, x, y, z, layer, sprites);
        }
    }
}