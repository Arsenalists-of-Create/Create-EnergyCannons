package net.arsenalists.createenergycannons.content.cannons.laser;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.concurrent.ConcurrentHashMap;

public class LaserBurnData {

    public static final ConcurrentHashMap<BlockPos, Integer> BURN_STAGES = new ConcurrentHashMap<>();

    public static void setBurnStage(Level level, BlockPos pos, int stage) {
        if (stage < 0 || stage > 9) return;

        Integer previousStage = BURN_STAGES.get(pos);

        if (previousStage == null || previousStage != stage) {
            BURN_STAGES.put(pos, stage);

            if (level != null && level.isClientSide) {
                playBurnSound(level, pos);
            }
        }
    }

    public static void removeBurn(Level level, BlockPos pos) {
        if (BURN_STAGES.remove(pos) != null && level != null && level.isClientSide) {
            playBurnSound(level, pos);
        }
    }

    public static void clearAll() {
        BURN_STAGES.clear();
    }

    private static void playBurnSound(Level level, BlockPos pos) {
        level.playLocalSound(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                SoundEvents.LAVA_EXTINGUISH,
                SoundSource.BLOCKS,
                0.5f,
                2.0f + level.getRandom().nextFloat() * 0.4f,
                false
        );
    }
}