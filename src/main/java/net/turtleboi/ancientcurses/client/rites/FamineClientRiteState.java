package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.turtleboi.ancientcurses.rite.ModRites;

public class FamineClientRiteState implements ClientRiteState {
    private static final String FETCH_ITEM_KEY = "FetchItem";
    private static final String FETCH_ITEMS_KEY = "FetchItems";
    private static final String FETCH_ITEMS_REQUIRED_KEY = "FetchItemsRequired";
    private final boolean complete;
    private final String fetchItem;
    private final int fetchItems;
    private final int fetchItemsRequired;
    private final int totalDegrees;
    private final int completedDegrees;
    private final int activeDegreeIndex;
    private final long createdAtMillis;

    public FamineClientRiteState(boolean complete, String fetchItem, int fetchItems, int fetchItemsRequired,
                                 int totalDegrees, int completedDegrees, int activeDegreeIndex) {
        this.complete = complete;
        this.fetchItem = fetchItem;
        this.fetchItems = fetchItems;
        this.fetchItemsRequired = fetchItemsRequired;
        this.totalDegrees = totalDegrees;
        this.completedDegrees = completedDegrees;
        this.activeDegreeIndex = activeDegreeIndex;
        this.createdAtMillis = System.currentTimeMillis();
    }

    public static FamineClientRiteState fromTag(boolean complete, CompoundTag tag) {
        return new FamineClientRiteState(
                complete,
                tag.getString(FETCH_ITEM_KEY),
                tag.getInt(FETCH_ITEMS_KEY),
                tag.getInt(FETCH_ITEMS_REQUIRED_KEY),
                tag.getInt(TOTAL_DEGREES_KEY),
                tag.getInt(COMPLETED_DEGREES_KEY),
                tag.getInt(ACTIVE_DEGREE_INDEX_KEY)
        );
    }

    @Override
    public String getRiteId() {
        return ModRites.FAMINE.toString();
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
        if (fetchItemsRequired == 0) {
            return 0.0F;
        }

        if (shouldAnimateRequirementReveal()) {
            long elapsedMillis = getElapsedMillis();
            if (elapsedMillis < PRE_START_BAR_FILL_MILLIS) {
                return Math.min(1.0F, (float) elapsedMillis / (float) PRE_START_BAR_FILL_MILLIS);
            }

            int displayedRequired = getDisplayedRequiredCount();
            if (displayedRequired <= 0) {
                return 1.0F;
            }
            return Math.max(0.0F, 1.0F - ((float) displayedRequired / (float) fetchItemsRequired));
        }

        return Math.min(1.0F, (float) fetchItems / (float) fetchItemsRequired);
    }

    @Override
    public Component getDisplayText() {
        if (isComplete()) {
            return Component.translatable("trial.ancientcurses.complete");
        }

        if (shouldAnimateRequirementReveal()) {
            long elapsedMillis = getElapsedMillis();
            if (elapsedMillis < PRE_START_BAR_FILL_MILLIS) {
                return Component.translatable("trial.ancientcurses.fetch", "~", "~", fetchItem);
            }

            int displayedRequired = getDisplayedRequiredCount();
            String requiredDisplay = displayedRequired <= 0 ? "~" : String.valueOf(displayedRequired);
            return Component.translatable("trial.ancientcurses.fetch", String.valueOf(fetchItems), requiredDisplay, fetchItem);
        }

        return Component.translatable("trial.ancientcurses.fetch", fetchItems, fetchItemsRequired, fetchItem);
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
        tag.putString(FETCH_ITEM_KEY, fetchItem);
        tag.putInt(FETCH_ITEMS_KEY, fetchItems);
        tag.putInt(FETCH_ITEMS_REQUIRED_KEY, fetchItemsRequired);
        return tag;
    }

    private boolean shouldAnimateRequirementReveal() {
        return !complete && fetchItems == 0 && fetchItemsRequired > 0;
    }

    private long getElapsedMillis() {
        return Math.max(0L, System.currentTimeMillis() - createdAtMillis);
    }

    private int getDisplayedRequiredCount() {
        if (!shouldAnimateRequirementReveal()) {
            return fetchItemsRequired;
        }

        long revealElapsedMillis = Math.max(0L, getElapsedMillis() - PRE_START_BAR_FILL_MILLIS);
        float revealProgress = Mth.clamp((float) revealElapsedMillis / (float) PRE_START_BAR_REVEAL_MILLIS, 0.0F, 1.0F);
        return Mth.ceil(fetchItemsRequired * revealProgress);
    }
}
