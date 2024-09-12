package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.util.function.Supplier;

public class VoidPacketS2C {
    private final boolean isVoid;
    private final long voidStartTime;

    public VoidPacketS2C(boolean isVoid, long voidStartTime) {
        this.isVoid = isVoid;
        this.voidStartTime = voidStartTime;
    }

    public VoidPacketS2C(FriendlyByteBuf buf) {
        this.isVoid = buf.readBoolean();
        this.voidStartTime = buf.readLong();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isVoid);
        buf.writeLong(voidStartTime);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PlayerClientData.setVoid(isVoid);
                PlayerClientData.setVoidStartTime(voidStartTime);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
