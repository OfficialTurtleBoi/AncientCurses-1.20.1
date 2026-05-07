package net.turtleboi.ancientcurses.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.entities.PlagueIdolEntity;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.turtlecore.client.renderer.PulseAuraRenderer;
import org.jetbrains.annotations.NotNull;

public class PlagueIdolRenderer extends EntityRenderer<PlagueIdolEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AncientCurses.MOD_ID, "textures/item/plague_idol.png");
    private final ItemRenderer itemRenderer;

    public PlagueIdolRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.25F;
    }

    @Override
    public void render(PlagueIdolEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.35F, 0.0F);
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        itemRenderer.renderStatic(
                new ItemStack(ModItems.PLAGUE_IDOL.get()),
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId());
        poseStack.popPose();

        if (bufferSource instanceof MultiBufferSource.BufferSource buffer) {
            PulseAuraRenderer.renderWaves(buffer, poseStack, entity, partialTick);
        } else {
            MultiBufferSource.BufferSource auraBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
            PulseAuraRenderer.renderWaves(
                    auraBuffer,
                    poseStack,
                    entity,
                    partialTick);
            auraBuffer.endBatch();
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PlagueIdolEntity entity) {
        return TEXTURE;
    }
}
