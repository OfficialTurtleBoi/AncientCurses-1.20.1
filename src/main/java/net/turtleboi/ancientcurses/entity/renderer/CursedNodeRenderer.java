package net.turtleboi.ancientcurses.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.entities.CursedNodeEntity;
import net.turtleboi.ancientcurses.rite.rites.EmbersRite;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import static net.turtleboi.turtlecore.client.util.VertexBuilder.vertex;

public class CursedNodeRenderer extends EntityRenderer<CursedNodeEntity> {
    public static final ModelLayerLocation CURSED_NODE_LAYER = new ModelLayerLocation(new ResourceLocation(AncientCurses.MOD_ID, "cursed_node"), "main");
    private static final ResourceLocation TEXTURE = new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/cursed_node.png");
    private static final ResourceLocation FLAME_ATLAS_TEXTURE = new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/cursed_fire.png");
    private static final float SIN_45 = (float)Math.sin((Math.PI / 4D));
    private static final float FLAME_FRAME_COUNT = 64.0f;
    private static final float FLAME_FRAME_HEIGHT = 1.0f / FLAME_FRAME_COUNT;
    private static final int START_TINT = 0x32E8EE;
    private static final int FINAL_TINT_A = 0xED4ACC;
    private static final int FINAL_TINT_B = 0xFE4AFF;
    private static final int FINAL_TINT_C = 0xFF51A8;
    private static final float FINAL_TINT_START = 0.85f;
    private static final float FINAL_TINT_CYCLE_SPEED = 0.04f;
    private final ModelPart core;
    private final ModelPart shell;

    public CursedNodeRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.shadowRadius = 0.5F;
        ModelPart modelpart = pContext.bakeLayer(CURSED_NODE_LAYER);
        this.shell = modelpart.getChild("shell");
        this.core = modelpart.getChild("core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("core", CubeListBuilder.create().texOffs(24, 6).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull CursedNodeEntity pEntity) {
        return TEXTURE;
    }

    public @NotNull ResourceLocation getFlatLocation(@NotNull CursedNodeEntity pEntity) {
        return FLAME_ATLAS_TEXTURE;
    }

    @Override
    public void render(CursedNodeEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        float f = getBobOffset(pEntity, pPartialTicks);
        float f1 = ((float)pEntity.time + pPartialTicks) * 3.0F;
        pPoseStack.pushPose();
        float t = Mth.clamp(pEntity.getProgress() / (float) EmbersRite.feedTicks, 0f, 1f);
        float scale = Mth.lerp(t, 0.5f, 2.0f);

        int tintColor;
        if (t < FINAL_TINT_START) {
            float tintT = Mth.clamp(t / FINAL_TINT_START, 0f, 1f);
            tintColor = lerpColor(tintT, START_TINT, FINAL_TINT_A);
        } else {
            float cycleTime = (pEntity.time + pPartialTicks) * FINAL_TINT_CYCLE_SPEED;
            tintColor = getCyclingFinalTint(cycleTime);
        }

        float rf = ((tintColor >> 16) & 0xFF) / 255.0f;
        float gf = ((tintColor >> 8) & 0xFF) / 255.0f;
        float bf = (tintColor & 0xFF) / 255.0f;
        int r = (int)(rf * 255f);
        int g = (int)(gf * 255f);
        int b = (int)(bf * 255f);

        pPoseStack.scale(scale, scale, scale);
        pPoseStack.translate(0.0F, f, 0.0F);

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            double playerX = player.getX();
            double playerZ = player.getZ();
            double entityX = pEntity.getX();
            double entityZ = pEntity.getZ();
            double dirX = playerX - entityX;
            double dirZ = playerZ - entityZ;

            float yaw = (float) (Math.atan2(dirZ, dirX) * (180 / Math.PI)) - 90.0F;

            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
            pPoseStack.translate(0, 0.2, 0);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            VertexConsumer flameConsumer = pBuffer.getBuffer(RenderType.entityTranslucentCull(this.getFlatLocation(pEntity)));
            PoseStack.Pose pose = pPoseStack.last();
            Matrix4f matrix = pose.pose();
            Matrix3f normalMatrix = pose.normal();
            int vertexAlpha = 240;
            int frameIndex = pEntity.getTextureIndex();
            float minU = 0.0f;
            float maxU = 1.0f;
            float minV = frameIndex * FLAME_FRAME_HEIGHT;
            float maxV = minV + FLAME_FRAME_HEIGHT;

            vertex(flameConsumer, matrix, normalMatrix, -0.5f, -0.5f, 0, minU, minV, r, g, b, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, 0.5f, -0.5f, 0, maxU, minV, r, g, b, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, 0.5f, 0.5f, 0, maxU, maxV, r, g, b, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, -0.5f, 0.5f, 0, minU, maxV, r, g, b, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, -0.5f, 0.5f, 0, minU, maxV, r, g, b, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, 0.5f, 0.5f, 0, maxU, maxV, r, g, b, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, 0.5f, -0.5f, 0, maxU, minV, r, g, b, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, -0.5f, -0.5f, 0, minU, minV, r, g, b, vertexAlpha);
            pPoseStack.popPose();
        }

        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f1));
        pPoseStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        this.shell.render(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, rf, gf, bf, 1.0f);
        pPoseStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f1));
        this.core.render(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, rf, gf, bf, 1.0f);
        pPoseStack.popPose();
        pPoseStack.popPose();

        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    public static float getBobOffset(CursedNodeEntity cursedNode, float partialTick) {
        float time = cursedNode.time + partialTick;
        float speed = 0.075F;
        float amplitude = 0.03F;
        return Mth.sin(time * speed) * amplitude;
    }

    private static int lerpColor(float progress, int startColor, int endColor) {
        int r = Mth.floor(Mth.lerp(progress, (startColor >> 16) & 0xFF, (endColor >> 16) & 0xFF));
        int g = Mth.floor(Mth.lerp(progress, (startColor >> 8) & 0xFF, (endColor >> 8) & 0xFF));
        int b = Mth.floor(Mth.lerp(progress, startColor & 0xFF, endColor & 0xFF));
        return (r << 16) | (g << 8) | b;
    }

    private static int getCyclingFinalTint(float time) {
        int[] colors = {FINAL_TINT_A, FINAL_TINT_B, FINAL_TINT_C, FINAL_TINT_A};
        float cycle = time % 3.0f;
        int index = (int) cycle;
        float localT = cycle - index;
        return lerpColor(localT, colors[index], colors[index + 1]);
    }
}
