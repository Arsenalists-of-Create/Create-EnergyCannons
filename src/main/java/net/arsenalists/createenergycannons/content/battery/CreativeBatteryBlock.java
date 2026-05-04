package net.arsenalists.createenergycannons.content.battery;


import com.simibubi.create.foundation.block.IBE;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeBatteryBlock extends Block implements IBE<CreativeBatteryBlockEntity> {
    public CreativeBatteryBlock(Properties properties) {
        super(properties);
    }

    //? if >=1.21
    public static final com.mojang.serialization.MapCodec<CreativeBatteryBlock> CODEC = simpleCodec(CreativeBatteryBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return CODEC;
    }


    @Override
    public Class<CreativeBatteryBlockEntity> getBlockEntityClass() {
        return CreativeBatteryBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CreativeBatteryBlockEntity> getBlockEntityType() {
        return CECBlockEntity.CREATIVE_BATTERY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().create(pos, state);
    }
}
