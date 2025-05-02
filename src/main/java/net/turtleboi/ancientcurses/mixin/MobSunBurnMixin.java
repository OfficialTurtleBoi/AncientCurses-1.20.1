package net.turtleboi.ancientcurses.mixin;

import net.minecraft.world.entity.Mob;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobSunBurnMixin {
    @Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
    protected void onIsSunBurnTick(CallbackInfoReturnable<Boolean> value) {
        Mob self = (Mob)(Object)this;
        if (self.getPersistentData().getBoolean(CursedAltarBlockEntity.CURSED_SPAWN)){
            value.setReturnValue(false);
        }
    }
}
