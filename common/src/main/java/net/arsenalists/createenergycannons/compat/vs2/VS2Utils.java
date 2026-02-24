package net.arsenalists.createenergycannons.compat.vs2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;


public class VS2Utils {

    public static boolean isBlockInShipyard(Level level, BlockPos blockPos) {
        return VSGameUtilsKt.isBlockInShipyard(level, blockPos);
    }

    public static Vec3 getWorldVec(Level level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        if (loadedShip != null) {
            Vec3 center = pos.getCenter();
            Vector3d vec = loadedShip.getShipToWorld().transformPosition(new Vector3d(center.x, center.y, center.z));
            return new Vec3(vec.x(), vec.y(), vec.z());
        }
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3 getWorldVec(Level level, Vec3 vec3) {
        Vec3i vec3i = new Vec3i((int) vec3.x, (int) vec3.y, (int) vec3.z);
        LoadedShip loadedShip = VSGameUtilsKt.getShipObjectManagingPos(level, vec3i);
        if (loadedShip != null) {
            Vector3d vec = loadedShip.getShipToWorld().transformPosition(new Vector3d(vec3.x, vec3.y, vec3.z));
            return new Vec3(vec.x(), vec.y(), vec.z());
        }
        return vec3;
    }

    public static Vec3 getWorldVecDirectionTransform(Level level, BlockPos pos, Vec3 dir) {
        LoadedShip loadedShip = VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        if (loadedShip != null) {
            Vector3d vec = loadedShip.getShipToWorld().transformDirection(new Vector3d(dir.x, dir.y, dir.z));
            return new Vec3(vec.x(), vec.y(), vec.z());
        }
        return dir;
    }

    public static LoadedShip getShipManagingPos(Level level, BlockPos pos) {
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos);
    }
}
