package net.arsenalists.createenergycannons.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import rbasamoyai.createbigcannons.cannon_control.fixed_cannon_mount.FixedCannonMountBlockEntity;

@Mixin(FixedCannonMountBlockEntity.class)
public interface FixedCannonMountBEAccessor {

    @Accessor(value = "running", remap = false)
    void setRunning(boolean running);
}
