package net.turtleboi.ancientcurses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.AncientCurses;

public class TrialEvent {
    private static final ResourceLocation TRIAL_BOSS_BAR =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/gui/quest_bar.png");
    private Minecraft minecraft;
    private String title;
    private float progress;
    private Player player;

    public void CustomBossBarRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.title = title;
        this.player = player;
        this.progress = 0.0F;
    }

    public void setProgress(float progress) {
        this.progress = Mth.clamp(progress, 0.0F, 1.0F); // Clamped between 0 and 1
    }

    public void render(GuiGraphics guiGraphics, int x, int y, BossEvent bossEvent) {
        RenderSystem.setShaderTexture(0, TRIAL_BOSS_BAR);

        int barWidth = 192;
        int barHeight = 9;
        int barBuffer = 5;

        int progressWidth = Mth.ceil(bossEvent.getProgress() * barWidth - (barBuffer * 2));
        guiGraphics.blit(TRIAL_BOSS_BAR, x, y, 0, 0, barWidth, barHeight);

        if (progressWidth > 0) {
            guiGraphics.blit(TRIAL_BOSS_BAR, x + barBuffer, y, barBuffer, 9, progressWidth, barHeight);
        }

        if (this.progress >= 1.0F) {
            guiGraphics.blit(TRIAL_BOSS_BAR, x + barBuffer, y, barBuffer, 18, barWidth - (barBuffer * 2), barHeight);
        }

        String title = bossEvent.getName().getString();
        int titleWidth = this.minecraft.font.width(title);
        guiGraphics.drawString(this.minecraft.font, title, x + (192 / 2) - (titleWidth / 2), y - 10, 0xFFFFFF);
    }
}
