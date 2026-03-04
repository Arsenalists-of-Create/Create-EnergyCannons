package net.arsenalists.createenergycannons.content.cannons.laser;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LaserBlockEntity extends SmartBlockEntity {
    private int fireRate = 0;
    private int lastUpdate = 0;
    private int range = 256;

    private FilteringBehaviour filtering;

    public LaserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        filtering = new FilteringBehaviour(this, new CenteredSideValueBoxTransform(
                (state, direction) -> state.hasProperty(LaserBlock.FACING)
                        && state.getValue(LaserBlock.FACING).getOpposite() == direction))
                .withPredicate(this::isGlassPane);
        behaviours.add(filtering);
    }

    private boolean isGlassPane(ItemStack stack) {
        return Block.byItem(stack.getItem()) instanceof StainedGlassPaneBlock;
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

    public void setRange(int range) {
        this.range = range;
        notifyUpdate();
    }

    public int getRange() {
        return this.range;
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        this.fireRate = tag.getInt("fireRate");
        this.lastUpdate = tag.getInt("lastUpdate");
        this.range = tag.getInt("range");
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("fireRate", this.fireRate);
        tag.putInt("lastUpdate", this.lastUpdate);
        tag.putInt("range", this.range);
    }
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(getRange() + 10);
    }

    public int getFireRate() {
        return this.fireRate;
    }

    public @Nullable DyeColor getLensColor() {
        if (filtering == null) return null;
        ItemStack filterItem = filtering.getFilter();
        if (filterItem.isEmpty()) return null;
        Block block = Block.byItem(filterItem.getItem());
        if (block instanceof StainedGlassPaneBlock pane) {
            return pane.getColor();
        }
        return null;
    }
}
