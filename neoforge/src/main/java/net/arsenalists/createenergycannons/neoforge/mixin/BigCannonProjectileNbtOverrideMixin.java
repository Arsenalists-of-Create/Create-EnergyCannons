package net.arsenalists.createenergycannons.neoforge.mixin;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.sled.IMagneticSled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileBlockEntity;

/**
 * Sled persistence for CBC below 5.11.4, where the projectile has no disk hooks of its own and
 * the only place to write is the ones it inherits. Selected by {@link CECMixinPlugin}.
 */
@Mixin(BigCannonProjectileBlockEntity.class)
public abstract class BigCannonProjectileNbtOverrideMixin extends SyncedBlockEntity {

    public BigCannonProjectileNbtOverrideMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Sled", ((IMagneticSled) this).isSled());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ((IMagneticSled) this).setSled(tag.getBoolean("Sled"));
    }
}
