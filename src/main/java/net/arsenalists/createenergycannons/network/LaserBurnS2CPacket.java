package net.arsenalists.createenergycannons.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBurnData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}

//? if <1.21 {
/*public class LaserBurnS2CPacket {
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

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        BlockPos pos = buf.readBlockPos();
        int stage = buf.readInt();
        ctx.queue(() -> {
            if (ctx.getEnvironment() == Env.CLIENT) {
                handleClient(pos, stage);
            }
        });
    }

    private static void handleClient(BlockPos pos, int stage) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (stage < 0) {
            LaserBurnData.removeBurn(mc.level, pos);
        } else {
            LaserBurnData.setBurnStage(mc.level, pos, stage);
        }
    }
}
*///?} else {
public record LaserBurnS2CPacket(BlockPos pos, int stage) implements CustomPacketPayload {

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

    /** S2C handler. ctx.queue() defers onto the main client thread. */
    public static void handle(LaserBurnS2CPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            if (ctx.getEnvironment() == Env.CLIENT) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null) return;
                if (packet.stage < 0) {
                    LaserBurnData.removeBurn(mc.level, packet.pos);
                } else {
                    LaserBurnData.setBurnStage(mc.level, packet.pos, packet.stage);
                }
            }
        });
    }
}
//?}
