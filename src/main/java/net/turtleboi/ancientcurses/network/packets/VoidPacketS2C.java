package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.util.function.Supplier;

public class VoidPacketS2C {
    private final boolean isVoid;
    private final int voidTimer;
    private final int voidTotalTime;

    public VoidPacketS2C(boolean isVoid, int voidTimer, int voidTotalTime) {
        this.isVoid = isVoid;
        this.voidTimer = voidTimer;
        this.voidTotalTime = voidTotalTime;
    }

    public VoidPacketS2C(FriendlyByteBuf buf) {
        this.isVoid = buf.readBoolean();
        this.voidTimer = buf.readInt();
        this.voidTotalTime = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isVoid);
        buf.writeInt(voidTimer);
        buf.writeInt(voidTotalTime);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            PlayerClientData.setVoid(isVoid);
            PlayerClientData.setVoidTimer(voidTimer);
            PlayerClientData.setTotalVoidTime(voidTotalTime);
        });
        context.setPacketHandled(true);
        return true;
    }
}
