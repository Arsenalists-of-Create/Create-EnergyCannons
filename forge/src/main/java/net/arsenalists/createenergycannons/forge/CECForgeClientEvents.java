package net.arsenalists.createenergycannons.forge;

import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.client.CECClientShaders;
import net.arsenalists.createenergycannons.client.LaserBurnRenderer;
import net.arsenalists.createenergycannons.client.MagneticSledWorldRenderer;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBeamGlobalRenderer;
import net.arsenalists.createenergycannons.content.particle.CECVertexFormats;
import net.arsenalists.createenergycannons.content.particle.EnergyCannonPlumeParticle;
import net.arsenalists.createenergycannons.content.particle.EnergyMuzzleParticle;
import net.arsenalists.createenergycannons.content.particle.LaserGlareParticle;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.arsenalists.createenergycannons.registry.CECParticles;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CECMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CECForgeClientEvents {

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(CECMod::clientInit);

        // Register Flywheel visual for the energy cannon mount
        VisualizerRegistry.setVisualizer(
            CECBlockEntity.ENERGY_CANNON_MOUNT.get(),
            new SimpleBlockEntityVisualizer<>(CannonMountVisual::new, be -> true)
        );
    }

    public static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(
                new ShaderInstance(
                    event.getResourceProvider(),
                    "createenergycannons:energy_muzzle_particle",
                    CECVertexFormats.PARTICLE_WITH_OVERLAY
                ),
                shader -> CECClientShaders.energyMuzzleParticleShader = shader
            );
            event.registerShader(
                new ShaderInstance(
                    event.getResourceProvider(),
                    "createenergycannons:laser_beam",
                    CECVertexFormats.PARTICLE_WITH_OVERLAY
                ),
                shader -> CECClientShaders.laserBeamShader = shader
            );
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to register CEC shaders", e);
        }
    }

    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CECParticles.LASER_GLARE.get(), LaserGlareParticle.Factory::new);
        event.registerSpriteSet(CECParticles.ENERGY_MUZZLE.get(), EnergyMuzzleParticle.Provider::new);
        event.registerSpecial(CECParticles.ENERGY_CANNON_PLUME.get(), new EnergyCannonPlumeParticle.Provider());
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            LaserBurnRenderer.renderLaserBurns(
                event.getPoseStack(),
                mc.renderBuffers().bufferSource(),
                cam, level
            );
            LaserBeamGlobalRenderer.renderFrame(
                event.getPoseStack(),
                mc.renderBuffers().bufferSource(),
                cam, event.getPartialTick(), level.getGameTime()
            );
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            MagneticSledWorldRenderer.renderSleds(
                event.getPoseStack(),
                mc.renderBuffers().bufferSource(),
                cam, level
            );
        }
    }
}
