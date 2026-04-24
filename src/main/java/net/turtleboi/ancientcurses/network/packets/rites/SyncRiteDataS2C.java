package net.turtleboi.ancientcurses.network.packets.rites;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.network.NetworkEvent;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.client.rites.ClientRiteState;
import net.turtleboi.ancientcurses.client.rites.ClientRiteStateRegistry;
import net.turtleboi.ancientcurses.client.rites.NoRiteState;
import net.turtleboi.ancientcurses.rite.ModRites;

import java.util.function.Supplier;

public class SyncRiteDataS2C {
    private static final String NONE_RITE_ID = "None";
    private final String riteId;
    private final boolean riteComplete;
    private final CompoundTag payload;


    public SyncRiteDataS2C(String riteId, boolean riteComplete, CompoundTag payload) {
        this.riteId = normalizeRiteId(riteId);
        this.riteComplete = riteComplete;
        this.payload = payload == null ? new CompoundTag() : payload.copy();
    }

    public SyncRiteDataS2C(FriendlyByteBuf buf) {
        this.riteId = normalizeRiteId(buf.readUtf());
        this.riteComplete = buf.readBoolean();
        CompoundTag readPayload = buf.readNbt();
        this.payload = readPayload == null ? new CompoundTag() : readPayload;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(riteId);
        buf.writeBoolean(riteComplete);
        buf.writeNbt(payload);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (riteId == null || riteId.equals(NONE_RITE_ID)) {
                PlayerClientData.setActiveRiteState(NoRiteState.INSTANCE);
            } else {
                ClientRiteState riteState = ClientRiteStateRegistry.decode(riteId, riteComplete, payload);
                PlayerClientData.setActiveRiteState(riteState);
            }
        });
        context.setPacketHandled(true);
        return true;
    }

    public static SyncRiteDataS2C none() {
        return new SyncRiteDataS2C(NONE_RITE_ID, false, new CompoundTag());
    }

    public static SyncRiteDataS2C fromState(ClientRiteState riteState) {
        if (riteState == null || riteState instanceof NoRiteState) {
            return none();
        }
        return new SyncRiteDataS2C(riteState.getRiteId(), riteState.isComplete(), riteState.toTag());
    }

    private static String normalizeRiteId(String riteIdString) {
        if (riteIdString == null || riteIdString.isBlank() || riteIdString.equals(NONE_RITE_ID)) {
            return NONE_RITE_ID;
        }

        net.minecraft.resources.ResourceLocation riteId = ModRites.parse(riteIdString);
        return riteId != null ? riteId.toString() : NONE_RITE_ID;
    }
}
