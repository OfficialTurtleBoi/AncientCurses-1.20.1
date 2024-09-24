package net.turtleboi.ancientcurses.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.network.packets.*;

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

        net.messageBuilder(SendParticlesS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SendParticlesS2C::new)
                .encoder(SendParticlesS2C::toBytes)
                .consumerMainThread(SendParticlesS2C::handle)
                .add();

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

        net.messageBuilder(SleepPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SleepPacketS2C::new)
                .encoder(SleepPacketS2C::toBytes)
                .consumerMainThread(SleepPacketS2C::handle)
                .add();

        net.messageBuilder(SyncTrialDataS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncTrialDataS2C::new)
                .encoder(SyncTrialDataS2C::toBytes)
                .consumerMainThread(SyncTrialDataS2C::handle)
                .add();
    }
    public static <MSG> void sendToServer (MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer (MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
