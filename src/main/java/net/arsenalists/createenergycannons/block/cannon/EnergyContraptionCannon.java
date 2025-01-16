package net.arsenalists.createenergycannons.block.cannon;

import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ContraptionType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
import rbasamoyai.createbigcannons.cannon_control.cannon_types.ICannonContraptionType;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public class EnergyContraptionCannon extends AbstractMountedCannonContraption {
    @Override
    public void onRedstoneUpdate(ServerLevel serverLevel, PitchOrientedContraptionEntity pitchOrientedContraptionEntity, boolean b, int i, ControlPitchContraption controlPitchContraption) {

    }

    @Override
    public void fireShot(ServerLevel serverLevel, PitchOrientedContraptionEntity pitchOrientedContraptionEntity) {

    }

    @Override
    public float getWeightForStress() {
        return 0;
    }

    @Override
    public Vec3 getInteractionVec(PitchOrientedContraptionEntity pitchOrientedContraptionEntity) {
        return null;
    }

    @Override
    public ICannonContraptionType getCannonType() {
        return null;
    }

    @Override
    public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
        return false;
    }

    @Override
    public ContraptionType getType() {
        return null;
    }
}
