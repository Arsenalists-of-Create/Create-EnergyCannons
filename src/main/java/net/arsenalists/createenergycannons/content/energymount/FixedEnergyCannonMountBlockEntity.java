package net.arsenalists.createenergycannons.content.energymount;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AssemblyException;
import joptsimple.internal.Strings;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBlock;
import net.arsenalists.createenergycannons.content.cannons.laser.MountedLaserCannonContraption;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.MountedEnergyCannonContraption;
import net.arsenalists.createenergycannons.content.energy.IModEnergyStorage;
import net.arsenalists.createenergycannons.registry.CECBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannon_control.fixed_cannon_mount.FixedCannonMountBlockEntity;

import java.util.List;

public class FixedEnergyCannonMountBlockEntity extends FixedCannonMountBlockEntity implements IEnergyCannonMount {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final EnergyMountCap energyCap;
    private long cooldownEndTime = 0;

    public FixedEnergyCannonMountBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        int capacity;
        try {
            capacity = CECConfig.server().mountEnergyCapacity.get();
        } catch (Exception e) {
            capacity = 500000;
            LOGGER.warn("Config not yet loaded, using default energy capacity");
        }
        this.energyCap = new EnergyMountCap(capacity, this::notifyUpdate);
    }

    @Override
    public IModEnergyStorage getEnergyStorage() {
        return energyCap;
    }

    @Override
    public void setCannonCooldown(long endTime) {
        this.cooldownEndTime = Math.max(this.cooldownEndTime, endTime);
        setChanged();
        sendData();
    }

    @Override
    public void accelerateCooldown(long currentTime, int ticks) {
        if (cooldownEndTime <= currentTime) return;
        cooldownEndTime = Math.max(currentTime, cooldownEndTime - ticks);
        setChanged();
    }

    @Override
    protected void assemble() throws AssemblyException {
        if (!CECBlocks.FIXED_ENERGY_CANNON_MOUNT.has(getBlockState())) return;

        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        BlockPos assemblyPos = worldPosition.relative(facing);
        if (getLevel().isOutsideBuildHeight(assemblyPos)) {
            throw CannonMountBlockEntity.cannonBlockOutsideOfWorld(assemblyPos);
        }

        BlockState blockAtAssembly = getLevel().getBlockState(assemblyPos);
        if (blockAtAssembly.isAir()) return;

        AbstractMountedCannonContraption mountedCannon = blockAtAssembly.getBlock() instanceof LaserBlock
                ? new MountedLaserCannonContraption()
                : new MountedEnergyCannonContraption();

        if (!mountedCannon.assemble(getLevel(), assemblyPos)) return;

        ((net.arsenalists.createenergycannons.mixin.FixedCannonMountBEAccessor) this).setRunning(true);
        Direction cannonFacing = mountedCannon.initialOrientation();
        mountedCannon.removeBlocksFromWorld(getLevel(), BlockPos.ZERO);

        PitchOrientedContraptionEntity contraptionEntity =
                PitchOrientedContraptionEntity.create(getLevel(), mountedCannon, cannonFacing, this);
        this.mountedContraption = contraptionEntity;
        resetContraptionToOffset();
        getLevel().addFreshEntity(contraptionEntity);
        sendData();

        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(getLevel(), worldPosition);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal("Energy ")
                .append(barComponent(energyCap.getEnergyStored() * 50 / energyCap.getMaxEnergyStored())));

        long currentTime = this.level.getGameTime();
        if (cooldownEndTime > currentTime) {
            long remaining = cooldownEndTime - currentTime;
            int cooldownTime = getCooldownTime();
            int cooldownProgress = (int) ((cooldownTime - remaining) * 50 / cooldownTime);
            tooltip.add(Component.literal("Cooldown ").append(cooldownBarComponent(cooldownProgress)));
        }
        return true;
    }

    private MutableComponent barComponent(int level) {
        return Component.empty()
                .append(bars(Math.max(0, level), ChatFormatting.GREEN))
                .append(bars(Math.max(0, 50 - level), ChatFormatting.DARK_RED));
    }

    private MutableComponent cooldownBarComponent(int level) {
        return Component.empty()
                .append(bars(Math.max(0, level), ChatFormatting.YELLOW))
                .append(bars(Math.max(0, 50 - level), ChatFormatting.RED));
    }

    private MutableComponent bars(int level, ChatFormatting format) {
        return Component.literal(Strings.repeat('|', level)).withStyle(format);
    }

    private int getCooldownTime() {
        try {
            return CECConfig.server().mountCoolDownTime.get();
        } catch (Exception e) {
            return 500;
        }
    }

    //? if <1.21 {
    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (tag.contains("energy")) energyCap.setEnergy(tag.getInt("energy"));
        cooldownEndTime = tag.getLong("CooldownEndTime");
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("energy", energyCap.getEnergyStored());
        tag.putLong("CooldownEndTime", cooldownEndTime);
    }
    //?} else {
    /*@Override
    protected void read(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("energy")) energyCap.setEnergy(tag.getInt("energy"));
        cooldownEndTime = tag.getLong("CooldownEndTime");
    }

    @Override
    protected void write(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("energy", energyCap.getEnergyStored());
        tag.putLong("CooldownEndTime", cooldownEndTime);
    }
    *///?}
}
