package net.arsenalists.createenergycannons.content.battery;

import com.simibubi.create.foundation.block.IBE;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CreativeBatteryBlock extends Block implements IBE<CreativeBatteryBlockEntity> {
    public CreativeBatteryBlock(Properties properties) {
        super(properties);
    }


    @Override
    public Class<CreativeBatteryBlockEntity> getBlockEntityClass() {
        return CreativeBatteryBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CreativeBatteryBlockEntity> getBlockEntityType() {
        return CECBlockEntity.CREATIVE_BATTERY.get();
    }
}
