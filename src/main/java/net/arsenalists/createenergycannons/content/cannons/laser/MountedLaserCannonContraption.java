package net.arsenalists.createenergycannons.content.cannons.laser;

import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ContraptionType;
import net.arsenalists.createenergycannons.registry.CECCannonContraptionTypes;
import net.arsenalists.createenergycannons.registry.CECContraptionTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
import rbasamoyai.createbigcannons.cannon_control.cannon_types.ICannonContraptionType;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MountedLaserCannonContraption extends AbstractMountedCannonContraption {
    @Override
    public void onRedstoneUpdate(ServerLevel serverLevel, PitchOrientedContraptionEntity pitchOrientedContraptionEntity, boolean togglePower, int firePower, ControlPitchContraption controlPitchContraption) {
        getLaser().ifPresent(laser -> laser.setFireRate(firePower));
    }

    public Optional<LaserBlockEntity> getLaser() {
        return this.presentBlockEntities.entrySet().stream().findFirst().map(entry -> entry.getValue() instanceof LaserBlockEntity laser ? laser : null);
    }
    @Override
    public void fireShot(ServerLevel serverLevel, PitchOrientedContraptionEntity pitchOrientedContraptionEntity) {
        System.out.println("Firing laser");
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
