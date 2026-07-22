package net.arsenalists.createenergycannons.content.cooling;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Holds the cannon's coolant: pipes fill the cold tank, firing circulates it to the hot tank,
 * and pipes drain hot water back out (or vent it at an open pipe). Sits beside the mount; the
 * assembled cannon reads it by position, so it costs the mount no faces.
 * <p>
 * The fluid plumbing is loader-specific: NeoForge uses its tanks inline here, while 1.20.1 (Forge
 * and Fabric) delegates to a {@link CoolantTanks} built by {@link CoolantTanksFactory}.
 */
public class CoolingUnitBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, CoolantUnit {

    //? if >=1.21 {
    /*private final CoolantTank coldTank;
    private final CoolantTank hotTank;
    private final MountCoolantHandler coolantHandler;
    *///?} else {
    private final CoolantTanks tanks;
    //?}

    private int lastCold = -1;
    private int lastHot = -1;

    public CoolingUnitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        int capacity;
        try {
            capacity = CECConfig.server().coolingUnitCapacity.get();
        } catch (Exception e) {
            capacity = 8000; // config not loaded yet during world load
        }
        //? if >=1.21 {
        /*coldTank = new CoolantTank(capacity);
        hotTank = new CoolantTank(capacity);
        coolantHandler = new MountCoolantHandler(coldTank, hotTank);
        coldTank.setOnChange(this::notifyUpdate);
        hotTank.setOnChange(this::notifyUpdate);
        *///?} else {
        tanks = CoolantTanksFactory.create(capacity, this::notifyUpdate);
        //?}
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;
        // Sync to client whenever a tank's level actually changes, regardless of how.
        if (getColdAmount() != lastCold || getHotAmount() != lastHot) {
            lastCold = getColdAmount();
            lastHot = getHotAmount();
            sendData();
        }
    }

    @Override
    public void markCoolantChanged() {
        setChanged();
    }

    //? if >=1.21 {
    /*public CoolantTank getColdTank() {
        return coldTank;
    }

    public CoolantTank getHotTank() {
        return hotTank;
    }

    public MountCoolantHandler getCoolantHandler() {
        return coolantHandler;
    }

    @Override
    protected void write(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("ColdTank", coldTank.writeToNBT(registries, new CompoundTag()));
        tag.put("HotTank", hotTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void read(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("ColdTank")) coldTank.readFromNBT(registries, tag.getCompound("ColdTank"));
        if (tag.contains("HotTank")) hotTank.readFromNBT(registries, tag.getCompound("HotTank"));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // Create's native tank tooltip; the hot tank's water shows its temperature via CreateLangMixin.
        return containedFluidTooltip(tooltip, isPlayerSneaking, coolantHandler);
    }

    @Override
    public int getColdAmount() {
        return coldTank.getFluidAmount();
    }

    @Override
    public int getHotAmount() {
        return hotTank.getFluidAmount();
    }

    @Override
    public int getHotCapacity() {
        return hotTank.getCapacity();
    }

    @Override
    public int circulate(int mb, int heatPerUse, int maxTemp, long now) {
        int draw = Math.min(mb, hotTank.getCapacity() - hotTank.getFluidAmount());
        if (draw <= 0) return 0;
        net.neoforged.neoforge.fluids.FluidStack drawn =
                coldTank.drain(draw, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        if (drawn.isEmpty()) return 0;
        int heated = Math.min(maxTemp, WaterTemp.effective(drawn, now) + heatPerUse);
        WaterTemp.heat(drawn, heated, now);
        int moved = drawn.getAmount();
        hotTank.fill(drawn, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        return moved;
    }
    *///?} else {
    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tanks.save(tag);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        tanks.load(tag);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return tanks.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    public int getColdAmount() {
        return tanks.getColdAmount();
    }

    @Override
    public int getHotAmount() {
        return tanks.getHotAmount();
    }

    @Override
    public int getHotCapacity() {
        return tanks.getHotCapacity();
    }

    @Override
    public int circulate(int mb, int heatPerUse, int maxTemp, long now) {
        return tanks.circulate(mb, heatPerUse, maxTemp, now);
    }

    public Object coolantFluidHandler() {
        return tanks.fluidHandler();
    }
    //?}
}
