package net.arsenalists.createenergycannons.mixin;

import net.arsenalists.createenergycannons.content.cannons.magnetic.sled.IMagneticSled;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlock;

/**
 * Copies the magnetic-sled flag from a block's BE into the item produced by
 * pick-block (Ctrl+Middle-click). Mirrors how CBC's own ProjectileBlock copies
 * tracer/fuze components — except sled is stored in BlockEntityTag instead of
 * a top-level component.
 */
@Mixin(ProjectileBlock.class)
public abstract class ProjectileBlockGetCloneItemStackMixin {

    @Inject(method = "getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), require = 0, remap = false)
    private void cec$copySledToCloneStack(LevelReader level, BlockPos pos, BlockState state,
                                          CallbackInfoReturnable<ItemStack> cir) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IMagneticSled sled) || !sled.isSled()) return;
        ItemStack stack = cir.getReturnValue();
        if (stack == null || stack.isEmpty()) return;

        //? if <1.21 {
        /*stack.getOrCreateTagElement("BlockEntityTag").putBoolean("Sled", true);
        *///?} else {
        net.minecraft.nbt.CompoundTag beTag = new net.minecraft.nbt.CompoundTag();
        net.minecraft.world.item.component.CustomData existing =
                stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        if (existing != null) beTag = existing.copyTag();
        beTag.putBoolean("Sled", true);
        if (!beTag.contains("id")) {
            net.minecraft.resources.ResourceLocation beId =
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
            if (beId != null) beTag.putString("id", beId.toString());
        }
        stack.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,
                net.minecraft.world.item.component.CustomData.of(beTag));
        //?}
    }
}
