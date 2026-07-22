package net.arsenalists.createenergycannons.network;

import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
//? if >=1.21 {
/*import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
*///?}

//? if <1.21 {
public class LaserBurnS2CPacket {
    private final BlockPos pos;
    private final int stage;

    public LaserBurnS2CPacket(BlockPos pos, int stage) {
        this.pos = pos;
        this.stage = stage;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(stage);
    }
}
//?} else {
/*public record LaserBurnS2CPacket(BlockPos pos, int stage) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<LaserBurnS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(CECMod.resource("laser_burn"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LaserBurnS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, LaserBurnS2CPacket::pos,
                    ByteBufCodecs.VAR_INT, LaserBurnS2CPacket::stage,
                    LaserBurnS2CPacket::new
            );

    @Override
    public CustomPacketPayload.Type<LaserBurnS2CPacket> type() {
        return TYPE;
    }
}
*///?}
