package net.turtleboi.ancientcurses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.trials.PlayerTrialData;

import java.util.UUID;

public class TrialEventBar extends BossEvent{
    private static final ResourceLocation TRIAL_BOSS_BAR =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/gui/quest_bar.png");

    public TrialEventBar(UUID pId, Component pName, BossBarColor pColor, BossBarOverlay pOverlay) {
        super(pId, pName, pColor, pOverlay);
    }

    public static void render(GuiGraphics guiGraphics, int x, int y, BossEvent bossEvent, Minecraft minecraft) {
        if (bossEvent == null) {
            return;
        }

        String trialName = bossEvent.getName().getString();
        if (!trialName.equalsIgnoreCase(PlayerTrialData.survivalTrial) && !trialName.equalsIgnoreCase(PlayerTrialData.eliminationTrial) || trialName.equalsIgnoreCase("None")) {
            return;
        }

        RenderSystem.setShaderTexture(0, TRIAL_BOSS_BAR);

        int barWidth = 192;
        int barHeight = 9;
        int barBuffer = 5;

        if (minecraft.player == null) return;
        int progressWidth = getProgress(bossEvent, barWidth, barBuffer);
        guiGraphics.blit(TRIAL_BOSS_BAR, x, y, 0, 0, barWidth, barHeight, barWidth, barHeight * 3);

        if (progressWidth > 0) {
            guiGraphics.blit(TRIAL_BOSS_BAR, x + barBuffer, y, barBuffer, 9, progressWidth, barHeight, barWidth, barHeight * 3);
        }

        if (bossEvent.getProgress() >= 1.0F) {
            guiGraphics.blit(TRIAL_BOSS_BAR, x + barBuffer, y, barBuffer, 18, barWidth - (barBuffer * 2), barHeight, barWidth, barHeight * 3);
        }

        if (trialName.equalsIgnoreCase(PlayerTrialData.survivalTrial)){
            int percentComplete = (int) ((bossEvent.getProgress() * 100));
            String percentText = "Survive: " + percentComplete + "%";
            int titleWidth = minecraft.font.width(percentText);
            guiGraphics.drawString(minecraft.font, percentText, x + (192 / 2) - (titleWidth / 2), y - 10, 0xFFFFFF);
        } else if (trialName.equalsIgnoreCase(PlayerTrialData.eliminationTrial)) {
            int eliminationCount = PlayerClientData.getEliminationKills();
            int requiredEliminations = PlayerClientData.getEliminationKillsRequired();
            String killsComplete = "Eliminate enemies: " + eliminationCount + "/" + requiredEliminations;
            int titleWidth = minecraft.font.width(killsComplete);
            guiGraphics.drawString(minecraft.font, killsComplete, x + (192 / 2) - (titleWidth / 2), y - 10, 0xFFFFFF);
        }

    }

    private static int getProgress(BossEvent bossEvent, int barWidth, int barBuffer) {
        float trialProgress = 0.0F;

        String trialName = bossEvent.getName().getString();
        if (trialName.equalsIgnoreCase(PlayerTrialData.survivalTrial)){
            long elapsedTime = PlayerClientData.getTrialDurationElapsed();
            long trialDuration = PlayerClientData.getTrialDurationTotal();
            trialProgress = Math.min(1.0F, (float) elapsedTime / (float) trialDuration);
        } else if (trialName.equalsIgnoreCase(PlayerTrialData.eliminationTrial)) {
            int eliminationCount = PlayerClientData.getEliminationKills();
            int requiredEliminations = PlayerClientData.getEliminationKillsRequired();
            trialProgress = Math.min(1.0F, (float) eliminationCount / (float) requiredEliminations);
        }

        return Mth.ceil(trialProgress * (barWidth - (barBuffer * 2)));
    }
}
