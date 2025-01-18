package net.arsenalists.createenergycannons.content.cannons.magnetic.railcoilgun.MountedRailCannonContrpation;


import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ContraptionType;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railcoilgun.IRailCannonBlockEntity;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railcoilgun.RailCannonBlock;
import net.arsenalists.createenergycannons.registry.CECCannonContraptionTypes;
import net.arsenalists.createenergycannons.registry.CECContraptionTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
import rbasamoyai.createbigcannons.cannon_control.cannon_types.ICannonContraptionType;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.QuickfiringBreechBlockEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.cannon_end.BigCannonEnd;
import rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterial;
import rbasamoyai.createbigcannons.index.CBCBigCannonMaterials;
import rbasamoyai.createbigcannons.utils.CBCUtils;

import java.util.ArrayList;
import java.util.List;

public class MountedRailCannonContraption extends AbstractMountedCannonContraption {

    private BigCannonMaterial cannonMaterial;
    public boolean hasFired = false;
    public boolean hasWeldedPenalty = false;
    protected TYPE type = TYPE.DEFAULT;

    public enum TYPE {
        DEFAULT,
        RAIL_CANNON,
        COIL_CANNON
    }

    @Override
    public boolean assemble(Level level, BlockPos pos) throws AssemblyException {
        if (!this.collectCannonBlocks(level, pos)) return false;
        this.bounds = this.createBoundsFromExtensionLengths();
        return !this.blocks.isEmpty();
    }

    private boolean collectCannonBlocks(Level level, BlockPos pos) throws AssemblyException {
        BlockState startState = level.getBlockState(pos);

        if (!(startState.getBlock() instanceof RailCannonBlock startCannon)) {
            return false;
        }

        if (!startCannon.isComplete(startState)) {
            throw hasIncompleteCannonBlocks(pos);
        }
        if (this.hasCannonLoaderInside(level, startState, pos)) {
            throw cannonLoaderInsideDuringAssembly(pos);
        }
        BigCannonMaterial material = startCannon.getCannonMaterial();
        BigCannonEnd startEnd = startCannon.getOpeningType(level, startState, pos);

        List<StructureBlockInfo> cannonBlocks = new ArrayList<>();
        cannonBlocks.add(new StructureBlockInfo(pos, startState, this.getBlockEntityNBT(level, pos)));

        int cannonLength = 1;

        Direction cannonFacing = startCannon.getFacing(startState);

        Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, cannonFacing.getAxis());
        Direction negative = positive.getOpposite();

        BlockPos start = pos;
        BlockState nextState = level.getBlockState(pos.relative(positive));

        BigCannonEnd positiveEnd = startEnd;
        while (this.isValidCannonBlock(level, nextState, start.relative(positive)) && this.isConnectedToCannon(level, nextState, start.relative(positive), positive, material)) {
            start = start.relative(positive);

            if (!((RailCannonBlock) nextState.getBlock()).isComplete(nextState)) {
                throw hasIncompleteCannonBlocks(start);
            }
            if (((RailCannonBlock) nextState.getBlock()).getType() != TYPE.DEFAULT) {
                if (this.type == TYPE.DEFAULT) {
                    this.type = startCannon.getType();
                } else if (this.type != startCannon.getType()) {
                    throw new AssemblyException("Cannon type mismatch");
                }
            }
            cannonBlocks.add(new StructureBlockInfo(start, nextState, this.getBlockEntityNBT(level, start)));
            this.frontExtensionLength++;
            cannonLength++;

            positiveEnd = ((RailCannonBlock) nextState.getBlock()).getOpeningType(level, nextState, start);

            if (this.hasCannonLoaderInside(level, nextState, start)) {
                throw cannonLoaderInsideDuringAssembly(start);
            }

            nextState = level.getBlockState(start.relative(positive));

            if (cannonLength > getMaxCannonLength()) {
                throw cannonTooLarge();
            }
            if (positiveEnd != BigCannonEnd.OPEN) break;
        }
        BlockPos positiveEndPos = positiveEnd == BigCannonEnd.OPEN ? start : start.relative(negative);

        start = pos;
        nextState = level.getBlockState(pos.relative(negative));

        BigCannonEnd negativeEnd = startEnd;
        while (this.isValidCannonBlock(level, nextState, start.relative(negative)) && this.isConnectedToCannon(level, nextState, start.relative(negative), negative, material)) {
            start = start.relative(negative);

            if (!((RailCannonBlock) nextState.getBlock()).isComplete(nextState)) {
                throw hasIncompleteCannonBlocks(start);
            }
            if (((RailCannonBlock) nextState.getBlock()).getType() != TYPE.DEFAULT) {
                if (this.type == TYPE.DEFAULT) {
                    this.type = startCannon.getType();
                } else if (this.type != startCannon.getType()) {
                    throw new AssemblyException("Cannon type mismatch");
                }
            }
            cannonBlocks.add(new StructureBlockInfo(start, nextState, this.getBlockEntityNBT(level, start)));
            this.backExtensionLength++;
            cannonLength++;

            negativeEnd = ((RailCannonBlock) nextState.getBlock()).getOpeningType(level, nextState, start);

            if (this.hasCannonLoaderInside(level, nextState, start)) {
                throw cannonLoaderInsideDuringAssembly(start);
            }

            nextState = level.getBlockState(start.relative(negative));

            if (cannonLength > getMaxCannonLength()) {
                throw cannonTooLarge();
            }
            if (negativeEnd != BigCannonEnd.OPEN) break;
        }
        BlockPos negativeEndPos = negativeEnd == BigCannonEnd.OPEN ? start : start.relative(positive);

        if (positiveEnd == negativeEnd) {
            throw invalidCannon();
        }

        boolean openEndFlag = positiveEnd == BigCannonEnd.OPEN;
        this.initialOrientation = openEndFlag ? positive : negative;
        this.startPos = openEndFlag ? negativeEndPos : positiveEndPos;
        this.anchor = pos;

        this.startPos = this.startPos.subtract(pos);
        for (StructureBlockInfo blockInfo : cannonBlocks) {
            BlockPos localPos = blockInfo.pos().subtract(pos);
            StructureBlockInfo localBlockInfo = new StructureBlockInfo(localPos, blockInfo.state(), blockInfo.nbt());
            this.getBlocks().put(localPos, localBlockInfo);

            if (blockInfo.nbt() == null) continue;
            BlockEntity be = BlockEntity.loadStatic(localPos, blockInfo.state(), blockInfo.nbt());
            this.presentBlockEntities.put(localPos, be);
            if (be instanceof IRailCannonBlockEntity cbe && cbe.cannonBehavior().isWelded())
                this.hasWeldedPenalty = true;
        }
        this.cannonMaterial = material;

        return true;
    }

    private boolean isValidCannonBlock(LevelAccessor level, BlockState state, BlockPos pos) {
        return state.getBlock() instanceof RailCannonBlock;
    }

    private boolean hasCannonLoaderInside(LevelAccessor level, BlockState state, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IRailCannonBlockEntity cannon)) return false;
        BlockState containedState = cannon.cannonBehavior().block().state();
        return IRailCannonBlockEntity.isValidLoader(null, new StructureBlockInfo(BlockPos.ZERO, containedState, null));
    }

    private boolean isConnectedToCannon(LevelAccessor level, BlockState state, BlockPos pos, Direction connection, BigCannonMaterial material) {
        RailCannonBlock cBlock = (RailCannonBlock) state.getBlock();
        if (cBlock.getCannonMaterialInLevel(level, state, pos) != material) return false;
        return level.getBlockEntity(pos) instanceof IRailCannonBlockEntity cbe
                && level.getBlockEntity(pos.relative(connection.getOpposite())) instanceof IRailCannonBlockEntity cbe1
                && cbe.cannonBehavior().isConnectedTo(connection.getOpposite())
                && cbe1.cannonBehavior().isConnectedTo(connection);
    }

    public float getWeightForStress() {
        if (this.cannonMaterial == null) {
            return this.blocks.size();
        }
        return this.blocks.size() * this.cannonMaterial.properties().weight();
    }

    @Override
    public void tick(Level level, PitchOrientedContraptionEntity entity) {
        super.tick(level, entity);

        BlockPos endPos = this.startPos.relative(this.initialOrientation.getOpposite());
        if (this.presentBlockEntities.get(endPos) instanceof QuickfiringBreechBlockEntity qfbreech)
            qfbreech.tickAnimation();
    }

    @Override
    public void onRedstoneUpdate(ServerLevel level, PitchOrientedContraptionEntity entity, boolean togglePower, int firePower, ControlPitchContraption controller) {
        if (!togglePower || firePower <= 0) return;
        this.fireShot(level, entity);
    }

    @Override
    public void fireShot(ServerLevel level, PitchOrientedContraptionEntity entity) {
        System.out.println("Firing shot");
    }

    @Override
    public Vec3 getInteractionVec(PitchOrientedContraptionEntity poce) {
        return poce.toGlobalVector(Vec3.atCenterOf(this.startPos.relative(this.initialOrientation.getOpposite())), 1);
    }

    @Override
    public ICannonContraptionType getCannonType() {
        return CECCannonContraptionTypes.RAIL_CANNON;
    }

    @Override
    public CompoundTag writeNBT(boolean clientData) {
        CompoundTag tag = super.writeNBT(clientData);
        tag.putString("CannonMaterial", this.cannonMaterial == null ? CBCBigCannonMaterials.CAST_IRON.name().toString() : this.cannonMaterial.name().toString());
        if (this.hasWeldedPenalty) tag.putBoolean("WeldedCannon", true);
        if (this.hasFired) tag.putBoolean("HasFired", true);
        return tag;
    }

    @Override
    public void readNBT(Level level, CompoundTag tag, boolean clientData) {
        super.readNBT(level, tag, clientData);
        this.cannonMaterial = BigCannonMaterial.fromNameOrNull(CBCUtils.location(tag.getString("CannonMaterial")));
        this.hasWeldedPenalty = tag.contains("WeldedCannon");
        if (this.cannonMaterial == null) this.cannonMaterial = CBCBigCannonMaterials.NETHERSTEEL;
        this.hasFired = tag.contains("HasFired");
    }

    @Override
    public ContraptionType getType() {
        return CECContraptionTypes.MOUNTED_RAIL_CANNON;
    }

}
