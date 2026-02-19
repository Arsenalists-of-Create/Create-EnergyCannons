package net.arsenalists.createenergycannons.registry;

import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class CECTags {
    public static class Blocks {
        public static final TagKey<Block> LASERPROOF =
                TagKey.create(Registries.BLOCK, new ResourceLocation(CECMod.MODID, "laserproof"));
    }
}
