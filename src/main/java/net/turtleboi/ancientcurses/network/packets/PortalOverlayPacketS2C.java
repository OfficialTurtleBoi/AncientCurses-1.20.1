package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.util.function.Supplier;

public class PortalOverlayPacketS2C {
    private final float portalAlpha;

    public PortalOverlayPacketS2C(float portalAlpha) {
        this.portalAlpha = portalAlpha;
    }

    public PortalOverlayPacketS2C(FriendlyByteBuf buf) {
        this.portalAlpha = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(portalAlpha);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            PlayerClientData.setPortalOverlayAlpha(portalAlpha);
        });
        context.setPacketHandled(true);
        return true;
    }
}
