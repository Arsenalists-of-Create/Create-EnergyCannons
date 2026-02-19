package net.arsenalists.createenergycannons.content.cannons.laser;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.content.contraptions.AssemblyException;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.arsenalists.createenergycannons.compat.vs2.PhysicsHandler;
import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.config.server.CECServerConfig;
import net.arsenalists.createenergycannons.network.LaserBurnS2CPacket;
import net.arsenalists.createenergycannons.network.PacketHandler;
import net.arsenalists.createenergycannons.registry.CECCannonContraptionTypes;
import net.arsenalists.createenergycannons.registry.CECContraptionTypes;
import net.arsenalists.createenergycannons.registry.CECParticles;
import net.arsenalists.createenergycannons.registry.CECTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EmptyEnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.slf4j.Logger;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
import rbasamoyai.createbigcannons.cannon_control.cannon_types.ICannonContraptionType;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MountedLaserCannonContraption extends AbstractMountedCannonContraption {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final AtomicInteger NEXT_BREAKER_ID = new AtomicInteger();
    protected int breakerId = -NEXT_BREAKER_ID.incrementAndGet();
    protected Map<BlockPos, Float> breakProgress = new HashMap<>();
    private CECServerConfig config = CECConfig.server();
    private static int LASER_ENERGY_BLOCK = CECConfig.server().laserPowerConsumption.get();

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
        int range = CECConfig.server().laserRange.get();
        int invert = pitchOrientedContraptionEntity.getInitialOrientation().getAxisDirection() == Direction.AxisDirection.POSITIVE ? -1 : 1;
        if (pitchOrientedContraptionEntity.getInitialOrientation().getAxis() == Direction.Axis.Z) {
            invert = -invert;
        }

        boolean onShip = PhysicsHandler.isBlockInShipyard(serverLevel, pitchOrientedContraptionEntity.blockPosition());

        final Vec3 worldStart;
        Vec3 direction = Vec3.directionFromRotation(invert * pitch, yaw);

        if (onShip) {
            worldStart = PhysicsHandler.getWorldVec(serverLevel, pitchOrientedContraptionEntity.blockPosition());
            direction = PhysicsHandler.getWorldVecDirectionTransform(serverLevel, pitchOrientedContraptionEntity.blockPosition(), direction);
        } else {
            worldStart = start;
        }

        Vec3 vecEnd = worldStart.add(direction.scale(range));
        HitResult result = serverLevel.clip(new ClipContext(worldStart, vecEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, pitchOrientedContraptionEntity));

        // Muzzle glow at beam START
        spawnGlareMuzzle(serverLevel, worldStart);

        getLaser().ifPresent(laser -> {
            laser.setRange((int) (worldStart.distanceTo(result.getLocation())));
            BigCannonBlock.writeAndSyncSingleBlockData(laser, this.blocks.get(laser.getBlockPos()), entity, this);
        });

        AABB laserAABB = new AABB(worldStart, result.getLocation()).inflate(1);
        Entity closestEntity = null;
        for (Entity livingEntity : serverLevel.getEntities(pitchOrientedContraptionEntity, laserAABB, entity -> entity instanceof LivingEntity)) {
            AABB entityAABB = livingEntity.getBoundingBox();
            if (entityAABB.clip(worldStart, result.getLocation()).isPresent()) {
                if (closestEntity != null) {
                    livingEntity.distanceTo(pitchOrientedContraptionEntity);
                    closestEntity.distanceTo(pitchOrientedContraptionEntity);
                }
                closestEntity = livingEntity;
            }
        }

        Vec3 endPoint;

        if (closestEntity != null) {
            final int newRange = (int) worldStart.distanceTo(closestEntity.position());
            endPoint = closestEntity.position();

            getLaser().ifPresent(laser -> {
                laser.setRange(newRange);
                BigCannonBlock.writeAndSyncSingleBlockData(laser, this.blocks.get(laser.getBlockPos()), entity, this);
            });
            closestEntity.hurt(serverLevel.damageSources().generic(), config.laserDamage.get());
            closestEntity.setSecondsOnFire(config.laserBurnTime.get());
            serverLevel.playSound(null, closestEntity.getX(), closestEntity.getY(), closestEntity.getZ(),
                    SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.4f,
                    1.8f + serverLevel.random.nextFloat() * 0.4f);
        } else {
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) result;
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockState blockState = serverLevel.getBlockState(blockPos);
                FluidState fluidState = serverLevel.getFluidState(blockPos);

                endPoint = result.getLocation();

                if (!fluidState.isEmpty()) {
                    serverLevel.playSound(null, endPoint.x, endPoint.y, endPoint.z,
                            SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.4f,
                            1.5f + serverLevel.random.nextFloat() * 0.5f);

                    for (int i = 0; i < 3; i++) {
                        double xVel = (serverLevel.random.nextDouble() - 0.5) * 0.1;
                        double yVel = 0.08 + serverLevel.random.nextDouble() * 0.05;
                        double zVel = (serverLevel.random.nextDouble() - 0.5) * 0.1;
                        serverLevel.sendParticles(ParticleTypes.CLOUD,
                                endPoint.x, endPoint.y, endPoint.z, 0, xVel, yVel, zVel, 1.0);
                    }

                } else if (blockState.is(CECTags.Blocks.LASERPROOF)) {
                    breakProgress.remove(blockPos);
                    PacketHandler.sendToAllTracking(new LaserBurnS2CPacket(blockPos, -1),
                            serverLevel, blockPos);

                } else {
                    breakProgress.putIfAbsent(blockPos, 0F);
                    float currentProgress = breakProgress.get(blockPos);

                    float hardness = blockState.getDestroySpeed(serverLevel, blockPos);
                    if (hardness <= 0) {
                        breakProgress.remove(blockPos);
                        PacketHandler.sendToAllTracking(new LaserBurnS2CPacket(blockPos, -1),
                                serverLevel, blockPos);
                    } else {
                        float progressIncrement = 1.0f / hardness;
                        breakProgress.put(blockPos, currentProgress + progressIncrement);

                        float progress = breakProgress.get(blockPos);
                        int stage = Math.min((int) progress, config.laserBlockBreakThreshold.get() - 1);

                        if (progress >= config.laserBlockBreakThreshold.get()) {
                            serverLevel.destroyBlock(blockPos, false);
                            breakProgress.remove(blockPos);
                            PacketHandler.sendToAllTracking(new LaserBurnS2CPacket(blockPos, -1),
                                    serverLevel, blockPos);
                        } else {
                            PacketHandler.sendToAllTracking(new LaserBurnS2CPacket(blockPos, stage),
                                    serverLevel, blockPos);
                        }
                    }
                }
            } else {
                endPoint = result.getLocation();
            }
        }
        spawnGlareImpact(serverLevel, endPoint);
    }


    private void spawnGlareMuzzle(ServerLevel level, Vec3 pos) {
        level.sendParticles(CECParticles.LASER_GLARE.get(),
                pos.x, pos.y, pos.z, 0, 0, 0, 0, 1.0);
    }

    private void spawnGlareImpact(ServerLevel level, Vec3 pos) {
        level.sendParticles(CECParticles.LASER_GLARE.get(),
                pos.x, pos.y, pos.z, 0, 1, 0, 0, 1.0);

        for (int i = 0; i < 2; i++) {
            double xVel = (level.random.nextDouble() - 0.5) * 0.02;
            double yVel = 0.05 + level.random.nextDouble() * 0.02;
            double zVel = (level.random.nextDouble() - 0.5) * 0.02;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    pos.x, pos.y, pos.z, 0, xVel, yVel, zVel, 1.0);
        }
    }

    @Override
    public void tick(Level level, PitchOrientedContraptionEntity entity) {
        super.tick(level, entity);

        if (level.isClientSide) {
            getLaser().ifPresent(laser -> {
                if (laser.getFireRate() > 0 && laser.getRange() > 0) {
                    Vec3 origin = anchor.getCenter();
                    Direction facing = entity.getInitialOrientation();

                    int invert = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? -1 : 1;
                    if (facing.getAxis() == Direction.Axis.Z) {
                        invert = -invert;
                    }

                    Vec3 direction = Vec3.directionFromRotation(invert * entity.pitch, entity.yaw);

                    if (PhysicsHandler.isBlockInShipyard(level, entity.blockPosition())) {
                        origin = PhysicsHandler.getWorldVec(level, entity.blockPosition());
                        direction = PhysicsHandler.getWorldVecDirectionTransform(level, entity.blockPosition(), direction);
                    }

                    LaserBeamGlobalRenderer.registerMountedBeam(
                            entity.getId(),
                            origin,
                            direction,
                            laser.getRange(),
                            level.getGameTime()
                    );
                } else {
                    LaserBeamGlobalRenderer.remove(entity.getId());
                }
            });
        } else {
            tryFire(level, entity);
        }
    }

    private void tryFire(Level level, PitchOrientedContraptionEntity entity) {
        Optional<LaserBlockEntity> laserOpt = getLaser();
        if (laserOpt.isEmpty()) return;

        LaserBlockEntity laser = laserOpt.get();
        if (laser.getFireRate() <= 0) return;

        if (!(level instanceof ServerLevel serverLevel)) return;

        BlockEntity energyBE = level.getBlockEntity(this.anchor.below(2));
        if (energyBE == null) return;

        IEnergyStorage energy = energyBE.getCapability(ForgeCapabilities.ENERGY).orElse(EmptyEnergyStorage.INSTANCE);

        int energyAvailable = energy.extractEnergy(LASER_ENERGY_BLOCK, true);
        if (energyAvailable < LASER_ENERGY_BLOCK) {
            laser.setFireRate(0);
            return;
        }

        int energyUsed = energy.extractEnergy(LASER_ENERGY_BLOCK, false);
        if (energyUsed < LASER_ENERGY_BLOCK) {
            return;
        }

        if (energyBE instanceof SmartBlockEntity smartBE) smartBE.notifyUpdate();

        fireShot(serverLevel, entity);
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