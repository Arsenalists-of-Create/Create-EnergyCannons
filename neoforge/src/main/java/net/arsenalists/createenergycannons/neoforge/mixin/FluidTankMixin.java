package net.arsenalists.createenergycannons.neoforge.mixin;

import net.arsenalists.createenergycannons.content.cooling.WaterTemp;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets water of differing temperatures mix and average in any tank, rather than a stock tank
 * rejecting the fill because the temperature component doesn't match exactly. Only same-fluid
 * water is affected - non-water and identical-temperature water fall through to stock behaviour.
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
        if (FluidStack.isSameFluidSameComponents(fluid, resource)) return; // matching water -> stock handles it
        if (!isFluidValid(resource)) return;

        int space = capacity - fluid.getAmount();
        if (space <= 0) {
            cir.setReturnValue(0);
            return;
        }
        int accepted = Math.min(space, resource.getAmount());
        if (action.execute()) {
            long now = WaterTemp.currentTick;
            int existingTemp = WaterTemp.effective(fluid, now);
            int incomingTemp = WaterTemp.effective(resource, now);
            int existingAmount = fluid.getAmount();
            int blended = (existingTemp * existingAmount + incomingTemp * accepted) / (existingAmount + accepted);
            fluid.setAmount(existingAmount + accepted);
            WaterTemp.heat(fluid, blended, now);
            onContentsChanged();
        }
        cir.setReturnValue(accepted);
    }
}
