package net.turtleboi.ancientcurses.entity.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.entities.VoodooSoulEntity;
import org.jetbrains.annotations.NotNull;

public class VoodooSoulRenderer extends EntityRenderer<VoodooSoulEntity> {
    private static final ResourceLocation FALLBACK_TEXTURE =
            new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/ancient_wraith.png");
    private static final float SOUL_RED = 0.15F;
    private static final float SOUL_GREEN = 1.0F;
    private static final float SOUL_BLUE = 0.9F;
    private static final float SOUL_ALPHA = 0.48F;

    private final EntityRenderDispatcher renderDispatcher;

    public VoodooSoulRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.renderDispatcher = context.getEntityRenderDispatcher();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(VoodooSoulEntity soul, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        Entity body = Minecraft.getInstance().level == null
                ? null
                : Minecraft.getInstance().level.getEntity(soul.getBodyEntityId());
        if (!(body instanceof LivingEntity livingBody) || body == soul) {
            super.render(soul, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.pushPose();
        poseStack.translate(0.0D, -livingBody.getBbHeight() * 0.5D, 0.0D);
        renderDispatcher.render(livingBody, 0.0D, 0.0D, 0.0D, soul.getYRot(), partialTick, poseStack,
                new SoulTintBufferSource(bufferSource), packedLight);
        poseStack.popPose();
        RenderSystem.disableBlend();

        super.render(soul, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull VoodooSoulEntity entity) {
        return FALLBACK_TEXTURE;
    }

    private record SoulTintBufferSource(MultiBufferSource delegate) implements MultiBufferSource {
        @Override
        public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
            return new SoulTintVertexConsumer(delegate.getBuffer(renderType));
        }
    }

    private record SoulTintVertexConsumer(VertexConsumer delegate) implements VertexConsumer {
        @Override
        public @NotNull VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public @NotNull VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color((int) (red * SOUL_RED), (int) (green * SOUL_GREEN), (int) (blue * SOUL_BLUE),
                    (int) (alpha * SOUL_ALPHA));
            return this;
        }

        @Override
        public @NotNull VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor((int) (red * SOUL_RED), (int) (green * SOUL_GREEN), (int) (blue * SOUL_BLUE),
                    (int) (alpha * SOUL_ALPHA));
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}
