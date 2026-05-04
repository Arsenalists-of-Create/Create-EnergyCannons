package net.arsenalists.createenergycannons.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;

@Mixin(CannonMountBlockEntity.class)
public interface CannonMountBEAccessor {

    @Accessor(value = "running", remap = false)
    boolean running();

    @Accessor(value = "running", remap = false)
    void setRunning(boolean running);

    @Accessor(value = "cannonYaw", remap = false)
    float cannonYaw();

    @Accessor(value = "cannonYaw", remap = false)
    void setCannonYaw(float yaw);

    @Accessor(value = "cannonPitch", remap = false)
    float cannonPitch();

    @Accessor(value = "cannonPitch", remap = false)
    void setCannonPitch(float pitch);

    @Accessor(value = "prevPitch", remap = false)
    float prevPitch();

    @Accessor(value = "prevPitch", remap = false)
    void setPrevPitch(float pitch);

    @Accessor(value = "prevYaw", remap = false)
    float prevYaw();

    @Accessor(value = "prevYaw", remap = false)
    void setPrevYaw(float yaw);

}
