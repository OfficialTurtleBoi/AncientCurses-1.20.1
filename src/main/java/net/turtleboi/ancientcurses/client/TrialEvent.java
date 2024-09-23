package net.turtleboi.ancientcurses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.AncientCurses;

import java.util.UUID;

public class TrialEvent extends BossEvent{
    private static final ResourceLocation TRIAL_BOSS_BAR =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/gui/quest_bar.png");

    public TrialEvent(UUID pId, Component pName, BossBarColor pColor, BossBarOverlay pOverlay) {
        super(pId, pName, pColor, pOverlay);
    }

    public static void render(GuiGraphics guiGraphics, int x, int y, BossEvent bossEvent, Minecraft minecraft) {
        RenderSystem.setShaderTexture(0, TRIAL_BOSS_BAR);

        int barWidth = 192;
        int barHeight = 9;
        int barBuffer = 5;

        int progressWidth = Mth.ceil(bossEvent.getProgress() * (barWidth - (barBuffer * 2)));
        guiGraphics.blit(TRIAL_BOSS_BAR, x, y, 0, 0, barWidth, barHeight, barWidth, barHeight * 3);

        if (progressWidth > 0) {
            guiGraphics.blit(TRIAL_BOSS_BAR, x + barBuffer, y, barBuffer, 9, progressWidth, barHeight, barWidth, barHeight * 3);
        }

        if (bossEvent.getProgress() >= 1.0F) {
            guiGraphics.blit(TRIAL_BOSS_BAR, x + barBuffer, y, barBuffer, 18, barWidth - (barBuffer * 2), barHeight, barWidth, barHeight * 3);
        }

        String title = bossEvent.getName().getString();
        int titleWidth = minecraft.font.width(title);
        guiGraphics.drawString(minecraft.font, title, x + (192 / 2) - (titleWidth / 2), y - 10, 0xFFFFFF);
    }
}
