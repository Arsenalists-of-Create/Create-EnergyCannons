package net.arsenalists.createenergycannons.fabric.mixin;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.arsenalists.createenergycannons.content.cooling.WaterTemp;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets water of differing temperatures mix and average in any tank, matching the NeoForge FluidTankMixin.
 * <p>
 * Porting Lib's tank stores its state in the {@code variant}/{@code amount} fields (the transaction
 * snapshot's source of truth) with {@code stack} as a derived cache. Those fields live on the
 * {@code SingleVariantStorage} superclass, which @Shadow can't reach, so we access them (and the public
 * {@code updateSnapshots}) through a cast. Averaging mirrors the stock insert contract: snapshot first,
 * then rewrite variant/amount and refresh the cache - a rollback restores it via the tank's readSnapshot.
 */
@Mixin(value = FluidTank.class, remap = false)
public abstract class FluidTankMixin {

    @Shadow protected long capacity;
    @Shadow protected FluidStack stack;

    @Inject(method = "insert(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;JLnet/fabricmc/fabric/api/transfer/v1/transaction/TransactionContext;)J",
            at = @At("HEAD"), cancellable = true)
    private void createEnergyCannons$averageWaterTemperature(FluidVariant inserted, long maxAmount,
                                                             TransactionContext transaction, CallbackInfoReturnable<Long> cir) {
        @SuppressWarnings("unchecked")
        SingleVariantStorage<FluidVariant> self = (SingleVariantStorage<FluidVariant>) (Object) this;
        FluidVariant current = self.variant;
        long amount = self.amount;

        if (amount <= 0 || current.isBlank()) return;                        // empty -> stock inserts normally
        if (!current.isOf(Fluids.WATER) || !inserted.isOf(Fluids.WATER)) return;
        if (inserted.equals(current)) return;                               // same temperature -> stock stacks it
        long space = capacity - amount;
        if (space <= 0) {
            cir.setReturnValue(0L);
            return;
        }
        long accepted = Math.min(space, maxAmount);

        long now = WaterTemp.currentTick;
        int blended = (int) ((temperature(current.getNbt(), now) * amount
                + (long) temperature(inserted.getNbt(), now) * accepted) / (amount + accepted));

        self.updateSnapshots(transaction);
        FluidVariant blendedVariant = FluidVariant.of(Fluids.WATER, tagFor(blended, now));
        self.variant = blendedVariant;
        self.amount = amount + accepted;
        this.stack = new FluidStack(blendedVariant, amount + accepted);
        cir.setReturnValue(accepted);
    }

    private static CompoundTag tagFor(int temp, long now) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("CECTemp", temp);
        tag.putLong("CECHeatedAt", now);
        return tag;
    }

    private static int temperature(CompoundTag tag, long now) {
        if (tag == null || !tag.contains("CECTemp")) return WaterTemp.AMBIENT;
        long heatedAt = tag.contains("CECHeatedAt") ? tag.getLong("CECHeatedAt") : now;
        return WaterTemp.effective(tag.getInt("CECTemp"), heatedAt, now);
    }
}
