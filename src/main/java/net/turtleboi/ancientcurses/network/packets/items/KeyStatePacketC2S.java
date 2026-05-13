package net.turtleboi.ancientcurses.network.packets.items;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.network.PlayerKeyStateCache;

import java.util.function.Supplier;

public class KeyStatePacketC2S {
    private final boolean ctrlDown;

    public KeyStatePacketC2S(boolean ctrlDown) {
        this.ctrlDown = ctrlDown;
    }

    public KeyStatePacketC2S(FriendlyByteBuf buf) {
        this.ctrlDown = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(ctrlDown);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PlayerKeyStateCache.setCtrlDown(player.getUUID(), ctrlDown);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
