package net.arsenalists.createenergycannons.mixin;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.sled.IMagneticSled;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileBlockEntity;

/**
 * Persists the magnetic-sled flag in BCPBE's saved NBT on 1.21+.
 *
 * On 1.20.1 BigCannonProjectileBlockEntityMixin handles this via saveAdditional/load,
 * but on 1.21 CBC's BCPBE doesn't override saveAdditional — persistence flows through
 * Create's SmartBlockEntity#write/read, which is inherited (so we can't target it on
 * BCPBE directly with mixin). We instead target SmartBlockEntity and gate by instanceof.
 */
@Mixin(SmartBlockEntity.class)
public abstract class SledPersistenceMixin {

    //? if >=1.21 {
    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V",
            at = @At("TAIL"), require = 0)
    private void cec$writeSled(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if ((Object) this instanceof BigCannonProjectileBlockEntity && (Object) this instanceof IMagneticSled sled) {
            tag.putBoolean("Sled", sled.isSled());
        }
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V",
            at = @At("TAIL"), require = 0)
    private void cec$readSled(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if ((Object) this instanceof BigCannonProjectileBlockEntity && (Object) this instanceof IMagneticSled sled) {
            sled.setSled(tag.getBoolean("Sled"));
        }
    }
    //?}
}
