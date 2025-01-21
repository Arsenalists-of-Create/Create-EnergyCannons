package net.arsenalists.createenergycannons.content.cannons.magnetic.railgun;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class RailCannonBlockEntity extends SmartBlockEntity implements IRailCannonBlockEntity {

    private RailCannonBehavior cannonBehavior;

    public RailCannonBlockEntity(BlockEntityType<? extends RailCannonBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviors) {
        super.addBehavioursDeferred(behaviors);
        behaviors.add(this.cannonBehavior = new RailCannonBehavior(this, this::canLoadBlock));
    }

    @Override
    public RailCannonBehavior cannonBehavior() {
        return this.cannonBehavior;
    }

}
