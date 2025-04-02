package net.turtleboi.ancientcurses.screen;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.turtleboi.ancientcurses.block.entity.LapidaristTableBlockEntity;
import net.turtleboi.ancientcurses.item.items.PreciousGemItem;
import net.turtleboi.ancientcurses.util.ModTags;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class PreciousGemSlot extends SlotItemHandler {
    private final LapidaristTableBlockEntity blockEntity;
    private final boolean ancientSlot;

    public PreciousGemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, LapidaristTableBlockEntity blockEntity,
                           boolean ancientSlot) {
        super(itemHandler, index, xPosition, yPosition);
        this.blockEntity = blockEntity;
        this.ancientSlot = ancientSlot;
    }

    private boolean slotActive() {
        ItemStack socketableItem = blockEntity.getInventory().getStackInSlot(LapidaristTableBlockEntity.socketableItemSlot);
        if (socketableItem.isEmpty() || !socketableItem.hasTag() || !socketableItem.getTag().getBoolean("Socketable")) {
            return true;
        }

        int socketCount = LapidaristTableContainerMenu.getSocketSlotCount(socketableItem);
        int slotIndex = this.getSlotIndex();

        return !switch (socketCount) {
            case 6 -> (slotIndex >= 1 && slotIndex <= 6);
            case 1 -> (slotIndex == 2);
            case 2 -> (slotIndex == 2 || slotIndex == 3);
            case 3 -> (slotIndex >= 2 && slotIndex <= 4);
            case 4 -> (slotIndex >= 2 && slotIndex <= 5);
            default -> false;
        };
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (slotActive()) {
            return false;
        }

        if (ancientSlot) {
            return isSocketableItemPresent() && super.mayPlace(stack) && stack.getItem() instanceof PreciousGemItem && !stack.is(ModTags.Items.MINOR_GEMS);
        } else {
            return isSocketableItemPresent() && super.mayPlace(stack) && stack.getItem() instanceof PreciousGemItem && !stack.is(ModTags.Items.ANCIENT_GEMS);
        }
    }

    @Override
    public boolean mayPickup(Player player) {
        if (slotActive()) {
            return false;
        }

        return isSocketableItemPresent() && super.mayPickup(player);
    }

    @Override
    public boolean isHighlightable() {
        if (slotActive()) {
            return false;
        }

        return isSocketableItemPresent();
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        super.set(stack);
        onSlotChanged();
    }

    @Override
    public void onQuickCraft(ItemStack oldStack, ItemStack newStack) {
        super.onQuickCraft(oldStack, newStack);
        onSlotChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onSlotChanged();
    }

    private void onSlotChanged() {
        blockEntity.onSlotChanged();
    }

    public boolean isSocketableItemPresent() {
        ItemStack socketableItem = blockEntity.getInventory().getStackInSlot(LapidaristTableBlockEntity.socketableItemSlot);
        return !socketableItem.isEmpty() && socketableItem.hasTag() && socketableItem.getTag().getBoolean("Socketable");
    }
}
