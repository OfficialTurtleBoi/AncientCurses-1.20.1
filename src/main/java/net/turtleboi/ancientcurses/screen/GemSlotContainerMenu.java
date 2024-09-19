package net.turtleboi.ancientcurses.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.turtleboi.ancientcurses.item.GoldenAmuletItem;

public class GemSlotContainerMenu extends AbstractContainerMenu {
    private final ItemStack amuletStack;
    private final ItemStackHandler gemSlots;

    public GemSlotContainerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf itemData) {
        super(ModMenuTypes.GOLDEN_AMULET_MENU.get(), containerId);
        this.amuletStack = itemData.readItem();

        if (this.amuletStack.getItem() instanceof GoldenAmuletItem) {
            this.gemSlots = ((GoldenAmuletItem) this.amuletStack.getItem()).getGemSlots();
        } else {
            throw new IllegalStateException("Expected an amulet!");
        }

        for (int i = 0; i < gemSlots.getSlots(); i++) {
            this.addSlot(new SlotItemHandler(gemSlots, i, 44 + i * 18, 20));
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 51 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 109));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return !amuletStack.isEmpty();
    }
}

