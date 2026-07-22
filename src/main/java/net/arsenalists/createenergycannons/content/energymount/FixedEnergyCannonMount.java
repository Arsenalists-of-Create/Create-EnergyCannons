package net.arsenalists.createenergycannons.content.energymount;

import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannon_control.fixed_cannon_mount.FixedCannonMountBlock;
import rbasamoyai.createbigcannons.cannon_control.fixed_cannon_mount.FixedCannonMountBlockEntity;

/** Energy-powered variant of CBC's fixed cannon mount. */
public class FixedEnergyCannonMount extends FixedCannonMountBlock {

    public FixedEnergyCannonMount(BlockBehaviour.Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    /*public static final com.mojang.serialization.MapCodec<FixedEnergyCannonMount> CODEC = simpleCodec(FixedEnergyCannonMount::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.DirectionalBlock> codec() {
        return CODEC;
    }
    *///?}

    @Override
    public BlockEntityType<? extends FixedCannonMountBlockEntity> getBlockEntityType() {
        return CECBlockEntity.FIXED_ENERGY_CANNON_MOUNT.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().create(pos, state);
    }
}
