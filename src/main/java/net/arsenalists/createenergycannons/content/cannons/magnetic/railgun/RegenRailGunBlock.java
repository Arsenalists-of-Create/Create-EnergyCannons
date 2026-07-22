package net.arsenalists.createenergycannons.content.cannons.magnetic.railgun;

import net.arsenalists.createenergycannons.content.cooling.IRegenBarrel;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;

import java.util.function.Supplier;

/** Railgun barrel with coolant channels - counts toward active cooling. */
public class RegenRailGunBlock extends RailGunBlock implements IRegenBarrel {

    public RegenRailGunBlock(Properties properties, BigCannonMaterial material, Supplier<CannonCastShape> cannonShape, VoxelShape base) {
        super(properties, material, cannonShape, base);
    }

    public static RegenRailGunBlock mediumRail(Properties properties, BigCannonMaterial material) {
        return new RegenRailGunBlock(properties, material, () -> CannonCastShape.MEDIUM, Shapes.block());
    }
}
