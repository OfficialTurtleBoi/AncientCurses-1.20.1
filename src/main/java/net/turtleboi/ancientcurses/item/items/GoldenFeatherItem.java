package net.turtleboi.ancientcurses.item.items;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.enchantment.ModEnchantments;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.turtlecore.client.renderer.ShockwaveRenderer;
import net.turtleboi.turtlecore.spells.ShockwaveSpell;
import org.jetbrains.annotations.NotNull;

public class GoldenFeatherItem extends Item {
    public GoldenFeatherItem(Properties pProperties) {
        super(pProperties);
    }

    private boolean stopParticles = true;

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack pItemStack = pPlayer.getItemInHand(pUsedHand);

        // ✅ Only start using if the feather can dash
        if (canDash(pItemStack)) {
            pPlayer.startUsingItem(pUsedHand);
            return InteractionResultHolder.success(pItemStack);
        } else {
            return InteractionResultHolder.fail(pItemStack);
        }
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
        if (pLivingEntity instanceof Player pPlayer) {
            // ✅ Only dash if the feather can dash
            if (canDash(pStack) && pRemainingUseDuration > 1) {
                int soaringLevel = pStack.getEnchantmentLevel(ModEnchantments.SOARING.get());

                Vec3 playerLook = pPlayer.getViewVector(1);
                double dashmodifier = 1 + soaringLevel * 0.42;
                Vec3 dashVec = new Vec3(
                        (playerLook.x() * 2) * dashmodifier,
                        playerLook.y() * 0.5 * dashmodifier + 0.4,
                        (playerLook.z() * 1.8) * dashmodifier
                );
                pPlayer.setDeltaMovement(dashVec);

                for (int j = 0; j < 5; j++) {
                    pLevel.addParticle(ModParticleTypes.GOLDEN_FEATHER_PARTICLE.get(),
                            pPlayer.getX() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getY() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getZ() + (pPlayer.getRandom().nextDouble() - 0.5),
                            0.0, 0.1, 0.0);
                }
            } else if (pRemainingUseDuration <= 1) {
                releaseUsing(pStack, pLevel, pLivingEntity, getUseDuration(pStack));
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
        int tailwindLevel = pStack.getEnchantmentLevel(ModEnchantments.TAILWIND.get());
        if (pLivingEntity instanceof Player pPlayer) {

            // ✅ Only damage and cooldown if the feather can dash
            if (canDash(pStack)) {
                pStack.hurtAndBreak(1, pPlayer, (player) -> player.broadcastBreakEvent(pPlayer.getUsedItemHand()));

                int cooldownReduction = 15 * tailwindLevel;
                pPlayer.getCooldowns().addCooldown(this, 120 - cooldownReduction);
                pPlayer.awardStat(Stats.ITEM_USED.get(this));

                if (!pPlayer.onGround()) {
                    stopParticles = false;
                }
            } else {
                // Optional: feedback when the feather is "too damaged"
                pPlayer.getCooldowns().addCooldown(this, 40);
            }
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack) {
        return 10;
    }

    public static boolean canDash(ItemStack pGoldenFeatherStack) {
        return pGoldenFeatherStack.getDamageValue() < pGoldenFeatherStack.getMaxDamage() - 1;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pEntity instanceof Player pPlayer) {
            int seismicLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.SEISMIC.get(), pPlayer);
            int zephyrLevel = pStack.getEnchantmentLevel(ModEnchantments.ZEPHYR_RUSH.get());

            if (!stopParticles) {
                for (int j = 0; j < 3; j++) {
                    pLevel.addParticle(ModParticleTypes.GOLDEN_FEATHER_PARTICLE.get(),
                            pPlayer.getX() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getY() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getZ() + (pPlayer.getRandom().nextDouble() - 0.5),
                            0.0, 0.1, 0.0);
                }

                if (pPlayer.onGround() || pPlayer.isSwimming()) {
                    if (seismicLevel > 0) {
                        int radius = 5;
                        ShockwaveRenderer.triggerShockwave(pPlayer, radius);
                        ShockwaveSpell.triggerShockwave(pPlayer, radius, seismicLevel);
                    }

                    if (zephyrLevel > 0) {
                        pPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, zephyrLevel - 1));
                    }
                    stopParticles = true;
                }
            }
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return true;
    }
    @Override
    public int getEnchantmentValue() {
        return 30;
    }
}
