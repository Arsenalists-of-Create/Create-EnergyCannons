package net.arsenalists.createenergycannons.content.cannons.laser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class LaserBeamGlobalRenderer {

    public record BeamData(
            Vec3 origin,
            Vec3 direction,
            int range,
            boolean isMounted,
            long lastUpdateTick
    ) {}

    private static final Map<Integer, BeamData> ACTIVE_BEAMS = new ConcurrentHashMap<>();

    public static void registerWorldBeam(LaserBlockEntity be) {
        if (be.getLevel() == null) return;
        if (be.getFireRate() <= 0 || be.getRange() <= 0) {
            ACTIVE_BEAMS.remove(be.getBlockPos().hashCode());
            return;
        }
        Direction dir = be.getBlockState().getValue(LaserBlock.FACING);
        Vec3 direction = Vec3.atLowerCornerOf(dir.getNormal());

        ACTIVE_BEAMS.put(be.getBlockPos().hashCode(), new BeamData(
                Vec3.atCenterOf(be.getBlockPos()),
                direction,
                be.getRange(),
                false,
                be.getLevel().getGameTime()
        ));
    }

    public static void registerMountedBeam(int entityId, Vec3 origin, Vec3 direction,
                                           int range, long gameTick) {
        if (range <= 0 || origin == null || direction == null || origin.equals(Vec3.ZERO)) {
            ACTIVE_BEAMS.remove(entityId);
            return;
        }
        ACTIVE_BEAMS.put(entityId, new BeamData(origin, direction, range, true, gameTick));
    }

    public static void remove(int key) {
        ACTIVE_BEAMS.remove(key);
    }

    public static void clear() {
        ACTIVE_BEAMS.clear();
    }

    public static void renderFrame(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                   Vec3 cam, float partialTick, long gameTime) {
        if (ACTIVE_BEAMS.isEmpty()) return;

        var it = ACTIVE_BEAMS.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            BeamData beam = entry.getValue();

            if (gameTime - beam.lastUpdateTick > 5) {
                it.remove();
                continue;
            }

            renderBeam(poseStack, bufferSource, cam, beam, partialTick, gameTime);
        }

        bufferSource.endBatch();
    }

    private static void renderBeam(PoseStack ps, MultiBufferSource buffer,
                                   Vec3 camera, BeamData beam,
                                   float partialTick, long gameTime) {
        ps.pushPose();

        ps.translate(
                beam.origin.x - camera.x,
                beam.origin.y - camera.y,
                beam.origin.z - camera.z
        );

        Vec3 dir = beam.direction.normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
        float pitch = (float) Math.toDegrees(Math.asin(dir.y));

        ps.mulPose(Axis.YP.rotationDegrees(yaw));
        ps.mulPose(Axis.XP.rotationDegrees(90.0F - pitch));

        ps.translate(-0.5D, 0.0D, -0.5D);

        float[] colors = {1f, 1f, 1f, 1f};
        LaserRenderer.renderBeaconBeam(ps, buffer, partialTick, gameTime, 0, beam.range + 1, colors);

        ps.popPose();
    }
}
