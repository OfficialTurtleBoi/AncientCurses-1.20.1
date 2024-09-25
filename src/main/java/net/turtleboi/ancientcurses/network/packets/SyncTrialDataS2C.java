package net.turtleboi.ancientcurses.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;

import java.util.function.Supplier;

public class SyncTrialDataS2C {
    private final String trialType;
    private final int eliminationKills;
    private final int eliminationKillsRequired;
    private final long trialDurationElapsed;
    private final long trialDurationTotal;


    public SyncTrialDataS2C(String trialType, int eliminationKills, int eliminationKillsRequired, long trialDurationElapsed, long trialDurationTotal) {
        this.trialType = trialType;
        this.eliminationKills = eliminationKills;
        this.eliminationKillsRequired = eliminationKillsRequired;
        this.trialDurationElapsed = trialDurationElapsed;
        this.trialDurationTotal = trialDurationTotal;
    }

    public SyncTrialDataS2C(FriendlyByteBuf buf) {
        this.trialType = buf.readUtf();
        this.eliminationKills = buf.readInt();
        this.eliminationKillsRequired = buf.readInt();
        this.trialDurationElapsed = buf.readLong();
        this.trialDurationTotal = buf.readLong();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(trialType);
        buf.writeInt(eliminationKills);
        buf.writeInt(eliminationKillsRequired);
        buf.writeLong(trialDurationElapsed);
        buf.writeLong(trialDurationTotal);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (trialType == null || trialType.equals("None")) {
                PlayerClientData.trialType = "None";
            } else {
                PlayerClientData.setTrialType(trialType);
                PlayerClientData.setEliminationKills(eliminationKills);
                PlayerClientData.setEliminationKillsRequired(eliminationKillsRequired);
                PlayerClientData.setTrialDurationElapsed(trialDurationElapsed);
                PlayerClientData.setTrialDurationTotal(trialDurationTotal);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
