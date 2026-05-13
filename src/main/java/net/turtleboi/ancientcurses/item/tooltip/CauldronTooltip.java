package net.turtleboi.ancientcurses.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CauldronTooltip implements TooltipComponent {
    private final List<ItemStack> potionStacks;
    private final List<Integer> potionUses;
    private final ItemStack modifierStack;

    public CauldronTooltip(List<ItemStack> potionStacks, List<Integer> potionUses, ItemStack modifierStack) {
        this.potionStacks = potionStacks;
        this.potionUses = potionUses;
        this.modifierStack = modifierStack;
    }

    public List<ItemStack> getPotionStacks() {
        return potionStacks;
    }

    public List<Integer> getPotionUses() {
        return potionUses;
    }

    public ItemStack getModifierStack() {
        return modifierStack;
    }
}
