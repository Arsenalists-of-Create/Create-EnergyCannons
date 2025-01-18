package net.arsenalists.createenergycannons.content.cannons.magnetic.railcoilgun.sliding;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railcoilgun.IRailCannonBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railcoilgun.RailCannonBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.List;

public abstract class AbstractRailCannonBreechBlockEntity extends KineticBlockEntity implements IRailCannonBlockEntity {

    protected RailCannonBehavior cannonBehavior;

    public AbstractRailCannonBreechBlockEntity(BlockEntityType<? extends AbstractRailCannonBreechBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviors) {
        super.addBehaviours(behaviors);
        behaviors.add(this.cannonBehavior = new RailCannonBehavior(this, this::canLoadBlock));
    }

    public abstract boolean isOpen();

    @Override
    public boolean canLoadBlock(StructureBlockInfo blockInfo) {
        return this.isOpen() && IRailCannonBlockEntity.super.canLoadBlock(blockInfo);
    }

    @Override
    public RailCannonBehavior cannonBehavior() {
        return this.cannonBehavior;
    }

}
