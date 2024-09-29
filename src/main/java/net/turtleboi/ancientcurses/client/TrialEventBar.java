package net.turtleboi.ancientcurses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.trials.PlayerTrialData;

import java.awt.*;

public class TrialEventBar {
    private static final ResourceLocation TRIAL_BOSS_BAR =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/gui/quest_bar.png");

    public static void render(GuiGraphics guiGraphics, int x, int y, Minecraft minecraft) {
        if (minecraft.player == null) return;

        String trialType = PlayerClientData.getTrialType();
        if (trialType == null || trialType.equalsIgnoreCase("None")) {
            System.out.println(Component.literal("Removing bar because trial is null"));
            return;
        }

        RenderSystem.setShaderTexture(0, TRIAL_BOSS_BAR);

        int barWidth = 192;
        int barHeight = 9;
        int barBuffer = 5;

        if (minecraft.player == null) return;
        int progressWidth = getProgress(barWidth, barBuffer);
        guiGraphics.blit(TRIAL_BOSS_BAR, x, y, 0, 0, barWidth, barHeight, barWidth, barHeight * 3);

        if (progressWidth > 0) {
            guiGraphics.blit(TRIAL_BOSS_BAR, x + barBuffer, y, barBuffer, 9, progressWidth, barHeight, barWidth, barHeight * 3);
        }

        if (PlayerClientData.getTrialProgress() >= 1.0F) {
            guiGraphics.blit(TRIAL_BOSS_BAR, x + barBuffer, y, barBuffer, 18, barWidth - (barBuffer * 2), barHeight, barWidth, barHeight * 3);
        }

        String displayText = getTrialDisplayText();
        int titleWidth = minecraft.font.width(displayText);
        int color = 0xFFFFFF;
        if (PlayerClientData.getTrialProgress() >= 1.0F){
            color = 0xFFAA00;
        }
        guiGraphics.drawString(minecraft.font, displayText, x + (barWidth / 2) - (titleWidth / 2), y - 9, color);
    }

    private static int getProgress(int barWidth, int barBuffer) {
        float trialProgress = PlayerClientData.getTrialProgress();
        return Mth.ceil(trialProgress * (barWidth - (barBuffer * 2)));
    }

    private static String getTrialDisplayText() {
        float trialProgress = PlayerClientData.getTrialProgress();
        if (trialProgress < 1.0F) {
            String trialType = PlayerClientData.getTrialType();
            if (trialType.equalsIgnoreCase(PlayerTrialData.survivalTrial)) {
                int percentComplete = (int) (PlayerClientData.getTrialProgress() * 100);
                return "Survive: " + percentComplete + "%";
            } else if (trialType.equalsIgnoreCase(PlayerTrialData.eliminationTrial)) {
                String targetName = PlayerClientData.getEliminationTarget();
                int eliminationCount = PlayerClientData.getEliminationKills();
                int requiredEliminations = PlayerClientData.getEliminationKillsRequired();
                return "Eliminate " + targetName + "s: " + eliminationCount + "/" + requiredEliminations;
            } else if (trialType.equalsIgnoreCase(PlayerTrialData.fetchTrial)) {
                String fetchItem = PlayerClientData.getFetchItem();
                int fetchItemCount = PlayerClientData.getFetchItems();
                int requiredFetchitems = PlayerClientData.getFetchItemsRequired();
                return "Feed the altar: " + fetchItemCount + "/" + requiredFetchitems + " " + fetchItem;
            }
        } else {
            return "The altar beckons your return...";
        }
        return "";
    }
}
