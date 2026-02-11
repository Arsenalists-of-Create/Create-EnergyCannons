package net.arsenalists.createenergycannons.content.cannons.magnetic.railgun;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.coilgun.CoilGunBlock;
import net.arsenalists.createenergycannons.content.cannons.magnetic.coilgun.CoilGunBlockEntity;
import net.arsenalists.createenergycannons.content.particle.EnergyCannonPlumeParticleData;
import net.arsenalists.createenergycannons.content.particle.EnergyMuzzleParticleData;
import net.arsenalists.createenergycannons.registry.CECCannonContraptionTypes;
import net.arsenalists.createenergycannons.registry.CECSoundEvents;
import net.arsenalists.createenergycannons.registry.CECContraptionTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EmptyEnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.slf4j.Logger;
import rbasamoyai.createbigcannons.CBCTags;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
import rbasamoyai.createbigcannons.cannon_control.cannon_types.ICannonContraptionType;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedBigCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBehavior;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock;
import rbasamoyai.createbigcannons.cannons.big_cannons.IBigCannonBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.QuickfiringBreechBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.cannon_end.BigCannonEnd;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.effects.particles.explosions.CannonBlastWaveEffectParticleData;
import rbasamoyai.createbigcannons.index.CBCEntityTypes;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedProjectileBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.BigCannonPropellantBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.IntegratedPropellantProjectile;
import rbasamoyai.createbigcannons.munitions.config.BigCannonPropellantCompatibilities;
import rbasamoyai.createbigcannons.munitions.config.BigCannonPropellantCompatibilityHandler;
import rbasamoyai.ritchiesprojectilelib.RitchiesProjectileLib;
import rbasamoyai.createbigcannons.index.CBCBigCannonMaterials;

import java.util.*;
import java.util.function.Consumer;

public class MountedEnergyCannonContraption extends MountedBigCannonContraption {


    BigCannonMaterial cannonMaterial;
    int railCount;
    private static final Logger LOGGER = LogUtils.getLogger();
    public int getMaxSafeCharges() {
        return 0;
    }

    int coilCount;
    enum Mode { NORMAL, COIL, RAIL }
    private Mode mode = Mode.NORMAL;

    private static final int OVERHEAT_DURATION = 500; // 25 seconds (500 ticks)
    private static final int CHARGE_DURATION = 20; // 1 second (20 ticks)
    private Map<BlockPos, Long> coilgunCooldownEndTimes = new HashMap<>();  // Game time when cooling finishes
    private boolean railgunCharging = false;

    @Override
    public boolean assemble(Level level, BlockPos pos) throws AssemblyException {
        LOGGER.warn("[EnergyContraption] assemble pos={} block={}", pos, level.getBlockState(pos));
        BlockState breech = level.getBlockState(pos);
        Direction facing = breech.getValue(BlockStateProperties.FACING);
        BlockPos next = pos.relative(facing);
        LOGGER.warn("[EnergyContraption] nextPos={} block={}", next, level.getBlockState(next));

        boolean ok = super.assemble(level, pos);
        LOGGER.warn("[EnergyContraption] super.assemble() returned: {}", ok);
        if (!ok) {
            LOGGER.error("[EnergyContraption] Assembly failed in parent class!");
            return false;
        }

        LOGGER.warn("[EnergyContraption] blocks size: {}", this.blocks.size());
        LOGGER.warn("[EnergyContraption] startPos: {}", this.startPos);
        LOGGER.warn("[EnergyContraption] initialOrientation: {}", this.initialOrientation);

        boolean coil = false, rail = false;
        int coilBlockCount = 0, railBlockCount = 0;

        LOGGER.warn("[EnergyContraption] Scanning {} blocks...", this.blocks.size());
        for (Map.Entry<BlockPos, StructureBlockInfo> entry : this.blocks.entrySet()) {
            StructureBlockInfo info = entry.getValue();
            Block b = info.state().getBlock();
            String blockName = b.getClass().getSimpleName();

            if (b instanceof CoilGunBlock) {
                coil = true;
                coilBlockCount++;

                if (info.state().hasProperty(CoilGunBlock.OVERHEATED) &&
                    info.state().getValue(CoilGunBlock.OVERHEATED)) {
                    if (info.nbt() != null && info.nbt().contains("CooldownEndTime")) {
                        long endTime = info.nbt().getLong("CooldownEndTime");
                        coilgunCooldownEndTimes.put(entry.getKey(), endTime);
                        LOGGER.warn("[EnergyContraption] Restored cooldown end time {} for pos {}", endTime, entry.getKey());
                    } else {
                        // No NBT data, assume full duration from now
                        long endTime = level.getGameTime() + OVERHEAT_DURATION;
                        coilgunCooldownEndTimes.put(entry.getKey(), endTime);
                        LOGGER.warn("[EnergyContraption] No NBT, set new cooldown end time {} for pos {}", endTime, entry.getKey());
                    }
                }
            }
            if (b instanceof RailGunBlock) {
                rail = true;
                railBlockCount++;
            }
        }

        // priority if both exist
        Mode oldMode = this.mode;
        this.mode = rail ? Mode.RAIL : (coil ? Mode.COIL : Mode.NORMAL);
        LOGGER.warn("[EnergyContraption] Mode decision: coilBlocks={}, railBlocks={}", coilBlockCount, railBlockCount);
        LOGGER.warn("[EnergyContraption] Mode changed from {} to {}", oldMode, this.mode);
        return true;
    }

    @Override
    public void tick(Level level, PitchOrientedContraptionEntity entity) {
        super.tick(level, entity);

        // Handle railgun charging
        if (!level.isClientSide() && railgunCharging) {
            long currentTime = level.getGameTime();
            boolean allCharged = true;

            for (BlockEntity be : this.presentBlockEntities.values()) {
                if (be instanceof RailGunBlockEntity railgunBE) {
                    if (!railgunBE.isFullyCharged(currentTime)) {
                        allCharged = false;
                        break;
                    }
                }
            }

            if (allCharged) {
                railgunCharging = false;
                for (BlockEntity be : this.presentBlockEntities.values()) {
                    if (be instanceof RailGunBlockEntity railgunBE) {
                        railgunBE.clearCharge();
                        StructureBlockInfo info = this.blocks.get(railgunBE.getBlockPos());
                        if (info != null) {
                            StructureBlockInfo unchargingInfo = new StructureBlockInfo(
                                info.pos(),
                                info.state().setValue(RailGunBlock.CHARGING, false),
                                info.nbt()
                            );
                            entity.setBlock(railgunBE.getBlockPos(), unchargingInfo);
                        }
                    }
                }
                // Actually fire the shot
                if (level instanceof ServerLevel serverLevel) {
                    actuallyFireRail(serverLevel, entity);
                }
            }
        }

        // Handle railgun cooldown
        if (!level.isClientSide()) {
            long currentTime = level.getGameTime();
            boolean anyRailgunCooled = false;

            for (BlockEntity be : this.presentBlockEntities.values()) {
                if (be instanceof RailGunBlockEntity railgunBE) {
                    long cooldownEnd = railgunBE.getCooldownEndTime();
                    if (cooldownEnd > 0 && currentTime >= cooldownEnd) {
                        railgunBE.setCooldownEndTime(0);
                        anyRailgunCooled = true;

                        // Update blockstate
                        StructureBlockInfo info = this.blocks.get(railgunBE.getBlockPos());
                        if (info != null) {
                            CompoundTag nbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
                            nbt.putLong("CooldownEndTime", 0);

                            StructureBlockInfo cooledInfo = new StructureBlockInfo(
                                info.pos(),
                                info.state().setValue(RailGunBlock.OVERHEATED, false),
                                nbt
                            );
                            entity.setBlock(railgunBE.getBlockPos(), cooledInfo);
                        }
                    }
                }
            }

            if (anyRailgunCooled) {
                Vec3 soundPos = entity.position();
                level.playSound(null, soundPos.x, soundPos.y, soundPos.z,
                    SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8f, 1.2f);
            }
        }

        // Handle coilgun cooldown
        if (!level.isClientSide()) {
            long currentTime = level.getGameTime();
            boolean anyCoilgunCooled = false;

            for (BlockEntity be : this.presentBlockEntities.values()) {
                if (be instanceof CoilGunBlockEntity coilgunBE) {
                    long cooldownEnd = coilgunBE.getCooldownEndTime();
                    if (cooldownEnd > 0 && currentTime >= cooldownEnd) {
                        coilgunBE.setCooldownEndTime(0);
                        anyCoilgunCooled = true;

                        coilgunCooldownEndTimes.remove(coilgunBE.getBlockPos());

                        // Update blockstate
                        StructureBlockInfo info = this.blocks.get(coilgunBE.getBlockPos());
                        if (info != null) {
                            CompoundTag nbt = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
                            nbt.putLong("CooldownEndTime", 0);

                            StructureBlockInfo cooledInfo = new StructureBlockInfo(
                                info.pos(),
                                info.state().setValue(CoilGunBlock.OVERHEATED, false),
                                nbt
                            );
                            entity.setBlock(coilgunBE.getBlockPos(), cooledInfo);
                        }
                    }
                }
            }

            if (anyCoilgunCooled) {
                Vec3 soundPos = entity.position();
                level.playSound(null, soundPos.x, soundPos.y, soundPos.z,
                    SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8f, 1.2f);
            }
        }
    }

    @Override
    public void fireShot(ServerLevel level, PitchOrientedContraptionEntity entity) {
        if (this.mode == Mode.NORMAL) {
            super.fireShot(level, entity);
            return;
        }

        if (this.mode == Mode.COIL) {
            fireCoil(level, entity);
            return;
        }

        fireRail(level, entity);
    }
    @Override
    public ICannonContraptionType getCannonType() {
        return CECCannonContraptionTypes.RAIL_CANNON;
    }
    @Override
    public ContraptionType getType() {
        return CECContraptionTypes.RAIL_CANNON;
    }

    public void fireRail(ServerLevel level, PitchOrientedContraptionEntity entity) {
        BlockPos endPos = this.startPos.relative(this.initialOrientation.getOpposite());
        if (this.presentBlockEntities.get(endPos) instanceof QuickfiringBreechBlockEntity qfbreech && qfbreech.getOpenProgress() > 0)
            return;
        if (this.isDropMortar()) return;

        long currentTime = level.getGameTime();

        for (BlockEntity be : this.presentBlockEntities.values()) {
            if (be instanceof RailGunBlockEntity railgunBE && railgunBE.isOverheated(currentTime)) {
                this.fail(railgunBE.getBlockPos(), level, entity, null, 10);
                return;
            }
        }

        // Check if already charging
        if (railgunCharging) {
            LOGGER.warn("[Railgun] Already charging, ignoring fire command");
            return;
        }

        // Start charging sequence
        LOGGER.warn("[Railgun] Starting charge sequence");
        railgunCharging = true;
        long chargeEndTime = currentTime + CHARGE_DURATION;

        for (BlockEntity be : this.presentBlockEntities.values()) {
            if (be instanceof RailGunBlockEntity railgunBE) {
                railgunBE.setChargeEndTime(chargeEndTime);

                // Update blockstate to show charging
                StructureBlockInfo info = this.blocks.get(railgunBE.getBlockPos());
                if (info != null) {
                    StructureBlockInfo chargingInfo = new StructureBlockInfo(
                        info.pos(),
                        info.state().setValue(RailGunBlock.CHARGING, true),
                        info.nbt()
                    );
                    entity.setBlock(railgunBE.getBlockPos(), chargingInfo);
                }
            }
        }
    }

    private void actuallyFireRail(ServerLevel level, PitchOrientedContraptionEntity entity) {
        LOGGER.warn("BOOM");
        BlockPos endPos = this.startPos.relative(this.initialOrientation.getOpposite());
        if (this.presentBlockEntities.get(endPos) instanceof QuickfiringBreechBlockEntity qfbreech && qfbreech.getOpenProgress() > 0)
            return;
        if (this.isDropMortar()) return;

        ControlPitchContraption controller = entity.getController();

        RandomSource rand = level.getRandom();
        BlockPos currentPos = this.startPos.immutable();
        int count = 0;
        int maxSafeCharges = this.getMaxSafeCharges();
        boolean canFail = !CBCConfigs.server().failure.disableAllFailure.get();
        boolean airGapPresent = false;

        PropellantContext propelCtx = new PropellantContext();

        List<StructureBlockInfo> projectileBlocks = new ArrayList<>();
        AbstractBigCannonProjectile projectile = null;
        BlockPos assemblyPos = null;
        BigCannonMaterial nethersteelMaterial = CBCBigCannonMaterials.NETHERSTEEL;
        float spreadSub = nethersteelMaterial.properties().spreadReductionPerBarrel();
        float minimumSpread = nethersteelMaterial.properties().minimumSpread();

        for (BlockEntity be : this.presentBlockEntities.values()) {
            if (be.getBlockState().getBlock() instanceof RailGunBlock) {
                railCount++;
            }
        }
        BlockEntity energyBE = level.getBlockEntity(this.anchor.below(2));
        if (energyBE == null) return;
        IEnergyStorage energy = energyBE.getCapability(ForgeCapabilities.ENERGY).orElse(EmptyEnergyStorage.INSTANCE);
        int energyUsed = energy.extractEnergy(railCount * 20000, false);
        if (energyBE instanceof SmartBlockEntity smartBE)
            smartBE.notifyUpdate();
        railCount = energyUsed / 20000;
        while (this.presentBlockEntities.get(currentPos) instanceof IBigCannonBlockEntity cbe) {
            BigCannonBehavior behavior = cbe.cannonBehavior();
            StructureBlockInfo containedBlockInfo = behavior.block();
            StructureBlockInfo cannonInfo = this.blocks.get(currentPos);
            if (cannonInfo == null) break;

            Block block = containedBlockInfo.state().getBlock();
            //todo better sled fail logic
            if (block instanceof FuzedProjectileBlock && (containedBlockInfo.nbt() == null || !containedBlockInfo.nbt().contains("Sled") || !containedBlockInfo.nbt().getBoolean("Sled"))) {
                if (canFail) {
                    LOGGER.warn("failed");
                    return;
                }
            }

            if (containedBlockInfo.state().isAir()) {
                if (count == 0)
                    return;
                if (projectile == null) {
                    if (projectileBlocks.isEmpty()) {
                        airGapPresent = true;
                        propelCtx.chargesUsed = Math.max(propelCtx.chargesUsed - 1, 0);
                    } else if (canFail) { // Incomplete projectile
                        this.fail(currentPos, level, entity, behavior.blockEntity, (int) propelCtx.chargesUsed);
                        return;
                    }
                } else {
                    ++propelCtx.barrelTravelled;
                    if (cannonInfo.state().is(CBCTags.CBCBlockTags.REDUCES_SPREAD)) {
                        propelCtx.spread = Math.max(propelCtx.spread - spreadSub, minimumSpread);
                    }
                }
            } else if (block instanceof BigCannonPropellantBlock cpropel && !(block instanceof ProjectileBlock)) {
                // Energy cannons don't use propellant - consume and skip it
                this.consumeBlock(behavior, currentPos);
                airGapPresent = false;
            } else if (block instanceof ProjectileBlock<?> projBlock && projectile == null) {
                projectileBlocks.add(containedBlockInfo);
                if (assemblyPos == null) assemblyPos = currentPos.immutable();

                List<StructureBlockInfo> copy = ImmutableList.copyOf(projectileBlocks);
                for (ListIterator<StructureBlockInfo> projIter = projectileBlocks.listIterator(); projIter.hasNext(); ) {
                    int i = projIter.nextIndex();
                    StructureBlockInfo projInfo = projIter.next();
                    if (projInfo.state().getBlock() instanceof ProjectileBlock<?> cproj1 && cproj1.isValidAddition(copy, projInfo, i, this.initialOrientation))
                        continue;
                    if (canFail)
                        this.fail(currentPos, level, entity, behavior.blockEntity, (int) propelCtx.chargesUsed);
                    return;
                }
                this.consumeBlock(behavior, currentPos);
                if (cannonInfo.state().is(CBCTags.CBCBlockTags.REDUCES_SPREAD)) {
                    propelCtx.spread = Math.max(propelCtx.spread - spreadSub, minimumSpread);
                }
                if (projBlock.isComplete(projectileBlocks, this.initialOrientation)) {
                    projectile = projBlock.getProjectile(level, projectileBlocks);
                    propelCtx.chargesUsed += projectile.addedChargePower();
                }
                airGapPresent = false;
            } else {
                if (canFail) {
                    this.fail(currentPos, level, entity, behavior.blockEntity, (int) propelCtx.chargesUsed);
                    return;
                } else {
                    this.consumeBlock(behavior, currentPos);
                }
            }
            currentPos = currentPos.relative(this.initialOrientation);
            BlockState cannonState = cannonInfo.state();
            if (cannonState.getBlock() instanceof BigCannonBlock cannon && cannon.getOpeningType(level, cannonState, currentPos) == BigCannonEnd.OPEN) {
                ++count;
            }
        }
        if (projectile == null && !projectileBlocks.isEmpty()) {
            StructureBlockInfo info = projectileBlocks.get(0);
            if (!(info.state().getBlock() instanceof ProjectileBlock<?> projBlock)) {
                if (canFail) this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                return;
            }
            int remaining = projBlock.getExpectedSize() - projectileBlocks.size();
            if (remaining < 1) {
                if (canFail) this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                return;
            }
            for (int i = 0; i < remaining; ++i) {
                StructureBlockInfo additionalInfo = this.blocks.remove(currentPos);
                if (additionalInfo == null) {
                    if (canFail) this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                    return;
                }
                projectileBlocks.add(additionalInfo);

                List<StructureBlockInfo> copy = ImmutableList.copyOf(projectileBlocks);
                for (ListIterator<StructureBlockInfo> projIter = projectileBlocks.listIterator(); projIter.hasNext(); ) {
                    int j = projIter.nextIndex();
                    StructureBlockInfo projInfo = projIter.next();
                    if (projInfo.state().getBlock() instanceof ProjectileBlock<?> cproj1 && cproj1.isValidAddition(copy, projInfo, j, this.initialOrientation))
                        continue;
                    if (canFail) this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                    return;
                }
                currentPos = currentPos.relative(this.initialOrientation);
            }
            assemblyPos = currentPos.immutable().relative(this.initialOrientation.getOpposite());
            if (projBlock.isComplete(projectileBlocks, this.initialOrientation)) {
                projectile = projBlock.getProjectile(level, projectileBlocks);
                propelCtx.chargesUsed += projectile.addedChargePower();
            } else if (canFail) {
                this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                return;
            }
        }

        Vec3 spawnPos = entity.toGlobalVector(Vec3.atCenterOf(currentPos.relative(this.initialOrientation)), 0);
        Vec3 vec = spawnPos.subtract(entity.toGlobalVector(Vec3.atCenterOf(BlockPos.ZERO), 0)).normalize();
        spawnPos = spawnPos.subtract(vec.scale(2));

        if (propelCtx.chargesUsed < minimumSpread) propelCtx.chargesUsed = minimumSpread;

        float recoilMagnitude = 0;

        if (projectile != null) {
            if (projectile instanceof IntegratedPropellantProjectile integPropel && !projectileBlocks.isEmpty()) {
                if (!propelCtx.addIntegratedPropellant(integPropel, projectileBlocks.get(0), this.initialOrientation) && canFail) {
                    this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                    return;
                }
            }
            StructureBlockInfo muzzleInfo = this.blocks.get(currentPos);
            if (canFail && muzzleInfo != null && !muzzleInfo.state().isAir()) {
                this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                return;
            }
            float power = railCount * 1.1f; //todo config
            projectile.setPos(spawnPos);
            projectile.setChargePower(power);
            projectile.shoot(vec.x, vec.y, vec.z, power, propelCtx.spread);
            projectile.xRotO = projectile.getXRot();
            projectile.yRotO = projectile.getYRot();

            projectile.addUntouchableEntity(entity, 1);
            Entity vehicle = entity.getVehicle();
            if (vehicle != null && CBCEntityTypes.CANNON_CARRIAGE.is(vehicle))
                projectile.addUntouchableEntity(vehicle, 1);

            level.addFreshEntity(projectile);
            recoilMagnitude += projectile.addedRecoil();
        }

        recoilMagnitude += propelCtx.recoil;
        recoilMagnitude *= CBCConfigs.server().cannons.bigCannonRecoilScale.getF();
        if (controller != null) controller.onRecoil(vec.scale(-recoilMagnitude), entity);

        this.hasFired = true;

        float soundPower = Mth.clamp(propelCtx.chargesUsed / 16f, 0, 1);
        float tone = 2 + soundPower * -8 + level.random.nextFloat() * 4f - 2f;
        float pitch = (float) Mth.clamp(Math.pow(2, tone / 12f), 0, 2);
        double shakeDistance = propelCtx.chargesUsed * CBCConfigs.server().cannons.bigCannonBlastDistanceMultiplier.getF();
        float volume = 10 + soundPower * 30;
        Vec3 plumePos = spawnPos.subtract(vec);
        propelCtx.smokeScale = Math.max(1, propelCtx.smokeScale);

        float smokeScale = Math.max(2, railCount * 2.0f);  // Increased from 0.5f for more visible spread
        EnergyCannonPlumeParticleData plumeParticle = new EnergyCannonPlumeParticleData(smokeScale, railCount, EnergyMuzzleParticleData.TYPE_RAIL, 10);
        CannonBlastWaveEffectParticleData blastEffect = new CannonBlastWaveEffectParticleData(shakeDistance,
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(CECSoundEvents.RAILGUN_FIRE.get()), SoundSource.BLOCKS,
                volume, pitch, 2, propelCtx.chargesUsed);
        Packet<?> blastWavePacket = new ClientboundLevelParticlesPacket(blastEffect, true, plumePos.x, plumePos.y, plumePos.z, 0, 0, 0, 1, 0);

        double blastDistSqr = volume * volume * 256 * 1.21;
        for (ServerPlayer player : level.players()) {
            level.sendParticles(player, plumeParticle, true, plumePos.x, plumePos.y, plumePos.z, 0, vec.x, vec.y, vec.z, 1.0f);
            if (player.distanceToSqr(plumePos.x, plumePos.y, plumePos.z) < blastDistSqr)
                player.connection.send(blastWavePacket);
        }

        if (projectile != null && CBCConfigs.server().munitions.projectilesCanChunkload.get()) {
            ChunkPos cpos1 = new ChunkPos(BlockPos.containing(projectile.position()));
            RitchiesProjectileLib.queueForceLoad(level, cpos1.x, cpos1.z);
        }

        // Mark all railgun blocks as overheated
        long cooldownEndTime = level.getGameTime() + OVERHEAT_DURATION;

        // Play overheat sound
        Vec3 soundPos = entity.position();
        level.playSound(null, soundPos.x, soundPos.y, soundPos.z,
            SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);

        for (BlockEntity be : this.presentBlockEntities.values()) {
            if (be instanceof RailGunBlockEntity railgunBE) {
                railgunBE.setCooldownEndTime(cooldownEndTime);

                // Update blockstate in contraption to show overheated
                StructureBlockInfo info = this.blocks.get(railgunBE.getBlockPos());
                if (info != null) {
                    StructureBlockInfo overheatedInfo = new StructureBlockInfo(
                        info.pos(),
                        info.state().setValue(RailGunBlock.OVERHEATED, true),
                        info.nbt()
                    );
                    entity.setBlock(railgunBE.getBlockPos(), overheatedInfo);
                }
            }
        }
    }
    public void fireCoil(ServerLevel level, PitchOrientedContraptionEntity entity){
            LOGGER.warn("bang2?");
            BlockPos endPos = this.startPos.relative(this.initialOrientation.getOpposite());
            if (this.presentBlockEntities.get(endPos) instanceof QuickfiringBreechBlockEntity qfbreech && qfbreech.getOpenProgress() > 0)
                return;
            if (this.isDropMortar()) return;

            // Check if any coilgun is overheated
            long currentTime = level.getGameTime();

            // Check all coilgun block entities
            for (BlockEntity be : this.presentBlockEntities.values()) {
                if (be instanceof CoilGunBlockEntity coilgunBE && coilgunBE.isOverheated(currentTime)) {
                    // Attempted to fire while overheated!
                    this.fail(coilgunBE.getBlockPos(), level, entity, null, 10);
                    return;
                }
            }
            ControlPitchContraption controller = entity.getController();
            RandomSource rand = level.getRandom();
            BlockPos currentPos = this.startPos.immutable();
            int count = 0;
            int maxSafeCharges = this.getMaxSafeCharges();
            boolean canFail = !CBCConfigs.server().failure.disableAllFailure.get();
            boolean airGapPresent = false;

            PropellantContext propelCtx = new PropellantContext();

            List<StructureBlockInfo> projectileBlocks = new ArrayList<>();
            AbstractBigCannonProjectile projectile = null;
            BlockPos assemblyPos = null;
            BigCannonMaterial coilMaterial = CBCBigCannonMaterials.STEEL; //Default
            for (BlockEntity be : this.presentBlockEntities.values()) {
                if (be.getBlockState().getBlock() instanceof CoilGunBlock coilBlock) {
                    coilMaterial = coilBlock.getCannonMaterial();
                    break;
                }
            }
            float spreadSub = coilMaterial.properties().spreadReductionPerBarrel();
            float minimumSpread = coilMaterial.properties().minimumSpread();

            for (BlockEntity be : this.presentBlockEntities.values()) {
                if (be.getBlockState().getBlock() instanceof CoilGunBlock) {
                    coilCount++;
                }
            }
            BlockEntity energyBE = level.getBlockEntity(this.anchor.below(2));
            if (energyBE == null) return;

            IEnergyStorage energy = energyBE.getCapability(ForgeCapabilities.ENERGY).orElse(EmptyEnergyStorage.INSTANCE);
            int energyUsed = energy.extractEnergy(coilCount * 10000, false);
            if (energyBE instanceof SmartBlockEntity smartBE)
                smartBE.notifyUpdate();
            coilCount = energyUsed / 10000;
            while (this.presentBlockEntities.get(currentPos) instanceof IBigCannonBlockEntity cbe) {
                BigCannonBehavior behavior = cbe.cannonBehavior();
                StructureBlockInfo containedBlockInfo = behavior.block();
                StructureBlockInfo cannonInfo = this.blocks.get(currentPos);
                if (cannonInfo == null) break;

                Block block = containedBlockInfo.state().getBlock();

                //todo better sled fail logic
                if (block instanceof FuzedProjectileBlock && (containedBlockInfo.nbt() == null || !containedBlockInfo.nbt().contains("Sled") || !containedBlockInfo.nbt().getBoolean("Sled"))) {
                    if (canFail) {
                        //this.fail(currentPos, level, entity, behavior.blockEntity, (int) propelCtx.chargesUsed);
                        return;
                    }
                }

                if (containedBlockInfo.state().isAir()) {
                    if (count == 0)
                        return;
                    if (projectile == null) {
                        if (projectileBlocks.isEmpty()) {
                            airGapPresent = true;
                            propelCtx.chargesUsed = Math.max(propelCtx.chargesUsed - 1, 0);
                        } else if (canFail) { // Incomplete projectile
                            this.fail(currentPos, level, entity, behavior.blockEntity, (int) propelCtx.chargesUsed);
                            return;
                        }
                    } else {
                        ++propelCtx.barrelTravelled;
                        if (cannonInfo.state().is(CBCTags.CBCBlockTags.REDUCES_SPREAD)) {
                            propelCtx.spread = Math.max(propelCtx.spread - spreadSub, minimumSpread);
                        }
                    }
                } else if (block instanceof BigCannonPropellantBlock cpropel && !(block instanceof ProjectileBlock)) {
                    // Energy cannons don't use propellant - consume and skip it
                    this.consumeBlock(behavior, currentPos);
                    airGapPresent = false;
                } else if (block instanceof ProjectileBlock<?> projBlock && projectile == null) {
                    projectileBlocks.add(containedBlockInfo);
                    if (assemblyPos == null) assemblyPos = currentPos.immutable();

                    List<StructureBlockInfo> copy = ImmutableList.copyOf(projectileBlocks);
                    for (ListIterator<StructureBlockInfo> projIter = projectileBlocks.listIterator(); projIter.hasNext(); ) {
                        int i = projIter.nextIndex();
                        StructureBlockInfo projInfo = projIter.next();
                        if (projInfo.state().getBlock() instanceof ProjectileBlock<?> cproj1 && cproj1.isValidAddition(copy, projInfo, i, this.initialOrientation))
                            continue;
                        if (canFail)
                            this.fail(currentPos, level, entity, behavior.blockEntity, (int) propelCtx.chargesUsed);
                        return;
                    }
                    this.consumeBlock(behavior, currentPos);
                    if (cannonInfo.state().is(CBCTags.CBCBlockTags.REDUCES_SPREAD)) {
                        propelCtx.spread = Math.max(propelCtx.spread - spreadSub, minimumSpread);
                    }
                    if (projBlock.isComplete(projectileBlocks, this.initialOrientation)) {
                        projectile = projBlock.getProjectile(level, projectileBlocks);
                        propelCtx.chargesUsed += projectile.addedChargePower();
                    }
                    airGapPresent = false;
                } else {
                    if (canFail) {
                        this.fail(currentPos, level, entity, behavior.blockEntity, (int) propelCtx.chargesUsed);
                        return;
                    } else {
                        this.consumeBlock(behavior, currentPos);
                    }
                }
                currentPos = currentPos.relative(this.initialOrientation);
                BlockState cannonState = cannonInfo.state();
                if (cannonState.getBlock() instanceof BigCannonBlock cannon && cannon.getOpeningType(level, cannonState, currentPos) == BigCannonEnd.OPEN) {
                    ++count;
                }
            }
            if (projectile == null && !projectileBlocks.isEmpty()) {
                StructureBlockInfo info = projectileBlocks.get(0);
                if (!(info.state().getBlock() instanceof ProjectileBlock<?> projBlock)) {
                    if (canFail) this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                    return;
                }
                int remaining = projBlock.getExpectedSize() - projectileBlocks.size();
                if (remaining < 1) {
                    if (canFail) this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                    return;
                }
                for (int i = 0; i < remaining; ++i) {
                    StructureBlockInfo additionalInfo = this.blocks.remove(currentPos);
                    if (additionalInfo == null) {
                        if (canFail) this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                        return;
                    }
                    projectileBlocks.add(additionalInfo);

                    List<StructureBlockInfo> copy = ImmutableList.copyOf(projectileBlocks);
                    for (ListIterator<StructureBlockInfo> projIter = projectileBlocks.listIterator(); projIter.hasNext(); ) {
                        int j = projIter.nextIndex();
                        StructureBlockInfo projInfo = projIter.next();
                        System.out.println(projInfo.nbt());
                        if (projInfo.state().getBlock() instanceof ProjectileBlock<?> cproj1 && cproj1.isValidAddition(copy, projInfo, j, this.initialOrientation))
                            continue;
                        if (canFail) this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                        return;
                    }
                    currentPos = currentPos.relative(this.initialOrientation);
                }
                assemblyPos = currentPos.immutable().relative(this.initialOrientation.getOpposite());
                if (projBlock.isComplete(projectileBlocks, this.initialOrientation)) {
                    projectile = projBlock.getProjectile(level, projectileBlocks);
                    propelCtx.chargesUsed += projectile.addedChargePower();
                } else if (canFail) {
                    this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                    return;
                }
            }

            Vec3 spawnPos = entity.toGlobalVector(Vec3.atCenterOf(currentPos.relative(this.initialOrientation)), 0);
            Vec3 vec = spawnPos.subtract(entity.toGlobalVector(Vec3.atCenterOf(BlockPos.ZERO), 0)).normalize();
            spawnPos = spawnPos.subtract(vec.scale(2));

            if (propelCtx.chargesUsed < minimumSpread) propelCtx.chargesUsed = minimumSpread;

            float recoilMagnitude = 0;

            if (projectile != null) {
                if (projectile instanceof IntegratedPropellantProjectile integPropel && !projectileBlocks.isEmpty()) {
                    if (!propelCtx.addIntegratedPropellant(integPropel, projectileBlocks.get(0), this.initialOrientation) && canFail) {
                        this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                        return;
                    }
                }
                StructureBlockInfo muzzleInfo = this.blocks.get(currentPos);
                if (canFail && muzzleInfo != null && !muzzleInfo.state().isAir()) {
                    this.fail(currentPos, level, entity, null, (int) propelCtx.chargesUsed);
                    return;
                }
                projectile.setPos(spawnPos);
                projectile.setChargePower(coilCount);
                projectile.shoot(vec.x, vec.y, vec.z, coilCount, propelCtx.spread);
                projectile.xRotO = projectile.getXRot();
                projectile.yRotO = projectile.getYRot();

                projectile.addUntouchableEntity(entity, 1);
                Entity vehicle = entity.getVehicle();
                if (vehicle != null && CBCEntityTypes.CANNON_CARRIAGE.is(vehicle))
                    projectile.addUntouchableEntity(vehicle, 1);

                level.addFreshEntity(projectile);
                recoilMagnitude += projectile.addedRecoil();
            }

            recoilMagnitude += propelCtx.recoil;
            recoilMagnitude *= CBCConfigs.server().cannons.bigCannonRecoilScale.getF();
            if (controller != null) controller.onRecoil(vec.scale(-recoilMagnitude), entity);

            this.hasFired = true;

            float soundPower = Mth.clamp(propelCtx.chargesUsed / 16f, 0, 1);
            float tone = 2 + soundPower * -8 + level.random.nextFloat() * 4f - 2f;
            float pitch = (float) Mth.clamp(Math.pow(2, tone / 12f), 0, 2);
            double shakeDistance = propelCtx.chargesUsed * CBCConfigs.server().cannons.bigCannonBlastDistanceMultiplier.getF();
            float volume = 10 + soundPower * 30;
            Vec3 plumePos = spawnPos.subtract(vec);
            propelCtx.smokeScale = Math.max(1, propelCtx.smokeScale);

            float smokeScale = Math.max(2, coilCount * 2.0f);
            EnergyCannonPlumeParticleData plumeParticle = new EnergyCannonPlumeParticleData(smokeScale, coilCount, EnergyMuzzleParticleData.TYPE_COIL, 10);
            CannonBlastWaveEffectParticleData blastEffect = new CannonBlastWaveEffectParticleData(shakeDistance,
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(CECSoundEvents.COILGUN_FIRE.get()), SoundSource.BLOCKS,
                    volume, pitch, 2, propelCtx.chargesUsed);
            Packet<?> blastWavePacket = new ClientboundLevelParticlesPacket(blastEffect, true, plumePos.x, plumePos.y, plumePos.z, 0, 0, 0, 1, 0);

            double blastDistSqr = volume * volume * 256 * 1.21;
            for (ServerPlayer player : level.players()) {
                level.sendParticles(player, plumeParticle, true, plumePos.x, plumePos.y, plumePos.z, 0, vec.x, vec.y, vec.z, 1.0f);
                if (player.distanceToSqr(plumePos.x, plumePos.y, plumePos.z) < blastDistSqr)
                    player.connection.send(blastWavePacket);
            }

            // Mark all coilgun blocks as overheated
            long cooldownEndTime = level.getGameTime() + OVERHEAT_DURATION;

            // Play overheat sound
            Vec3 soundPos = entity.position();
            level.playSound(null, soundPos.x, soundPos.y, soundPos.z,
                SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);

            for (BlockEntity be : this.presentBlockEntities.values()) {
                if (be instanceof CoilGunBlockEntity coilgunBE) {
                    coilgunBE.setCooldownEndTime(cooldownEndTime);

                    StructureBlockInfo info = this.blocks.get(coilgunBE.getBlockPos());
                    if (info != null) {
                        StructureBlockInfo overheatedInfo = new StructureBlockInfo(
                            info.pos(),
                            info.state().setValue(CoilGunBlock.OVERHEATED, true),
                            info.nbt()
                        );
                        entity.setBlock(coilgunBE.getBlockPos(), overheatedInfo);
                    }
                }
            }

            if (projectile != null && CBCConfigs.server().munitions.projectilesCanChunkload.get()) {
                ChunkPos cpos1 = new ChunkPos(BlockPos.containing(projectile.position()));
                RitchiesProjectileLib.queueForceLoad(level, cpos1.x, cpos1.z);
            }
        }


    private void consumeBlock(BigCannonBehavior behavior, BlockPos pos) {
        this.consumeBlock(behavior, pos, BigCannonBehavior::removeBlock);
    }

    private void consumeBlock(BigCannonBehavior behavior, BlockPos pos, Consumer<BigCannonBehavior> action) {
        action.accept(behavior);
        CompoundTag tag = behavior.blockEntity.saveWithFullMetadata();
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");

        StructureBlockInfo oldInfo = this.blocks.get(pos);
        if (oldInfo == null) return;
        StructureBlockInfo consumedInfo = new StructureBlockInfo(oldInfo.pos(), oldInfo.state(), tag);
        this.blocks.put(oldInfo.pos(), consumedInfo);
    }
    protected static class PropellantContext {
        public float chargesUsed = 0;
        public float recoil = 0;
        public float stress = 0;
        public float smokeScale = 0;
        public int barrelTravelled = 0;
        public float spread = 0.0f;
        public List<StructureBlockInfo> propellantBlocks = new ArrayList<>();

        public boolean addPropellant(BigCannonPropellantBlock propellant, StructureBlockInfo info, Direction initialOrientation) {
            this.propellantBlocks.add(info);
            if (!safeLoad(ImmutableList.copyOf(this.propellantBlocks), initialOrientation)) return false;
            float power = Math.max(0, propellant.getChargePower(info));
            this.chargesUsed += power;
            this.smokeScale += power;
            this.recoil = Math.max(0, propellant.getRecoil(info));
            this.stress += propellant.getStressOnCannon(info);
            this.spread += propellant.getSpread(info);
            return true;
        }

        public boolean addIntegratedPropellant(IntegratedPropellantProjectile propellant, StructureBlockInfo firstInfo, Direction initialOrientation) {
            List<StructureBlockInfo> copy = ImmutableList.<StructureBlockInfo>builder().addAll(this.propellantBlocks).add(firstInfo).build();
            if (!safeLoad(copy, initialOrientation)) return false;
            float power = Math.max(0, propellant.getChargePower());
            this.chargesUsed += power;
            this.smokeScale += power;
            this.stress += propellant.getStressOnCannon();
            this.spread += propellant.getSpread();
            return true;
        }

        public static boolean safeLoad(List<StructureBlockInfo> propellant, Direction orientation) {
            Map<Block, Integer> allowedCounts = new HashMap<>();
            Map<Block, Integer> actualCounts = new HashMap<>();
            for (ListIterator<StructureBlockInfo> iter = propellant.listIterator(); iter.hasNext(); ) {
                int index = iter.nextIndex();
                StructureBlockInfo info = iter.next();

                Block block = info.state().getBlock();
                if (!(block instanceof BigCannonPropellantBlock cpropel) || !(cpropel.isValidAddition(info, index, orientation)))
                    return false;
                if (actualCounts.containsKey(block)) {
                    actualCounts.put(block, actualCounts.get(block) + 1);
                } else {
                    actualCounts.put(block, 1);
                }
                BigCannonPropellantCompatibilities compatibilities = BigCannonPropellantCompatibilityHandler.getCompatibilities(block);
                for (Map.Entry<Block, Integer> entry : compatibilities.validPropellantCounts().entrySet()) {
                    Block block1 = entry.getKey();
                    int oldCount = allowedCounts.getOrDefault(block1, -1);
                    int newCount = entry.getValue();
                    if (newCount >= 0 && (oldCount < 0 || newCount < oldCount)) allowedCounts.put(block1, newCount);
                }
            }
            for (Map.Entry<Block, Integer> entry : actualCounts.entrySet()) {
                Block block = entry.getKey();
                if (allowedCounts.containsKey(block) && allowedCounts.get(block) < entry.getValue()) return false;
            }
            return true;
        }
    }

    @Override
    public void readNBT(Level world, CompoundTag nbt, boolean spawnData) {
        super.readNBT(world, nbt, spawnData);

        // Read cooldown end times
        coilgunCooldownEndTimes.clear();
        if (nbt.contains("CooldownEndTimes")) {
            CompoundTag timersTag = nbt.getCompound("CooldownEndTimes");
            for (String key : timersTag.getAllKeys()) {
                if (key.endsWith("_Pos")) {
                    String baseKey = key.substring(0, key.length() - 4);
                    BlockPos pos = NbtUtils.readBlockPos(timersTag.getCompound(key));
                    long endTime = timersTag.getLong(baseKey);
                    coilgunCooldownEndTimes.put(pos, endTime);
                }
            }
        }
    }

    @Override
    public CompoundTag writeNBT(boolean spawnPacket) {
        CompoundTag nbt = super.writeNBT(spawnPacket);

        // Write cooldown end times
        if (!coilgunCooldownEndTimes.isEmpty()) {
            CompoundTag timersTag = new CompoundTag();
            int idx = 0;
            for (Map.Entry<BlockPos, Long> entry : coilgunCooldownEndTimes.entrySet()) {
                String key = "Timer" + idx;
                timersTag.put(key + "_Pos", NbtUtils.writeBlockPos(entry.getKey()));
                timersTag.putLong(key, entry.getValue());
                idx++;
            }
            nbt.put("CooldownEndTimes", timersTag);
        }

        return nbt;
    }


}
