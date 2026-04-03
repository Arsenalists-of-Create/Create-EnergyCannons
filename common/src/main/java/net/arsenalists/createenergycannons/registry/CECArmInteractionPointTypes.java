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

    public static void register() {
        try {
            Registry<?> reg = CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE;
            for (Field f : reg.getClass().getDeclaredFields()) {
                if (f.getType() == boolean.class) {
                    f.setAccessible(true);
                    if (f.getBoolean(reg)) {
                        f.setBoolean(reg, false);
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            CECMod.getLogger().warn("Failed to unfreeze ARM_INTERACTION_POINT_TYPE registry", t);
        }

        Registry.register(
                CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE,
                CECMod.resource("energy_cannon_mount"),
                new EnergyCannonMountType()
        );
    }


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
