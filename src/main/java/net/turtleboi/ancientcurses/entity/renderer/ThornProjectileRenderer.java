package net.turtleboi.ancientcurses.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.entities.items.ThornProjectileEntity;
import net.turtleboi.ancientcurses.entity.model.ThornProjectileModel;
import org.jetbrains.annotations.NotNull;

public class ThornProjectileRenderer extends EntityRenderer<ThornProjectileEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/thorn_projectile.png");

    private final ThornProjectileModel<ThornProjectileEntity> model;

    public ThornProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ThornProjectileModel<>(context.bakeLayer(ThornProjectileModel.THORN_PROJECTILE_LAYER));
    }

    @Override
    public void render(ThornProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        poseStack.translate(0.0D, -1.0D, 0.0D);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ThornProjectileEntity entity) {
        return TEXTURE;
    }
}
