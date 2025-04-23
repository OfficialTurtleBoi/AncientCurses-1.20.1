package net.turtleboi.ancientcurses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.item.items.DowsingRod;
import org.joml.Vector3f;

public class DowsingRodRenderer {
    public static void renderFirstPerson(MultiBufferSource bufferSource, PoseStack poseStack, InteractionHand usedHand, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof DowsingRod)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        float animationProgress = 1f;
        if (PlayerClientData.getItemUsedTime() > 0) {
            long elapsed = System.currentTimeMillis() - PlayerClientData.getItemUsedTime();
            animationProgress = Mth.clamp((float)elapsed / 300, 0f, 1f);
            if (animationProgress >= 1f) {
                PlayerClientData.setItemUsedTime(-1);
            }
        }

        ClientLevel level = minecraft.level;
        Vec3 eyePos = player.getEyePosition(minecraft.getFrameTime());
        Vec3 target = new Vec3(
                PlayerClientData.getAltarX() + 0.5,
                PlayerClientData.getAltarY() + 0.5,
                PlayerClientData.getAltarZ() + 0.5);
        Vec3 delta = target.subtract(eyePos);
        float angleToAltar = (float)Math.toDegrees(Math.atan2(delta.z, delta.x)) - 110f;
        float yawDelta = Mth.wrapDegrees(angleToAltar - player.getYRot());

        ItemDisplayContext displayContext = (usedHand == InteractionHand.MAIN_HAND)
                ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        Vector3f transA = new Vector3f(0f,-0.125f,-1.0f);
        float yaw =  90f;
        float roll = -75f;
        Vector3f pivot = new Vector3f(0f,0.125f,0f);
        Vector3f transB = new Vector3f(0f,-0.1f,-0.075f );
        float desiredScale = 1.125f;

        poseStack.pushPose();

        poseStack.translate(transA.x() * animationProgress,transA.y() * animationProgress,transA.z() * animationProgress);

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw  * animationProgress));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll * animationProgress));

        poseStack.translate(pivot.x() * animationProgress, pivot.y() * animationProgress, pivot.z() * animationProgress);
        poseStack.mulPose(Axis.XP.rotationDegrees(yawDelta * animationProgress));
        poseStack.translate(-pivot.x() * animationProgress, -pivot.y()*animationProgress, -pivot.z() * animationProgress);

        poseStack.translate(transB.x() * animationProgress, transB.y() * animationProgress, transB.z() * animationProgress);

        float scale = 1f + (desiredScale - 1f) * animationProgress;
        poseStack.scale(scale, scale, scale);

        minecraft.getItemRenderer().renderStatic(
                player,
                itemStack,
                displayContext,
                usedHand == InteractionHand.OFF_HAND,
                poseStack,
                bufferSource,
                level,
                LightTexture.pack(
                        level.getBrightness(LightLayer.BLOCK, player.blockPosition()),
                        level.getBrightness(LightLayer.SKY,   player.blockPosition())),
                OverlayTexture.NO_OVERLAY,
                0);
        poseStack.popPose();
    }
}
