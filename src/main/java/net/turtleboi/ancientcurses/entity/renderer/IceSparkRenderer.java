package net.turtleboi.ancientcurses.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.entities.items.ThrownIceSpark;
import org.jetbrains.annotations.NotNull;

public class IceSparkRenderer extends EntityRenderer<ThrownIceSpark> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AncientCurses.MOD_ID, "textures/item/ice_spark.png");
    private final ItemRenderer itemRenderer;

    public IceSparkRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThrownIceSpark entity, float entityYaw, float partialTick, PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 6.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.translate((1/16F), -(3/16F), -(entity.getBbHeight()/2));
        poseStack.scale(1.5F, 1.5F, 1.5F);
        ItemStack stack = entity.getItem();
        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(@NotNull ThrownIceSpark entity) {
        return TEXTURE;
    }
}
