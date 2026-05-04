package net.arsenalists.createenergycannons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBurnData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class LaserBurnRenderer {

    private static final RenderType[] BURN_RENDER_TYPES = new RenderType[10];

    static {
        for (int i = 0; i < 10; i++) {
            ResourceLocation texture = net.arsenalists.createenergycannons.CECMod.resource(
                    "textures/block/laser_burn/burn_stage_" + i + ".png");
            BURN_RENDER_TYPES[i] = CECRenderTypes.burnDecal(texture);
        }
    }

    public static void renderLaserBurns(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
                                        Vec3 cam, Level level) {
        if (LaserBurnData.BURN_STAGES.isEmpty()) return;

        long currentTime = level.getGameTime();

        Iterator<Map.Entry<net.minecraft.core.BlockPos, LaserBurnData.BurnEntry>> iterator =
                LaserBurnData.BURN_STAGES.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<net.minecraft.core.BlockPos, LaserBurnData.BurnEntry> entry = iterator.next();
            net.minecraft.core.BlockPos pos = entry.getKey();
            LaserBurnData.BurnEntry burnEntry = entry.getValue();
            int stage = burnEntry.stage;

            if (stage < 0 || stage > 9) continue;

            float alpha = LaserBurnData.getFadeAlpha(pos, currentTime);
            if (alpha <= 0) {
                iterator.remove();
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                iterator.remove();
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);

            PoseStack.Pose pose = poseStack.last();
            VertexConsumer baseConsumer = buffer.getBuffer(BURN_RENDER_TYPES[stage]);

            VertexConsumer emissiveConsumer = new EmissiveAlphaVertexConsumer(baseConsumer, alpha);

            //? if <1.21 {
            /*VertexConsumer consumer = new SheetedDecalTextureGenerator(
                    emissiveConsumer,
                    pose.pose(),
                    pose.normal(),
                    1.0f
            );
            *///?} else
            VertexConsumer consumer = new SheetedDecalTextureGenerator(emissiveConsumer, pose, 1.0f);

            Minecraft.getInstance().getBlockRenderer().renderBreakingTexture(state, pos, level, poseStack, consumer);

            poseStack.popPose();
        }

        // Flush only our burn decal batches, not all batches
        // (flushing all batches can break Iris's deferred rendering pipeline)
        for (RenderType rt : BURN_RENDER_TYPES) {
            buffer.endBatch(rt);
        }
    }

    // Wraps a VertexConsumer to force full brightness and support alpha
    private static class EmissiveAlphaVertexConsumer implements VertexConsumer {
        private final VertexConsumer wrapped;
        private final float alpha;
        private static final int FULL_LIGHT = 0x00F000F0;

        public EmissiveAlphaVertexConsumer(VertexConsumer wrapped, float alpha) {
            this.wrapped = wrapped;
            this.alpha = alpha;
        }

        //? if <1.21 {
        /*@Override
        public VertexConsumer vertex(double x, double y, double z) {
            return wrapped.vertex(x, y, z);
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            return wrapped.color(r, g, b, (int)(a * alpha));
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
        *///?} else {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return wrapped.addVertex(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return wrapped.setColor(r, g, b, (int)(a * alpha));
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return wrapped.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return wrapped.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            // Force full brightness regardless of input light coords.
            return wrapped.setUv2(FULL_LIGHT & 0xFFFF, (FULL_LIGHT >> 16) & 0xFFFF);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return wrapped.setNormal(x, y, z);
        }
        //?}
    }
}
