package net.arsenalists.createenergycannons.content.cannons.magnetic.railgun;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlockEntity;

public class RailGunBlockEntity extends BigCannonBlockEntity {
    private long cooldownEndTime = 0;

    public RailGunBlockEntity(BlockEntityType<? extends BigCannonBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public long getCooldownEndTime() {
        return cooldownEndTime;
    }

    public void setCooldownEndTime(long time) {
        this.cooldownEndTime = time;
        setChanged();
    }

    public boolean isOverheated(long currentTime) {
        return currentTime < cooldownEndTime;
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putLong("CooldownEndTime", cooldownEndTime);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        cooldownEndTime = tag.getLong("CooldownEndTime");
    }
}
