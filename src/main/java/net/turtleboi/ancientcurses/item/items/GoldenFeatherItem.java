package net.turtleboi.ancientcurses.item.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.enchantment.ModEnchantments;

public class GoldenFeatherItem extends Item {

    public GoldenFeatherItem(Properties pProperties) {
        super(pProperties);
    }



    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {

        ItemStack item = pPlayer.getItemInHand(pUsedHand);
        int furtherdashlevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.FURTHER_DASH.get(), pPlayer);
        int quickdashlevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.QUICK_DASH.get(), pPlayer);
        int speeddashlevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.SPEED_DASH.get(), pPlayer);


        Vec3 playerLook = pPlayer.getViewVector(1);
        double dashmodifier = 1+furtherdashlevel*0.4;
        int cooldownreduction = 15*quickdashlevel;
        Vec3 dashVec = new Vec3((playerLook.x()*2)*dashmodifier, playerLook.y()*0.5+0.4, (playerLook.z()*1.8)*dashmodifier);

        pPlayer.setDeltaMovement(dashVec);
        for (int j = 0; j < 5; j++) {
            pLevel.addParticle(ParticleTypes.CLOUD,
                    pPlayer.getX() + (pPlayer.getRandom().nextDouble() - 0.5),
                    pPlayer.getY() + (pPlayer.getRandom().nextDouble() - 0.5),
                    pPlayer.getZ() + (pPlayer.getRandom().nextDouble() - 0.5),
                    0.0, 0.1, 0.0);
        }

        if (speeddashlevel>0){
            pPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,50,0));
        }


        pPlayer.getCooldowns().addCooldown(this,75-cooldownreduction);
        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(item,pLevel.isClientSide);
    }
}
