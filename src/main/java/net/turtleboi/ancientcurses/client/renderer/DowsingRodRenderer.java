package net.turtleboi.ancientcurses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.item.items.DowsingRod;
import net.turtleboi.ancientcurses.item.items.FirstBeaconItem;
import net.turtleboi.turtlecore.client.renderer.ArcaneCircleRenderer;

public class DowsingRodRenderer {
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
        if (!(itemStack.getItem() instanceof DowsingRod)) return;
        BlockPos altarPos = new BlockPos((int) PlayerClientData.getAltarX(), (int) PlayerClientData.getAltarY(), (int) PlayerClientData.getAltarZ());

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        ServerLevel world = (ServerLevel) player.level();
        BlockPos eyePos = player.blockPosition().above((int) player.getEyeHeight());

        double dx = (altarPos.getX() + 0.5) - eyePos.getX();
        double dz = (altarPos.getZ() + 0.5) - eyePos.getZ();
        float angleToAltar = (float) Math.toDegrees(Math.atan2(dz, dx));
        float yawDelta = Mth.wrapDegrees(angleToAltar - player.getYRot());

        BakedModel model = itemStack.getItem().asItem().
        ItemTransform transform = model.getTransforms().getTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);

        poseStack.pushPose();

        transform.apply(/* leftHand? */ interactionHand == InteractionHand.OFF_HAND, poseStack);
        poseStack.mulPose(Axis.YP.rotationDegrees(yawDelta));

        minecraft.getItemRenderer().renderStatic(
                itemStack,
                ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                minecraft.level.getBrightness(LightLayer.BLOCK, player.blockPosition()),
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                minecraft.level,
                0
        );
        poseStack.popPose();
    }


}
