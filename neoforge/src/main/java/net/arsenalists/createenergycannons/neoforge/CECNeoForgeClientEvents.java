package net.arsenalists.createenergycannons.neoforge;

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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlockItem;

@EventBusSubscriber(modid = CECMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class CECNeoForgeClientEvents {

    /** Hooked from CECModNeoForge - registers mod-bus client listeners. */
    static void register(IEventBus modEventBus) {
        modEventBus.addListener(CECNeoForgeClientEvents::onClientSetup);
        modEventBus.addListener(CECNeoForgeClientEvents::registerShaders);
        modEventBus.addListener(CECNeoForgeClientEvents::registerParticleFactories);
        modEventBus.addListener(net.arsenalists.createenergycannons.neoforge.client.SledModelHandler::onRegisterAdditional);
        modEventBus.addListener(net.arsenalists.createenergycannons.neoforge.client.SledModelHandler::onModifyBakingResult);
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            net.arsenalists.createenergycannons.content.cooling.WaterTemp.currentTick = level.getGameTime();
        }
        net.arsenalists.createenergycannons.client.CrashReportClient.clientTick();
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(CECMod::clientInit);
        event.enqueueWork(CECNeoForgeClientEvents::registerSledItemProperty);

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
                            ResourceLocation.fromNamespaceAndPath(CECMod.MODID, "energy_muzzle_particle"),
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
                            ResourceLocation.fromNamespaceAndPath(CECMod.MODID, "laser_beam"),
                            CECVertexFormats.PARTICLE_WITH_OVERLAY
                    ),
                    shader -> CECClientShaders.laserBeamShader = shader
            );
        } catch (Exception e) {
            CECMod.getLogger().error("Failed to register laser_beam shader, beam will use fallback renderer", e);
        }
    }

    private static void registerSledItemProperty() {
        ResourceLocation sledProp = ResourceLocation.fromNamespaceAndPath(CECMod.MODID, "has_sled");
        int count = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ProjectileBlockItem) {
                ItemProperties.register(item, sledProp, (stack, level, entity, seed) -> {
                    CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                    if (data != null) {
                        return data.copyTag().getBoolean("Sled") ? 1.0f : 0.0f;
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
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            try {
                LaserBeamGlobalRenderer.renderFrame(
                        event.getPoseStack(),
                        cam, event.getPartialTick().getGameTimeDeltaPartialTick(false), level.getGameTime()
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
