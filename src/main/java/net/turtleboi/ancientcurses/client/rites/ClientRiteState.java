package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;

public interface ClientRiteState {
    String TOTAL_DEGREES_KEY = "TotalDegrees";
    String COMPLETED_DEGREES_KEY = "CompletedDegrees";
    String ACTIVE_DEGREE_INDEX_KEY = "ActiveDegreeIndex";
    String REQUIRED_DEGREES_KEY = "RequiredDegrees";
    long PRE_START_BAR_TOTAL_MILLIS = 10_000L;
    long PRE_START_BAR_FILL_MILLIS = 4_000L;
    long PRE_START_BAR_REVEAL_MILLIS = PRE_START_BAR_TOTAL_MILLIS - PRE_START_BAR_FILL_MILLIS;
    long PRE_START_BAR_TOTAL_TICKS = PRE_START_BAR_TOTAL_MILLIS / 50L;

    String getRiteId();
    boolean isComplete();
    float getProgress();
    Component getDisplayText();

    default int getTotalDegrees() {
        return 0;
    }

    default int getCompletedDegrees() {
        return 0;
    }

    default int getActiveDegreeIndex() {
        return -1;
    }

    default int getRequiredDegrees() {
        return Math.min(1, getTotalDegrees());
    }

    default CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TOTAL_DEGREES_KEY, getTotalDegrees());
        tag.putInt(COMPLETED_DEGREES_KEY, getCompletedDegrees());
        tag.putInt(ACTIVE_DEGREE_INDEX_KEY, getActiveDegreeIndex());
        tag.putInt(REQUIRED_DEGREES_KEY, getRequiredDegrees());
        return tag;
    }
}
