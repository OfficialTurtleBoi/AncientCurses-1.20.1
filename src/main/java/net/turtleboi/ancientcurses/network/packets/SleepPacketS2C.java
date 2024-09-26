package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.util.function.Supplier;

public class SleepPacketS2C {
    private final boolean isAsleep;

    public SleepPacketS2C(boolean isAsleep) {
        this.isAsleep = isAsleep;
    }

    public SleepPacketS2C(FriendlyByteBuf buf) {
        this.isAsleep = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isAsleep);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            PlayerClientData.setAsleep(isAsleep);
        });
        context.setPacketHandled(true);
        return true;
    }
}
