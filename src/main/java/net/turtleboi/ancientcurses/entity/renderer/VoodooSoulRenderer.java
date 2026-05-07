package net.turtleboi.ancientcurses.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.entities.VoodooSoulEntity;
import net.turtleboi.ancientcurses.entity.model.AncientWraithModel;

public class VoodooSoulRenderer extends MobRenderer<VoodooSoulEntity, AncientWraithModel<VoodooSoulEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/ancient_wraith.png");

    public VoodooSoulRenderer(EntityRendererProvider.Context context) {
        super(context, new AncientWraithModel<>(context.bakeLayer(AncientWraithModel.ANCIENT_WRAITH_LAYER)), 0.35F);
    }

    @Override
    public void render(VoodooSoulEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.75F, 0.75F, 0.75F);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(VoodooSoulEntity entity) {
        return TEXTURE;
    }
}
