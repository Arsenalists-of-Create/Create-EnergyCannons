package net.arsenalists.createenergycannons.mixin;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
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

/**
 * This covers every shell type (fused, solid shot, AP, etc.) without individual mixins.
 */
@Mixin(BigCannonProjectileBlockEntity.class)
public abstract class BigCannonProjectileBlockEntityMixin extends SyncedBlockEntity implements IMagneticSled {

    @Unique
    private boolean create_EnergyCannons$sled = false;

    public BigCannonProjectileBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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


    //? if <1.21 {
    /*@Inject(method = {"saveAdditional", "method_11007", "m_183515_"}, at = @At("TAIL"), remap = false, require = 1)
    private void saveSledState(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("Sled", this.create_EnergyCannons$sled);
    }

    @Inject(method = {"load", "method_11014", "m_142466_"}, at = @At("TAIL"), remap = false, require = 1)
    private void loadSledState(CompoundTag tag, CallbackInfo ci) {
        this.create_EnergyCannons$sled = tag.getBoolean("Sled");
    }
    *///?}
    // 1.21: CBC 5.11.2's BigCannonProjectileBlockEntity doesn't override
    // saveAdditional/loadAdditional directly (they go through Create's SmartBlockEntity
    // write/read pattern instead). Skip the inject on 1.21
}
