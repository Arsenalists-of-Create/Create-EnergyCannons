package net.arsenalists.createenergycannons.compat.vs2;

import net.arsenalists.createenergycannons.compat.Mods;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


public class PhysicsHandler {

    public static boolean isBlockInShipyard(Level level, BlockPos blockPos) {
        if (!Mods.VALKYRIENSKIES.isLoaded())
            return false;
        return VS2Utils.isBlockInShipyard(level, blockPos);
    }

    public static Vec3 getWorldVec(Level level, BlockPos pos) {
        if (!Mods.VALKYRIENSKIES.isLoaded())
            return new Vec3(pos.getX(), pos.getY(), pos.getZ());
        return VS2Utils.getWorldVec(level, pos);
    }

    public static Vec3 getWorldVec(Level level, Vec3 vec3) {
        if (!Mods.VALKYRIENSKIES.isLoaded())
            return vec3;
        return VS2Utils.getWorldVec(level, vec3);
    }

    public static Vec3 getWorldVecDirectionTransform(Level level, BlockPos pos, Vec3 dir) {
        if (!Mods.VALKYRIENSKIES.isLoaded())
            return dir;
        return VS2Utils.getWorldVecDirectionTransform(level, pos, dir);
    }
}
