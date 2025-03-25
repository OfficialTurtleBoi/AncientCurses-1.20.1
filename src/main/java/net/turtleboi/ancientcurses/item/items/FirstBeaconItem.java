package net.turtleboi.ancientcurses.item.items;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.enchantment.ModEnchantments;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.turtlecore.util.TargetingUtils;

import java.util.List;

public class FirstBeaconItem extends Item {
    public static double range = 64.0F;
    private static int maxChargeTicks = 100;
    private int remainingUseDuration;
    private boolean beingUsed;

    public FirstBeaconItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack pItemStack = pPlayer.getItemInHand(pUsedHand);

        pPlayer.startUsingItem(pUsedHand);
        return InteractionResultHolder.success(pItemStack);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
        if (pLivingEntity instanceof Player pPlayer) {
            beingUsed = true;
            Vec3 lookVec = pPlayer.getLookAngle();
            Vec3 startVec = pPlayer.getEyePosition(1.0f);
            Vec3 endVec = startVec.add(lookVec.scale(range));
            setRemainingUseDuration(pRemainingUseDuration);
            int chargeProgress = getUseDuration(pStack) - getRemainingUseDuration();

            List<EntityHitResult> hitResults = TargetingUtils.rayTraceEntities(pPlayer, startVec, endVec);
            if (!hitResults.isEmpty()) {
                float baseDamage = 5.0f * Math.min(1.0f, (float) chargeProgress / maxChargeTicks);
                for (int i = 0; i < hitResults.size(); i++) {
                    EntityHitResult hit = hitResults.get(i);
                    if (hit.getEntity() instanceof LivingEntity targetEntity) {
                        float damageMultiplier = 1.0f - (i * 0.05f);
                        damageMultiplier = Math.max(0, damageMultiplier);

                        float adjustedDamage = baseDamage * damageMultiplier;
                        //System.out.println("Hurting " + targetEntity + " with " + adjustedDamage + " damage (multiplier: " + damageMultiplier + ")");
                        targetEntity.hurt(pLevel.damageSources().magic(), adjustedDamage);
                    }
                }
            }
        }
    }


    @Override
    public boolean useOnRelease(ItemStack pStack) {
        beingUsed = false;
        return super.useOnRelease(pStack);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    public boolean isBeingUsed(){
        return beingUsed;
    }

    public int getMaxChargeTicks(){
        return maxChargeTicks;
    }

    public int getRemainingUseDuration(){
        return remainingUseDuration;
    }

    public void setRemainingUseDuration(int remainingUseDuration) {
        this.remainingUseDuration = remainingUseDuration;
    }
}
