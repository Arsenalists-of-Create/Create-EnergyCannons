package net.arsenalists.createenergycannons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arsenalists.createenergycannons.content.cannons.magnetic.sled.IMagneticSled;
import net.arsenalists.createenergycannons.registry.CECPartials;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

/**
 * Renders the magnetic sled model on non-fused shells placed in the world.
 * Fused shells are handled by FuzedBlockEntityRendererMixin.
 * This covers all BigCannonProjectileBlockEntity subtypes that lack a dedicated renderer.
 */
@Environment(EnvType.CLIENT)
public class MagneticSledWorldRenderer {

    public static void renderSleds(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
                                   Vec3 cam, Level level) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int renderDistance = mc.options.renderDistance().get();
        int playerChunkX = (int) mc.player.getX() >> 4;
        int playerChunkZ = (int) mc.player.getZ() >> 4;

        for (int cx = playerChunkX - renderDistance; cx <= playerChunkX + renderDistance; cx++) {
            for (int cz = playerChunkZ - renderDistance; cz <= playerChunkZ + renderDistance; cz++) {
                if (!level.hasChunk(cx, cz)) continue;
                LevelChunk chunk = level.getChunk(cx, cz);

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    // Fused shells are handled by FuzedBlockEntityRendererMixin — skip them here
                    if (be instanceof FuzedBlockEntity) continue;
                    if (!(be instanceof IMagneticSled sled)) continue;
                    if (!sled.isSled()) continue;
                    if (!be.getBlockState().hasProperty(BlockStateProperties.FACING)) continue;

                    var pos = be.getBlockPos();
                    int packedLight = LevelRenderer.getLightColor(level, pos);

                    poseStack.pushPose();
                    poseStack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);

                    var facing = be.getBlockState().getValue(BlockStateProperties.FACING);
                    SuperByteBuffer sledRender = CachedBuffers.partialFacing(CECPartials.MAGNETIC_SLED, be.getBlockState(), facing);
                    sledRender.light(packedLight);
                    sledRender.renderInto(poseStack, buffer.getBuffer(RenderType.cutout()));

                    poseStack.popPose();
                }
            }
        }

        buffer.endBatch(RenderType.cutout());
    }
}
