package net.arsenalists.createenergycannons.neoforge.mixin;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import net.arsenalists.createenergycannons.content.cooling.CoolantTank;
import net.arsenalists.createenergycannons.registry.CECDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * An open-ended pipe carrying our hot water vents steam and consumes it instead of placing
 * a water source block. Ambient water is untouched, so ordinary pipes behave normally.
 */
@Mixin(value = OpenEndedPipe.class, remap = false)
public abstract class OpenEndedPipeMixin {

    @Shadow private Level world;
    @Shadow private BlockPos outputPos;

    @Inject(method = "provideFluidToSpace", at = @At("HEAD"), cancellable = true)
    private void createEnergyCannons$ventHotWater(FluidStack fluid, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (fluid.getFluid() != Fluids.WATER) return;
        int temp = fluid.getOrDefault(CECDataComponents.TEMPERATURE.get(), CoolantTank.AMBIENT_TEMPERATURE);
        if (temp <= CoolantTank.AMBIENT_TEMPERATURE) return;

        if (!simulate && world instanceof ServerLevel serverLevel) {
            // campfire signal smoke is the biggest rising white column vanilla offers
            for (int i = 0; i < 5; i++) {
                double vy = 0.05 + serverLevel.random.nextDouble() * 0.05;
                double ox = (serverLevel.random.nextDouble() - 0.5) * 0.3;
                double oz = (serverLevel.random.nextDouble() - 0.5) * 0.3;
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                        outputPos.getX() + 0.5 + ox, outputPos.getY() + 0.5, outputPos.getZ() + 0.5 + oz,
                        0, 0.0, vy, 0.0, 1.0);
            }
        }
        cir.setReturnValue(true);
    }
}
