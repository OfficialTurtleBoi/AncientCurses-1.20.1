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
import net.minecraft.world.phys.HitResult;
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
    private double hitDistance;

    public FirstBeaconItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack pItemStack = pPlayer.getItemInHand(pUsedHand);
        setRemainingUseDuration(getUseDuration(pItemStack));

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
            float chargeProgress = Math.min(1.0f, (float) (getUseDuration(pStack) - getRemainingUseDuration()) / maxChargeTicks);
            Vec3 endVec = startVec.add(lookVec.scale(range));
            setRemainingUseDuration(pRemainingUseDuration);

            HitResult result = TargetingUtils.rayTraceClosest(pPlayer, startVec, endVec);
            double hitDistance = startVec.distanceTo(result.getLocation());
            setHitDistance(hitDistance);

            List<EntityHitResult> hitResults = TargetingUtils.rayTraceEntities(pPlayer, startVec, endVec);
            if (!hitResults.isEmpty() && chargeProgress > 0.35f) {
                float baseDamage = 10.0f * chargeProgress;
                for (int i = 0; i < hitResults.size(); i++) {
                    EntityHitResult hit = hitResults.get(i);
                    if (hit.getEntity() instanceof LivingEntity targetEntity) {
                        float damageMultiplier = 1.0f - (i * 0.05f);
                        damageMultiplier = Math.max(0, damageMultiplier);

                        float adjustedDamage = baseDamage * damageMultiplier;
                        //System.out.println("Hurting " + targetEntity + " with " + adjustedDamage + " damage (multiplier: " + damageMultiplier + ")");
                        targetEntity.hurt(pPlayer.level().damageSources().magic(), adjustedDamage);
                    }
                }
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
        beingUsed = false;
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

    public double getHitDistance(){
        return hitDistance;
    }

    public void setHitDistance(double hitDistance) {
        this.hitDistance = hitDistance;
    }
}
