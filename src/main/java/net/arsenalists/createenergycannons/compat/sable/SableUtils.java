package net.arsenalists.createenergycannons.compat.sable;

//? if >=1.21 {
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Sable / Create Aeronautics sub-level coordinate transforms.
 */
public class SableUtils {

    /** True if {@code blockPos} is inside a Sable sub-level. */
    public static boolean isInSubLevel(Level level, BlockPos blockPos) {
        //? if >=1.21
        return SableCompanion.INSTANCE.getContaining(level, blockPos) != null;
        //? if <1.21
        /*return false;*/
    }

    /** Transform a sub-level-local block center into global world coordinates. */
    public static Vec3 getWorldVec(Level level, BlockPos pos) {
        //? if >=1.21 {
        SubLevelAccess sub = SableCompanion.INSTANCE.getContaining(level, pos);
        if (sub == null) return Vec3.atCenterOf(pos);
        Pose3dc pose = sub.logicalPose();
        return pose.transformPosition(Vec3.atCenterOf(pos));
        //?} else
        /*return Vec3.atCenterOf(pos);*/
    }

    /** Transform a sub-level-local point into global world coordinates. */
    public static Vec3 getWorldVec(Level level, Vec3 vec3) {
        //? if >=1.21 {
        SubLevelAccess sub = SableCompanion.INSTANCE.getContaining(level, BlockPos.containing(vec3));
        if (sub == null) return vec3;
        return sub.logicalPose().transformPosition(vec3);
        //?} else
        /*return vec3;*/
    }

    /**
     * Transform a sub-level-local direction (unit vector) into global world coordinates.
     * Uses Pose3dc.transformNormal — applies orientation+scale, no translation.
     */
    public static Vec3 getWorldVecDirectionTransform(Level level, BlockPos pos, Vec3 dir) {
        //? if >=1.21 {
        SubLevelAccess sub = SableCompanion.INSTANCE.getContaining(level, pos);
        if (sub == null) return dir;
        return sub.logicalPose().transformNormal(dir);
        //?} else
        /*return dir;*/
    }

    //? if >=1.21 {
    /**
     * Returns the Sable sub-level containing {@code pos}, or null if {@code pos} is in
     * regular (non-sub-level) world space.
     */
    public static SubLevelAccess getSubLevel(Level level, BlockPos pos) {
        return SableCompanion.INSTANCE.getContaining(level, pos);
    }


    public static org.joml.Matrix4f bakePose(SubLevelAccess sub) {
        org.joml.Matrix4d md = new org.joml.Matrix4d();
        sub.logicalPose().bakeIntoMatrix(md);
        return new org.joml.Matrix4f().set(md);
    }
    //?}
}
