package net.arsenalists.createenergycannons.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserBurnData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

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
