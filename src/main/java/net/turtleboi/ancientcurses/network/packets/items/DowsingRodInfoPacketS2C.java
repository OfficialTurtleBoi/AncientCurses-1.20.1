package net.turtleboi.ancientcurses.network.packets.items;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.util.function.Supplier;

public class DowsingRodInfoPacketS2C {
    private final boolean beingUsed;
    private final long usedAt;
    private final double xCoord;
    private final double yCoord;
    private final double zCoord;

    public DowsingRodInfoPacketS2C(boolean beingUsed, long usedAt, double xCoord, double yCoord, double zCoord) {
        this.beingUsed = beingUsed;
        this.usedAt = usedAt;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = zCoord;
    }

    public DowsingRodInfoPacketS2C(FriendlyByteBuf buf) {
        this.beingUsed = buf.readBoolean();
        this.usedAt = buf.readLong();
        this.xCoord = buf.readDouble();
        this.yCoord = buf.readDouble();
        this.zCoord = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(beingUsed);
        buf.writeLong(usedAt);
        buf.writeDouble(xCoord);
        buf.writeDouble(yCoord);
        buf.writeDouble(zCoord);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            PlayerClientData.setItemUsed(beingUsed);
            PlayerClientData.setItemUsedTime(usedAt);
            PlayerClientData.setAltarX(xCoord);
            PlayerClientData.setAltarY(yCoord);
            PlayerClientData.setAltarZ(zCoord);
        });
        context.setPacketHandled(true);
        return true;
    }
}
