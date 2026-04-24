package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.turtleboi.ancientcurses.rite.ModRites;

public class CarnageClientRiteState implements ClientRiteState {
    private static final String TARGET_KEY = "EliminationTarget";
    private static final String WAVE_COUNT_KEY = "WaveCount";
    private static final String KILLS_REMAINING_KEY = "KillsRemaining";
    private static final String WAVE_KILL_TOTAL_KEY = "WaveKillTotal";
    private static final String DURATION_ELAPSED_KEY = "DurationElapsed";
    private static final String DURATION_TOTAL_KEY = "DurationTotal";

    private final boolean complete;
    private final String eliminationTarget;
    private final int waveCount;
    private final int killsRemaining;
    private final int waveKillTotal;
    private final long durationElapsed;
    private final long durationTotal;
    private final int totalDegrees;
    private final int completedDegrees;
    private final int activeDegreeIndex;

    public CarnageClientRiteState(boolean complete, String eliminationTarget, int waveCount, int killsRemaining,
                                  int waveKillTotal, long durationElapsed, long durationTotal,
                                  int totalDegrees, int completedDegrees, int activeDegreeIndex) {
        this.complete = complete;
        this.eliminationTarget = eliminationTarget;
        this.waveCount = waveCount;
        this.killsRemaining = killsRemaining;
        this.waveKillTotal = waveKillTotal;
        this.durationElapsed = durationElapsed;
        this.durationTotal = durationTotal;
        this.totalDegrees = totalDegrees;
        this.completedDegrees = completedDegrees;
        this.activeDegreeIndex = activeDegreeIndex;
    }

    public static CarnageClientRiteState fromTag(boolean complete, CompoundTag tag) {
        return new CarnageClientRiteState(
                complete,
                tag.getString(TARGET_KEY),
                tag.getInt(WAVE_COUNT_KEY),
                tag.getInt(KILLS_REMAINING_KEY),
                tag.getInt(WAVE_KILL_TOTAL_KEY),
                tag.getLong(DURATION_ELAPSED_KEY),
                tag.getLong(DURATION_TOTAL_KEY),
                tag.getInt(TOTAL_DEGREES_KEY),
                tag.getInt(COMPLETED_DEGREES_KEY),
                tag.getInt(ACTIVE_DEGREE_INDEX_KEY)
        );
    }

    @Override
    public String getRiteId() {
        return ModRites.CARNAGE.toString();
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public float getProgress() {
        if (durationElapsed < durationTotal && durationElapsed != 0 && durationTotal != 0) {
            return Math.min(1.0F, 1.0F - ((float) durationElapsed / (float) durationTotal));
        }
        if (waveKillTotal <= 0) {
            return 0.0F;
        }
        return Math.min(1.0F, (float) killsRemaining / (float) waveKillTotal);
    }

    @Override
    public Component getDisplayText() {
        if (isComplete()) {
            return Component.translatable("trial.ancientcurses.complete");
        }

        String displayWaveCount = waveCount == 0 ? "~" : String.valueOf(waveCount);
        String displayKillsRemaining = killsRemaining == 0 ? "~" : String.valueOf(killsRemaining);
        return Component.translatable("trial.ancientcurses.elimination", eliminationTarget, displayWaveCount, displayKillsRemaining);
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
        tag.putString(TARGET_KEY, eliminationTarget);
        tag.putInt(WAVE_COUNT_KEY, waveCount);
        tag.putInt(KILLS_REMAINING_KEY, killsRemaining);
        tag.putInt(WAVE_KILL_TOTAL_KEY, waveKillTotal);
        tag.putLong(DURATION_ELAPSED_KEY, durationElapsed);
        tag.putLong(DURATION_TOTAL_KEY, durationTotal);
        return tag;
    }
}
