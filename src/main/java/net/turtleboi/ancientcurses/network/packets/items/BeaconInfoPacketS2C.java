package net.turtleboi.ancientcurses.network.packets.items;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.util.function.Supplier;

public class BeaconInfoPacketS2C {
    private final int maxDurationTicks;
    private final int remainingDurationTicks;
    private final double hitDistance;
    private final boolean beingUsed;

    public BeaconInfoPacketS2C(int maxDurationTicks, int remainingDurationTicks, double hitDistance, boolean beingUsed) {
        this.maxDurationTicks = maxDurationTicks;
        this.remainingDurationTicks = remainingDurationTicks;
        this.hitDistance = hitDistance;
        this.beingUsed = beingUsed;
    }

    public BeaconInfoPacketS2C(FriendlyByteBuf buf) {
        this.maxDurationTicks = buf.readInt();
        this.remainingDurationTicks = buf.readInt();
        this.hitDistance = buf.readDouble();
        this.beingUsed = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(maxDurationTicks);
        buf.writeInt(remainingDurationTicks);
        buf.writeDouble(hitDistance);
        buf.writeBoolean(beingUsed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            PlayerClientData.setItemMaxDurationTicks(maxDurationTicks);
            PlayerClientData.setItemRemainingUseTime(remainingDurationTicks);
            if (hitDistance != 0) {
                PlayerClientData.setItemHitDistance(hitDistance);
            }
            PlayerClientData.setItemUsed(beingUsed);
        });
        context.setPacketHandled(true);
        return true;
    }
}
