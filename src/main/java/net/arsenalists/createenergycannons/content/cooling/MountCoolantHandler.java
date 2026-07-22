package net.arsenalists.createenergycannons.content.cooling;

//? if >=1.21 {
/*import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/^*
 * The mount's coolant interface for Create pipes: filling adds cold water, draining pulls
 * hot water back out. Tank 0 is cold (input), tank 1 is hot (output).
 ^/
public class MountCoolantHandler implements IFluidHandler {
    private final CoolantTank cold;
    private final CoolantTank hot;

    public MountCoolantHandler(CoolantTank cold, CoolantTank hot) {
        this.cold = cold;
        this.hot = hot;
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return (tank == 0 ? cold : hot).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return (tank == 0 ? cold : hot).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return (tank == 0 ? cold : hot).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return cold.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return hot.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return hot.drain(maxDrain, action);
    }
}
*///?} else {
public class MountCoolantHandler {
}
//?}
