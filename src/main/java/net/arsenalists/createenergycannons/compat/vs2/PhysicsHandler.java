package net.arsenalists.createenergycannons.compat.vs2;

import net.arsenalists.createenergycannons.compat.Mods;
//? if >=1.21
/*import net.arsenalists.createenergycannons.compat.sable.SableUtils;*/
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Physics-mod dispatcher. Translates cannon block positions/directions into
 * world space when the cannon sits inside a moving structure.
 *
 * Backends:
 *   - Valkyrien Skies (VS2)        - 1.20.1 only
 *   - Sable / Create Aeronautics   - 1.21+ only
 *
 * The first backend that claims the position wins; if none do, returns the
 * input unchanged so cannons in the regular world keep working.
 */
public class PhysicsHandler {

    public static boolean isBlockInShipyard(Level level, BlockPos blockPos) {
        if (Mods.VALKYRIENSKIES.isLoaded()) {
            //? if <1.21
            if (VS2Utils.isBlockInShipyard(level, blockPos)) return true;
        }
        //? if >=1.21
        /*if (SableUtils.isInSubLevel(level, blockPos)) return true;*/
        return false;
    }

    public static Vec3 getWorldVec(Level level, BlockPos pos) {
        if (Mods.VALKYRIENSKIES.isLoaded()) {
            //? if <1.21
            return VS2Utils.getWorldVec(level, pos);
        }
        //? if >=1.21
        /*return SableUtils.getWorldVec(level, pos);*/
        //? if <1.21
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3 getWorldVec(Level level, Vec3 vec3) {
        if (Mods.VALKYRIENSKIES.isLoaded()) {
            //? if <1.21
            return VS2Utils.getWorldVec(level, vec3);
        }
        //? if >=1.21
        /*return SableUtils.getWorldVec(level, vec3);*/
        //? if <1.21
        return vec3;
    }

    public static Vec3 getWorldVecDirectionTransform(Level level, BlockPos pos, Vec3 dir) {
        if (Mods.VALKYRIENSKIES.isLoaded()) {
            //? if <1.21
            return VS2Utils.getWorldVecDirectionTransform(level, pos, dir);
        }
        //? if >=1.21
        /*return SableUtils.getWorldVecDirectionTransform(level, pos, dir);*/
        //? if <1.21
        return dir;
    }
}
