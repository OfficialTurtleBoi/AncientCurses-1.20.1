package net.turtleboi.ancientcurses.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.animations.AncientWraithAnimations;
import net.turtleboi.ancientcurses.entity.entities.AncientWraithEntity;

public class AncientWraithModel <T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation ANCIENT_WRAITH_LAYER = new ModelLayerLocation(
            new ResourceLocation(AncientCurses.MOD_ID, "ancient_wraith"), "main");
    private final ModelPart ancient_wraith;
    private final ModelPart body;
    private final ModelPart cloak;
    private final ModelPart left_cloak;
    private final ModelPart right_cloak;
    private final ModelPart wings;
    private final ModelPart left_wing;
    private final ModelPart right_wing;
    private final ModelPart head;

    public AncientWraithModel(ModelPart root) {
        this.ancient_wraith = root.getChild("ancient_wraith");
        this.body = this.ancient_wraith.getChild("body");
        this.cloak = this.body.getChild("cloak");
        this.left_cloak = this.cloak.getChild("left_cloak");
        this.right_cloak = this.cloak.getChild("right_cloak");
        this.wings = this.body.getChild("wings");
        this.left_wing = this.wings.getChild("left_wing");
        this.right_wing = this.wings.getChild("right_wing");
        this.head = this.ancient_wraith.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ancient_wraith = partdefinition.addOrReplaceChild("ancient_wraith", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition body = ancient_wraith.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 10).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 33).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cloak = body.addOrReplaceChild("cloak", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition left_cloak = cloak.addOrReplaceChild("left_cloak", CubeListBuilder.create().texOffs(40, 34).addBox(0.25F, 0.25F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -12.25F, 0.0F));

        PartDefinition right_cloak = cloak.addOrReplaceChild("right_cloak", CubeListBuilder.create().texOffs(24, 34).addBox(-4.25F, 0.25F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -12.25F, 0.0F));

        PartDefinition wings = body.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offset(0.0F, 0.5F, 0.0F));

        PartDefinition left_wing = wings.addOrReplaceChild("left_wing", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -2.5F, -2.0F, -0.2618F, 0.0F, 0.0F));

        PartDefinition left_bottom_r1 = left_wing.addOrReplaceChild("left_bottom_r1", CubeListBuilder.create().texOffs(0, 23).addBox(0.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0154F, 0.1739F, 0.6968F));

        PartDefinition left_mid_r1 = left_wing.addOrReplaceChild("left_mid_r1", CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.2618F, 0.0F));

        PartDefinition left_top_r1 = left_wing.addOrReplaceChild("left_top_r1", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, -0.0406F, 0.4346F, -0.8816F));

        PartDefinition right_wing = wings.addOrReplaceChild("right_wing", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -2.5F, -2.0F, 0.2618F, 0.0F, -3.1416F));

        PartDefinition right_bottom_r1 = right_wing.addOrReplaceChild("right_bottom_r1", CubeListBuilder.create().texOffs(0, 23).addBox(0.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1262F, 0.1739F, -0.6968F));

        PartDefinition right_mid_r1 = right_wing.addOrReplaceChild("right_mid_r1", CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 3.0543F, 0.2618F, 0.0F));

        PartDefinition right_top_r1 = right_wing.addOrReplaceChild("right_top_r1", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -2.5F, 0.0F, 16.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, -3.101F, 0.4346F, 0.8816F));

        PartDefinition head = ancient_wraith.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 22).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        AncientWraithEntity ancientWraith = (AncientWraithEntity) entity;

        this.applyHeadRotation(netHeadYaw, headPitch);
        //this.animateWalk(AncientWraithAnimations.WALKING_ANIMATION, limbSwing, limbSwingAmount, 2f, 2.4f);
        float bodyPitch = ancientWraith.getDragPitch();
        this.body.xRot += bodyPitch;

        float partsPitch = ancientWraith.getPartsPitch();

        float maxHeadDown = 1f;
        float maxHeadForward = 4.0f;

        this.head.y += maxHeadDown * partsPitch;
        this.head.z += maxHeadForward * partsPitch;

        float cloakMaxRadians = 15.0f * Mth.DEG_TO_RAD;
        this.left_cloak.zRot = -cloakMaxRadians * partsPitch;
        this.right_cloak.zRot = cloakMaxRadians * partsPitch;
        this.animate(ancientWraith.idleAnimationState, AncientWraithAnimations.IDLE_ANIMATION, ageInTicks, 1f);
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 45.0F);

        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = -pHeadPitch * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        ancient_wraith.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return ancient_wraith;
    }
}
