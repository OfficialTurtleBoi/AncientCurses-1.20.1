package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.turtleboi.ancientcurses.rite.ModRites;

public class SacrificeClientRiteState implements ClientRiteState {
    private static final String TOTAL_HEALTH_OFFERED_KEY = "TotalHealthOffered";
    private static final String NEXT_THRESHOLD_KEY = "NextThreshold";

    private final boolean complete;
    private final int totalHealthOffered;
    private final int nextThreshold;
    private final int totalDegrees;
    private final int completedDegrees;
    private final int activeDegreeIndex;

    public SacrificeClientRiteState(boolean complete, int totalHealthOffered, int nextThreshold,
                                    int totalDegrees, int completedDegrees, int activeDegreeIndex) {
        this.complete = complete;
        this.totalHealthOffered = totalHealthOffered;
        this.nextThreshold = nextThreshold;
        this.totalDegrees = totalDegrees;
        this.completedDegrees = completedDegrees;
        this.activeDegreeIndex = activeDegreeIndex;
    }

    public static SacrificeClientRiteState fromTag(boolean complete, CompoundTag tag) {
        return new SacrificeClientRiteState(
                complete,
                tag.getInt(TOTAL_HEALTH_OFFERED_KEY),
                tag.getInt(NEXT_THRESHOLD_KEY),
                tag.getInt(TOTAL_DEGREES_KEY),
                tag.getInt(COMPLETED_DEGREES_KEY),
                tag.getInt(ACTIVE_DEGREE_INDEX_KEY)
        );
    }

    @Override
    public String getRiteId() {
        return ModRites.SACRIFICE.toString();
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public float getProgress() {
        if (nextThreshold <= 0) {
            return complete ? 1.0F : 0.0F;
        }
        return Math.min(1.0F, (float) totalHealthOffered / (float) nextThreshold);
    }

    @Override
    public Component getDisplayText() {
        if (isComplete()) {
            return Component.translatable("trial.ancientcurses.complete");
        }

        return Component.literal("Health Offered: " + totalHealthOffered + " / " + nextThreshold);
    }

    @Override
    public int getTotalDegrees() {
        return totalDegrees;
    }

    @Override
    public int getCompletedDegrees() {
        return completedDegrees;
    }

    @Override
    public int getActiveDegreeIndex() {
        return activeDegreeIndex;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = ClientRiteState.super.toTag();
        tag.putInt(TOTAL_HEALTH_OFFERED_KEY, totalHealthOffered);
        tag.putInt(NEXT_THRESHOLD_KEY, nextThreshold);
        return tag;
    }
}
