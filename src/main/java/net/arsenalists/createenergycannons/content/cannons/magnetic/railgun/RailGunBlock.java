package net.arsenalists.createenergycannons.content.cannons.magnetic.railgun;

import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonTubeBlock;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;

import java.util.function.Supplier;

public class RailGunBlock extends BigCannonTubeBlock {

    public RailGunBlock(Properties properties, BigCannonMaterial material, Supplier<CannonCastShape> cannonShape, VoxelShape base) {
        super(properties, material, cannonShape, base);
    }

    @Override
    public @NotNull AbstractMountedCannonContraption getCannonContraption() {
        return new MountedRailCannonContraption();
    }

    public static RailGunBlock mediumRail(Properties properties, BigCannonMaterial material) {
        return new RailGunBlock(properties, material, () -> CannonCastShape.MEDIUM, Shapes.block());
    }

    @Override
    public BlockEntityType<? extends BigCannonBlockEntity> getBlockEntityType() {
        return CECBlockEntity.BIG_CANNON.get();
    }
}
