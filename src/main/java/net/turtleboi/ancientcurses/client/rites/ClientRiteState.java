package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;

public interface ClientRiteState {
    String TOTAL_DEGREES_KEY = "TotalDegrees";
    String COMPLETED_DEGREES_KEY = "CompletedDegrees";
    String ACTIVE_DEGREE_INDEX_KEY = "ActiveDegreeIndex";

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

    default CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TOTAL_DEGREES_KEY, getTotalDegrees());
        tag.putInt(COMPLETED_DEGREES_KEY, getCompletedDegrees());
        tag.putInt(ACTIVE_DEGREE_INDEX_KEY, getActiveDegreeIndex());
        return tag;
    }
}
