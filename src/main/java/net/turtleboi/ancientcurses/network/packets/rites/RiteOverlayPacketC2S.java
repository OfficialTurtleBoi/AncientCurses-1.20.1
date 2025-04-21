package net.turtleboi.ancientcurses.network.packets.rites;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.network.ModNetworking;

import java.util.function.Supplier;

public class RiteOverlayPacketC2S {
    public RiteOverlayPacketC2S() {
    }

    public RiteOverlayPacketC2S(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
                    if (riteData.getCurrentAltarPos() == null) {
                        ModNetworking.sendToPlayer(
                                new SyncRiteDataS2C(
                                        "None",
                                        false,
                                        "",
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        "",
                                        0,
                                        0),
                                player);
                    }
                });
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
