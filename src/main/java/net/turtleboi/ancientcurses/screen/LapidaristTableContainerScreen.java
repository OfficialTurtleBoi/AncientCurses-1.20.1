package net.turtleboi.ancientcurses.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.turtleboi.ancientcurses.AncientCurses;

public class LapidaristTableContainerScreen extends AbstractContainerScreen<LapidaristTableContainerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/gui/lapidarist_table_gui.png");

    public LapidaristTableContainerScreen(LapidaristTableContainerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - 176) / 2;
        int y = (height - 166) / 2;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, 176, 166);


        //pGuiGraphics.blit(TEXTURE, x + 79, y + 8, 176, 0, 18, 18);
        //pGuiGraphics.blit(TEXTURE, x + 52, y + 28, 176, 0, 18, 18);
        //pGuiGraphics.blit(TEXTURE, x + 106, y + 28, 176, 0, 18, 18);
        //pGuiGraphics.blit(TEXTURE, x + 61, y + 58, 176, 0, 18, 18);
        //pGuiGraphics.blit(TEXTURE, x + 97, y + 58, 176, 0, 18, 18);
//
        //pGuiGraphics.blit(TEXTURE, x + 76, y + 30, 176, 18, 24, 24);

        int activeGemSlotCount = menu.data.get(0);

        for (int i = 0; i < activeGemSlotCount && i < menu.getAllGemSlots().size(); i++) {
            PreciousGemSlot slot = menu.getAllGemSlots().get(i);
            int slotX = x + slot.x - 1;
            int slotY = y + slot.y - 1;
            pGuiGraphics.blit(TEXTURE, slotX, slotY, 176, 0, 18, 18);
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
