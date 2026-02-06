package net.arsenalists.createenergycannons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBurnData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
            VertexConsumer consumer = new SheetedDecalTextureGenerator(
                    buffer.getBuffer(BURN_RENDER_TYPES[stage]),
                    pose.pose(),
                    pose.normal(),
                    1.0f
            );

            mc.getBlockRenderer().renderBreakingTexture(state, pos, level, poseStack, consumer);

            poseStack.popPose();
        }

        buffer.endBatch();
    }
}