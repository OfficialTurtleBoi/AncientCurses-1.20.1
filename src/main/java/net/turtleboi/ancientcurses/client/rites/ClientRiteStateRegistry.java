package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.nbt.CompoundTag;
import net.turtleboi.ancientcurses.rite.ModRites;

import java.util.HashMap;
import java.util.Map;

public final class ClientRiteStateRegistry {
    private static final Map<String, Decoder> DECODERS = new HashMap<>();

    static {
        register(ModRites.CARNAGE.toString(), CarnageClientRiteState::fromTag);
        register(ModRites.EMBERS.toString(), EmbersClientRiteState::fromTag);
        register(ModRites.FAMINE.toString(), FamineClientRiteState::fromTag);
        register(ModRites.SACRIFICE.toString(), SacrificeClientRiteState::fromTag);
    }

    private ClientRiteStateRegistry() {
    }

    public static void register(String riteId, Decoder decoder) {
        DECODERS.put(riteId, decoder);
    }

    public static ClientRiteState decode(String riteIdString, boolean complete, CompoundTag tag) {
        if (riteIdString == null || riteIdString.equals("None")) {
            return NoRiteState.INSTANCE;
        }

        net.minecraft.resources.ResourceLocation riteId = ModRites.parse(riteIdString);
        if (riteId == null) {
            return NoRiteState.INSTANCE;
        }

        String normalizedRiteId = riteId.toString();
        Decoder decoder = DECODERS.get(normalizedRiteId);
        if (decoder == null) {
            return NoRiteState.INSTANCE;
        }

        return decoder.decode(complete, tag == null ? new CompoundTag() : tag);
    }

    @FunctionalInterface
    public interface Decoder {
        ClientRiteState decode(boolean complete, CompoundTag tag);
    }
}
