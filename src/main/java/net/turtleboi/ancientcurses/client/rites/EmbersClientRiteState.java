package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.turtleboi.ancientcurses.rites.ModRites;

public class EmbersClientRiteState implements ClientRiteState {
    private static final String DURATION_ELAPSED_KEY = "DurationElapsed";
    private static final String DURATION_TOTAL_KEY = "DurationTotal";

    private final boolean complete;
    private final long durationElapsed;
    private final long durationTotal;

    public EmbersClientRiteState(boolean complete, long durationElapsed, long durationTotal) {
        this.complete = complete;
        this.durationElapsed = durationElapsed;
        this.durationTotal = durationTotal;
    }

    public static EmbersClientRiteState fromTag(boolean complete, CompoundTag tag) {
        return new EmbersClientRiteState(
                complete,
                tag.getLong(DURATION_ELAPSED_KEY),
                tag.getLong(DURATION_TOTAL_KEY)
        );
    }

    @Override
    public String getRiteId() {
        return ModRites.EMBERS.toString();
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public float getProgress() {
        if (durationTotal == 0) {
            return 0.0F;
        }
        return Math.min(1.0F, (float) durationElapsed / (float) durationTotal);
    }

    @Override
    public Component getDisplayText() {
        if (isComplete()) {
            return Component.translatable("trial.ancientcurses.complete");
        }

        int percentComplete = (int) (getProgress() * 100);
        return Component.translatable("trial.ancientcurses.survival", percentComplete);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(DURATION_ELAPSED_KEY, durationElapsed);
        tag.putLong(DURATION_TOTAL_KEY, durationTotal);
        return tag;
    }
}
