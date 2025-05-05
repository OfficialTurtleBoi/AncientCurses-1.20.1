package net.turtleboi.ancientcurses.entity.client.renderer;

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
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.CursedNodeEntity;
import net.turtleboi.ancientcurses.rites.EmbersRite;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import static net.turtleboi.turtlecore.client.util.VertexBuilder.vertex;

public class CursedNodeRenderer extends EntityRenderer<CursedNodeEntity> {
    public static final ModelLayerLocation CURSED_NODE_LAYER = new ModelLayerLocation(new ResourceLocation(AncientCurses.MOD_ID, "cursed_node"), "main");
    private static final ResourceLocation TEXTURE = new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/cursed_node.png");
    private static final float SIN_45 = (float)Math.sin((Math.PI / 4D));
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
        int textureIndex = pEntity.getTextureIndex();
        return new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/cursed_flame/cursed_fire" + textureIndex + ".png");
    }

    @Override
    public void render(CursedNodeEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        float f = getBobOffset(pEntity, pPartialTicks);
        float f1 = ((float)pEntity.time + pPartialTicks) * 3.0F;
        pPoseStack.pushPose();
        float t = Mth.clamp(pEntity.getProgress() / (float) EmbersRite.feedTicks, 0f, 1f);
        float scale = Mth.lerp(t, 0.5f, 2.0f);

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
            vertex(flameConsumer, matrix, normalMatrix, -0.5f, -0.5f, 0, 0, 0, 255, 255, 255, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, 0.5f, -0.5f, 0, 1, 0, 255, 255, 255, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, 0.5f, 0.5f, 0, 1, 1, 255, 255, 255, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, -0.5f, 0.5f, 0, 0, 1, 255, 255, 255, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, -0.5f, 0.5f, 0, 0, 1, 255, 255, 255, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, 0.5f, 0.5f, 0, 1, 1, 255, 255, 255, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, 0.5f, -0.5f, 0, 1, 0, 255, 255, 255, vertexAlpha);
            vertex(flameConsumer, matrix, normalMatrix, -0.5f, -0.5f, 0, 0, 0, 255, 255, 255, vertexAlpha);
            pPoseStack.popPose();
        }

        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f1));
        pPoseStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        this.shell.render(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY);
        pPoseStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f1));
        this.core.render(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY);
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
}
