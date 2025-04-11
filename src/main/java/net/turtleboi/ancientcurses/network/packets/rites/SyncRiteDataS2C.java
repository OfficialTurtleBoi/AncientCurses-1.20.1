package net.turtleboi.ancientcurses.network.packets.rites;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.util.function.Supplier;

public class SyncRiteDataS2C {
    private final String riteType;
    private final boolean riteComplete;
    private final String eliminationTarget;
    private final int waveCount;
    private final int killsRemaining;
    private final int waveKillTotal;
    private final long durationElapsed;
    private final long durationTotal;
    private final String fetchItem;
    private final int itemCount;
    private final int fetchItemsRequired;


    public SyncRiteDataS2C(String riteType, boolean riteComplete, String eliminationTarget, int waveCount, int killsRemaining,
                           int waveKillTotal, long durationElapsed, long durationTotal,
                           String fetchItem, int itemCount, int itemRequired) {
        this.riteType = riteType;
        this.riteComplete = riteComplete;
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

    public SyncRiteDataS2C(FriendlyByteBuf buf) {
        this.riteType = buf.readUtf();
        this.riteComplete = buf.readBoolean();
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
        buf.writeUtf(riteType);
        buf.writeBoolean(riteComplete);
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
            //System.out.println(Component.literal("Sending rite information!"));
            PlayerClientData.setRiteType(riteType);
            if (riteType == null || riteType.equals("None")) {
                PlayerClientData.riteType = "None";
            } else {
                PlayerClientData.setRiteComplete(riteComplete);
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
