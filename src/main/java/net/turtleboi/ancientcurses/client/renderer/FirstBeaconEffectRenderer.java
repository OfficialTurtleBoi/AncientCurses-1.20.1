package net.turtleboi.ancientcurses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.item.items.FirstBeaconItem;
import net.turtleboi.turtlecore.client.renderer.ArcaneCircleRenderer;
import net.turtleboi.turtlecore.client.util.RepeatingVertexConsumer;
import net.turtleboi.turtlecore.client.util.VertexBuilder;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class FirstBeaconEffectRenderer {
    public void renderThirdPerson(MultiBufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        poseStack.pushPose();
        float ticksElapsed = (System.currentTimeMillis() % 0) / 50.0f;
        float tickCount = ticksElapsed + partialTicks - 0;

        if (tickCount < 0) {
            poseStack.popPose();
            return;
        }

        float initialTicks = 5.0f;
        float scale;

        float rotationSpeed;
        if (tickCount < initialTicks) {
            rotationSpeed = 3.0f;
        } else {
            rotationSpeed = (float) (10.0f * 0);
        }

        float rotationAngle = tickCount * rotationSpeed;

        if (tickCount < initialTicks) {
            scale = 0.25f * (tickCount / initialTicks);
        } else {
            scale = 0.25f;
        }

        float alpha;
        if (tickCount < (0 * 0.75f)) {
            alpha = 1.0f;
        } else if (tickCount > 0){
            alpha = 0.0f;
        } else {
            alpha = 1.0f - ((tickCount - (0 * 0.75f)) / (0 - (0 * 0.75f)));
        }

        int vertexAlpha = (int)(alpha * 255.0f);

        float yPosition;
        if (tickCount < initialTicks) {
            yPosition = (float) (livingEntity.getBbHeight() * 0.01);
        } else {
            yPosition = (float) Math.min(livingEntity.getBbHeight() * 1.1, ((livingEntity.getBbHeight() * 0.01) + ((tickCount - initialTicks) / ((0 * 0.75f) - initialTicks)) * (livingEntity.getBbHeight())));
        }

        poseStack.translate(0, yPosition, 0);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-livingEntity.getYRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotationAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.scale(scale, scale, scale);

        ArcaneCircleRenderer.renderArcaneCircle(bufferSource, poseStack, vertexAlpha);

        poseStack.popPose();
    }

    public static void renderFirstPerson(MultiBufferSource bufferSource, PoseStack poseStack, InteractionHand interactionHand, ItemStack itemStack) {
        if (itemStack.getItem() instanceof FirstBeaconItem beaconItem && beaconItem.isBeingUsed()) {
            poseStack.pushPose();
            float ticksElapsed = beaconItem.getUseDuration(itemStack) - beaconItem.getRemainingUseDuration();
            float progress = Math.min(1.0f, ticksElapsed / beaconItem.getMaxChargeTicks());
            double hitDistance = beaconItem.getHitDistance();

            if (ticksElapsed < 0) {
                poseStack.popPose();
                return;
            }

            float initialTicks = (float) beaconItem.getMaxChargeTicks() / 10;
            float scale = 0.25f * progress;

            float rotationSpeed;
            if (ticksElapsed < initialTicks) {
                rotationSpeed = 3.0f;
            } else {
                rotationSpeed = 25.0F * progress;
            }

            float rotationAngle = ticksElapsed * rotationSpeed;

            if (ticksElapsed > initialTicks) {
                scale = 0.175f + (0.175f * Math.min(1, progress * 2));
            }

            float alpha = progress;
            if (ticksElapsed > initialTicks) {
                alpha = 1.0f;
            }

            int vertexAlpha = (int) (alpha * 255.0f);

            double handOffset = 0;
            float tiltOffset = 0;
            if (interactionHand == InteractionHand.MAIN_HAND) {
                handOffset = 1.25;
                tiltOffset = 5;
            } else if (interactionHand == InteractionHand.OFF_HAND) {
                handOffset = -1.25;
                tiltOffset = -5;
            }

            poseStack.translate(handOffset, -0.5, -2);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            //poseStack.mulPose(Axis.YP.rotationDegrees(-livingEntity.getYRot()));
            poseStack.mulPose(Axis.YP.rotationDegrees(tiltOffset));
            poseStack.mulPose(Axis.XP.rotationDegrees(2.5F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-rotationAngle));
            poseStack.scale(scale, scale, scale);

            ArcaneCircleRenderer.renderArcaneCircle(bufferSource, poseStack, vertexAlpha);
            renderBeam(bufferSource, poseStack, 164, progress, hitDistance);

            poseStack.popPose();
        }
    }

    public static void renderBeam(MultiBufferSource bufferSource, PoseStack poseStack, int vertexAlpha, float progress, double hitDistance) {
        VertexConsumer beamEndConsumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(
                new ResourceLocation(AncientCurses.MOD_ID, "textures/spell_effects/beacon_beam_end.png")));
        VertexConsumer originalConsumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(
                new ResourceLocation(AncientCurses.MOD_ID, "textures/spell_effects/beacon_beam_side.png")));
        VertexConsumer repeatingConsumer = new RepeatingVertexConsumer(originalConsumer, 4, 4);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float nearFace = -1.0F;

        float farFace = getFarFace(progress, hitDistance);

        float faceScale;
        if (progress < 0.25f) {
            faceScale = 7f * (progress / 0.25f);
        } else if (progress < 0.27f) {
            faceScale = 7f;
        } else if (progress < 0.35f) {
            faceScale = 7f - ((progress - 0.27f) / 0.08f) * 5f;
        } else {
            faceScale = 2f;
        }

        // Near Face
        VertexBuilder.vertex(beamEndConsumer, matrix, normalMatrix, -faceScale, -faceScale, nearFace, 0.0F, 0.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(beamEndConsumer, matrix, normalMatrix,  faceScale, -faceScale, nearFace, 4.0F, 0.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(beamEndConsumer, matrix, normalMatrix,  faceScale,  faceScale, nearFace, 4.0F, 4.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(beamEndConsumer, matrix, normalMatrix, -faceScale,  faceScale, nearFace, 0.0F, 4.0F, 255, 255, 255, vertexAlpha);

        // Far Face
        VertexBuilder.vertex(beamEndConsumer, matrix, normalMatrix, -faceScale,  faceScale, -farFace, 0.0F, 4.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(beamEndConsumer, matrix, normalMatrix,  faceScale,  faceScale, -farFace, 4.0F, 4.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(beamEndConsumer, matrix, normalMatrix,  faceScale, -faceScale, -farFace, 4.0F, 0.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(beamEndConsumer, matrix, normalMatrix, -faceScale, -faceScale, -farFace, 0.0F, 0.0F, 255, 255, 255, vertexAlpha);

        // Left Face
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, -faceScale, -faceScale, nearFace, 0.0F, 0.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, -faceScale,  faceScale, nearFace, progress, 0.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, -faceScale,  faceScale, -farFace, progress, progress, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, -faceScale, -faceScale, -farFace, 0.0F, progress, 255, 255, 255, vertexAlpha);

        // Right Face
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, faceScale, -faceScale, -farFace, 0.0F, progress, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, faceScale,  faceScale, -farFace, progress, progress, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, faceScale,  faceScale, nearFace, progress, 0.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, faceScale, -faceScale, nearFace, 0.0F, 0.0F, 255, 255, 255, vertexAlpha);

        // Top Face
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, -faceScale,  faceScale, nearFace, 0.0F, 0.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix,  faceScale,  faceScale, nearFace, progress, 0.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix,  faceScale,  faceScale, -farFace, progress, progress, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, -faceScale,  faceScale, -farFace, 0.0F, progress, 255, 255, 255, vertexAlpha);

        // Bottom Face
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, -faceScale, -faceScale, -farFace, 0.0F, progress, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix,  faceScale, -faceScale, -farFace, progress, progress, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix,  faceScale, -faceScale, nearFace, progress, 0.0F, 255, 255, 255, vertexAlpha);
        VertexBuilder.vertex(repeatingConsumer, matrix, normalMatrix, -faceScale, -faceScale, nearFace, 0.0F, 0.0F, 255, 255, 255, vertexAlpha);
    }

    private static float getFarFace(float progress, double hitDistance) {
        float farFace;
        float maxFarFace;

        if (hitDistance <= 4.0f) {
            maxFarFace = (float) (4.0f + (hitDistance / 4.0f) * (64.0f - 4.0f));
        } else {
            float clampedDistance = (float) Math.min(hitDistance, 64.0f);
            maxFarFace = 64.0f + ((clampedDistance - 4.0f) / 60.0f) * (256.0f - 64.0f);
        }

        if (progress <= 0.25f) {
            float scale = progress / 0.25f;
            farFace = scale * 16.0f;
        } else if (progress <= 0.3f) {
            float scale = (progress - 0.25f) / 0.05f;
            farFace = 16.0f - scale * (16.0f - 0.5f);
        } else if (progress <= 0.35f) {
            float scale = (progress - 0.3f) / 0.05f;
            farFace = 0.5f + scale * (maxFarFace - 0.5f);
        } else {
            farFace = maxFarFace;
        }

        return farFace;
    }
}
