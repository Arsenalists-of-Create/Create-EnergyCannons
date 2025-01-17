package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.block.battery.CreativeBatteryBlock;
import net.arsenalists.createenergycannons.block.energymount.EnergyCannonMount;
import net.arsenalists.createenergycannons.block.laser.LaserBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockItem;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonTubeBlock;
import rbasamoyai.createbigcannons.cannons.big_cannons.BuiltUpCannonCTBehavior;
import rbasamoyai.createbigcannons.datagen.assets.CBCBuilderTransformers;
import rbasamoyai.createbigcannons.index.CBCBigCannonMaterials;
import rbasamoyai.createbigcannons.index.CBCSpriteShifts;

public class CECBlocks {

    public static final BlockEntry<CreativeBatteryBlock> BATTERY_BLOCK = CECMod.REGISTRATE
            .block("creative_battery", CreativeBatteryBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .addLayer(() -> RenderType::cutout)
            .simpleItem()
            .register();

    public static final BlockEntry<BigCannonTubeBlock> RAILGUN_BARREL = CECMod.REGISTRATE
            .block("railgun_barrel", p -> BigCannonTubeBlock.medium(p, CBCBigCannonMaterials.NETHERSTEEL))
            .initialProperties(SharedProperties::softMetal)
            .blockstate((c, p) -> p.directionalBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .onRegister(CreateRegistrate.connectedTextures(() -> new BuiltUpCannonCTBehavior(CBCSpriteShifts.NETHERSTEEL_CANNON_CHAMBER)))
            .addLayer(() -> RenderType::cutout)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .loot(CBCBuilderTransformers.nethersteelScrapLoot(10))
            .item(BigCannonBlockItem::new).build()
            .register();

    public static final BlockEntry<BigCannonTubeBlock> COILGUN_BARREL = CECMod.REGISTRATE
            .block("coilgun_barrel", p -> BigCannonTubeBlock.medium(p, CBCBigCannonMaterials.NETHERSTEEL))
            .initialProperties(SharedProperties::softMetal)
            .blockstate((c, p) -> p.directionalBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .onRegister(CreateRegistrate.connectedTextures(() -> new BuiltUpCannonCTBehavior(CBCSpriteShifts.NETHERSTEEL_CANNON_CHAMBER)))
            .addLayer(() -> RenderType::cutout)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .loot(CBCBuilderTransformers.nethersteelScrapLoot(10))
            .item(BigCannonBlockItem::new).build()
            .register();

    public static final BlockEntry<LaserBlock> LASER = CECMod.REGISTRATE
            .block("laser", LaserBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .blockstate((c, p) -> p.directionalBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .addLayer(() -> RenderType::cutout)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .simpleItem()
            .register();

    public static final BlockEntry<EnergyCannonMount> ENERGY_CANNON_MOUNT = CECMod.REGISTRATE
            .block("energy_cannon_mount", EnergyCannonMount::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .blockstate(NonNullBiConsumer.noop())
            .properties(p -> p.isRedstoneConductor((pState, pLevel, pPos) -> false))
            .addLayer(() -> RenderType::cutout)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item()
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public static void register() {
        CECMod.getLogger().info("Registering Blocks");
    }
}
