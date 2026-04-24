package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.network.chat.Component;

public final class NoRiteState implements ClientRiteState {
    public static final NoRiteState INSTANCE = new NoRiteState();

    private NoRiteState() {
    }

    @Override
    public String getRiteId() {
        return "None";
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public float getProgress() {
        return 0.0F;
    }

    @Override
    public Component getDisplayText() {
        return Component.empty();
    }
}
