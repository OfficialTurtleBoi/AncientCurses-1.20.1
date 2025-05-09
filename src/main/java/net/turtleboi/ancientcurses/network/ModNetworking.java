package net.turtleboi.ancientcurses.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.network.packets.*;
import net.turtleboi.ancientcurses.network.packets.items.BeaconInfoPacketC2S;
import net.turtleboi.ancientcurses.network.packets.items.BeaconInfoPacketS2C;
import net.turtleboi.ancientcurses.network.packets.items.DowsingRodInfoPacketS2C;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.ancientcurses.network.packets.rites.RiteOverlayPacketC2S;

public class ModNetworking {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }
    public static void register () {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(AncientCurses.MOD_ID, "networking"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(LustedPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(LustedPacketS2C::new)
                .encoder(LustedPacketS2C::toBytes)
                .consumerMainThread(LustedPacketS2C::handle)
                .add();

        net.messageBuilder(VoidPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(VoidPacketS2C::new)
                .encoder(VoidPacketS2C::toBytes)
                .consumerMainThread(VoidPacketS2C::handle)
                .add();

        net.messageBuilder(SyncRiteDataS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncRiteDataS2C::new)
                .encoder(SyncRiteDataS2C::toBytes)
                .consumerMainThread(SyncRiteDataS2C::handle)
                .add();

        net.messageBuilder(RiteOverlayPacketC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RiteOverlayPacketC2S::new)
                .encoder(RiteOverlayPacketC2S::toBytes)
                .consumerMainThread(RiteOverlayPacketC2S::handle)
                .add();

        net.messageBuilder(PortalOverlayPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PortalOverlayPacketS2C::new)
                .encoder(PortalOverlayPacketS2C::toBytes)
                .consumerMainThread(PortalOverlayPacketS2C::handle)
                .add();

        net.messageBuilder(PortalOverlayPacketC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PortalOverlayPacketC2S::new)
                .encoder(PortalOverlayPacketC2S::toBytes)
                .consumerMainThread(PortalOverlayPacketC2S::handle)
                .add();

        net.messageBuilder(RefreshLapidaryScreenS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RefreshLapidaryScreenS2C::new)
                .encoder(RefreshLapidaryScreenS2C::toBytes)
                .consumerMainThread(RefreshLapidaryScreenS2C::handle)
                .add();

        net.messageBuilder(RefreshLapidaryScreenC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RefreshLapidaryScreenC2S::new)
                .encoder(RefreshLapidaryScreenC2S::toBytes)
                .consumerMainThread(RefreshLapidaryScreenC2S::handle)
                .add();

        net.messageBuilder(BeaconInfoPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BeaconInfoPacketS2C::new)
                .encoder(BeaconInfoPacketS2C::toBytes)
                .consumerMainThread(BeaconInfoPacketS2C::handle)
                .add();

        net.messageBuilder(BeaconInfoPacketC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(BeaconInfoPacketC2S::new)
                .encoder(BeaconInfoPacketC2S::toBytes)
                .consumerMainThread(BeaconInfoPacketC2S::handle)
                .add();

        net.messageBuilder(DowsingRodInfoPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DowsingRodInfoPacketS2C::new)
                .encoder(DowsingRodInfoPacketS2C::toBytes)
                .consumerMainThread(DowsingRodInfoPacketS2C::handle)
                .add();
    }

    public static <MSG> void sendToPlayer (MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToServer (MSG message) {
        INSTANCE.sendToServer(message);
    }
}
