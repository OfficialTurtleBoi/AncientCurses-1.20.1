package net.turtleboi.ancientcurses.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.network.packets.LustedPacketS2C;
import net.turtleboi.ancientcurses.network.packets.SendParticlesS2C;

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
    }
    public static <MSG> void sendToServer (MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer (MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
