package net.arsenalists.createenergycannons.content.cannons.magnetic.coilgun;

import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonTubeBlock;
import rbasamoyai.createbigcannons.cannons.big_cannons.cannon_end.BigCannonEndBlock;
import rbasamoyai.createbigcannons.cannons.big_cannons.cannon_end.BigCannonEndBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;

import java.util.function.Supplier;

public class CoilGunBlock extends BigCannonEndBlock {

    public CoilGunBlock(Properties properties, BigCannonMaterial material, Supplier<CannonCastShape> cannonShape, VoxelShape base) {
        super(properties, material);// cannonShape, base);
    }

    @Override
    public @NotNull AbstractMountedCannonContraption getCannonContraption() {
        return new MountedCoilCannonContraption();
    }

    public static CoilGunBlock mediumCoil(BlockBehaviour.Properties properties, BigCannonMaterial material) {
        return new CoilGunBlock(properties, material, () -> CannonCastShape.MEDIUM, Shapes.block());
    }

    @Override
    public BlockEntityType<? extends BigCannonEndBlockEntity> getBlockEntityType() {
        return CECBlockEntity.BIG_CANNON.get();
    }
}
