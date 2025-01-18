package net.arsenalists.createenergycannons.content.cannons.magnetic.railcoilgun;

import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.VoxelShaper;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railcoilgun.MountedRailCannonContrpation.MountedRailCannonContraption;
import net.arsenalists.createenergycannons.registry.CECBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import rbasamoyai.createbigcannons.cannons.big_cannons.cannon_end.BigCannonEnd;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;

import java.util.function.Supplier;

public class RailCannonTubeBlock extends RailCannonBaseBlock implements IBE<RailCannonBlockEntity> {
    private final MountedRailCannonContraption.TYPE type;
    private final VoxelShaper visualShapes;
    private final VoxelShaper collisionShapes;
    private final Supplier<CannonCastShape> cannonShape;

    public RailCannonTubeBlock(Properties properties, BigCannonMaterial material, Supplier<CannonCastShape> cannonShape, VoxelShape base, MountedRailCannonContraption.TYPE type) {
        this(properties, material, cannonShape, base, base, type);
    }

    public RailCannonTubeBlock(Properties properties, BigCannonMaterial material, Supplier<CannonCastShape> cannonShape, VoxelShape visualShape, VoxelShape collisionShape, MountedRailCannonContraption.TYPE type) {
        super(properties, material);
        this.visualShapes = new AllShapes.Builder(visualShape).forDirectional();
        this.collisionShapes = new AllShapes.Builder(collisionShape).forDirectional();
        this.cannonShape = cannonShape;
        this.type = type;
    }


    public static RailCannonTubeBlock medium(Properties properties, BigCannonMaterial material, MountedRailCannonContraption.TYPE type) {
        return new RailCannonTubeBlock(properties, material, () -> CannonCastShape.MEDIUM, Shapes.block(), type);
    }


    @Override
    public CannonCastShape getCannonShape() {
        return this.cannonShape.get();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.collisionShapes.get(this.getFacing(state));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.visualShapes.get(this.getFacing(state));
    }

    @Override
    public MountedRailCannonContraption.TYPE getType() {
        return type;
    }

    @Override
    public BigCannonEnd getDefaultOpeningType() {
        return BigCannonEnd.OPEN;
    }

    @Override
    public boolean isComplete(BlockState state) {
        return true;
    }

    @Override
    public Class<RailCannonBlockEntity> getBlockEntityClass() {
        return RailCannonBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RailCannonBlockEntity> getBlockEntityType() {
        return CECBlockEntity.CANNON.get();
    }

}
