package net.turtleboi.ancientcurses.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Slime.class)
public abstract class SlimeMixin {

    @Inject(
            method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void ancientcurses$tagSplitChildren(Entity.RemovalReason reason, CallbackInfo ci, int i, Component component,
                                                boolean flag, float f, int j, int k, int l, float f1, float f2, Slime slime) {
        Slime parentSlime = (Slime)(Object)this;
        if (!parentSlime.level().isClientSide && parentSlime.isDeadOrDying() && parentSlime.getPersistentData().getBoolean(CursedAltarBlockEntity.CURSED_SPAWN)) {
            slime.getPersistentData().putBoolean(CursedAltarBlockEntity.CURSED_SPAWN, true);
        }
    }
}
