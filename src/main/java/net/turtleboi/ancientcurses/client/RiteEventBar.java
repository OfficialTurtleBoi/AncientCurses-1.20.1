package net.turtleboi.ancientcurses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.client.rites.ClientRiteState;

public class RiteEventBar {
    private static final ResourceLocation RITE_BOSS_BAR =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/gui/quest_bar.png");
    private static final int BAR_WIDTH = 192;
    private static final int BAR_HEIGHT = 9;
    private static final int BAR_BUFFER = 5;
    private static final int TEXTURE_HEIGHT = 34;
    private static final int DEGREE_NODE_SIZE = 7;
    private static final int DEGREE_NODE_SPACING = 1;
    private static final int DEGREE_NODE_X_OFFSET = 6;
    private static final int DEGREE_NODE_Y_OFFSET = 5;
    private static final int DEGREE_NODE_RADIUS = DEGREE_NODE_SIZE / 2;
    private static final int DEGREE_NODE_U = 0;
    private static final int DEGREE_NODE_V = 27;

    private static final int NODE_INACTIVE_BASE = 0xFF571300;
    private static final int NODE_INACTIVE_CENTER = 0xFF240800;
    private static final int NODE_ACTIVE_BASE = 0xFFEC3500;
    private static final int NODE_COMPLETED_BASE = 0xFFFF4DFE;

    private static final float NODE_INACTIVE_FLASH_SPEED = 0.05f;
    private static final float NODE_ACTIVE_FLASH_SPEED = 0.16f;
    private static final float NODE_COMPLETED_FLASH_SPEED = 0.10f;
    private static final float NODE_BREATHE_SPEED = 0.018f;
    private static final float NODE_FLASH_AMOUNT = 0.055f;
    private static final float NODE_BREATHE_AMOUNT = 0.03f;
    private static final float NODE_TRANSITION_TICKS = 20.0f;
    private static final float NODE_ACTIVE_EDGE_DARK = 0.48f;
    private static final float NODE_COMPLETED_EDGE_DARK = 0.44f;
    private static float[] nodeVisualStates = new float[0];
    private static float lastNodeUpdateTime = Float.NaN;
    private static String lastRiteVisualKey = "";

    public static void render(GuiGraphics guiGraphics, int x, int y, Minecraft minecraft) {
        if (minecraft.player == null) return;

        if (!PlayerClientData.hasRite()) {
            resetNodeVisualState();
            System.out.println(Component.literal("Removing bar because rite is null"));
            return;
        }

        RenderSystem.setShaderTexture(0, RITE_BOSS_BAR);

        if (minecraft.player == null) return;
        int progressWidth = getProgress(BAR_WIDTH, BAR_BUFFER);
        guiGraphics.blit(RITE_BOSS_BAR, x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, TEXTURE_HEIGHT);

        if (progressWidth > 0) {
            guiGraphics.blit(RITE_BOSS_BAR, x + BAR_BUFFER, y, BAR_BUFFER, 9, progressWidth, BAR_HEIGHT, BAR_WIDTH, TEXTURE_HEIGHT);
        }

        if (PlayerClientData.isRiteComplete()) {
            guiGraphics.blit(RITE_BOSS_BAR, x + BAR_BUFFER, y, BAR_BUFFER, 18, BAR_WIDTH - (BAR_BUFFER * 2), BAR_HEIGHT, BAR_WIDTH, TEXTURE_HEIGHT);
        }

        renderDegreeNodes(guiGraphics, x, y);

        Component displayText = getTrialDisplayText();
        int titleWidth = minecraft.font.width(displayText);
        int color = 0xFFFFFF;
        if (PlayerClientData.isRiteComplete()){
            color = 0xFFAA00;
        }
        guiGraphics.drawString(minecraft.font, displayText, x + (BAR_WIDTH / 2) - (titleWidth / 2), y - 9, color);
    }

    private static int getProgress(int barWidth, int barBuffer) {
        float trialProgress = PlayerClientData.getRiteProgress();
        return Mth.ceil(trialProgress * (barWidth - (barBuffer * 2)));
    }

    private static Component getTrialDisplayText() {
        return PlayerClientData.getActiveRiteState().getDisplayText();
    }

    private static void renderDegreeNodes(GuiGraphics guiGraphics, int x, int y) {
        ClientRiteState riteState = PlayerClientData.getActiveRiteState();
        int totalDegrees = riteState.getTotalDegrees();
        if (totalDegrees <= 0) {
            return;
        }

        int completedDegrees = Mth.clamp(riteState.getCompletedDegrees(), 0, totalDegrees);
        int activeDegreeIndex = riteState.getActiveDegreeIndex();
        float time = getNodeTime();
        float delta = getNodeDelta(time);
        ensureNodeVisualStateCapacity(totalDegrees, riteState);

        for (int i = 0; i < totalDegrees; i++) {
            int nodeState = 0;
            if (i < completedDegrees) {
                nodeState = 2;
            } else if (i == activeDegreeIndex) {
                nodeState = 1;
            }

            nodeVisualStates[i] = approach(nodeVisualStates[i], nodeState, delta / NODE_TRANSITION_TICKS);

            int drawX = x + DEGREE_NODE_X_OFFSET + (i * (DEGREE_NODE_SIZE + DEGREE_NODE_SPACING));
            int drawY = y + DEGREE_NODE_Y_OFFSET;
            renderAnimatedNode(guiGraphics, drawX, drawY, nodeVisualStates[i], i, time);
        }
    }

    private static void renderAnimatedNode(GuiGraphics guiGraphics, int x, int y, float visualState, int nodeIndex, float time) {
        float phase = nodePhase(nodeIndex);
        float stateProgress = visualState < 1.0f
                ? Mth.clamp(visualState, 0.0f, 1.0f)
                : Mth.clamp(visualState - 1.0f, 0.0f, 1.0f);
        float burstProgress = burstTransition(stateProgress);
        float stateBlend = Mth.clamp(burstProgress, 0.0f, 1.0f);
        float overshoot = Math.max(0.0f, burstProgress - 1.0f);

        float inactiveToActive = visualState < 1.0f ? stateBlend : 1.0f;
        float activeToCompleted = visualState < 1.0f ? 0.0f : stateBlend;
        float flashSpeed = visualState < 1.0f
                ? Mth.lerp(inactiveToActive, NODE_INACTIVE_FLASH_SPEED, NODE_ACTIVE_FLASH_SPEED)
                : Mth.lerp(activeToCompleted, NODE_ACTIVE_FLASH_SPEED, NODE_COMPLETED_FLASH_SPEED);
        float breathe = 0.5f + 0.5f * Mth.sin((time * NODE_BREATHE_SPEED) + phase);
        float flash = 0.5f + 0.5f * Mth.sin((time * flashSpeed) + phase + 0.8f);

        int baseColor = visualState < 1.0f
                ? lerpColor(NODE_INACTIVE_BASE, NODE_ACTIVE_BASE, inactiveToActive)
                : lerpColor(NODE_ACTIVE_BASE, NODE_COMPLETED_BASE, activeToCompleted);
        if (overshoot > 0.0f) {
            baseColor = brighten(baseColor, 0.18f * overshoot);
        }

        float inactiveFlashStrength = NODE_FLASH_AMOUNT * 0.30f;
        float activeFlashStrength = NODE_FLASH_AMOUNT * 0.75f;
        float completedFlashStrength = NODE_FLASH_AMOUNT * 0.55f;
        float flashStrength = visualState < 1.0f
                ? Mth.lerp(inactiveToActive, inactiveFlashStrength, activeFlashStrength)
                : Mth.lerp(activeToCompleted, activeFlashStrength, completedFlashStrength);
        baseColor = lerpColor(baseColor, brighten(baseColor, 0.35f), flashStrength * flash);
        baseColor = lerpColor(baseColor, brighten(baseColor, 0.12f), NODE_BREATHE_AMOUNT * breathe);

        float edgeDarkness = visualState < 1.0f
                ? lerpColorFactor(0.72f, NODE_ACTIVE_EDGE_DARK, inactiveToActive)
                : lerpColorFactor(NODE_ACTIVE_EDGE_DARK, NODE_COMPLETED_EDGE_DARK, activeToCompleted);
        int edgeColor = darken(baseColor, edgeDarkness);

        int inactiveCenterColor = lerpColor(baseColor, NODE_INACTIVE_CENTER, 0.55f);
        int activeCenterColor = brighten(baseColor, 0.18f + (0.12f * overshoot));
        int completedCenterColor = brighten(baseColor, 0.07f);
        int centerColor = visualState < 1.0f
                ? lerpColor(inactiveCenterColor, activeCenterColor, inactiveToActive)
                : lerpColor(activeCenterColor, completedCenterColor, activeToCompleted);

        float edgeShadedMix = visualState < 1.0f
                ? smoothStep(Mth.clamp((inactiveToActive - 0.18f) / 0.32f, 0.0f, 1.0f))
                : smoothStep(Mth.clamp(0.75f + (activeToCompleted * 0.25f), 0.0f, 1.0f));

        for (int pixelY = 0; pixelY < DEGREE_NODE_SIZE; pixelY++) {
            for (int pixelX = 0; pixelX < DEGREE_NODE_SIZE; pixelX++) {
                int localX = pixelX - DEGREE_NODE_RADIUS;
                int localY = pixelY - DEGREE_NODE_RADIUS;
                if (!isNodePixel(localX, localY)) {
                    continue;
                }

                float gradient = nodeGradient(localX, localY);
                int centerDarkColor = lerpColor(edgeColor, centerColor, gradient);
                int edgeDarkColor = lerpColor(centerColor, edgeColor, gradient);
                int color = lerpColor(centerDarkColor, edgeDarkColor, edgeShadedMix);

                guiGraphics.fill(x + pixelX, y + pixelY, x + pixelX + 1, y + pixelY + 1, color);
            }
        }

        guiGraphics.blit(RITE_BOSS_BAR, x, y, DEGREE_NODE_U, DEGREE_NODE_V, DEGREE_NODE_SIZE, DEGREE_NODE_SIZE, BAR_WIDTH, TEXTURE_HEIGHT);
    }

    private static void ensureNodeVisualStateCapacity(int totalDegrees, ClientRiteState riteState) {
        String riteKey = riteState.getRiteId() + "|" + totalDegrees;
        if (nodeVisualStates.length != totalDegrees || !riteKey.equals(lastRiteVisualKey)) {
            nodeVisualStates = new float[totalDegrees];
            lastRiteVisualKey = riteKey;
        }
    }

    private static void resetNodeVisualState() {
        nodeVisualStates = new float[0];
        lastNodeUpdateTime = Float.NaN;
        lastRiteVisualKey = "";
    }

    private static float getNodeDelta(float time) {
        if (Float.isNaN(lastNodeUpdateTime)) {
            lastNodeUpdateTime = time;
            return 0.0f;
        }
        float delta = Mth.clamp(time - lastNodeUpdateTime, 0.0f, 5.0f);
        lastNodeUpdateTime = time;
        return delta;
    }

    private static float getNodeTime() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.getGameTime() + minecraft.getFrameTime();
        }
        return (float) (System.currentTimeMillis() / 50.0);
    }

    private static boolean isNodePixel(int localX, int localY) {
        int absX = Math.abs(localX);
        int absY = Math.abs(localY);
        return absX <= 2 && absY <= 2 && !(absX == 2 && absY == 2);
    }

    private static boolean isNodeEdge(int localX, int localY) {
        int absX = Math.abs(localX);
        int absY = Math.abs(localY);
        return absX == 2 || absY == 2;
    }

    private static float nodeGradient(int localX, int localY) {
        float distance = Math.abs(localX) + Math.abs(localY);
        return Mth.clamp(distance / 4.0f, 0.0f, 1.0f);
    }

    private static float approach(float current, float target, float amount) {
        if (current < target) {
            return Math.min(current + amount, target);
        }
        if (current > target) {
            return Math.max(current - amount, target);
        }
        return current;
    }

    private static float nodePhase(int nodeIndex) {
        int value = nodeIndex * 1103515245 + 12345;
        value ^= (value >>> 16);
        return ((value & 0xFFFF) / 65535.0f) * Mth.TWO_PI;
    }

    private static float smoothStep(float value) {
        value = Mth.clamp(value, 0.0f, 1.0f);
        return value * value * (3.0f - (2.0f * value));
    }

    private static float burstTransition(float value) {
        value = Mth.clamp(value, 0.0f, 1.0f);
        if (value <= 0.55f) {
            return 0.28f * smoothStep(value / 0.55f);
        }

        float burst = (value - 0.55f) / 0.45f;
        return 0.28f + (0.72f * easeOutBack(burst));
    }

    private static float easeOutBack(float value) {
        value = Mth.clamp(value, 0.0f, 1.0f);
        float overshoot = 1.70158f;
        float shifted = value - 1.0f;
        return 1.0f + ((overshoot + 1.0f) * shifted * shifted * shifted) + (overshoot * shifted * shifted);
    }

    private static int lerpColor(int from, int to, float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        int fromA = (from >>> 24) & 0xFF;
        int fromR = (from >>> 16) & 0xFF;
        int fromG = (from >>> 8) & 0xFF;
        int fromB = from & 0xFF;
        int toA = (to >>> 24) & 0xFF;
        int toR = (to >>> 16) & 0xFF;
        int toG = (to >>> 8) & 0xFF;
        int toB = to & 0xFF;
        return (((int) (fromA + ((toA - fromA) * t))) << 24)
                | (((int) (fromR + ((toR - fromR) * t))) << 16)
                | (((int) (fromG + ((toG - fromG) * t))) << 8)
                | ((int) (fromB + ((toB - fromB) * t)));
    }

    private static int darken(int color, float factor) {
        factor = Mth.clamp(factor, 0.0f, 1.0f);
        int alpha = (color >>> 24) & 0xFF;
        int red = (int) (((color >>> 16) & 0xFF) * factor);
        int green = (int) (((color >>> 8) & 0xFF) * factor);
        int blue = (int) ((color & 0xFF) * factor);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int brighten(int color, float factor) {
        factor = Mth.clamp(factor, 0.0f, 1.0f);
        int alpha = (color >>> 24) & 0xFF;
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;
        red = (int) (red + ((255 - red) * factor));
        green = (int) (green + ((255 - green) * factor));
        blue = (int) (blue + ((255 - blue) * factor));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static float lerpColorFactor(float from, float to, float t) {
        return Mth.lerp(Mth.clamp(t, 0.0f, 1.0f), from, to);
    }

}
