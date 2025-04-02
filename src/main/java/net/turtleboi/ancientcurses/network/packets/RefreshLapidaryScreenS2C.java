package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.screen.LapidaristTableContainerScreen;

import java.util.function.Supplier;

public class RefreshLapidaryScreenS2C {
    private final BlockPos blockPos;
    private final int containerId;

    public RefreshLapidaryScreenS2C(BlockPos blockPos, int containerId) {
        this.blockPos = blockPos;
        this.containerId = containerId;
    }

    public RefreshLapidaryScreenS2C(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.containerId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(blockPos);
        buf.writeInt(containerId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (minecraft.screen instanceof LapidaristTableContainerScreen && player != null) {
                System.out.println("Closing the screen!");
                player.closeContainer();
                ModNetworking.sendToServer(new RefreshLapidaryScreenC2S(blockPos));
            }
        });
        return true;
    }
}

