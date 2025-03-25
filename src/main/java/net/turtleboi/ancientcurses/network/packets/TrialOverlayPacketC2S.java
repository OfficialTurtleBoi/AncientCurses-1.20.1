package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.capabilities.trials.PlayerTrialProvider;
import net.turtleboi.ancientcurses.network.ModNetworking;

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
                player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
                    if (trialData.getCurrentAltarPos() == null) {
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
                });
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
