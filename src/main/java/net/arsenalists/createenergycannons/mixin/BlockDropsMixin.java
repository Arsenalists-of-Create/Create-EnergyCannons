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
 * Injects the Sled tag into any block drop whose block entity has a sled attached.
 * Targets Block directly, so it covers modded shell types too.
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
                    //? if <1.21 {
                    stack.getOrCreateTagElement("BlockEntityTag").putBoolean("Sled", true);
                    //?} else {
                    /*net.minecraft.nbt.CompoundTag beTag = new net.minecraft.nbt.CompoundTag();
                    net.minecraft.world.item.component.CustomData existing = stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
                    if (existing != null) beTag = existing.copyTag();
                    beTag.putBoolean("Sled", true);
                    if (!beTag.contains("id")) {
                        net.minecraft.resources.ResourceLocation beId = net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
                        if (beId != null) beTag.putString("id", beId.toString());
                    }
                    stack.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.CustomData.of(beTag));
                    *///?}
                }
            }
        }
    }
}
