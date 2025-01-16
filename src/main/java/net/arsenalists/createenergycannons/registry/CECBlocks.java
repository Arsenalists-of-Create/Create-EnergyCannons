package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CECBlocks {

    public static final BlockEntry<Block> BATTERY_BLOCK = CECMod.REGISTRATE.block("creative_battery", Block::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .addLayer(() -> RenderType::cutout)
            .simpleItem()
            .register();

    public static void register() {
        CECMod.getLogger().info("Registering Blocks");
    }
}
