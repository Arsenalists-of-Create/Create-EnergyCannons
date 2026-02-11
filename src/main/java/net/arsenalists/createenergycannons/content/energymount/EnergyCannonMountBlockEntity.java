package net.arsenalists.createenergycannons.content.energymount;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import joptsimple.internal.Strings;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBlock;
import net.arsenalists.createenergycannons.content.cannons.laser.MountedLaserCannonContraption;
import net.arsenalists.createenergycannons.content.cannons.magnetic.coilgun.CoilGunBlock;
import net.arsenalists.createenergycannons.content.cannons.magnetic.coilgun.CoilGunBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.MountedEnergyCannonContraption;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.RailGunBlock;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.RailGunBlockEntity;
import net.arsenalists.createenergycannons.mixin.CannonMountBEAccessor;
import net.arsenalists.createenergycannons.registry.CECBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlock;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

import java.util.List;
import java.util.Map;

public class EnergyCannonMountBlockEntity extends CannonMountBlockEntity {

    private final EnergyMountCap energyCap = new EnergyMountCap(500000, this::notifyUpdate);
    private LazyOptional<IEnergyStorage> lazyEnergyHandler;
    private static final Logger LOGGER = LogUtils.getLogger();

    private long cooldownEndTime = 0;

    public EnergyCannonMountBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        lazyEnergyHandler = LazyOptional.of(() -> energyCap);
        if (CECBlocks.ENERGY_CANNON_MOUNT.has(state)) {
            accessor().setCannonYaw(state.getValue(EnergyCannonMount.HORIZONTAL_FACING).toYRot());
        }
    }

    public CannonMountBEAccessor accessor() {
        return (CannonMountBEAccessor) this;
    }

    @Override
    public void tick() {
        if (!accessor().running() && !this.isVirtual()) {
            if (CECBlocks.ENERGY_CANNON_MOUNT.has(this.getBlockState())) {
                accessor().setCannonYaw(this.getBlockState().getValue(EnergyCannonMount.HORIZONTAL_FACING).toYRot());
                accessor().setPrevYaw(accessor().cannonYaw());
                accessor().setCannonPitch(0.0F);
                accessor().setPrevPitch(0.0F);
            }

        }
        super.tick();
    }

    protected void assemble() throws AssemblyException {
        LOGGER.warn("[EnergyMount] WorldPos: {}", this.worldPosition);
        LOGGER.warn("[EnergyMount] BlockState: {}", this.getBlockState());

        if (!CECBlocks.ENERGY_CANNON_MOUNT.has(this.getBlockState())) {
            return;
        }

        Direction vertical = this.getBlockState().getValue(BlockStateProperties.VERTICAL_DIRECTION);

        BlockPos assemblyPos = this.worldPosition.relative(vertical, -2);
        LOGGER.warn("[EnergyMount] Assembly position: {}", assemblyPos);
        LOGGER.warn("[EnergyMount] Block at assembly pos: {}", this.getLevel().getBlockState(assemblyPos));

        if (this.getLevel().isOutsideBuildHeight(assemblyPos)) {
            LOGGER.error("[EnergyMount] Assembly position outside world bounds!");
            throw cannonBlockOutsideOfWorld(assemblyPos);
        }

        // CHOOSE THE CONTRAPTION
        AbstractMountedCannonContraption mountedCannon;
        if (this.getLevel().getBlockState(assemblyPos).getBlock() instanceof LaserBlock) {
            mountedCannon = new MountedLaserCannonContraption();
            LOGGER.warn("[EnergyMount] Created MountedLaserCannonContraption");
        } else {
            mountedCannon = new MountedEnergyCannonContraption();
            LOGGER.warn("[EnergyMount] Created MountedEnergyCannonContraption");
        }

        LOGGER.warn("[EnergyMount] Calling mountedCannon.assemble()...");
        boolean assembled = mountedCannon.assemble(this.getLevel(), assemblyPos);
        LOGGER.warn("[EnergyMount] mountedCannon.assemble() returned: {}", assembled);

        if (mountedCannon != null && assembled) {
            LOGGER.warn("[EnergyMount] Contraption assembled successfully!");

            Direction facing = this.getBlockState().getValue(CannonMountBlock.HORIZONTAL_FACING);
            Direction cannonFacing = mountedCannon.initialOrientation();

            if (facing.getAxis() != cannonFacing.getAxis() && cannonFacing.getAxis().isHorizontal()) {
                LOGGER.warn("[EnergyMount] Cannon axis doesn't match mount axis, returning");
                return;
            }

            ((CannonMountBEAccessor) this).setRunning(true);

            mountedCannon.removeBlocksFromWorld(this.getLevel(), BlockPos.ZERO);

            PitchOrientedContraptionEntity contraptionEntity =
                    PitchOrientedContraptionEntity.create(this.getLevel(), mountedCannon, cannonFacing, this);

            this.mountedContraption = contraptionEntity;
            this.resetContraptionToOffset();
            this.getLevel().addFreshEntity(contraptionEntity);

            this.sendData();
            AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(this.getLevel(), this.worldPosition);
        }
    }


    @Nullable
    @Override
    public KineticBlockEntity getInterfacingBlockEntity(BlockPos from) {
        boolean upsideDown = this.getBlockState().getValue(BlockStateProperties.VERTICAL_DIRECTION) == Direction.UP;
        if (from.equals(new BlockPos(0, upsideDown ? 1 : -1, 0)))
            return this.yawInterface;
        BlockState state = this.getBlockState();
        Direction.Axis axis = ((EnergyCannonMount) state.getBlock()).getRotationAxis(state);
        BlockPos test1 = BlockPos.ZERO.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
        if (from.equals(test1))
            return this.pitchInterface;
        return from.equals(BlockPos.ZERO.subtract(test1)) ? this.pitchInterface : null;
    }


    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal("Energy ").append(barComponent(energyCap.getEnergyStored() * 50 / energyCap.getMaxEnergyStored())));

        long currentTime = this.level.getGameTime();
        if (cooldownEndTime > currentTime) {
            long remaining = cooldownEndTime - currentTime;
            int cooldownProgress = (int) ((500 - remaining) * 50 / 500);
            tooltip.add(Component.literal("Cooldown ").append(cooldownBarComponent(cooldownProgress)));
        } else {
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
        return Component.literal(Strings.repeat('|', level))
                .withStyle(format);
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        return super.getCapability(cap);
    }

    public void setCannonCooldown(long endTime) {
        this.cooldownEndTime = Math.max(this.cooldownEndTime, endTime);
        this.setChanged();
        this.sendData(); // Sync to client
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (tag.contains("energy"))
            energyCap.deserializeNBT(tag.get("energy"));
        cooldownEndTime = tag.getLong("CooldownEndTime");
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("energy", energyCap.serializeNBT());
        tag.putLong("CooldownEndTime", cooldownEndTime);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyEnergyHandler = LazyOptional.of(() -> energyCap);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyEnergyHandler.invalidate();
    }
}
