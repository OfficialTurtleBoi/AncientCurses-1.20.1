package net.turtleboi.ancientcurses.item.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.turtleboi.ancientcurses.item.items.FathomlessCauldronItem;

@OnlyIn(Dist.CLIENT)
public class CauldronTooltipComponent implements ClientTooltipComponent {
    private static final ResourceLocation INVENTORY_TEXTURE =
            new ResourceLocation("textures/gui/container/inventory.png");
    private static final int SLOT_SIZE = 18;
    private static final int MODIFIER_GAP = 4;
    private static final int BAR_HEIGHT = 2;
    private static final int TOTAL_HEIGHT = SLOT_SIZE + 1 + BAR_HEIGHT;

    private final CauldronTooltip tooltip;

    public CauldronTooltipComponent(CauldronTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getWidth(Font font) {
        return FathomlessCauldronItem.MAX_POTION_SLOTS * SLOT_SIZE + MODIFIER_GAP + SLOT_SIZE;
    }

    @Override
    public int getHeight() {
        return TOTAL_HEIGHT;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        for (int slotIndex = 0; slotIndex < FathomlessCauldronItem.MAX_POTION_SLOTS; slotIndex++) {
            int slotX = x + slotIndex * SLOT_SIZE;
            ItemStack potionStack = tooltip.getPotionStacks().get(slotIndex);
            int uses = tooltip.getPotionUses().get(slotIndex);
            renderPotionSlot(font, graphics, slotX, y, potionStack, uses);
        }

        int modifierSlotX = x + FathomlessCauldronItem.MAX_POTION_SLOTS * SLOT_SIZE + MODIFIER_GAP;
        renderModifierSlot(font, graphics, modifierSlotX, y, tooltip.getModifierStack());
    }

    private void renderPotionSlot(Font font, GuiGraphics graphics, int slotX, int slotY, ItemStack stack, int uses) {
        renderSlotBackground(graphics, slotX, slotY);

        if (!stack.isEmpty()) {
            graphics.renderItem(stack, slotX + 1, slotY + 1);
            renderUsesBar(graphics, slotX, slotY, uses);
        }
    }

    private void renderModifierSlot(Font font, GuiGraphics graphics, int slotX, int slotY, ItemStack stack) {
        renderSlotBackground(graphics, slotX, slotY);

        if (!stack.isEmpty()) {
            graphics.renderItem(stack, slotX + 1, slotY + 1);
            graphics.renderItemDecorations(font, stack, slotX + 1, slotY + 1);
        }
    }

    private void renderSlotBackground(GuiGraphics graphics, int slotX, int slotY) {
        graphics.blit(INVENTORY_TEXTURE, slotX, slotY, 7, 83, SLOT_SIZE, SLOT_SIZE);
    }

    private void renderUsesBar(GuiGraphics graphics, int slotX, int slotY, int uses) {
        int barX = slotX + 1;
        int barY = slotY + SLOT_SIZE + 1;
        int barMaxWidth = SLOT_SIZE - 2;

        graphics.fill(barX, barY, barX + barMaxWidth, barY + BAR_HEIGHT, 0xFF000000);

        float fillFraction = (float) uses / FathomlessCauldronItem.USES_PER_POTION;
        int filledWidth = Math.round(fillFraction * barMaxWidth);

        if (filledWidth > 0) {
            int red   = Math.round((1.0F - fillFraction) * 255F);
            int green = Math.round(fillFraction * 255F);
            int barColor = 0xFF000000 | (red << 16) | (green << 8);
            graphics.fill(barX, barY, barX + filledWidth, barY + 1, barColor);
        }
    }
}
