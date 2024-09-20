package net.turtleboi.ancientcurses.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.turtleboi.ancientcurses.util.ModTags;
import org.jetbrains.annotations.NotNull;

public class GoldenAmuletItem extends Item {
    private final int maxSlots = 6;
    private final ItemStackHandler gemSlots;

    public GoldenAmuletItem(Properties properties) {
        super(properties);
        this.gemSlots = new ItemStackHandler(maxSlots) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return stack.is(ModTags.Items.PRECIOUS_GEMS);
            }
        };
    }

    public ItemStackHandler getGemSlots() {
        return gemSlots;
    }
}

