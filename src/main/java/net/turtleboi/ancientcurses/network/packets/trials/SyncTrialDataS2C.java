package net.turtleboi.ancientcurses.network.packets.trials;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.util.function.Supplier;

public class SyncTrialDataS2C {
    private final String trialType;
    private final boolean trialComplete;
    private final String eliminationTarget;
    private final int waveCount;
    private final int killsRemaining;
    private final int waveKillTotal;
    private final long durationElapsed;
    private final long durationTotal;
    private final String fetchItem;
    private final int itemCount;
    private final int fetchItemsRequired;


    public SyncTrialDataS2C(String trialType, boolean trialComplete, String eliminationTarget, int waveCount, int killsRemaining,
                            int waveKillTotal, long durationElapsed, long durationTotal,
                            String fetchItem, int itemCount, int itemRequired) {
        this.trialType = trialType;
        this.trialComplete = trialComplete;
        this.eliminationTarget = eliminationTarget;
        this.waveCount = waveCount;
        this.killsRemaining = killsRemaining;
        this.waveKillTotal = waveKillTotal;
        this.durationElapsed = durationElapsed;
        this.durationTotal = durationTotal;
        this.fetchItem = fetchItem;
        this.itemCount = itemCount;
        this.fetchItemsRequired = itemRequired;
    }

    public SyncTrialDataS2C(FriendlyByteBuf buf) {
        this.trialType = buf.readUtf();
        this.trialComplete = buf.readBoolean();
        this.eliminationTarget = buf.readUtf();
        this.waveCount = buf.readInt();
        this.killsRemaining = buf.readInt();
        this.waveKillTotal = buf.readInt();
        this.durationElapsed = buf.readLong();
        this.durationTotal = buf.readLong();
        this.fetchItem = buf.readUtf();
        this.itemCount = buf.readInt();
        this.fetchItemsRequired = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(trialType);
        buf.writeBoolean(trialComplete);
        buf.writeUtf(eliminationTarget);
        buf.writeInt(waveCount);
        buf.writeInt(killsRemaining);
        buf.writeInt(waveKillTotal);
        buf.writeLong(durationElapsed);
        buf.writeLong(durationTotal);
        buf.writeUtf(fetchItem);
        buf.writeInt(itemCount);
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
                PlayerClientData.setTrialComplete(trialComplete);
                PlayerClientData.setEliminationTarget(eliminationTarget);
                PlayerClientData.setWaveCount(waveCount);
                PlayerClientData.setKillsRemaining(killsRemaining);
                PlayerClientData.setWaveKillTotal(waveKillTotal);
                PlayerClientData.setDurationElapsed(durationElapsed);
                PlayerClientData.setDurationTotal(durationTotal);
                PlayerClientData.setFetchItem(fetchItem);
                PlayerClientData.setFetchItems(itemCount);
                PlayerClientData.setFetchItemsRequired(fetchItemsRequired);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
