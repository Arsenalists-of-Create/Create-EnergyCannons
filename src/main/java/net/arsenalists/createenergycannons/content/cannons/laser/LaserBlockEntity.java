package net.arsenalists.createenergycannons.content.cannons.laser;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LaserBlockEntity extends SmartBlockEntity {
    private int fireRate = 0;
    private int lastUpdate = 0;

    public LaserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void tick() {
        super.tick();
        if (fireRate > 0)
            lastUpdate++;
        if (lastUpdate > 5)
            fireRate = 0;
    }

    //todo better rendering method for laser
    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(256);
    }

    public void setFireRate(int firePower) {
        this.fireRate = firePower;
        lastUpdate = 0;
        notifyUpdate();
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        this.fireRate = tag.getInt("fireRate");
        this.lastUpdate = tag.getInt("lastUpdate");
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("fireRate", this.fireRate);
        tag.putInt("lastUpdate", this.lastUpdate);
    }

    public int getFireRate() {
        return this.fireRate;
    }

}
