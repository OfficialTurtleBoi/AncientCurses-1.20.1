package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.turtleboi.ancientcurses.rite.ModRites;

public class EmbersClientRiteState implements ClientRiteState {
    private static final String DURATION_ELAPSED_KEY = "DurationElapsed";
    private static final String DURATION_TOTAL_KEY = "DurationTotal";
    private static final String ACTIVE_NODE_PROGRESS_KEY = "ActiveNodeProgress";
    private static final String SHOW_NODE_PROGRESS_KEY = "ShowNodeProgress";
    private static final long COUNTDOWN_REVEAL_TICKS = 80L;
    private final boolean complete;
    private final long durationElapsed;
    private final long durationTotal;
    private final float activeNodeProgress;
    private final boolean showNodeProgress;
    private final int totalDegrees;
    private final int completedDegrees;
    private final int activeDegreeIndex;

    public EmbersClientRiteState(boolean complete, long durationElapsed, long durationTotal,
                                 float activeNodeProgress, boolean showNodeProgress,
                                 int totalDegrees, int completedDegrees, int activeDegreeIndex) {
        this.complete = complete;
        this.durationElapsed = durationElapsed;
        this.durationTotal = durationTotal;
        this.activeNodeProgress = activeNodeProgress;
        this.showNodeProgress = showNodeProgress;
        this.totalDegrees = totalDegrees;
        this.completedDegrees = completedDegrees;
        this.activeDegreeIndex = activeDegreeIndex;
    }

    public static EmbersClientRiteState fromTag(boolean complete, CompoundTag tag) {
        return new EmbersClientRiteState(
                complete,
                tag.getLong(DURATION_ELAPSED_KEY),
                tag.getLong(DURATION_TOTAL_KEY),
                tag.getFloat(ACTIVE_NODE_PROGRESS_KEY),
                tag.getBoolean(SHOW_NODE_PROGRESS_KEY),
                tag.getInt(TOTAL_DEGREES_KEY),
                tag.getInt(COMPLETED_DEGREES_KEY),
                tag.getInt(ACTIVE_DEGREE_INDEX_KEY)
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
        if (isComplete()) {
            return 1.0F;
        }
        if (durationTotal == 0) {
            return 0.0F;
        }

        if (durationElapsed < PRE_START_BAR_TOTAL_TICKS) {
            return Math.min(1.0F, (float) durationElapsed / (float) PRE_START_BAR_TOTAL_TICKS);
        }

        long remainingTicks = Math.max(0L, durationTotal - getCountdownElapsedTicks());
        return Math.min(1.0F, (float) remainingTicks / (float) durationTotal);
    }

    @Override
    public Component getDisplayText() {
        if (isComplete()) {
            return Component.translatable("trial.ancientcurses.complete");
        }

        return Component.literal("Feed the souls: " + getDisplayedCountdown());
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

    public float getActiveNodeProgress() {
        return activeNodeProgress;
    }

    public boolean shouldShowNodeProgress() {
        return showNodeProgress;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = ClientRiteState.super.toTag();
        tag.putLong(DURATION_ELAPSED_KEY, durationElapsed);
        tag.putLong(DURATION_TOTAL_KEY, durationTotal);
        tag.putFloat(ACTIVE_NODE_PROGRESS_KEY, activeNodeProgress);
        tag.putBoolean(SHOW_NODE_PROGRESS_KEY, showNodeProgress);
        return tag;
    }

    private static String formatRemainingTime(long remainingTicks) {
        long clampedTicks = Math.max(0L, remainingTicks);
        long totalSeconds = (clampedTicks + 19L) / 20L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private long getCountdownElapsedTicks() {
        return Math.max(0L, durationElapsed - PRE_START_BAR_TOTAL_TICKS);
    }

    private String getDisplayedCountdown() {
        String fullCountdown = formatRemainingTime(durationTotal);
        if (durationElapsed < PRE_START_BAR_TOTAL_TICKS) {
            return revealCountdownFromLeft(fullCountdown);
        }

        return formatRemainingTime(durationTotal - getCountdownElapsedTicks());
    }

    private String revealCountdownFromLeft(String fullCountdown) {
        StringBuilder builder = new StringBuilder("~~:~~");
        long revealStartTick = Math.max(0L, PRE_START_BAR_TOTAL_TICKS - COUNTDOWN_REVEAL_TICKS);
        if (durationElapsed < revealStartTick) {
            return builder.toString();
        }

        float revealProgress = Math.min(1.0F, (float) (durationElapsed - revealStartTick) / (float) COUNTDOWN_REVEAL_TICKS);
        int digitsToReveal = Math.min(4, (int) Math.floor(revealProgress * 5.0F));
        int[] revealPositions = {0, 1, 3, 4};
        for (int i = 0; i < digitsToReveal; i++) {
            int position = revealPositions[i];
            builder.setCharAt(position, fullCountdown.charAt(position));
        }
        return builder.toString();
    }
}
