package net.arsenalists.createenergycannons.fabric.mixin;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.arsenalists.createenergycannons.content.cooling.WaterTemp;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * An open-ended pipe carrying our hot water vents steam and consumes it instead of placing a water
 * source block. Matches the NeoForge OpenEndedPipeMixin; on Fabric the fluid flows through a
 * transaction, so the steam particles are emitted only when it actually commits (not on simulation).
 */
@Mixin(value = OpenEndedPipe.class, remap = false)
public abstract class OpenEndedPipeMixin {

    @Shadow private Level world;
    @Shadow private BlockPos outputPos;

    @Inject(method = "provideFluidToSpace", at = @At("HEAD"), cancellable = true)
    private void createEnergyCannons$ventHotWater(FluidStack fluid, TransactionContext transaction, CallbackInfoReturnable<Boolean> cir) {
        if (fluid.getFluid() != Fluids.WATER) return;
        CompoundTag tag = fluid.getTag();
        if (tag == null || !tag.contains("CECTemp")) return;
        long now = WaterTemp.currentTick;
        long heatedAt = tag.contains("CECHeatedAt") ? tag.getLong("CECHeatedAt") : now;
        if (WaterTemp.effective(tag.getInt("CECTemp"), heatedAt, now) <= WaterTemp.AMBIENT) return;

        if (world instanceof ServerLevel serverLevel) {
            transaction.addOuterCloseCallback(result -> {
                if (!result.wasCommitted()) return;
                for (int i = 0; i < 5; i++) {
                    double vy = 0.05 + serverLevel.random.nextDouble() * 0.05;
                    double ox = (serverLevel.random.nextDouble() - 0.5) * 0.3;
                    double oz = (serverLevel.random.nextDouble() - 0.5) * 0.3;
                    serverLevel.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                            outputPos.getX() + 0.5 + ox, outputPos.getY() + 0.5, outputPos.getZ() + 0.5 + oz,
                            0, 0.0, vy, 0.0, 1.0);
                }
            });
        }
        cir.setReturnValue(true);
    }
}
