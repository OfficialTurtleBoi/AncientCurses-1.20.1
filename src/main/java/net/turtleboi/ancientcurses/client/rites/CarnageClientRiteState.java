package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.turtleboi.ancientcurses.rite.ModRites;

public class CarnageClientRiteState implements ClientRiteState {
    private static final String TARGET_KEY = "EliminationTarget";
    private static final String WAVE_COUNT_KEY = "WaveCount";
    private static final String KILLS_REMAINING_KEY = "KillsRemaining";
    private static final String WAVE_KILL_TOTAL_KEY = "WaveKillTotal";
    private static final String MAIN_BAR_PROGRESS_KEY = "MainBarProgress";
    private static final String SUB_BAR_PROGRESS_KEY = "SubBarProgress";
    private static final String DEGREE_PROGRESS_KEY = "DegreeProgress";

    private final boolean complete;
    private final String eliminationTarget;
    private final int waveCount;
    private final int killsRemaining;
    private final int waveKillTotal;
    private final float mainBarProgress;
    private final float subBarProgress;
    private final float degreeProgress;
    private final int totalDegrees;
    private final int requiredDegrees;
    private final int completedDegrees;
    private final int activeDegreeIndex;

    public CarnageClientRiteState(boolean complete, String eliminationTarget, int waveCount, int killsRemaining,
                                  int waveKillTotal, float mainBarProgress, float subBarProgress, float degreeProgress,
                                  int totalDegrees, int requiredDegrees, int completedDegrees, int activeDegreeIndex) {
        this.complete = complete;
        this.eliminationTarget = eliminationTarget;
        this.waveCount = waveCount;
        this.killsRemaining = killsRemaining;
        this.waveKillTotal = waveKillTotal;
        this.mainBarProgress = mainBarProgress;
        this.subBarProgress = subBarProgress;
        this.degreeProgress = degreeProgress;
        this.totalDegrees = totalDegrees;
        this.requiredDegrees = requiredDegrees;
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
                tag.getFloat(MAIN_BAR_PROGRESS_KEY),
                tag.getFloat(SUB_BAR_PROGRESS_KEY),
                tag.getFloat(DEGREE_PROGRESS_KEY),
                tag.getInt(TOTAL_DEGREES_KEY),
                tag.contains(REQUIRED_DEGREES_KEY) ? tag.getInt(REQUIRED_DEGREES_KEY) : tag.getInt(TOTAL_DEGREES_KEY),
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
        if (isComplete()) {
            return 1.0F;
        }
        return clampProgress(mainBarProgress);
    }

    @Override
    public float getDegreeDisplayProgress() {
        return clampProgress(degreeProgress);
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
    public int getRequiredDegrees() {
        return requiredDegrees;
    }

    @Override
    public int getActiveDegreeIndex() {
        return activeDegreeIndex;
    }

    public boolean shouldShowWaveDelayBar() {
        return !isComplete();
    }

    public float getWaveDelayBarProgress() {
        return clampProgress(subBarProgress);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = ClientRiteState.super.toTag();
        tag.putString(TARGET_KEY, eliminationTarget);
        tag.putInt(WAVE_COUNT_KEY, waveCount);
        tag.putInt(KILLS_REMAINING_KEY, killsRemaining);
        tag.putInt(WAVE_KILL_TOTAL_KEY, waveKillTotal);
        tag.putFloat(MAIN_BAR_PROGRESS_KEY, mainBarProgress);
        tag.putFloat(SUB_BAR_PROGRESS_KEY, subBarProgress);
        tag.putFloat(DEGREE_PROGRESS_KEY, degreeProgress);
        return tag;
    }

    private float clampProgress(float progress) {
        return Math.max(0.0F, Math.min(1.0F, progress));
    }
}
