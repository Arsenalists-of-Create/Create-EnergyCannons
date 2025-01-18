package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.battery.CreativeBatteryBlock;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBlock;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.BuiltUpCannonCTBehavior;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.MountedRailCannonContrpation.MountedRailCannonContraption;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.RailCannonBlockItem;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.RailCannonTubeBlock;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.quickfire.QuickfiringBreechBlock;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.screwbreech.ScrewBreechBlock;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.sliding.SlidingBreechCTBehavior;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.sliding.SlidingBreechBlock;
import net.arsenalists.createenergycannons.content.energymount.EnergyCannonMount;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import rbasamoyai.createbigcannons.base.CBCDefaultStress;
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

    public static final BlockEntry<RailCannonTubeBlock> RAILGUN_BARREL = CECMod.REGISTRATE
            .block("railgun_barrel", p -> RailCannonTubeBlock.medium(p, CBCBigCannonMaterials.NETHERSTEEL, MountedRailCannonContraption.TYPE.RAIL_CANNON))
            .initialProperties(SharedProperties::softMetal)
            .blockstate((c, p) -> p.directionalBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .onRegister(CreateRegistrate.connectedTextures(() -> new BuiltUpCannonCTBehavior(CBCSpriteShifts.NETHERSTEEL_CANNON_BARREL)))
            .addLayer(() -> RenderType::cutout)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .loot(CBCBuilderTransformers.nethersteelScrapLoot(10))
            .item(RailCannonBlockItem::new).build()
            .register();

    public static final BlockEntry<ScrewBreechBlock> NETHERSTEEL_SCREW_BREECH = CECMod.REGISTRATE
            .block("nethersteel_rail_screw_breech", p -> new ScrewBreechBlock(p, CBCBigCannonMaterials.NETHERSTEEL))
            .loot(CBCBuilderTransformers.nethersteelScrapLoot(10))
            .transform(CBCDefaultStress.setImpact(40.0d))
            .blockstate(NonNullBiConsumer.noop())
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .onRegister(CreateRegistrate.connectedTextures(() -> new BuiltUpCannonCTBehavior(CBCSpriteShifts.NETHERSTEEL_CANNON_CHAMBER)))
            .item(RailCannonBlockItem::new)
            .model(AssetLookup.existingItemModel())
            .build()
            .register();

    public static final BlockEntry<ScrewBreechBlock> STEEL_RAIL_SCREW_BREECH = CECMod.REGISTRATE
            .block("steel_rail_screw_breech", p -> new ScrewBreechBlock(p, CBCBigCannonMaterials.STEEL))
            .loot(CBCBuilderTransformers.nethersteelScrapLoot(10))
            .transform(CBCDefaultStress.setImpact(40.0d))
            .blockstate(NonNullBiConsumer.noop())
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(RailCannonBlockItem::new)
            .model(AssetLookup.existingItemModel())
            .build()
            .register();

    public static final BlockEntry<QuickfiringBreechBlock> STEEL_RAIL_QUICKFIRING_BREECH = CECMod.REGISTRATE
            .block("steel_rail_quickfiring_breech", p -> new QuickfiringBreechBlock(p, CBCBigCannonMaterials.STEEL, steelSlidingBreech()))
            .lang("Steel Rail Quick-Firing Breech")
            .loot(CBCBuilderTransformers.nethersteelScrapLoot(10))
            .transform(CBCDefaultStress.setImpact(40.0d))
            .addLayer(() -> RenderType::cutoutMipped)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .blockstate(NonNullBiConsumer.noop())
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(RailCannonBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .onRegister(CreateRegistrate.connectedTextures(() ->
                    new SlidingBreechCTBehavior(CBCSpriteShifts.STEEL_SLIDING_BREECH_SIDE, CBCSpriteShifts.STEEL_SLIDING_BREECH_SIDE_HOLE)))
            .register();

    private static NonNullSupplier<? extends Block> steelSlidingBreech() {
        return STEEL_SLIDING_BREECH;
    }


    public static final BlockEntry<SlidingBreechBlock> STEEL_SLIDING_BREECH = CECMod.REGISTRATE
            .block("steel_rail_sliding_breech", p -> new SlidingBreechBlock(p, CBCBigCannonMaterials.STEEL, STEEL_RAIL_QUICKFIRING_BREECH))
            .loot(CBCBuilderTransformers.steelScrapLoot(10))
            .transform(CBCDefaultStress.setImpact(32.0d))
            .addLayer(() -> RenderType::cutout)
            .blockstate(NonNullBiConsumer.noop())
            .properties(BlockBehaviour.Properties::noOcclusion)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .onRegister(CreateRegistrate.connectedTextures(() ->
                    new SlidingBreechCTBehavior(CBCSpriteShifts.STEEL_SLIDING_BREECH_SIDE, CBCSpriteShifts.STEEL_SLIDING_BREECH_SIDE_HOLE)))
            .item(RailCannonBlockItem::new)
            .model(AssetLookup.existingItemModel())
            .build()
            .register();


    public static final BlockEntry<RailCannonTubeBlock> STEEL_COILGUN_BARREL = CECMod.REGISTRATE
            .block("steel_coilgun_barrel", p -> RailCannonTubeBlock.medium(p, CBCBigCannonMaterials.STEEL, MountedRailCannonContraption.TYPE.COIL_CANNON))
            .initialProperties(SharedProperties::softMetal)
            .blockstate((c, p) -> p.directionalBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .onRegister(CreateRegistrate.connectedTextures(() -> new BuiltUpCannonCTBehavior(CBCSpriteShifts.STEEL_CANNON_BARREL)))
            .addLayer(() -> RenderType::cutout)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .loot(CBCBuilderTransformers.nethersteelScrapLoot(10))
            .item(RailCannonBlockItem::new).build()
            .register();

    public static final BlockEntry<RailCannonTubeBlock> NETHERSTEEL_COILGUN_BARREL = CECMod.REGISTRATE
            .block("nethersteel_coilgun_barrel", p -> RailCannonTubeBlock.medium(p, CBCBigCannonMaterials.NETHERSTEEL, MountedRailCannonContraption.TYPE.COIL_CANNON))
            .initialProperties(SharedProperties::softMetal)
            .blockstate((c, p) -> p.directionalBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .onRegister(CreateRegistrate.connectedTextures(() -> new BuiltUpCannonCTBehavior(CBCSpriteShifts.NETHERSTEEL_CANNON_BARREL)))
            .addLayer(() -> RenderType::cutout)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .loot(CBCBuilderTransformers.nethersteelScrapLoot(10))
            .item(RailCannonBlockItem::new).build()
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
