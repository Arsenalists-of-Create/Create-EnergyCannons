package net.arsenalists.createenergycannons.network;

import net.arsenalists.createenergycannons.content.cannons.laser.LaserBurnData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LaserBurnS2CPacket {
    private final BlockPos pos;
    private final int stage;

    public LaserBurnS2CPacket(BlockPos pos, int stage) {
        this.pos = pos;
        this.stage = stage;
    }

    public LaserBurnS2CPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.stage = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(stage);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.level == null) return;

                    if (stage < 0) {
                        LaserBurnData.removeBurn(mc.level, pos);
                    } else {
                        LaserBurnData.setBurnStage(mc.level, pos, stage);
                    }
                })
        );
        return true;
    }
}