package net.arsenalists.createenergycannons.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PacketHandler {
    public static final ResourceLocation LASER_BURN_PACKET = CECMod.resource("laser_burn");

    public static void register() {
        if (dev.architectury.platform.Platform.getEnvironment() == dev.architectury.utils.Env.CLIENT) {
            registerClient();
        } else {
            //? if >=1.21
            NetworkManager.registerS2CPayloadType(LaserBurnS2CPacket.TYPE, LaserBurnS2CPacket.STREAM_CODEC);
        }
    }

    private static void registerClient() {
        //? if <1.21 {
        /*NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                LASER_BURN_PACKET,
                LaserBurnS2CPacket::handle
        );
        *///?} else {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
                LaserBurnS2CPacket.TYPE, LaserBurnS2CPacket.STREAM_CODEC, LaserBurnS2CPacket::handle);
        //?}
    }

    //? if <1.21 {
    /*private static FriendlyByteBuf toBuffer(LaserBurnS2CPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.toBytes(buf);
        return buf;
    }
    *///?}

    public static void sendToAllTracking(LaserBurnS2CPacket packet, Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Vec3 blockCenter = Vec3.atCenterOf(pos);
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(blockCenter) < 64 * 64) {
                //? if <1.21 {
                /*NetworkManager.sendToPlayer(player, LASER_BURN_PACKET, toBuffer(packet));
                *///?} else
                NetworkManager.sendToPlayer(player, packet);
            }
        }
    }
}
