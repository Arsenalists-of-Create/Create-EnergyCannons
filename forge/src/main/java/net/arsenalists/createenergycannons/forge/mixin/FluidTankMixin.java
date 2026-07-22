package net.arsenalists.createenergycannons.forge.mixin;

import net.arsenalists.createenergycannons.content.cooling.WaterTemp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets water of differing temperatures mix and average in any tank, rather than a stock tank rejecting
 * the fill because the temperatures don't match. Matches the NeoForge FluidTankMixin; temperature is
 * read from NBT on 1.20.1. Only same-fluid water is affected.
 */
@Mixin(value = FluidTank.class, remap = false)
public abstract class FluidTankMixin {

    @Shadow protected FluidStack fluid;
    @Shadow protected int capacity;

    @Shadow protected abstract void onContentsChanged();

    @Shadow public abstract boolean isFluidValid(FluidStack stack);

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void createEnergyCannons$averageWaterTemperature(FluidStack resource, IFluidHandler.FluidAction action,
                                                             CallbackInfoReturnable<Integer> cir) {
        if (resource.isEmpty() || fluid.isEmpty()) return;
        if (fluid.getFluid() != Fluids.WATER || resource.getFluid() != Fluids.WATER) return;
        if (fluid.isFluidEqual(resource)) return; // same water same temperature -> stock handles it
        if (!isFluidValid(resource)) return;

        int space = capacity - fluid.getAmount();
        if (space <= 0) {
            cir.setReturnValue(0);
            return;
        }
        int accepted = Math.min(space, resource.getAmount());
        if (action.execute()) {
            long now = WaterTemp.currentTick;
            int existingAmount = fluid.getAmount();
            int blended = (temperature(fluid, now) * existingAmount + temperature(resource, now) * accepted)
                    / (existingAmount + accepted);
            fluid.setAmount(existingAmount + accepted);
            CompoundTag tag = fluid.getOrCreateTag();
            tag.putInt("CECTemp", blended);
            tag.putLong("CECHeatedAt", now);
            onContentsChanged();
        }
        cir.setReturnValue(accepted);
    }

    private static int temperature(FluidStack stack, long now) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("CECTemp")) return WaterTemp.AMBIENT;
        long heatedAt = tag.contains("CECHeatedAt") ? tag.getLong("CECHeatedAt") : now;
        return WaterTemp.effective(tag.getInt("CECTemp"), heatedAt, now);
    }
}
