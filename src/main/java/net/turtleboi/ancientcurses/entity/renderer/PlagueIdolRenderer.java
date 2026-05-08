package net.turtleboi.ancientcurses.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.entities.PlagueIdolEntity;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.turtlecore.client.util.TintingVertexConsumer;
import net.turtleboi.turtlecore.client.renderer.PulseAuraRenderer;
import org.jetbrains.annotations.NotNull;

public class PlagueIdolRenderer extends EntityRenderer<PlagueIdolEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AncientCurses.MOD_ID, "textures/item/plague_idol.png");
    private static final ResourceLocation EYES_MODEL = new ResourceLocation(AncientCurses.MOD_ID, "item/plague_idol_eyes");
    private final ItemRenderer itemRenderer;

    public PlagueIdolRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.25F;
    }

    @Override
    public void render(PlagueIdolEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        ItemStack idolStack = new ItemStack(ModItems.PLAGUE_IDOL.get());
        poseStack.pushPose();
        poseStack.translate(0.0F, (4.25/16f), 0.0F);
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        MultiBufferSource.BufferSource idolBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        itemRenderer.renderStatic(
                idolStack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                idolBuffer,
                entity.level(),
                entity.getId());
        idolBuffer.endBatch();
        poseStack.popPose();

        MultiBufferSource.BufferSource auraBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        PulseAuraRenderer.renderWaves(
                auraBuffer,
                poseStack,
                entity,
                partialTick);
        auraBuffer.endBatch();

        poseStack.pushPose();
        poseStack.translate(0.0F, (4.25/16f), 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        renderEyeGlow(entity, partialTick, poseStack, idolStack);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PlagueIdolEntity entity) {
        return TEXTURE;
    }

    private void renderEyeGlow(PlagueIdolEntity entity, float partialTick, PoseStack poseStack,
                               ItemStack idolStack) {
        float intensity = entity.getEyePulseIntensity(partialTick);
        if (intensity <= 0.0F) {
            return;
        }

        BakedModel eyeModel = Minecraft.getInstance().getModelManager().getModel(EYES_MODEL);
        MultiBufferSource.BufferSource eyeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        itemRenderer.render(
                idolStack,
                ItemDisplayContext.FIXED,
                false,
                poseStack,
                new EyeGlowBufferSource(eyeBuffer, intensity),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                eyeModel);
        eyeBuffer.endBatch();
    }

    private record EyeGlowBufferSource(MultiBufferSource delegate, float alpha) implements MultiBufferSource {
        @Override
        public @NotNull VertexConsumer getBuffer(@NotNull net.minecraft.client.renderer.RenderType renderType) {
            return new TintingVertexConsumer(
                    delegate.getBuffer(renderType),
                    1.0F,
                    1.0F,
                    1.0F,
                    alpha);
        }
    }
}
