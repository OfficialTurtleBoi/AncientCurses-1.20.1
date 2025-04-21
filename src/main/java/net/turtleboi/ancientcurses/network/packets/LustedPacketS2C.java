package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.util.function.Supplier;

public class LustedPacketS2C {
    public final boolean isLusted;

    public LustedPacketS2C(boolean isLusted) {
        this.isLusted = isLusted;
    }

    public LustedPacketS2C(FriendlyByteBuf buf) {
        this.isLusted = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isLusted);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            PlayerClientData.setObsessed(isLusted);
        });
        context.setPacketHandled(true);
        return true;
    }
}
