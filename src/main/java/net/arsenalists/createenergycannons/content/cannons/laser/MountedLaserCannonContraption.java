package net.arsenalists.createenergycannons.content.cannons.laser;

import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ContraptionType;
import net.arsenalists.createenergycannons.registry.CECCannonContraptionTypes;
import net.arsenalists.createenergycannons.registry.CECContraptionTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
import rbasamoyai.createbigcannons.cannon_control.cannon_types.ICannonContraptionType;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MountedLaserCannonContraption extends AbstractMountedCannonContraption {
    public static final AtomicInteger NEXT_BREAKER_ID = new AtomicInteger();
    protected int breakerId = -NEXT_BREAKER_ID.incrementAndGet();
    protected static Map<BlockPos, Float> breakProgress = new HashMap<>();

    @Override
    public void onRedstoneUpdate(ServerLevel serverLevel, PitchOrientedContraptionEntity pitchOrientedContraptionEntity, boolean togglePower, int firePower, ControlPitchContraption controlPitchContraption) {
        getLaser().ifPresent(laser -> {
                    laser.setFireRate(firePower);
                    BigCannonBlock.writeAndSyncSingleBlockData(laser, this.blocks.get(laser.getBlockPos()), entity, this);
                }
        );

    }

    public Optional<LaserBlockEntity> getLaser() {
        return this.presentBlockEntities.entrySet().stream().findFirst().map(entry -> entry.getValue() instanceof LaserBlockEntity laser ? laser : null);
    }
    @Override
    public void fireShot(ServerLevel serverLevel, PitchOrientedContraptionEntity pitchOrientedContraptionEntity) {
        Vec3 start = anchor.getCenter();
        float pitch = pitchOrientedContraptionEntity.pitch;
        float yaw = pitchOrientedContraptionEntity.yaw;
        int range = 256;
        int invert = pitchOrientedContraptionEntity.getInitialOrientation().getAxisDirection() == Direction.AxisDirection.POSITIVE ? -1 : 1;
        if (pitchOrientedContraptionEntity.getInitialOrientation().getAxis() == Direction.Axis.Z) {
            invert = -invert;
        }
        Vec3 vecEnd = start.add(Vec3.directionFromRotation(invert * pitch, yaw).scale(range));
        HitResult result = serverLevel.clip(new ClipContext(start, vecEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, pitchOrientedContraptionEntity));

        getLaser().ifPresent(laser -> {
            laser.setRange((int) (start.distanceTo(result.getLocation())));
            BigCannonBlock.writeAndSyncSingleBlockData(laser, this.blocks.get(laser.getBlockPos()), entity, this);
        });


        AABB laserAABB = new AABB(start, result.getLocation()).inflate(1);
        Entity closestEntity = null;
        for (Entity livingEntity : serverLevel.getEntities(pitchOrientedContraptionEntity, laserAABB, entity -> entity instanceof LivingEntity)) {
            AABB entityAABB = livingEntity.getBoundingBox();
            if (entityAABB.clip(start, result.getLocation()).isPresent()) {
                if (closestEntity != null) {
                    livingEntity.distanceTo(pitchOrientedContraptionEntity);
                    closestEntity.distanceTo(pitchOrientedContraptionEntity);
                }
                closestEntity = livingEntity;
            }
        }
        if (closestEntity != null) {
            final int newRange = (int) start.distanceTo(closestEntity.position());
            getLaser().ifPresent(laser -> {
                laser.setRange(newRange);
                BigCannonBlock.writeAndSyncSingleBlockData(laser, this.blocks.get(laser.getBlockPos()), entity, this);
            });
            closestEntity.hurt(serverLevel.damageSources().generic(), 1);
            closestEntity.setSecondsOnFire(2);
        } else {
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) result;
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockState blockState = serverLevel.getBlockState(blockPos);

                breakProgress.putIfAbsent(blockHitResult.getBlockPos(), 0F);
                breakProgress.get(blockHitResult.getBlockPos());
                breakProgress.put(blockHitResult.getBlockPos(), breakProgress.get(blockHitResult.getBlockPos()) + 1 / blockState.getDestroySpeed(serverLevel, blockHitResult.getBlockPos()));

                if (!serverLevel.isClientSide) {
                    if (breakProgress.get(blockHitResult.getBlockPos()) > 10) {
                        serverLevel.destroyBlock(blockHitResult.getBlockPos(), false);
                        breakProgress.put(blockHitResult.getBlockPos(), 0F);
                        serverLevel.destroyBlockProgress(breakerId, blockHitResult.getBlockPos(), 0);

                    } else
                        serverLevel.destroyBlockProgress(breakerId, blockHitResult.getBlockPos(), (int) (float) breakProgress.get(blockHitResult.getBlockPos()));
                }
            }
        }
    }


    @Override
    public void tick(Level level, PitchOrientedContraptionEntity entity) {
        super.tick(level, entity);
        tryFire(level, entity);
    }

    private void tryFire(Level level, PitchOrientedContraptionEntity entity) {
        if (level.isClientSide) return;
        if (getLaser().map(laser -> laser.getFireRate() > 0).orElse(false) && level instanceof ServerLevel serverLevel) {
            fireShot(serverLevel, entity);
        }
    }

    @Override
    public float getWeightForStress() {
        return 16;
    }

    @Override
    public Vec3 getInteractionVec(PitchOrientedContraptionEntity pitchOrientedContraptionEntity) {
        return Vec3.ZERO;
    }

    @Override
    public ICannonContraptionType getCannonType() {
        return CECCannonContraptionTypes.LASER;
    }

    @Override
    public boolean assemble(Level level, BlockPos pos) throws AssemblyException {
        if (!this.collectCannonBlocks(level, pos)) return false;
        this.bounds = this.createBoundsFromExtensionLengths();
        return !this.blocks.isEmpty();
    }

    private boolean collectCannonBlocks(Level level, BlockPos pos) throws AssemblyException {
        BlockState startState = level.getBlockState(pos);

        if (!(startState.getBlock() instanceof LaserBlock startCannon)) {
            return false;
        }
        List<StructureTemplate.StructureBlockInfo> cannonBlocks = new ArrayList<>();
        cannonBlocks.add(new StructureTemplate.StructureBlockInfo(pos, startState, this.getBlockEntityNBT(level, pos)));

        Direction facing = startCannon.getFacing(startState);
        if (facing == Direction.DOWN || facing == Direction.UP) {
            throw new AssemblyException(Component.literal("Invalid Cannon Orientation"));
        }
        this.initialOrientation = startCannon.getFacing(startState);
        this.startPos = pos;
        this.anchor = pos;

        for (StructureTemplate.StructureBlockInfo blockInfo : cannonBlocks) {
            BlockPos localPos = blockInfo.pos().subtract(pos);
            StructureTemplate.StructureBlockInfo localBlockInfo = new StructureTemplate.StructureBlockInfo(localPos, blockInfo.state(), blockInfo.nbt());
            this.getBlocks().put(localPos, localBlockInfo);

            if (blockInfo.nbt() == null) continue;
            BlockEntity be = BlockEntity.loadStatic(localPos, blockInfo.state(), blockInfo.nbt());
            this.presentBlockEntities.put(localPos, be);
        }
        return true;
    }

    @Override
    public ContraptionType getType() {
        return CECContraptionTypes.MOUNTED_LASER_CANNON;
    }
}
