package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;

public interface ClientRiteState {
    String getRiteId();
    boolean isComplete();
    float getProgress();
    Component getDisplayText();

    default CompoundTag toTag() {
        return new CompoundTag();
    }
}
