package net.arsenalists.createenergycannons.content.cannons.magnetic.coilgun;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EmptyEnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import rbasamoyai.createbigcannons.CBCTags;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
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
import rbasamoyai.createbigcannons.effects.particles.plumes.BigCannonPlumeParticleData;
import rbasamoyai.createbigcannons.index.CBCBigCannonMaterials;
import rbasamoyai.createbigcannons.index.CBCEntityTypes;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.BigCannonPropellantBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.IntegratedPropellantProjectile;
import rbasamoyai.createbigcannons.munitions.config.BigCannonPropellantCompatibilities;
import rbasamoyai.createbigcannons.munitions.config.BigCannonPropellantCompatibilityHandler;
import rbasamoyai.ritchiesprojectilelib.RitchiesProjectileLib;

import java.util.*;
import java.util.function.Consumer;

public class MountedCoilCannonContraption extends MountedBigCannonContraption {


    BigCannonMaterial cannonMaterial = CBCBigCannonMaterials.STEEL;
    int coilCount;

    public int getMaxSafeCharges() {
        return 0;
    }

    @Override
    public boolean assemble(Level level, BlockPos pos) throws AssemblyException {
        if (level.getBlockState(pos).getBlock() instanceof BigCannonBlock cannon) {
            this.cannonMaterial = cannon.getCannonMaterial();
        }
        return super.assemble(level, pos);

    }

    @Override
    public void fireShot(ServerLevel level, PitchOrientedContraptionEntity entity) {
        BlockPos endPos = this.startPos.relative(this.initialOrientation.getOpposite());
        if (this.presentBlockEntities.get(endPos) instanceof QuickfiringBreechBlockEntity qfbreech && qfbreech.getOpenProgress() > 0)
            return;
        if (this.isDropMortar()) return;

        ControlPitchContraption controller = entity.getController();

        RandomSource rand = level.getRandom();
        BlockPos currentPos = this.startPos.immutable();
        int count = 0;
        int maxSafeCharges = this.getMaxSafeCharges();
        boolean canFail = !CBCConfigs.SERVER.failure.disableAllFailure.get();
        float spreadSub = this.cannonMaterial.properties().spreadReductionPerBarrel();
        boolean airGapPresent = false;

        PropellantContext propelCtx = new PropellantContext();

        List<StructureBlockInfo> projectileBlocks = new ArrayList<>();
        AbstractBigCannonProjectile projectile = null;
        BlockPos assemblyPos = null;

        float minimumSpread = this.cannonMaterial.properties().minimumSpread();

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
                // Initial ignition
                if (count == 0 && !cpropel.canBeIgnited(containedBlockInfo, this.initialOrientation))
                    return;
                // Incompatible propellant
                if (!propelCtx.addPropellant(cpropel, containedBlockInfo, this.initialOrientation) && canFail) {
                    this.fail(currentPos, level, entity, behavior.blockEntity, (int) propelCtx.chargesUsed);
                    return;
                }
                this.consumeBlock(behavior, currentPos, cpropel::consumePropellant);
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
        recoilMagnitude *= CBCConfigs.SERVER.cannons.bigCannonRecoilScale.getF();
        if (controller != null) controller.onRecoil(vec.scale(-recoilMagnitude), entity);

        this.hasFired = true;

        float soundPower = Mth.clamp(propelCtx.chargesUsed / 16f, 0, 1);
        float tone = 2 + soundPower * -8 + level.random.nextFloat() * 4f - 2f;
        float pitch = (float) Mth.clamp(Math.pow(2, tone / 12f), 0, 2);
        double shakeDistance = propelCtx.chargesUsed * CBCConfigs.SERVER.cannons.bigCannonBlastDistanceMultiplier.getF();
        float volume = 10 + soundPower * 30;
        Vec3 plumePos = spawnPos.subtract(vec);
        propelCtx.smokeScale = Math.max(1, propelCtx.smokeScale);

        BigCannonPlumeParticleData plumeParticle = new BigCannonPlumeParticleData(propelCtx.smokeScale, propelCtx.chargesUsed, 10);
        CannonBlastWaveEffectParticleData blastEffect = new CannonBlastWaveEffectParticleData(shakeDistance,
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(CBCSoundEvents.FIRE_BIG_CANNON.getMainEvent()), SoundSource.BLOCKS,
                volume, pitch, 2, propelCtx.chargesUsed);
        Packet<?> blastWavePacket = new ClientboundLevelParticlesPacket(blastEffect, true, plumePos.x, plumePos.y, plumePos.z, 0, 0, 0, 1, 0);

        double blastDistSqr = volume * volume * 256 * 1.21;
        for (ServerPlayer player : level.players()) {
            level.sendParticles(player, plumeParticle, true, plumePos.x, plumePos.y, plumePos.z, 0, vec.x, vec.y, vec.z, 1.0f);
            if (player.distanceToSqr(plumePos.x, plumePos.y, plumePos.z) < blastDistSqr)
                player.connection.send(blastWavePacket);
        }

        if (projectile != null && CBCConfigs.SERVER.munitions.projectilesCanChunkload.get()) {
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


}
