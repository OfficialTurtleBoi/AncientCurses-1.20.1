package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.turtleboi.ancientcurses.block.entity.LapidaristTableBlockEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;

import java.util.function.Supplier;

public class RefreshLapidaryScreenC2S {
    private final BlockPos blockPos;

    public RefreshLapidaryScreenC2S(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public RefreshLapidaryScreenC2S(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(blockPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                if (player.level().getBlockEntity(blockPos) instanceof LapidaristTableBlockEntity blockEntity) {
                    System.out.println("Opening the screen!");
                    NetworkHooks.openScreen(player, blockEntity, blockPos);
                }
            }
        });
        return true;
    }
}

