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
    private static final float windupDurationMs = 4000.0F;
    private static final float windupBaseRadius = 0.5F;
    private static final float windupRadiusDelta = 0.5F;
    private static final float windupBaseHeight = 1.0F;
    private static final float windupTargetHeight = 1.25F;
    private static final float windupBaseOrbitSpeed = 0.05F;
    private static final float holdOrbitSpeed = 0.42F;
    private static final float resolveDurationMs = 2400.0F;
    private static final float resolveBaseOrbitSpeed = 0.22F;
    private static final float resolveOrbitSpeedDelta = 0.04F;
    private final BookModel bookModel;
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public CursedAltarRenderer(BlockEntityRendererProvider.Context pContext) {
        this.bookModel = new BookModel(pContext.bakeLayer(ModelLayers.BOOK));
    }

    private void renderOrbitingItem(ItemStack stack, float hoverHeight, float orbitRadius, float baseAngle, float f1, float spinSpeed, float orbitSpeedMultiplier, PoseStack pPoseStack, int pPackedLight, int pPackedOverlay, MultiBufferSource pBuffer, BlockEntity pBlockEntity) {
        if (!stack.isEmpty()) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5F, hoverHeight, 0.5F);
            float orbitAngle = baseAngle + (f1 * orbitSpeedMultiplier);
            pPoseStack.translate(orbitRadius * Math.cos(orbitAngle), 0.0, orbitRadius * Math.sin(orbitAngle));
            pPoseStack.mulPose(Axis.YP.rotation(spinSpeed));
            this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }
    }

    private void renderGems(PoseStack pPoseStack, CursedAltarBlockEntity pBlockEntity, float f1, int pPackedLight, int pPackedOverlay, MultiBufferSource pBuffer, float orbitRadius, float orbitSpeedMultiplier, float hoverHeight) {
        ItemStack gem1 = pBlockEntity.getGemInSlot(0);
        ItemStack gem2 = pBlockEntity.getGemInSlot(1);
        ItemStack gem3 = pBlockEntity.getGemInSlot(2);

        float spinSpeed1 = (f1 % 360) * (float) Math.PI / 180.0F;
        float spinSpeed2 = (f1 + 120 % 360 + 120) * (float) Math.PI / 180.0F;
        float spinSpeed3 = (f1 + 240 % 360 + 240) * (float) Math.PI / 180.0F;

        int gemCount = (!gem1.isEmpty() ? 1 : 0) + (!gem2.isEmpty() ? 1 : 0) + (!gem3.isEmpty() ? 1 : 0);

        if (gemCount == 1) {
            renderOrbitingItem(gem1, hoverHeight, 0.0F, 0.0F, f1, spinSpeed1, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
        } else if (gemCount == 2) {
            renderOrbitingItem(gem1, hoverHeight, orbitRadius, 0.0F, f1, spinSpeed1, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
            renderOrbitingItem(gem2, hoverHeight, orbitRadius, (float) Math.PI, f1, spinSpeed2, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
        } else if (gemCount == 3) {
            renderOrbitingItem(gem1, hoverHeight, orbitRadius, 0.0F, f1, spinSpeed1, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
            renderOrbitingItem(gem2, hoverHeight, orbitRadius, (float) (2 * Math.PI / 3), f1, spinSpeed2, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
            renderOrbitingItem(gem3, hoverHeight, orbitRadius, (float) (4 * Math.PI / 3), f1, spinSpeed3, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
        }
    }

    private void renderRitualItems(PoseStack pPoseStack, CursedAltarBlockEntity pBlockEntity, float f1, int pPackedLight, int pPackedOverlay, MultiBufferSource pBuffer) {
        float hoverHeight = 1.25F + Mth.sin(f1 * 0.1F) * 0.04F;
        float catalystSpinSpeed = (f1 % 360) * (float) Math.PI / 180.0F;
        ItemStack catalyst = pBlockEntity.getRitualItemInSlot(0);

        if (!catalyst.isEmpty()) {
            renderOrbitingItem(catalyst, hoverHeight, 0.0F, 0.0F, f1, catalystSpinSpeed, 0.0F, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
        }

        ItemStack[] ingredientStacks = new ItemStack[8];
        int ingredientCount = 0;
        for (int slot = 1; slot <= 8; slot++) {
            ItemStack ingredient = pBlockEntity.getRitualItemInSlot(slot);
            if (!ingredient.isEmpty()) {
                ingredientStacks[ingredientCount++] = ingredient;
            }
        }

        if (ingredientCount == 0) {
            return;
        }

        float orbitRadius = 0.525F;
        float orbitSpeedMultiplier = 0.035F;
        for (int i = 0; i < ingredientCount; i++) {
            float baseAngle = ((float) (Math.PI * 2) / ingredientCount) * i;
            float spinSpeed = ((f1 + (i * (360.0F / ingredientCount))) % 360.0F) * (float) Math.PI / 180.0F;
            renderOrbitingItem(ingredientStacks[i], hoverHeight - 0.25F, orbitRadius, baseAngle, f1, spinSpeed, orbitSpeedMultiplier, pPoseStack, pPackedLight, pPackedOverlay, pBuffer, pBlockEntity);
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
        float hoverHeight = windupTargetHeight + Mth.sin(f1 * 0.1F) * 0.05F;

        if (pBlockEntity.hasPendingGemFusion() && !pBlockEntity.isResolvingPendingGemFusion()) {
            long currentTime = System.currentTimeMillis();
            float transitionProgress = 1.0F;
            if (pBlockEntity.getAnimationStartTime() > 0L) {
                transitionProgress = Math.min((currentTime - pBlockEntity.getAnimationStartTime()) / windupDurationMs, 1.0F);
            }

            float easedProgress = transitionProgress * transitionProgress * (3.0F - 2.0F * transitionProgress);
            orbitRadius = windupBaseRadius + (windupRadiusDelta * easedProgress);
            orbitSpeedMultiplier = Mth.lerp(easedProgress, windupBaseOrbitSpeed, holdOrbitSpeed);
            hoverHeight = Mth.lerp(easedProgress, windupBaseHeight, windupTargetHeight)
                    + Mth.sin(f1 * 0.1F) * 0.05F;
        } else if (pBlockEntity.hasPendingGemFusion() && pBlockEntity.isResolvingPendingGemFusion()) {
            long currentTime = System.currentTimeMillis();
            float resolveProgress = 1.0F;
            if (pBlockEntity.getAnimationStartTime() > 0L) {
                resolveProgress = Math.min((currentTime - pBlockEntity.getAnimationStartTime()) / resolveDurationMs, 1.0F);
            }

            float easedProgress = 1.0F - (float) Math.pow(1.0F - resolveProgress, 3.0F);
            orbitRadius = Math.max(0.0F, 1.0F - easedProgress);
            orbitSpeedMultiplier = resolveBaseOrbitSpeed - (resolveOrbitSpeedDelta * easedProgress);
        } else if (pBlockEntity.isAnimating()) {
            //System.out.println("Rendering with animation!");
            long currentTime = System.currentTimeMillis();
            long animationDuration = currentTime - pBlockEntity.getAnimationStartTime();
            float animationProgress = Math.min(animationDuration / 8000.0F, 1.0F);
            float baseRadius = 0.5F;
            float maxRadius = 1F;
            orbitRadius = baseRadius + (maxRadius - baseRadius) * (1 - (4 * ((animationProgress * 1.25F) - 0.5F) * ((animationProgress * 1.25F) - 0.5F)));

            orbitRadius = Math.max(0.0F, orbitRadius);

            orbitSpeedMultiplier = 0.05F + (0.18F * animationProgress) + (0.12F * animationProgress * animationProgress);

            //if (animationProgress >= 1.0F) {
            //    pBlockEntity.stopAnimation();
            //}
        } else {
            //System.out.println("Rendering without animation.");
        }

        renderGems(pPoseStack, pBlockEntity, f1, pPackedLight, pPackedOverlay, pBuffer, orbitRadius, orbitSpeedMultiplier, hoverHeight);
        renderRitualItems(pPoseStack, pBlockEntity, f1, pPackedLight, pPackedOverlay, pBuffer);
    }
}
