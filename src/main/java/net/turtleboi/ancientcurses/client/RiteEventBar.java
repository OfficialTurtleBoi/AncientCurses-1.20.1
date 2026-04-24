package net.turtleboi.ancientcurses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.turtleboi.ancientcurses.AncientCurses;

public class RiteEventBar {
    private static final ResourceLocation RITE_BOSS_BAR =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/gui/quest_bar.png");

    public static void render(GuiGraphics guiGraphics, int x, int y, Minecraft minecraft) {
        if (minecraft.player == null) return;

        if (!PlayerClientData.hasRite()) {
            System.out.println(Component.literal("Removing bar because rite is null"));
            return;
        }

        RenderSystem.setShaderTexture(0, RITE_BOSS_BAR);

        int barWidth = 192;
        int barHeight = 9;
        int barBuffer = 5;

        if (minecraft.player == null) return;
        int progressWidth = getProgress(barWidth, barBuffer);
        guiGraphics.blit(RITE_BOSS_BAR, x, y, 0, 0, barWidth, barHeight, barWidth, barHeight * 3);

        if (progressWidth > 0) {
            guiGraphics.blit(RITE_BOSS_BAR, x + barBuffer, y, barBuffer, 9, progressWidth, barHeight, barWidth, barHeight * 3);
        }

        if (PlayerClientData.isRiteComplete()) {
            guiGraphics.blit(RITE_BOSS_BAR, x + barBuffer, y, barBuffer, 18, barWidth - (barBuffer * 2), barHeight, barWidth, barHeight * 3);
        }

        Component displayText = getTrialDisplayText();
        int titleWidth = minecraft.font.width(displayText);
        int color = 0xFFFFFF;
        if (PlayerClientData.isRiteComplete()){
            color = 0xFFAA00;
        }
        guiGraphics.drawString(minecraft.font, displayText, x + (barWidth / 2) - (titleWidth / 2), y - 9, color);
    }

    private static int getProgress(int barWidth, int barBuffer) {
        float trialProgress = PlayerClientData.getRiteProgress();
        return Mth.ceil(trialProgress * (barWidth - (barBuffer * 2)));
    }

    private static Component getTrialDisplayText() {
        return PlayerClientData.getActiveRiteState().getDisplayText();
    }

}
