package net.arsenalists.createenergycannons.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if <1.21 {
import net.minecraft.world.level.Level;
//?} else
/*import net.minecraft.world.item.Item;*/
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlockItem;

import java.util.List;

@Mixin(ProjectileBlockItem.class)
public abstract class ProjectileBlockItemMixin {

    //? if <1.21 {
    @Inject(method = {"appendHoverText", "method_7851", "m_7373_"}, at = @At("TAIL"), remap = false, require = 1)
    private void addSledTooltip(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        CompoundTag beTag = stack.getOrCreateTag().getCompound("BlockEntityTag");
        if (beTag.getBoolean("Sled")) {
            tooltip.add(Component.translatable("tooltip.createenergycannons.magnetic_sled")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
    //?} else {
    /*@Inject(method = {"appendHoverText", "method_7851", "m_7373_"}, at = @At("TAIL"), remap = false, require = 1)
    private void addSledTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        if (customData == null) return;
        CompoundTag beTag = customData.copyTag();
        if (beTag.getBoolean("Sled")) {
            tooltip.add(Component.translatable("tooltip.createenergycannons.magnetic_sled")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
    *///?}
}
