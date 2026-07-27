package net.arsenalists.createenergycannons.neoforge.mixin;

import net.arsenalists.createenergycannons.content.cannons.magnetic.sled.IMagneticSled;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileBlockEntity;

/**
 * Sled persistence for CBC 5.11.4+, which saves its own data in these methods - piggyback on
 * them instead of declaring them, or Mixin replaces CBC's copy and drops the shell's tracer.
 * Selected by {@link CECMixinPlugin}.
 */
@Mixin(BigCannonProjectileBlockEntity.class)
public abstract class BigCannonProjectileNbtInjectMixin {

    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = false)
    private void createEnergyCannons$saveSled(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putBoolean("Sled", ((IMagneticSled) this).isSled());
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = false)
    private void createEnergyCannons$loadSled(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        ((IMagneticSled) this).setSled(tag.getBoolean("Sled"));
    }
}
