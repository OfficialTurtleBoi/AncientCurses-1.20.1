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
        int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.FURTHER_DASH.get(), pPlayer);

        Vec3 playerLook = pPlayer.getViewVector(1);
        double modifier = 1+enchantmentLevel*0.4;
        Vec3 dashVec = new Vec3((playerLook.x()*2)*modifier, playerLook.y()*0.5+0.4, (playerLook.z()*1.8)*modifier);

        pPlayer.setDeltaMovement(dashVec);
        for (int j = 0; j < 5; j++) {
            pLevel.addParticle(ParticleTypes.CLOUD,
                    pPlayer.getX() + (pPlayer.getRandom().nextDouble() - 0.5),
                    pPlayer.getY() + (pPlayer.getRandom().nextDouble() - 0.5),
                    pPlayer.getZ() + (pPlayer.getRandom().nextDouble() - 0.5),
                    0.0, 0.1, 0.0);
        }




        pPlayer.getCooldowns().addCooldown(this,75);
        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(item,pLevel.isClientSide);
    }
}
