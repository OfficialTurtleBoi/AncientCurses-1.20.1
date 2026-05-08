package net.turtleboi.ancientcurses.network.packets.items;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.VoodooSoulClientData;

import java.util.UUID;
import java.util.function.Supplier;

public class VoodooSoulSyncS2C {
    private final UUID soulUuid;
    private final boolean active;

    public VoodooSoulSyncS2C(UUID soulUuid, boolean active) {
        this.soulUuid = soulUuid;
        this.active = active;
    }

    public VoodooSoulSyncS2C(FriendlyByteBuf buf) {
        this.soulUuid = buf.readUUID();
        this.active = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(soulUuid);
        buf.writeBoolean(active);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> VoodooSoulClientData.setSoulClone(soulUuid, active));
        context.setPacketHandled(true);
        return true;
    }
}
