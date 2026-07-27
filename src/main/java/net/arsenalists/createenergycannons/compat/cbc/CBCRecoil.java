package net.arsenalists.createenergycannons.compat.cbc;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * CBC 5.11.4 added the cannon origin to {@code ControlPitchContraption#onRecoil}; earlier builds
 * only take the force vector. Bind whichever one is actually present so a single jar runs on both.
 */
public final class CBCRecoil {

    private static final MethodHandle ON_RECOIL;
    private static final boolean TAKES_ORIGIN;
    private static boolean failed;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle handle = null;
        boolean takesOrigin = false;
        try {
            handle = lookup.findVirtual(ControlPitchContraption.class, "onRecoil",
                MethodType.methodType(void.class, Vec3.class, Vec3.class, AbstractContraptionEntity.class));
            takesOrigin = true;
        } catch (ReflectiveOperationException newSignatureAbsent) {
            try {
                handle = lookup.findVirtual(ControlPitchContraption.class, "onRecoil",
                    MethodType.methodType(void.class, Vec3.class, AbstractContraptionEntity.class));
            } catch (ReflectiveOperationException e) {
                CECMod.getLogger().error("No usable ControlPitchContraption#onRecoil found; cannon recoil disabled", e);
            }
        }
        ON_RECOIL = handle;
        TAKES_ORIGIN = takesOrigin;
    }

    private CBCRecoil() {
    }

    public static void apply(ControlPitchContraption controller, Vec3 force, Vec3 origin, AbstractContraptionEntity entity) {
        if (ON_RECOIL == null || failed) return;
        try {
            if (TAKES_ORIGIN) {
                ON_RECOIL.invoke(controller, force, origin, entity);
            } else {
                ON_RECOIL.invoke(controller, force, entity);
            }
        } catch (Throwable t) {
            failed = true;
            CECMod.getLogger().error("Failed to apply cannon recoil; disabling it for this session", t);
        }
    }
}
