package net.arsenalists.createenergycannons.client;

import dev.architectury.networking.NetworkManager;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBurnData;
import net.arsenalists.createenergycannons.network.LaserBurnS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
//? if <1.21 {
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
//?}

/**
 * Client-only handler for the laser burn packet. The packet class itself stays
 * free of client references so the dedicated server can register the packet type
 * without loading client classes like ClientLevel.
 */
@Environment(EnvType.CLIENT)
public class LaserBurnClient {

    //? if <1.21 {
    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        BlockPos pos = buf.readBlockPos();
        int stage = buf.readInt();
        ctx.queue(() -> apply(pos, stage));
    }

    private static void apply(BlockPos pos, int stage) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (stage < 0) {
            LaserBurnData.removeBurn(mc.level, pos);
        } else {
            LaserBurnData.setBurnStage(mc.level, pos, stage);
        }
    }
    //?} else {
    /*public static void handle(LaserBurnS2CPacket packet, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            if (packet.stage() < 0) {
                LaserBurnData.removeBurn(mc.level, packet.pos());
            } else {
                LaserBurnData.setBurnStage(mc.level, packet.pos(), packet.stage());
            }
        });
    }
    *///?}
}
