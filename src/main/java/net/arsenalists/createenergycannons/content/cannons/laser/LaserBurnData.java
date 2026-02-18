package net.arsenalists.createenergycannons.content.cannons.laser;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.concurrent.ConcurrentHashMap;

public class LaserBurnData {

    public static final ConcurrentHashMap<BlockPos, BurnEntry> BURN_STAGES = new ConcurrentHashMap<>();
    private static final int FADE_DURATION_TICKS = 200; // 10 seconds

    public static class BurnEntry {
        public final int stage;
        public final long timestamp;

        public BurnEntry(int stage, long timestamp) {
            this.stage = stage;
            this.timestamp = timestamp;
        }
    }

    public static void setBurnStage(Level level, BlockPos pos, int stage) {
        if (stage < 0 || stage > 9) return;

        BurnEntry previousEntry = BURN_STAGES.get(pos);
        int previousStage = previousEntry != null ? previousEntry.stage : -1;

        if (previousStage != stage) {
            long timestamp = level != null ? level.getGameTime() : 0;
            BURN_STAGES.put(pos, new BurnEntry(stage, timestamp));

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

    public static boolean shouldFade(BlockPos pos, long currentTime) {
        BurnEntry entry = BURN_STAGES.get(pos);
        if (entry == null) return true;
        return (currentTime - entry.timestamp) >= FADE_DURATION_TICKS;
    }

    public static float getFadeAlpha(BlockPos pos, long currentTime) {
        BurnEntry entry = BURN_STAGES.get(pos);
        if (entry == null) return 0f;
        long elapsed = currentTime - entry.timestamp;
        if (elapsed >= FADE_DURATION_TICKS) return 0f;
        long fadeStart = FADE_DURATION_TICKS - 40;
        if (elapsed < fadeStart) return 1f;
        return 1f - ((elapsed - fadeStart) / 40f);
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