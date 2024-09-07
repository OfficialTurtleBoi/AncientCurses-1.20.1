package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.init.ModAttributes;
import net.turtleboi.ancientcurses.util.AttributeModifierUtil;

import java.util.Random;


public class CurseOfGreedEffect extends MobEffect {
    private static final Random random = new Random();
    private int tickCounter = 0;
    private static final ResourceLocation healthUpdateTag = new ResourceLocation("ancientcurses", "health_updated");
    public CurseOfGreedEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {


            if (pAmplifier >= 1) {
                double hitChanceReduction = getItemDestroyChance(pAmplifier);
                AttributeModifierUtil.applyTransientModifier(
                        player,
                        ModAttributes.ITEM_DESTROY_CHANCE.get(),
                        "COFItemDestroyChance",
                        hitChanceReduction,
                        AttributeModifier.Operation.ADDITION);
            }

        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            AttributeModifierUtil.removeModifier(player, ModAttributes.ITEM_DESTROY_CHANCE.get(), "COFItemDestroyChance");
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }









    private double getItemDestroyChance(int pAmplifier) {
        switch (pAmplifier) {
            case 0: return 0.25;
            case 1: return 0.50;
            case 2: return 0.75;
            default: return 0;
        }
    }


}
