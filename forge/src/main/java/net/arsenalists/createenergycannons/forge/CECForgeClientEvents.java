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
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlockItem;
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
        event.enqueueWork(CECForgeClientEvents::registerSledItemProperty);

        // Register Flywheel visual for the energy cannon mount
        VisualizerRegistry.setVisualizer(
            CECBlockEntity.ENERGY_CANNON_MOUNT.get(),
            new SimpleBlockEntityVisualizer<>(CannonMountVisual::new, be -> true)
        );
    }

    public static void registerShaders(RegisterShadersEvent event) {
        LaserBeamGlobalRenderer.clearGradientCache();
        try {
            event.registerShader(
                new ShaderInstance(
                    event.getResourceProvider(),
                    "createenergycannons:energy_muzzle_particle",
                    CECVertexFormats.PARTICLE_WITH_OVERLAY
                ),
                shader -> CECClientShaders.energyMuzzleParticleShader = shader
            );
        } catch (Exception e) {
            CECMod.getLogger().error("Failed to register energy_muzzle_particle shader, particles will use vanilla fallback", e);
        }
        try {
            event.registerShader(
                new ShaderInstance(
                    event.getResourceProvider(),
                    "createenergycannons:laser_beam",
                    CECVertexFormats.PARTICLE_WITH_OVERLAY
                ),
                shader -> CECClientShaders.laserBeamShader = shader
            );
        } catch (Exception e) {
            CECMod.getLogger().error("Failed to register laser_beam shader, laser beams will not render", e);
        }
    }

    private static void registerSledItemProperty() {
        ResourceLocation sledProp = new ResourceLocation(CECMod.MODID, "has_sled");
        int count = 0;
        for (Item item : net.minecraftforge.registries.ForgeRegistries.ITEMS) {
            if (item instanceof rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlockItem) {
                ItemProperties.register(item, sledProp, (stack, level, entity, seed) -> {
                    CompoundTag tag = stack.getTag();
                    if (tag != null && tag.contains("BlockEntityTag")) {
                        return tag.getCompound("BlockEntityTag").getBoolean("Sled") ? 1.0f : 0.0f;
                    }
                    return 0.0f;
                });
                count++;
            }
        }
        CECMod.getLogger().info("Registered has_sled item property for {} projectile items", count);
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
            try {
                LaserBurnRenderer.renderLaserBurns(
                    event.getPoseStack(),
                    mc.renderBuffers().bufferSource(),
                    cam, level
                );
            } catch (Exception e) {
                CECMod.getLogger().error("Error rendering laser burns", e);
            }
            try {
                LaserBeamGlobalRenderer.renderFrame(
                    event.getPoseStack(),
                    mc.renderBuffers().bufferSource(),
                    cam, event.getPartialTick(), level.getGameTime()
                );
            } catch (Exception e) {
                CECMod.getLogger().error("Error rendering laser beams", e);
            }
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            try {
                MagneticSledWorldRenderer.renderSleds(
                    event.getPoseStack(),
                    mc.renderBuffers().bufferSource(),
                    cam, level
                );
            } catch (Exception e) {
                CECMod.getLogger().error("Error rendering magnetic sleds", e);
            }
        }
    }
}
