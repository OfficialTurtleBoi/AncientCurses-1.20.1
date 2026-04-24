package net.turtleboi.ancientcurses.client.rites;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.turtleboi.ancientcurses.rites.ModRites;

public class FamineClientRiteState implements ClientRiteState {
    private static final String FETCH_ITEM_KEY = "FetchItem";
    private static final String FETCH_ITEMS_KEY = "FetchItems";
    private static final String FETCH_ITEMS_REQUIRED_KEY = "FetchItemsRequired";

    private final boolean complete;
    private final String fetchItem;
    private final int fetchItems;
    private final int fetchItemsRequired;

    public FamineClientRiteState(boolean complete, String fetchItem, int fetchItems, int fetchItemsRequired) {
        this.complete = complete;
        this.fetchItem = fetchItem;
        this.fetchItems = fetchItems;
        this.fetchItemsRequired = fetchItemsRequired;
    }

    public static FamineClientRiteState fromTag(boolean complete, CompoundTag tag) {
        return new FamineClientRiteState(
                complete,
                tag.getString(FETCH_ITEM_KEY),
                tag.getInt(FETCH_ITEMS_KEY),
                tag.getInt(FETCH_ITEMS_REQUIRED_KEY)
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
        if (fetchItemsRequired == 0) {
            return 0.0F;
        }
        return Math.min(1.0F, (float) fetchItems / (float) fetchItemsRequired);
    }

    @Override
    public Component getDisplayText() {
        if (isComplete()) {
            return Component.translatable("trial.ancientcurses.complete");
        }

        return Component.translatable("trial.ancientcurses.fetch", fetchItems, fetchItemsRequired, fetchItem);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString(FETCH_ITEM_KEY, fetchItem);
        tag.putInt(FETCH_ITEMS_KEY, fetchItems);
        tag.putInt(FETCH_ITEMS_REQUIRED_KEY, fetchItemsRequired);
        return tag;
    }
}
