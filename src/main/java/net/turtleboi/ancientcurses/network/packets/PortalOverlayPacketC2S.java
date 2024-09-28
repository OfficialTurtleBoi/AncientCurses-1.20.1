package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;

import java.util.List;
import java.util.function.Supplier;

public class PortalOverlayPacketC2S {
    private final float portalAlpha;

    public PortalOverlayPacketC2S(float portalAlpha) {
        this.portalAlpha = portalAlpha;
    }

    public PortalOverlayPacketC2S(FriendlyByteBuf buf) {
        this.portalAlpha = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(portalAlpha);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                List<CursedPortalEntity> cursedPortalEntities = player.level().getEntitiesOfClass(
                        CursedPortalEntity.class, player.getBoundingBox().inflate(2.5));
                if (cursedPortalEntities.isEmpty()){
                    float newAlpha = Math.max(0, portalAlpha - 0.001F);
                    ModNetworking.sendToPlayer(new PortalOverlayPacketS2C(newAlpha), player);
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
