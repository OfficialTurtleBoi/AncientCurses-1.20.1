package net.turtleboi.ancientcurses.network.packets.items;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BeaconInfoPacketC2S {
    private final int maxChargeTicks;
    private final int remainingUseTime;
    private final double hitDistance;
    private final boolean beingUsed;

    public BeaconInfoPacketC2S(int maxChargeTicks, int remainingUseTime, double hitDistance, boolean beingUsed) {
        this.maxChargeTicks = maxChargeTicks;
        this.remainingUseTime = remainingUseTime;
        this.hitDistance = hitDistance;
        this.beingUsed = beingUsed;
    }

    public BeaconInfoPacketC2S(FriendlyByteBuf buf) {
        this.maxChargeTicks = buf.readInt();
        this.remainingUseTime = buf.readInt();
        this.hitDistance = buf.readDouble();
        this.beingUsed = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(maxChargeTicks);
        buf.writeInt(remainingUseTime);
        buf.writeDouble(hitDistance);
        buf.writeBoolean(beingUsed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {

            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
