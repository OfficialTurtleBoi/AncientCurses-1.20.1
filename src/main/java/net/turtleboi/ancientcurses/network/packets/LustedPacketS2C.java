package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
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
            PlayerClientData.setLusted(isLusted);
        });
        context.setPacketHandled(true);
        return true;
    }
}
