package net.arsenalists.createenergycannons.content.cannons.magnetic.coilgun;

import net.arsenalists.createenergycannons.content.cooling.IRegenBarrel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;

import java.util.function.Supplier;

/** Coilgun barrel with coolant channels - counts toward active cooling. */
public class RegenCoilGunBlock extends CoilGunBlock implements IRegenBarrel {

    public RegenCoilGunBlock(Properties properties, BigCannonMaterial material, Supplier<CannonCastShape> cannonShape, VoxelShape base) {
        super(properties, material, cannonShape, base);
    }

    public static RegenCoilGunBlock mediumCoil(BlockBehaviour.Properties properties, BigCannonMaterial material) {
        return new RegenCoilGunBlock(properties, material, () -> CannonCastShape.MEDIUM, Shapes.block());
    }
}
