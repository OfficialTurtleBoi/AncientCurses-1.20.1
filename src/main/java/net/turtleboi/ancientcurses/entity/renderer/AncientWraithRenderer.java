package net.turtleboi.ancientcurses.entity.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.entities.AncientWraithEntity;
import net.turtleboi.ancientcurses.entity.model.AncientWraithModel;

public class AncientWraithRenderer extends MobRenderer<AncientWraithEntity, AncientWraithModel<AncientWraithEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/ancient_wraith.png");
    public AncientWraithRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new AncientWraithModel<>(pContext.bakeLayer(AncientWraithModel.ANCIENT_WRAITH_LAYER)),0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(AncientWraithEntity pEntity) {
        return TEXTURE;
    }
}
