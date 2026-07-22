package net.arsenalists.createenergycannons.content.cooling;

//? if >=1.21 {
/*import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/^*
 * Water-only tank that notifies its owner on change. Temperature and its passive decay live on
 * the stored FluidStack (via WaterTemp); mixing/averaging of differing temperatures is handled
 * for all tanks by FluidTankMixin.
 ^/
public class CoolantTank extends FluidTank {
    public static final int AMBIENT_TEMPERATURE = WaterTemp.AMBIENT;

    private Runnable onChange = () -> {};

    public CoolantTank(int capacity) {
        super(capacity, fs -> fs.getFluid() == Fluids.WATER);
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    @Override
    protected void onContentsChanged() {
        onChange.run();
    }

    public int getTemperature() {
        return WaterTemp.effective(getFluid());
    }
}
*///?} else {
public class CoolantTank {
}
//?}
