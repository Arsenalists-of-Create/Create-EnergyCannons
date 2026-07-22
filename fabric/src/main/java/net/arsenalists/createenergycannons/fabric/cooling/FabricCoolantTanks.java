package net.arsenalists.createenergycannons.fabric.cooling;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.arsenalists.createenergycannons.content.cooling.CoolantTanks;
import net.arsenalists.createenergycannons.content.cooling.WaterTemp;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

/**
 * Fabric coolant plumbing over the Transfer API (Porting Lib). Internally the tanks count droplets
 * (81 per mB); the {@link CoolantTanks} seam speaks millibuckets, so amounts convert at the boundary.
 * Temperature rides on the stored {@link FluidVariant}'s NBT; circulation averages the hot tank in place.
 * <p>
 * Implements {@link IHaveGoggleInformation} only to borrow its {@code containedFluidTooltip} default,
 * so the goggle display matches NeoForge's native fluid tooltip.
 */
public class FabricCoolantTanks implements CoolantTanks, IHaveGoggleInformation {

    private static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000; // 81
    private static final String TEMP = "CECTemp";
    private static final String HEATED_AT = "CECHeatedAt";

    private final WaterTank cold;
    private final WaterTank hot;
    private final Storage<FluidVariant> exposed;

    public FabricCoolantTanks(int capacityMb, Runnable onChange) {
        long cap = capacityMb * DROPLETS_PER_MB;
        this.cold = new WaterTank(cap, onChange);
        this.hot = new WaterTank(cap, onChange);
        // Pipes fill the cold tank and drain the hot tank - the Transfer-API equivalent of NeoForge's
        // fill-cold / drain-hot handler.
        this.exposed = new CombinedStorage<>(List.of(
                FilteringStorage.insertOnlyOf(cold),
                FilteringStorage.extractOnlyOf(hot)));
    }

    @Override
    public int getColdAmount() {
        return (int) (cold.getFluidAmount() / DROPLETS_PER_MB);
    }

    @Override
    public int getHotAmount() {
        return (int) (hot.getFluidAmount() / DROPLETS_PER_MB);
    }

    @Override
    public int getHotCapacity() {
        return (int) (hot.getCapacity() / DROPLETS_PER_MB);
    }

    @Override
    public int circulate(int mb, int heatPerUse, int maxTemp, long now) {
        long want = Math.min((long) mb * DROPLETS_PER_MB, hot.getCapacity() - hot.getFluidAmount());
        long moved = Math.min(want, cold.getFluidAmount());
        if (moved <= 0) return 0;

        int heated = Math.min(maxTemp, temperature(cold.getFluid(), now) + heatPerUse);
        cold.setContents(cold.variant, cold.getFluidAmount() - moved, now);

        long hotAmt = hot.getFluidAmount();
        int blended = hotAmt == 0
                ? heated
                : (int) ((temperature(hot.getFluid(), now) * hotAmt + (long) heated * moved) / (hotAmt + moved));
        hot.setContents(waterVariant(blended, now), hotAmt + moved, now);
        return (int) (moved / DROPLETS_PER_MB);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.put("ColdTank", cold.writeToNBT(new CompoundTag()));
        tag.put("HotTank", hot.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("ColdTank")) cold.readFromNBT(tag.getCompound("ColdTank"));
        if (tag.contains("HotTank")) hot.readFromNBT(tag.getCompound("HotTank"));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        // Create's native fluid tooltip; hot water shows its temperature inline via CreateLangMixin - as on NeoForge.
        return containedFluidTooltip(tooltip, sneaking, exposed);
    }

    @Override
    public Object fluidHandler() {
        return exposed;
    }

    private static FluidVariant waterVariant(int temperature, long now) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TEMP, temperature);
        tag.putLong(HEATED_AT, now);
        return FluidVariant.of(Fluids.WATER, tag);
    }

    private static int temperature(FluidStack stack, long now) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TEMP)) return WaterTemp.AMBIENT;
        long heatedAt = tag.contains(HEATED_AT) ? tag.getLong(HEATED_AT) : now;
        return WaterTemp.effective(tag.getInt(TEMP), heatedAt, now);
    }

    /** Water-only Transfer-API tank with a change hook and direct (non-transactional) setters for circulation. */
    private static final class WaterTank extends FluidTank {
        private final Runnable onChange;

        WaterTank(long capacity, Runnable onChange) {
            super(capacity, fs -> fs.getFluid() == Fluids.WATER);
            this.onChange = onChange;
        }

        void setContents(FluidVariant variant, long amount, long now) {
            if (amount <= 0) {
                this.variant = FluidVariant.blank();
                this.amount = 0;
            } else {
                this.variant = variant;
                this.amount = amount;
            }
            onChange.run();
        }

        @Override
        protected void onFinalCommit() {
            onChange.run();
        }
    }
}
