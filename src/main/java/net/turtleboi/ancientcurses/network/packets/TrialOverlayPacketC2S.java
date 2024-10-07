package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.trials.PlayerTrialData;

import java.util.List;
import java.util.function.Supplier;

public class TrialOverlayPacketC2S {
    public TrialOverlayPacketC2S() {
    }

    public TrialOverlayPacketC2S(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                if (PlayerTrialData.getCurrentAltarPos(player) == null) {
                    ModNetworking.sendToPlayer(
                            new SyncTrialDataS2C(
                                    "None",
                                    "",
                                    0,
                                    0,
                                    0,
                                    0,
                                    "",
                                    0,
                                    0),
                                    player);
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
