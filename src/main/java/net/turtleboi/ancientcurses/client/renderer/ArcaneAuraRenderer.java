package net.turtleboi.ancientcurses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.item.items.FirstBeaconItem;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArcaneAuraRenderer {
    private final long spawnTime;
    private final int totalAnimationTime;
    private final double amplifier;
    private final int delayTicks;

    public static final ResourceLocation ARCANE_AURA_TEXTURE = new ResourceLocation(AncientCurses.MOD_ID, "textures/gui/arcane_aura.png");
    public static final Map<UUID, List<ArcaneAuraRenderer>> ENTITY_AURAS = new ConcurrentHashMap<>();

    public ArcaneAuraRenderer(long currentTime, int totalAnimationTime, double amplifier, int delayTicks) {
        this.spawnTime = currentTime;
        this.totalAnimationTime = totalAnimationTime;
        this.amplifier = amplifier;
        this.delayTicks = delayTicks;
    }

    public static void addAuraForEntity(LivingEntity livingEntity, long currentTime, int totalAnimationTime, double amplifier) {
        UUID uuid = livingEntity.getUUID();
        List<ArcaneAuraRenderer> auraList = ENTITY_AURAS.computeIfAbsent(uuid, key -> new CopyOnWriteArrayList<>());
        int delay = auraList.size() * 10;
        auraList.add(new ArcaneAuraRenderer(currentTime, totalAnimationTime, amplifier, delay));
    }

    public static void renderAuras(MultiBufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        UUID uuid = livingEntity.getUUID();
        List<ArcaneAuraRenderer> auraList = ENTITY_AURAS.get(uuid);
        if (auraList != null) {
            auraList.removeIf(ArcaneAuraRenderer::isExpired);

            for (ArcaneAuraRenderer aura : auraList) {
                aura.renderThirdPersonAura(bufferSource, poseStack, livingEntity, partialTicks);
            }
        }
    }

    public void renderThirdPersonAura(MultiBufferSource bufferSource, PoseStack poseStack, LivingEntity livingEntity, float partialTicks) {
        poseStack.pushPose();
        float ticksElapsed = (System.currentTimeMillis() % spawnTime) / 50.0f;
        float tickCount = ticksElapsed + partialTicks - delayTicks;

        if (tickCount < 0) {
            poseStack.popPose();
            return;
        }

        float initialTicks = 5.0f;
        float scale;

        float rotationSpeed;
        if (tickCount < initialTicks) {
            rotationSpeed = 3.0f;
        } else {
            rotationSpeed = (float) (10.0f * amplifier);
        }

        float rotationAngle = tickCount * rotationSpeed;

        if (tickCount < initialTicks) {
            scale = 0.25f * (tickCount / initialTicks);
        } else {
            scale = 0.25f;
        }

        float alpha;
        if (tickCount < (totalAnimationTime * 0.75f)) {
            alpha = 1.0f;
        } else if (tickCount > totalAnimationTime){
            alpha = 0.0f;
        } else {
            alpha = 1.0f - ((tickCount - (totalAnimationTime * 0.75f)) / (totalAnimationTime - (totalAnimationTime * 0.75f)));
        }

        int vertexAlpha = (int)(alpha * 255.0f);

        float yPosition;
        if (tickCount < initialTicks) {
            yPosition = (float) (livingEntity.getBbHeight() * 0.01);
        } else {
            yPosition = (float) Math.min(livingEntity.getBbHeight() * 1.1, ((livingEntity.getBbHeight() * 0.01) + ((tickCount - initialTicks) / ((totalAnimationTime * 0.75f) - initialTicks)) * (livingEntity.getBbHeight())));
        }

        poseStack.translate(0, yPosition, 0);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-livingEntity.getYRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotationAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.scale(scale, scale, scale);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(ARCANE_AURA_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        vertex(vertexConsumer, matrix, normalMatrix, -6, -6, 0, 0, 0, 255, 255, 255, vertexAlpha);
        vertex(vertexConsumer, matrix, normalMatrix, 6, -6, 0, 1, 0, 255, 255, 255, vertexAlpha);
        vertex(vertexConsumer, matrix, normalMatrix, 6, 6, 0, 1, 1, 255, 255, 255, vertexAlpha);
        vertex(vertexConsumer, matrix, normalMatrix, -6, 6, 0, 0, 1, 255, 255, 255, vertexAlpha);

        vertex(vertexConsumer, matrix, normalMatrix, -6, 6, 0, 0, 1, 255, 255, 255, vertexAlpha);
        vertex(vertexConsumer, matrix, normalMatrix, 6, 6, 0, 1, 1, 255, 255, 255, vertexAlpha);
        vertex(vertexConsumer, matrix, normalMatrix, 6, -6, 0, 1, 0, 255, 255, 255, vertexAlpha);
        vertex(vertexConsumer, matrix, normalMatrix, -6, -6, 0, 0, 0, 255, 255, 255, vertexAlpha);

        poseStack.popPose();
    }

    public static void renderFirstPersonAura(MultiBufferSource bufferSource, PoseStack poseStack, InteractionHand interactionHand, ItemStack itemStack) {
        if (itemStack.getItem() instanceof FirstBeaconItem beaconItem && beaconItem.isBeingUsed()) {
            poseStack.pushPose();
            float ticksElapsed = beaconItem.getUseDuration(itemStack) - beaconItem.getRemainingUseDuration();
            float progress = Math.min(1.0f, ticksElapsed / beaconItem.getMaxChargeTicks());

            if (ticksElapsed < 0) {
                poseStack.popPose();
                return;
            }

            float initialTicks = (float) beaconItem.getMaxChargeTicks() / 10;
            float scale = 0.25f * progress;

            float rotationSpeed;
            if (ticksElapsed < initialTicks) {
                rotationSpeed = 3.0f;
            } else {
                rotationSpeed = 25f * progress;
            }

            float rotationAngle = ticksElapsed * rotationSpeed;

            if (ticksElapsed > initialTicks) {
                scale = 0.175f + (0.175f * progress);
            }

            float alpha = progress;
            if (ticksElapsed > initialTicks) {
                alpha = 1.0f;
            }

            int vertexAlpha = (int) (alpha * 255.0f);

            double handOffset = 0;
            float tiltOffset = 0;
            if (interactionHand == InteractionHand.MAIN_HAND) {
                handOffset = 1.25;
                tiltOffset = 5;
            } else if (interactionHand == InteractionHand.OFF_HAND) {
                handOffset = -1.25;
                tiltOffset = -5;
            }

            poseStack.translate(handOffset, 0, -2);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            //poseStack.mulPose(Axis.YP.rotationDegrees(-livingEntity.getYRot()));
            poseStack.mulPose(Axis.YP.rotationDegrees(tiltOffset));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-rotationAngle));
            poseStack.scale(scale, scale, scale);

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(ARCANE_AURA_TEXTURE));
            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();
            Matrix3f normalMatrix = pose.normal();

            vertex(vertexConsumer, matrix, normalMatrix, -6, -6, 0, 0, 0, 255, 255, 255, vertexAlpha);
            vertex(vertexConsumer, matrix, normalMatrix, 6, -6, 0, 1, 0, 255, 255, 255, vertexAlpha);
            vertex(vertexConsumer, matrix, normalMatrix, 6, 6, 0, 1, 1, 255, 255, 255, vertexAlpha);
            vertex(vertexConsumer, matrix, normalMatrix, -6, 6, 0, 0, 1, 255, 255, 255, vertexAlpha);

            vertex(vertexConsumer, matrix, normalMatrix, -6, 6, 0, 0, 1, 255, 255, 255, vertexAlpha);
            vertex(vertexConsumer, matrix, normalMatrix, 6, 6, 0, 1, 1, 255, 255, 255, vertexAlpha);
            vertex(vertexConsumer, matrix, normalMatrix, 6, -6, 0, 1, 0, 255, 255, 255, vertexAlpha);
            vertex(vertexConsumer, matrix, normalMatrix, -6, -6, 0, 0, 0, 255, 255, 255, vertexAlpha);

            poseStack.popPose();
        }
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix, Matrix3f normalMatrix,
                               int x, int y, int z, float u, float v, int red, int green, int blue, int vertexAlpha) {
        vertexConsumer.vertex(matrix, (float)x, (float)y, (float)z)
                .color(red, green, blue, vertexAlpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normalMatrix,0, 0, 1)
                .endVertex();;
    }

    public boolean isExpired() {
        long effectiveSpawnTime = spawnTime + delayTicks * 50L;
        return (System.currentTimeMillis() - effectiveSpawnTime) > (totalAnimationTime * 50L);
    }
}
