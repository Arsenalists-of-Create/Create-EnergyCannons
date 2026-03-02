package net.arsenalists.createenergycannons.content.cannons.magnetic.sled;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SledItem extends Item {
    public SledItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos blockPos = pContext.getClickedPos();
        if (level.getBlockEntity(blockPos) instanceof IMagneticSled sled) {
            if (!sled.isSled()) {
                sled.setSled(true);
                level.playSound(null, blockPos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5f, 1.0f);
                if (pContext.getPlayer() != null && !pContext.getPlayer().isCreative())
                    pContext.getItemInHand().shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(pContext);
    }
}
