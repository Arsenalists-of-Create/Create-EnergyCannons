package net.arsenalists.createenergycannons.compat.vs2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedShip;

import java.lang.reflect.Method;

/**
 * Wraps VS2 (Valkyrien Skies 2) calls via reflection so the common module
 * has no compile-time dependency on the VS2 mod JAR.
 * All public entry points are guarded by PhysicsHandler checks at the call site.
 */
public class VS2Utils {

    private static final Method IS_IN_SHIPYARD;
    private static final Method GET_SHIP_AT_BLOCK;
    private static final Method GET_SHIP_AT_VEC3I;

    static {
        Method isInShipyard = null, getAtBlock = null, getAtVec3i = null;
        try {
            Class<?> cls = Class.forName("org.valkyrienskies.mod.common.VSGameUtilsKt");
            isInShipyard = cls.getMethod("isBlockInShipyard", Level.class, BlockPos.class);
            getAtBlock   = cls.getMethod("getShipObjectManagingPos", Level.class, BlockPos.class);
            getAtVec3i   = cls.getMethod("getShipObjectManagingPos", Level.class, Vec3i.class);
        } catch (Exception ignored) {}
        IS_IN_SHIPYARD    = isInShipyard;
        GET_SHIP_AT_BLOCK = getAtBlock;
        GET_SHIP_AT_VEC3I = getAtVec3i;
    }

    public static boolean isBlockInShipyard(Level level, BlockPos blockPos) {
        if (IS_IN_SHIPYARD == null) return false;
        try {
            return (boolean) IS_IN_SHIPYARD.invoke(null, level, blockPos);
        } catch (Exception e) {
            return false;
        }
    }

    public static Vec3 getWorldVec(Level level, BlockPos pos) {
        LoadedShip ship = getShipManagingPos(level, pos);
        if (ship != null) {
            Vec3 center = pos.getCenter();
            Vector3d v = ship.getShipToWorld().transformPosition(new Vector3d(center.x, center.y, center.z));
            return new Vec3(v.x(), v.y(), v.z());
        }
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3 getWorldVec(Level level, Vec3 vec3) {
        Vec3i vi = new Vec3i((int) vec3.x, (int) vec3.y, (int) vec3.z);
        LoadedShip ship = getShipManagingPosVec(level, vi);
        if (ship != null) {
            Vector3d v = ship.getShipToWorld().transformPosition(new Vector3d(vec3.x, vec3.y, vec3.z));
            return new Vec3(v.x(), v.y(), v.z());
        }
        return vec3;
    }

    public static Vec3 getWorldVecDirectionTransform(Level level, BlockPos pos, Vec3 dir) {
        LoadedShip ship = getShipManagingPos(level, pos);
        if (ship != null) {
            Vector3d v = ship.getShipToWorld().transformDirection(new Vector3d(dir.x, dir.y, dir.z));
            return new Vec3(v.x(), v.y(), v.z());
        }
        return dir;
    }

    public static LoadedShip getShipManagingPos(Level level, BlockPos pos) {
        if (GET_SHIP_AT_BLOCK == null) return null;
        try {
            return (LoadedShip) GET_SHIP_AT_BLOCK.invoke(null, level, pos);
        } catch (Exception e) {
            return null;
        }
    }

    private static LoadedShip getShipManagingPosVec(Level level, Vec3i pos) {
        if (GET_SHIP_AT_VEC3I == null) return null;
        try {
            return (LoadedShip) GET_SHIP_AT_VEC3I.invoke(null, level, pos);
        } catch (Exception e) {
            return null;
        }
    }
}
