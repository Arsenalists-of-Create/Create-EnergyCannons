package net.arsenalists.createenergycannons.forge.cooling;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.arsenalists.createenergycannons.content.cooling.CoolantTanks;
import net.arsenalists.createenergycannons.content.cooling.WaterTemp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;

/**
 * Forge coolant plumbing. Two water-only tanks in millibuckets; temperature rides on each
 * {@link FluidStack}'s NBT (data components don't exist on 1.20.1). Circulation averages the hot
 * tank's temperature in place, so no fill mixin is needed.
 */
public class ForgeCoolantTanks implements CoolantTanks, IHaveGoggleInformation {

    private static final String TEMP = "CECTemp";
    private static final String HEATED_AT = "CECHeatedAt";

    private final FluidTank cold;
    private final FluidTank hot;
    private final IFluidHandler handler;

    public ForgeCoolantTanks(int capacity, Runnable onChange) {
        this.cold = new WaterTank(capacity, onChange);
        this.hot = new WaterTank(capacity, onChange);
        this.handler = new CoolantHandler();
    }

    @Override
    public int getColdAmount() {
        return cold.getFluidAmount();
    }

    @Override
    public int getHotAmount() {
        return hot.getFluidAmount();
    }

    @Override
    public int getHotCapacity() {
        return hot.getCapacity();
    }

    @Override
    public int circulate(int mb, int heatPerUse, int maxTemp, long now) {
        int draw = Math.min(mb, hot.getCapacity() - hot.getFluidAmount());
        if (draw <= 0) return 0;
        FluidStack drawn = cold.drain(draw, IFluidHandler.FluidAction.EXECUTE);
        if (drawn.isEmpty()) return 0;
        int moved = drawn.getAmount();
        int heated = Math.min(maxTemp, temperature(drawn, now) + heatPerUse);

        int hotAmt = hot.getFluidAmount();
        int blended = hotAmt == 0
                ? heated
                : (temperature(hot.getFluid(), now) * hotAmt + heated * moved) / (hotAmt + moved);
        FluidStack merged = new FluidStack(Fluids.WATER, hotAmt + moved);
        stamp(merged, blended, now);
        hot.setFluid(merged);
        return moved;
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
        return containedFluidTooltip(tooltip, sneaking, LazyOptional.of(() -> handler));
    }

    @Override
    public Object fluidHandler() {
        return handler;
    }

    private static int temperature(FluidStack stack, long now) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TEMP)) return WaterTemp.AMBIENT;
        long heatedAt = tag.contains(HEATED_AT) ? tag.getLong(HEATED_AT) : now;
        return WaterTemp.effective(tag.getInt(TEMP), heatedAt, now);
    }

    private static void stamp(FluidStack stack, int temperature, long now) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TEMP, temperature);
        tag.putLong(HEATED_AT, now);
    }

    private static final class WaterTank extends FluidTank {
        private final Runnable onChange;

        WaterTank(int capacity, Runnable onChange) {
            super(capacity, fs -> fs.getFluid() == Fluids.WATER);
            this.onChange = onChange;
        }

        @Override
        protected void onContentsChanged() {
            onChange.run();
        }
    }

    /** Create pipes fill cold (tank 0) and drain hot (tank 1). */
    private final class CoolantHandler implements IFluidHandler {
        @Override public int getTanks() { return 2; }
        @Override public FluidStack getFluidInTank(int tank) { return (tank == 0 ? cold : hot).getFluid(); }
        @Override public int getTankCapacity(int tank) { return (tank == 0 ? cold : hot).getCapacity(); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return stack.getFluid() == Fluids.WATER; }
        @Override public int fill(FluidStack resource, FluidAction action) { return cold.fill(resource, action); }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return hot.drain(resource, action); }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return hot.drain(maxDrain, action); }
    }
}
