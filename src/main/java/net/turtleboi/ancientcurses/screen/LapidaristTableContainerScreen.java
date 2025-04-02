package net.turtleboi.ancientcurses.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.api.ISlotAccessor;
import net.turtleboi.ancientcurses.block.entity.LapidaristTableBlockEntity;

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

        if (!mainSlotEmpty()) {
            renderSlots(pGuiGraphics, x, y);
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    private boolean mainSlotEmpty(){
        return menu.blockEntity.getInventory().getStackInSlot(LapidaristTableBlockEntity.socketableItemSlot).isEmpty();
    }

    private void renderSlots(GuiGraphics guiGraphics, int x, int y) {
        int socketCount = LapidaristTableContainerMenu.getSocketSlotCount(
                menu.blockEntity.getInventory().getStackInSlot(LapidaristTableBlockEntity.socketableItemSlot));
        if (socketCount == 6) {
            guiGraphics.blit(TEXTURE, x + 79, y + 8, 176, 0, 18, 18);
            guiGraphics.blit(TEXTURE, x + 52, y + 28, 176, 0, 18, 18);
            guiGraphics.blit(TEXTURE, x + 106, y + 28, 176, 0, 18, 18);
            guiGraphics.blit(TEXTURE, x + 61, y + 58, 176, 0, 18, 18);
            guiGraphics.blit(TEXTURE, x + 97, y + 58, 176, 0, 18, 18);

            guiGraphics.blit(TEXTURE, x + 76, y + 30, 176, 18, 24, 24);
        } else if (socketCount == 1) {
            guiGraphics.blit(TEXTURE, x + 79, y + 31, 176, 0, 18, 18);
        } else if (socketCount == 2) {
            guiGraphics.blit(TEXTURE, x + 66, y + 31, 176, 0, 18, 18);
            guiGraphics.blit(TEXTURE, x + 92, y + 31, 176, 0, 18, 18);
        } else if (socketCount == 3) {
            guiGraphics.blit(TEXTURE, x + 79, y + 19, 176, 0, 18, 18);
            guiGraphics.blit(TEXTURE, x + 66, y + 44, 176, 0, 18, 18);
            guiGraphics.blit(TEXTURE, x + 92, y + 44, 176, 0, 18, 18);
        } else if (socketCount == 4) {
            guiGraphics.blit(TEXTURE, x + 66, y + 19, 176, 0, 18, 18);
            guiGraphics.blit(TEXTURE, x + 92, y + 19, 176, 0, 18, 18);
            guiGraphics.blit(TEXTURE, x + 66, y + 44, 176, 0, 18, 18);
            guiGraphics.blit(TEXTURE, x + 92, y + 44, 176, 0, 18, 18);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        ItemStack socketableItem = menu.blockEntity.getInventory().getStackInSlot(LapidaristTableBlockEntity.socketableItemSlot);
        if (socketableItem.isEmpty() || !socketableItem.hasTag() || !socketableItem.getTag().getBoolean("Socketable")) {
            return;
        }

        int socketCount = LapidaristTableContainerMenu.getSocketSlotCount(socketableItem);

        Slot ancientGemSlot = menu.getSlot(37);
        Slot minorSlot1 = menu.getSlot(38);
        Slot minorSlot2 = menu.getSlot(39);
        Slot minorSlot3 = menu.getSlot(40);
        Slot minorSlot4 = menu.getSlot(41);
        Slot minorSlot5 = menu.getSlot(42);

        int offScreen = 10000;

        if (socketCount == 6) {
            setSlotPosition(ancientGemSlot, 80, 34);
            setSlotPosition(minorSlot1, 80, 9);
            setSlotPosition(minorSlot2, 53, 29);
            setSlotPosition(minorSlot3, 107, 29);
            setSlotPosition(minorSlot4, 62, 59);
            setSlotPosition(minorSlot5, 98, 59);
        } else if (socketCount == 1) {
            setSlotPosition(ancientGemSlot, offScreen, offScreen);
            setSlotPosition(minorSlot1, 80, 32); // Slot 1
            setSlotPosition(minorSlot2, 26, 0);
            setSlotPosition(minorSlot3, 52, 0);
            setSlotPosition(minorSlot4, 78, 0);
            setSlotPosition(minorSlot5, 104, 0);
        } else if (socketCount == 2) {
            setSlotPosition(ancientGemSlot, offScreen, offScreen);
            setSlotPosition(minorSlot1, 67, 32); // Slot 1
            setSlotPosition(minorSlot2, 93, 32); // Slot 2
            setSlotPosition(minorSlot3, offScreen, offScreen);
            setSlotPosition(minorSlot4, offScreen, offScreen);
            setSlotPosition(minorSlot5, offScreen, offScreen);
        } else if (socketCount == 3) {
            setSlotPosition(ancientGemSlot, offScreen, offScreen);
            setSlotPosition(minorSlot1, 80, 20); // Slot 1
            setSlotPosition(minorSlot2, 67, 44); // Slot 2
            setSlotPosition(minorSlot3, 93, 44); // Slot 3
            setSlotPosition(minorSlot4, offScreen, offScreen);
            setSlotPosition(minorSlot5, offScreen, offScreen);
        } else if (socketCount == 4) {
            setSlotPosition(ancientGemSlot, offScreen, offScreen);
            setSlotPosition(minorSlot1, 67, 20); // Slot 1
            setSlotPosition(minorSlot2, 93, 20); // Slot 2
            setSlotPosition(minorSlot3, 67, 44); // Slot 3
            setSlotPosition(minorSlot4, 93, 44); // Slot 4
            setSlotPosition(minorSlot5, offScreen, offScreen);
        } else {
            setSlotPosition(ancientGemSlot, offScreen, offScreen);
            setSlotPosition(minorSlot1, offScreen, offScreen);
            setSlotPosition(minorSlot2, offScreen, offScreen);
            setSlotPosition(minorSlot3, offScreen, offScreen);
            setSlotPosition(minorSlot4, offScreen, offScreen);
            setSlotPosition(minorSlot5, offScreen, offScreen);
        }
        menu.broadcastFullState();
    }

    public static void setSlotPosition(Slot slot, int x, int y) {
        ISlotAccessor accessor = (ISlotAccessor) slot;
        if (accessor.getX() != x || accessor.getY() != y) {
            accessor.setSlotPosition(x, y);
        }
    }

}
