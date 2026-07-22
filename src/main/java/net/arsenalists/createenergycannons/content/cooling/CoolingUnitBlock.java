package net.arsenalists.createenergycannons.content.cooling;

import com.simibubi.create.foundation.block.IBE;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CoolingUnitBlock extends Block implements IBE<CoolingUnitBlockEntity> {
    public CoolingUnitBlock(Properties properties) {
        super(properties);
    }

    //? if >=1.21 {
    /*public static final com.mojang.serialization.MapCodec<CoolingUnitBlock> CODEC = simpleCodec(CoolingUnitBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return CODEC;
    }
    *///?}

    @Override
    public Class<CoolingUnitBlockEntity> getBlockEntityClass() {
        return CoolingUnitBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CoolingUnitBlockEntity> getBlockEntityType() {
        return CECBlockEntity.COOLING_UNIT.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().create(pos, state);
    }
}
