package net.turtleboi.ancientcurses.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

public class CursedAltarRenderer implements BlockEntityRenderer<CursedAltarBlockEntity> {
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/ancient_book.png");
    private final BookModel bookModel;
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public CursedAltarRenderer(BlockEntityRendererProvider.Context pContext) {
        this.bookModel = new BookModel(pContext.bakeLayer(ModelLayers.BOOK));
    }

    public void render(CursedAltarBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 0.75F, 0.5F);
        float f1 = (float)pBlockEntity.time + pPartialTick;
        pPoseStack.translate(0.0F, 0.1F + Mth.sin(f1 * 0.1F) * 0.01F, 0.0F);

        float rotation;
        for (rotation = pBlockEntity.rot - pBlockEntity.oRot; rotation >= (float)Math.PI; rotation -= (float)(Math.PI * 2)) {}
        while (rotation < -(float)Math.PI) {
            rotation += (float)(Math.PI * 2);
        }

        float f2;
        for(f2 = pBlockEntity.rot - pBlockEntity.oRot; f2 >= 3.1415927F; f2 -= 6.2831855F) {
        }

        while(f2 < -3.1415927F) {
            f2 += 6.2831855F;
        }

        float f3 = pBlockEntity.oRot + f2 * pPartialTick;
        pPoseStack.mulPose(Axis.YP.rotation(-f3));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float f4 = Mth.lerp(pPartialTick, pBlockEntity.oFlip, pBlockEntity.flip);
        float f5 = Mth.frac(f4 + 0.25F) * 1.6F - 0.3F;
        float f6 = Mth.frac(f4 + 0.75F) * 1.6F - 0.3F;
        float f7 = Mth.lerp(pPartialTick, pBlockEntity.oOpen, pBlockEntity.open);
        this.bookModel.setupAnim(f1, Mth.clamp(f5, 0.0F, 1.0F), Mth.clamp(f6, 0.0F, 1.0F), f7);
        VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entityCutout(BOOK_LOCATION));
        this.bookModel.render(pPoseStack, vertexConsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        pPoseStack.popPose();

        ItemStack hoveringItem = pBlockEntity.getHoveringItem();
        if (!hoveringItem.isEmpty()) {
            System.out.println("Rendering hovering item: " + hoveringItem.getItem());
            pPoseStack.pushPose();

            // Adjust the height and rotation for the hovering item
            float hoverHeight = 1.25F + Mth.sin(f1 * 0.1F) * 0.05F;
            float spinSpeed = (f1 % 360) * (float) Math.PI / 180.0F;
            pPoseStack.translate(0.5F, hoverHeight, 0.5F); // Position item above altar
            pPoseStack.mulPose(Axis.YP.rotation(spinSpeed)); // Spin the item

            double orbitRadius = 0.5F;
            pPoseStack.translate(orbitRadius * Math.cos(f1 * 0.05), 0.0, orbitRadius * Math.sin(f1 * 0.05));

            // Scale the item slightly
            pPoseStack.scale(1.0F, 1.0F, 1.0F);

            // Render the item
            this.itemRenderer.renderStatic(hoveringItem, ItemDisplayContext.GROUND, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);

            pPoseStack.popPose();
        }
    }
}

