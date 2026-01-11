package net.arsenalists.createenergycannons.content.energymount;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import joptsimple.internal.Strings;
import net.arsenalists.createenergycannons.mixin.CannonMountBEAccessor;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlock;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

import java.util.List;

public class EnergyCannonMountBlockEntity extends CannonMountBlockEntity {

    private final EnergyMountCap energyCap = new EnergyMountCap(500000, this::notifyUpdate);
    private LazyOptional<IEnergyStorage> lazyEnergyHandler;

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
        if (CECBlocks.ENERGY_CANNON_MOUNT.has(this.getBlockState())) {
            Direction vertical = (Direction) this.getBlockState().getValue(BlockStateProperties.VERTICAL_DIRECTION);
            BlockPos assemblyPos = this.worldPosition.relative(vertical, -2);
            if (this.getLevel().isOutsideBuildHeight(assemblyPos)) {
                throw cannonBlockOutsideOfWorld(assemblyPos);
            } else {
                AbstractMountedCannonContraption mountedCannon = accessor().getAbstractCannon(assemblyPos);
                if (mountedCannon != null && mountedCannon.assemble(this.getLevel(), assemblyPos)) {
                    Direction facing = (Direction) this.getBlockState().getValue(CannonMountBlock.HORIZONTAL_FACING);
                    Direction facing1 = mountedCannon.initialOrientation();
                    if (facing.getAxis() == facing1.getAxis() || !facing1.getAxis().isHorizontal()) {
                        accessor().setRunning(true);
                        mountedCannon.removeBlocksFromWorld(this.getLevel(), BlockPos.ZERO);
                        PitchOrientedContraptionEntity contraptionEntity = PitchOrientedContraptionEntity.create(this.getLevel(), mountedCannon, facing1, this);
                        this.mountedContraption = contraptionEntity;
                        this.resetContraptionToOffset();
                        this.getLevel().addFreshEntity(contraptionEntity);
                        this.sendData();
                        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(this.getLevel(), this.worldPosition);
                    }
                }
            }
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
        return true;
    }

    private MutableComponent barComponent(int level) {
        return Component.empty()
                .append(bars(Math.max(0, level), ChatFormatting.GREEN))
                .append(bars(Math.max(0, 50 - level), ChatFormatting.DARK_RED));

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

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (tag.contains("energy"))
            energyCap.deserializeNBT(tag.get("energy"));
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.put("energy", energyCap.serializeNBT());
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
