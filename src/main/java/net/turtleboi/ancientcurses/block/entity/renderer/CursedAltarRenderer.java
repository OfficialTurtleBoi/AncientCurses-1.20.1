package net.turtleboi.ancientcurses.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

public class CursedAltarRenderer implements BlockEntityRenderer<CursedAltarBlockEntity> {
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/ancient_book.png");
    private final BookModel bookModel;
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public CursedAltarRenderer(BlockEntityRendererProvider.Context pContext) {
        this.bookModel = new BookModel(pContext.bakeLayer(ModelLayers.BOOK));
    }

    private void renderGem(ItemStack gem, float hoverHeight, float orbitRadius, float baseAngle, float f1, float spinSpeed, float orbitSpeedMultiplier, PoseStack pPoseStack, int pPackedLight, int pPackedOverlay, MultiBufferSource pBuffer, BlockEntity pBlockEntity) {
        if (!gem.isEmpty()) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5F, hoverHeight, 0.5F);
            pPoseStack.mulPose(Axis.YP.rotation(spinSpeed));
            float normalizedTime = f1 % 360.0F;
            float adjustedOrbitSpeedMultiplier = orbitSpeedMultiplier * (1.0F / normalizedTime);
            float adjustedOrbitAngle = baseAngle + (normalizedTime * adjustedOrbitSpeedMultiplier);
            adjustedOrbitAngle = (float) (adjustedOrbitAngle % (2 * Math.PI));
            pPoseStack.translate(orbitRadius * Math.cos(adjustedOrbitAngle), 0.0, orbitRadius * Math.sin(adjustedOrbitAngle));
            this.itemRenderer.renderStatic(gem, ItemDisplayContext.GROUND, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }
    }

    private void renderGems(PoseStack pPoseStack, CursedAltarBlockEntity pBlockEntity, float f1, int pPackedLight, int pPackedOverlay, MultiBufferSource pBuffer, float orbitRadius, float orbitSpeedMultiplier) {
        ItemStack gem1 = pBlockEntity.getGemInSlot(0);
        ItemStack gem2 = pBlockEntity.getGemInSlot(1);
        ItemStack gem3 = pBlockEntity.getGemInSlot(2);

        float hoverHeight = 1.25F + Mth.sin(f1 * 0.1F) * 0.05F;
        float spinSpeed1 = (f1 % 360) * (float) Math.PI / 180.0F;
        float spinSpeed2 = (f1 + 120 % 360 + 120) * (float) Math.PI / 180.0F;
        float spinSpeed3 = (f1 + 240 % 360 + 240) * (float) Math.PI / 180.0F;

        int gemCount = (!gem1.isEmpty() ? 1 : 0) + (!gem2.isEmpty() ? 1 : 0) + (!gem3.isEmpty() ? 1 : 0);

        if (gemCount == 1) {
            renderGem(gem1, hoverHeight, 0.0F, 0.0F, f1, spinSpeed1, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
        } else if (gemCount == 2) {
            renderGem(gem1, hoverHeight, orbitRadius, 0.0F, f1, spinSpeed1, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
            renderGem(gem2, hoverHeight, orbitRadius, (float) (Math.PI / 2) - (orbitSpeedMultiplier * (float) Math.PI * 2), f1, spinSpeed2, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
        } else if (gemCount == 3) {
            renderGem(gem1, hoverHeight, orbitRadius, 0.0F, f1, spinSpeed1, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
            renderGem(gem2, hoverHeight, orbitRadius, (float) (2 * Math.PI / 3), f1, spinSpeed2, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
            renderGem(gem3, hoverHeight, orbitRadius, (float) (4 * Math.PI / 3), f1, spinSpeed3, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
        }
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

        float orbitRadius = 0.5F;
        float orbitSpeedMultiplier = 0.05F;

        if (pBlockEntity.isAnimating()) {
            //System.out.println("Rendering with animation!");
            long currentTime = System.currentTimeMillis();
            long animationDuration = currentTime - pBlockEntity.getAnimationStartTime();
            float animationProgress = Math.min(animationDuration / 8000.0F, 1.0F);
            float baseRadius = 0.5F;
            float maxRadius = 1F;
            orbitRadius = baseRadius + (maxRadius - baseRadius) * (1 - (4 * ((animationProgress * 1.25F) - 0.5F) * ((animationProgress * 1.25F) - 0.5F)));

            orbitRadius = Math.max(0.0F, orbitRadius);

            orbitSpeedMultiplier = 0.05F + (-(96.0F * (animationProgress * animationProgress)));

            if (animationProgress >= 1.0F) {
                pBlockEntity.stopAnimation();
            }
        } else {
            //System.out.println("Rendering without animation.");
        }

        renderGems(pPoseStack, pBlockEntity, f1, pPackedLight, pPackedOverlay, pBuffer, orbitRadius, orbitSpeedMultiplier);
    }
}

