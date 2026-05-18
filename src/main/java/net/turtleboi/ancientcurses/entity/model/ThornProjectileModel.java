package net.turtleboi.ancientcurses.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.turtleboi.ancientcurses.AncientCurses;

public class ThornProjectileModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation THORN_PROJECTILE_LAYER =
            new ModelLayerLocation(new ResourceLocation(AncientCurses.MOD_ID, "thorn_projectile"), "main");

    private final ModelPart thorn;

    public ThornProjectileModel(ModelPart root) {
        this.thorn = root.getChild("thorn");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition thorn = root.addOrReplaceChild(
                "thorn",
                CubeListBuilder.create().texOffs(8, 8)
                        .addBox(-2.0F, -10.0F, 5.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        thorn.addOrReplaceChild(
                "face2_r1",
                CubeListBuilder.create().texOffs(0, -8)
                        .addBox(0.0F, -2.0F, -6.0F, 0.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));
        thorn.addOrReplaceChild(
                "face1_r1",
                CubeListBuilder.create().texOffs(0, -12)
                        .addBox(0.0F, -2.0F, -6.0F, 0.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -3.1416F, 0.0F, -1.5708F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        thorn.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
