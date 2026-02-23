package net.arsenalists.createenergycannons.fabric.client;

import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.client.CECClientShaders;
import net.arsenalists.createenergycannons.content.particle.CECVertexFormats;
import net.arsenalists.createenergycannons.content.particle.EnergyCannonPlumeParticle;
import net.arsenalists.createenergycannons.content.particle.EnergyMuzzleParticle;
import net.arsenalists.createenergycannons.content.particle.LaserGlareParticle;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.arsenalists.createenergycannons.registry.CECBlocks;
import net.arsenalists.createenergycannons.registry.CECParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountVisual;

public final class CECModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CECMod.clientInit();

        // Register Flywheel visual for the energy cannon mount
        VisualizerRegistry.setVisualizer(
            CECBlockEntity.ENERGY_CANNON_MOUNT.get(),
            new SimpleBlockEntityVisualizer<>(CannonMountVisual::new, be -> true)
        );

        // Register render layers for transparent textures (cutout)
        BlockRenderLayerMap.INSTANCE.putBlock(CECBlocks.BATTERY_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(CECBlocks.NETHERSTEEL_RAILGUN_BARREL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(CECBlocks.STEEL_RAILGUN_BARREL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(CECBlocks.STEEL_COILGUN_BARREL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(CECBlocks.NETHERSTEEL_COILGUN_BARREL.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(CECBlocks.LASER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(CECBlocks.ENERGY_CANNON_MOUNT.get(), RenderType.cutout());

        // Register custom shader for energy muzzle particle
        CoreShaderRegistrationCallback.EVENT.register(ctx -> {
            ctx.register(
                CECMod.resource("energy_muzzle_particle"),
                CECVertexFormats.PARTICLE_WITH_OVERLAY,
                shader -> CECClientShaders.energyMuzzleParticleShader = shader
            );
        });

        // Register particle factories
        ParticleFactoryRegistry.getInstance().register(
            CECParticles.LASER_GLARE.get(), LaserGlareParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(
            CECParticles.ENERGY_MUZZLE.get(), EnergyMuzzleParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(
            CECParticles.ENERGY_CANNON_PLUME.get(), spriteProvider -> new EnergyCannonPlumeParticle.Provider());

        // Register render events
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CECModFabricClient::renderAfterTranslucent);
        WorldRenderEvents.AFTER_ENTITIES.register(CECModFabricClient::renderAfterEntities);
    }

    private static void renderAfterTranslucent(WorldRenderContext ctx) {
        if (ctx.matrixStack() == null) return;
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        Vec3 cam = ctx.camera().getPosition();
        net.arsenalists.createenergycannons.client.LaserBurnRenderer.renderLaserBurns(
            ctx.matrixStack(), mc.renderBuffers().bufferSource(), cam, level);
        net.arsenalists.createenergycannons.content.cannons.laser.LaserBeamGlobalRenderer.renderFrame(
            ctx.matrixStack(), mc.renderBuffers().bufferSource(),
            cam, ctx.tickDelta(), level.getGameTime());
    }

    private static void renderAfterEntities(WorldRenderContext ctx) {
        if (ctx.matrixStack() == null) return;
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        Vec3 cam = ctx.camera().getPosition();
        net.arsenalists.createenergycannons.client.MagneticSledWorldRenderer.renderSleds(
            ctx.matrixStack(), mc.renderBuffers().bufferSource(), cam, level);
    }
}
