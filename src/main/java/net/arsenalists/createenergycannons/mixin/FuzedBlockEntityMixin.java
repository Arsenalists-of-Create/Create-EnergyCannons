package net.arsenalists.createenergycannons.mixin;

import net.arsenalists.createenergycannons.content.cannons.magnetic.sled.IMagneticSled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileBlockEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

/**
 * Mixin class for the FuzedBlockEntity to implement the IMagneticSled interface.
 * This mixin adds functionality to handle the sled state of the Fuzed Shell for Magnetic-Related Energy Cannons.
 */
@Mixin(FuzedBlockEntity.class)
public abstract class FuzedBlockEntityMixin extends BigCannonProjectileBlockEntity implements IMagneticSled {
    @Unique
    private boolean create_EnergyCannons$sled = false;

    public FuzedBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setSled(boolean sled) {
        this.create_EnergyCannons$sled = sled;
    }

    @Override
    public boolean isSled() {
        return this.create_EnergyCannons$sled;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void saveSledState(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("Sled", this.create_EnergyCannons$sled);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void loadSledState(CompoundTag tag, CallbackInfo ci) {
        this.create_EnergyCannons$sled = tag.getBoolean("Sled");
    }
}
