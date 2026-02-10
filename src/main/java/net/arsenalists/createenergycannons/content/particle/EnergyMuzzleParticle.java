package net.arsenalists.createenergycannons.content.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.particle.CECVertexFormats;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EnergyMuzzleParticle extends TextureSheetParticle {

    private static ParticleRenderType makeRenderType(ResourceLocation gradient, String name) {
        return new ParticleRenderType() {
            @Override
            public void begin(BufferBuilder builder, TextureManager textureManager) {
                RenderSystem.depthMask(true);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                );
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
                RenderSystem.setShaderTexture(3, gradient);
                RenderSystem.setShader(CECMod.ClientModEvents::getEnergyMuzzleParticleShader);
                builder.begin(VertexFormat.Mode.QUADS, CECVertexFormats.PARTICLE_WITH_OVERLAY);
            }

            @Override
            public void end(Tesselator tesselator) {
                tesselator.end();
            }

            @Override
            public String toString() { return name; }
        };
    }

    static final ParticleRenderType RAIL_RENDER_TYPE = makeRenderType(
        CECMod.resource("textures/particle/rail_muzzle_gradient.png"), "CEC_RAIL_MUZZLE");
    static final ParticleRenderType COIL_RENDER_TYPE = makeRenderType(
        CECMod.resource("textures/particle/coil_muzzle_gradient.png"), "CEC_COIL_MUZZLE");

    private final int power;
    private final int cannonType;
    private final SpriteSet sprites;

    protected EnergyMuzzleParticle(ClientLevel level, double x, double y, double z,
                                    double dx, double dy, double dz,
                                    SpriteSet sprites, int power, int cannonType) {
        super(level, x, y, z, dx, dy, dz);
        this.sprites = sprites;
        this.power = power;
        this.cannonType = cannonType;

        this.lifetime = 40 + level.random.nextInt(20);
        this.quadSize = 1.5f + level.random.nextFloat() * 0.5f;
        this.gravity = -0.02f;
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.alpha = 0.8f;

        this.xd = dx * 0.3 + (level.random.nextFloat() - 0.5) * 0.05;
        this.yd = dy * 0.3 + level.random.nextFloat() * 0.02;
        this.zd = dz * 0.3 + (level.random.nextFloat() - 0.5) * 0.05;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        float lifeRatio = (float) this.age / (float) this.lifetime;
        this.alpha = Mth.lerp(lifeRatio, 0.8f, 0.0f);
        this.quadSize *= 1.01f;
        this.xd *= 0.95;
        this.yd *= 0.95;
        this.zd *= 0.95;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return cannonType == 0 ? RAIL_RENDER_TYPE : COIL_RENDER_TYPE;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 vec3 = renderInfo.getPosition();
        float f = (float)(Mth.lerp(partialTicks, this.xo, this.x) - vec3.x());
        float g = (float)(Mth.lerp(partialTicks, this.yo, this.y) - vec3.y());
        float h = (float)(Mth.lerp(partialTicks, this.zo, this.z) - vec3.z());
        Quaternionf quaternion;
        if (this.roll == 0.0F) {
            quaternion = renderInfo.rotation();
        } else {
            quaternion = new Quaternionf(renderInfo.rotation());
            float i = Mth.lerp(partialTicks, this.oRoll, this.roll);
            quaternion.mul(Axis.ZP.rotation(i));
        }

        Vector3f[] vector3fs = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F),
            new Vector3f(-1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float j = this.getQuadSize(partialTicks);

        for(int k = 0; k < 4; ++k) {
            Vector3f vector3f2 = vector3fs[k];
            quaternion.transform(vector3f2);
            vector3f2.mul(j);
            vector3f2.add(f, g, h);
        }

        float l = this.getU0();
        float m = this.getU1();
        float n = this.getV0();
        float o = this.getV1();
        int p = this.getLightColor(partialTicks);
        int cannonPower = this.power;

        buffer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z())
            .uv(m, o)
            .overlayCoords(0, cannonPower)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(p)
            .endVertex();
        buffer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z())
            .uv(m, n)
            .overlayCoords(0, cannonPower)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(p)
            .endVertex();
        buffer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z())
            .uv(l, n)
            .overlayCoords(0, cannonPower)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(p)
            .endVertex();
        buffer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z())
            .uv(l, o)
            .overlayCoords(0, cannonPower)
            .color(this.rCol, this.gCol, this.bCol, this.alpha)
            .uv2(p)
            .endVertex();
    }

    public static class Provider implements ParticleProvider<EnergyMuzzleParticleData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(EnergyMuzzleParticleData data, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new EnergyMuzzleParticle(level, x, y, z, dx, dy, dz, sprites, data.power(), data.cannonType());
        }
    }
}
