package net.turtleboi.ancientcurses.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.turtleboi.ancientcurses.event.ModClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends AbstractClientPlayer> {
    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void injectBeaconArmPose(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!ModClientEvents.isBeaconArmPoseActive()) {
            return;
        }
        PlayerModel<?> model = (PlayerModel<?>) (Object) this;
        model.rightArm.xRot = -(Mth.PI / 2.0f) + model.head.xRot;
        model.rightArm.yRot = -0.1f + model.head.yRot;
        model.leftArm.xRot = -(Mth.PI / 2.0f) + model.head.xRot;
        model.leftArm.yRot = 0.1f + model.head.yRot + 0.4f;
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
    }
}
