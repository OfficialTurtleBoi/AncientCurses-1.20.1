package net.turtleboi.ancientcurses.item.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
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

import java.util.List;

public class GoldenFeatherItem extends Item {

    public GoldenFeatherItem(Properties pProperties) {
        super(pProperties);
    }

    private static int particletime = 0;


    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {

        ItemStack item = pPlayer.getItemInHand(pUsedHand);
        if(isDashEnabled(item))
        {
            int furtherdashlevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.FURTHER_DASH.get(), pPlayer);
            int quickdashlevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.QUICK_DASH.get(), pPlayer);
            int speeddashlevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.SPEED_DASH.get(), pPlayer);


            Vec3 playerLook = pPlayer.getViewVector(1);
            double dashmodifier = 1 + furtherdashlevel * 0.42;
            int cooldownreduction = 15 * quickdashlevel;
            Vec3 dashVec = new Vec3((playerLook.x() * 2) * dashmodifier, playerLook.y() * 0.5 * dashmodifier + 0.4, (playerLook.z() * 1.8) * dashmodifier);
            particletime = 140;
            pPlayer.setDeltaMovement(dashVec);
            for (int j = 0; j < 5; j++) {
                pLevel.addParticle(ModParticleTypes.GOLDEN_FEATHER_PARTICLE.get(),
                        pPlayer.getX() + (pPlayer.getRandom().nextDouble() - 0.5),
                        pPlayer.getY() + (pPlayer.getRandom().nextDouble() - 0.5),
                        pPlayer.getZ() + (pPlayer.getRandom().nextDouble() - 0.5),
                        0.0, 0.1, 0.0);
            }
            item.hurtAndBreak(1, pPlayer, (p_41300_) -> {
                p_41300_.broadcastBreakEvent(pUsedHand);
            });
            if (speeddashlevel > 0) {
                pPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, 0));
            }


            pPlayer.getCooldowns().addCooldown(this, 75 - cooldownreduction);
            pPlayer.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(item, pLevel.isClientSide);
        }
        else{

            return InteractionResultHolder.pass(item);
        }

    }
    public static boolean isDashEnabled(ItemStack pGoldenFeatherStack) {
        return pGoldenFeatherStack.getDamageValue() < pGoldenFeatherStack.getMaxDamage() - 1;
    }
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {

        if(pEntity instanceof Player player) {

            if (particletime>0){
                int seismicdashlevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.SEISMIC_DASH.get(), player);

                if (player.onGround()){
                    if(seismicdashlevel>0) {
                        int damage = (140-particletime) / 10;
                        AreaEffectCloud DamageCloud = new AreaEffectCloud(pLevel, pEntity.getX() + 0.5D, pEntity.getY() + 0.5D, pEntity.getZ() + 0.5D);
                        DamageCloud.setDuration(1);
                        DamageCloud.setRadius(3);
                        DamageCloud.setParticle(ParticleTypes.CLOUD);
                        pLevel.addFreshEntity(DamageCloud);
                        List<Entity> entities = pLevel.getEntitiesOfClass(Entity.class, DamageCloud.getBoundingBox().inflate(1));
                        for (Entity entity : entities) {
                            if (entity instanceof LivingEntity livingEntity) {
                                if(!livingEntity.is(pEntity)) {
                                    Vec3 dashVec = new Vec3(0, 1, 0);
                                    livingEntity.setDeltaMovement(dashVec);
                                    livingEntity.hurt(pLevel.damageSources().magic(), damage);
                                }
                            }
                        }
                    }
                    particletime=0;

                }
                for (int j = 0; j < 2; j++) {
                    pLevel.addParticle(ModParticleTypes.GOLDEN_FEATHER_PARTICLE.get(),
                            pEntity.getX() + (pLevel.getRandom().nextDouble() - 0.5),
                            pEntity.getY() + (pLevel.getRandom().nextDouble() - 0.5),
                            pEntity.getZ() + (pLevel.getRandom().nextDouble() - 0.5),
                            0.0, 0.1, 0.0);
                }
                particletime-=1;
                if (player.isFallFlying()){
                    particletime-=1;
                }
            }
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }
}
