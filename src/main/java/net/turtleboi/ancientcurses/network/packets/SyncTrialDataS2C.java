package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.awt.*;
import java.util.function.Supplier;

public class SyncTrialDataS2C {
    private final String trialType;
    private final int eliminationKills;
    private final int eliminationKillsRequired;
    private final long trialDurationElapsed;
    private final long trialDurationTotal;
    private final String fetchItem;
    private final int fetchItems;
    private final int fetchItemsRequired;


    public SyncTrialDataS2C(String trialType, int eliminationKills, int eliminationKillsRequired, long trialDurationElapsed, long trialDurationTotal,
                            String fetchItem, int fetchItems, int fetchItemsRequired) {
        this.trialType = trialType;
        this.eliminationKills = eliminationKills;
        this.eliminationKillsRequired = eliminationKillsRequired;
        this.trialDurationElapsed = trialDurationElapsed;
        this.trialDurationTotal = trialDurationTotal;
        this.fetchItem = fetchItem;
        this.fetchItems = fetchItems;
        this.fetchItemsRequired = fetchItemsRequired;
    }

    public SyncTrialDataS2C(FriendlyByteBuf buf) {
        this.trialType = buf.readUtf();
        this.eliminationKills = buf.readInt();
        this.eliminationKillsRequired = buf.readInt();
        this.trialDurationElapsed = buf.readLong();
        this.trialDurationTotal = buf.readLong();
        this.fetchItem = buf.readUtf();
        this.fetchItems = buf.readInt();
        this.fetchItemsRequired = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(trialType);
        buf.writeInt(eliminationKills);
        buf.writeInt(eliminationKillsRequired);
        buf.writeLong(trialDurationElapsed);
        buf.writeLong(trialDurationTotal);
        buf.writeUtf(fetchItem);
        buf.writeInt(fetchItems);
        buf.writeInt(fetchItemsRequired);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            //System.out.println(Component.literal("Sending trial information!"));
            PlayerClientData.setTrialType(trialType);
            if (trialType == null || trialType.equals("None")) {
                PlayerClientData.trialType = "None";
            } else {
                PlayerClientData.setEliminationKills(eliminationKills);
                PlayerClientData.setEliminationKillsRequired(eliminationKillsRequired);
                PlayerClientData.setTrialDurationElapsed(trialDurationElapsed);
                PlayerClientData.setTrialDurationTotal(trialDurationTotal);
                PlayerClientData.setFetchItem(fetchItem);
                PlayerClientData.setFetchItems(fetchItems);
                PlayerClientData.setFetchItemsRequired(fetchItemsRequired);
                System.out.println(Component.literal("You need: " + fetchItem));
                System.out.println(Component.literal("You need this many items: " + fetchItems));
                System.out.println(Component.literal("You have this many items: " + fetchItemsRequired));
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
