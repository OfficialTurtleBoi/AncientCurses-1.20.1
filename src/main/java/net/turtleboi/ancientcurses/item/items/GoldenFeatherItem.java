package net.turtleboi.ancientcurses.item.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class GoldenFeatherItem extends Item {
    public GoldenFeatherItem(Properties pProperties) {
        super(pProperties);
    }

    private boolean stopParticles = true;

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack pItemStack = pPlayer.getItemInHand(pUsedHand);

        pPlayer.startUsingItem(pUsedHand);
        return InteractionResultHolder.success(pItemStack);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
        if (pLivingEntity instanceof Player pPlayer ) {
            if (pRemainingUseDuration > 1) {
                int furtherDashLevel = pStack.getEnchantmentLevel(ModEnchantments.FURTHER_DASH.get());

                Vec3 playerLook = pPlayer.getViewVector(1);
                double dashmodifier = 1 + furtherDashLevel * 0.42;
                Vec3 dashVec = new Vec3((playerLook.x() * 2) * dashmodifier, playerLook.y() * 0.5 * dashmodifier + 0.4, (playerLook.z() * 1.8) * dashmodifier);
                pPlayer.setDeltaMovement(dashVec);

                for (int j = 0; j < 5; j++) {
                    pLevel.addParticle(ModParticleTypes.GOLDEN_FEATHER_PARTICLE.get(),
                            pPlayer.getX() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getY() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getZ() + (pPlayer.getRandom().nextDouble() - 0.5),
                            0.0, 0.1, 0.0);
                }
            } else {
                releaseUsing(pStack, pLevel, pLivingEntity, getUseDuration(pStack));
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
        int speedDashLevel = pStack.getEnchantmentLevel(ModEnchantments.SPEED_DASH.get());
        int quickDashLevel = pStack.getEnchantmentLevel(ModEnchantments.QUICK_DASH.get());
        if (pLivingEntity instanceof Player pPlayer) {
            if (speedDashLevel > 0) {
                pPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, speedDashLevel - 1));
            }

            pStack.hurtAndBreak(1, pPlayer, (p_41300_) -> {
                p_41300_.broadcastBreakEvent(Objects.requireNonNull(pStack.getEquipmentSlot()));
            });

            int cooldownReduction = 15 * quickDashLevel;
            pPlayer.getCooldowns().addCooldown(this, 75 - cooldownReduction);
            pPlayer.awardStat(Stats.ITEM_USED.get(this));

            if (!pPlayer.onGround()){
                stopParticles = false;
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
        if(pEntity instanceof Player pPlayer) {
            int seismicDashLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.SEISMIC_DASH.get(), pPlayer);

            if (!stopParticles) {
                for (int j = 0; j < 5; j++) {
                    pLevel.addParticle(ModParticleTypes.GOLDEN_FEATHER_PARTICLE.get(),
                            pPlayer.getX() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getY() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getZ() + (pPlayer.getRandom().nextDouble() - 0.5),
                            0.0, 0.1, 0.0);
                }

                if (pPlayer.onGround()) {
                    if(seismicDashLevel > 0) {
                        AreaEffectCloud DamageCloud = new AreaEffectCloud(pLevel, pEntity.getX(), pEntity.getY(), pEntity.getZ());
                        DamageCloud.setDuration(1);
                        DamageCloud.setRadius(3);
                        DamageCloud.setParticle(ParticleTypes.CLOUD);
                        pLevel.addFreshEntity(DamageCloud);
                        List<Entity> entities = pLevel.getEntitiesOfClass(Entity.class, DamageCloud.getBoundingBox().inflate(1.5));
                        for (Entity entity : entities) {
                            if (entity instanceof LivingEntity livingEntity) {
                                if(!livingEntity.is(pEntity)) {
                                    double throwAngle = 1 + (pLevel.getRandom().nextDouble());
                                    Vec3 direction = new Vec3(pPlayer.getX() - livingEntity.getX(), pPlayer.getY() - (livingEntity.getY() + throwAngle), pPlayer.getZ() - livingEntity.getZ());
                                    direction = direction.normalize().scale(-2 * pLevel.getRandom().nextDouble());
                                    livingEntity.setDeltaMovement(direction);
                                    livingEntity.hurtMarked = true;

                                    float damage = 3.0f * Math.max(1.0f, (3.0f - pPlayer.distanceTo(livingEntity)));
                                    livingEntity.hurt(pLevel.damageSources().magic(), damage);
                                }
                            }
                        }
                    }
                    stopParticles = true;
                }
            }
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }
}
