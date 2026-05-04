package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMountBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;
import rbasamoyai.createbigcannons.index.CBCArmInteractionPointTypes;

import java.lang.reflect.Field;

public class CECArmInteractionPointTypes {
    public static EnergyCannonMountType ENERGY_CANNON_MOUNT;

    private CECArmInteractionPointTypes() {}

    public static class EnergyCannonMountType extends CBCArmInteractionPointTypes.CannonMountType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return CECBlocks.ENERGY_CANNON_MOUNT.has(state)
                    && level.getBlockEntity(pos) instanceof EnergyCannonMountBlockEntity;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new CannonMountPoint(this, level, pos, state);
        }
    }
}
