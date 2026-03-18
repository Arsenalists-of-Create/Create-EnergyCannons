package net.arsenalists.createenergycannons.mixin;

import net.arsenalists.createenergycannons.content.cannons.magnetic.sled.IMagneticSled;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Universal fix: injects the "Sled" tag into ANY block drop whose BlockEntity
 * has a magnetic sled attached. Works for all shell types, including modded ones.
 */
@Mixin(Block.class)
public abstract class BlockDropsMixin {

    @Inject(method = {"getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
                       "method_9609", "m_49874_"},
            at = @At("RETURN"), remap = false, require = 1)
    private static void addSledToDrops(BlockState state, ServerLevel level, BlockPos pos,
                                       BlockEntity blockEntity, Entity entity, ItemStack tool,
                                       CallbackInfoReturnable<List<ItemStack>> cir) {
        if (blockEntity instanceof IMagneticSled sled && sled.isSled()) {
            List<ItemStack> drops = cir.getReturnValue();
            for (ItemStack stack : drops) {
                if (!stack.isEmpty()) {
                    stack.getOrCreateTagElement("BlockEntityTag").putBoolean("Sled", true);
                }
            }
        }
    }
}
