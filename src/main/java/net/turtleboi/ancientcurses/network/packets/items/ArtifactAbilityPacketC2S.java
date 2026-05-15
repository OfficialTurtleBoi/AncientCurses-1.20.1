package net.turtleboi.ancientcurses.network.packets.items;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.item.items.ArtifactItem;

import java.util.function.Supplier;

public class ArtifactAbilityPacketC2S {
    public ArtifactAbilityPacketC2S() {
    }

    public ArtifactAbilityPacketC2S(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ArtifactItem.tryActivate(player);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
