package net.arsenalists.createenergycannons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBurnData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "createenergycannons", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LaserBurnRenderer {

    private static final RenderType[] BURN_RENDER_TYPES = new RenderType[10];

    static {
        for (int i = 0; i < 10; i++) {
            ResourceLocation texture = new ResourceLocation("createenergycannons",
                    "textures/block/laser_burn/burn_stage_" + i + ".png");
            BURN_RENDER_TYPES[i] = RenderType.crumbling(texture);
        }
    }

    @SubscribeEvent
    public static void renderLaserBurns(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        if (LaserBurnData.BURN_STAGES.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().crumblingBufferSource();

        Iterator<Map.Entry<net.minecraft.core.BlockPos, Integer>> iterator =
                LaserBurnData.BURN_STAGES.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<net.minecraft.core.BlockPos, Integer> entry = iterator.next();
            net.minecraft.core.BlockPos pos = entry.getKey();
            int stage = entry.getValue();

            if (stage < 0 || stage > 9) continue;

            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                iterator.remove();
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);

            PoseStack.Pose pose = poseStack.last();
            VertexConsumer baseConsumer = buffer.getBuffer(BURN_RENDER_TYPES[stage]);

            VertexConsumer emissiveConsumer = new EmissiveVertexConsumer(baseConsumer);

            VertexConsumer consumer = new SheetedDecalTextureGenerator(
                    emissiveConsumer,
                    pose.pose(),
                    pose.normal(),
                    1.0f
            );

            mc.getBlockRenderer().renderBreakingTexture(state, pos, level, poseStack, consumer);

            poseStack.popPose();
        }

        buffer.endBatch();
    }

     // Wraps a VertexConsumer to force full brightness
    private static class EmissiveVertexConsumer implements VertexConsumer {
        private final VertexConsumer wrapped;
        private static final int FULL_LIGHT = 0x00F000F0;

        public EmissiveVertexConsumer(VertexConsumer wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            return wrapped.vertex(x, y, z);
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            return wrapped.color(r, g, b, a);
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            return wrapped.uv(u, v);
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return wrapped.overlayCoords(u, v);
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return wrapped.uv2(FULL_LIGHT);
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return wrapped.normal(x, y, z);
        }

        @Override
        public void endVertex() {
            wrapped.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            wrapped.defaultColor(r, g, b, a);
        }

        @Override
        public void unsetDefaultColor() {
            wrapped.unsetDefaultColor();
        }
    }
}