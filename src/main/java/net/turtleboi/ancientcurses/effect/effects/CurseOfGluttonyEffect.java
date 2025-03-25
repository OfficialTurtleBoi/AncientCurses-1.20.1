package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.SendParticlesS2C;


public class CurseOfGluttonyEffect extends MobEffect {
    public CurseOfGluttonyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            if (pLivingEntity.tickCount % 20 == 0) {
                int effectColor = this.getColor();
                float red = ((effectColor >> 16) & 0xFF) / 255.0F;
                float green = ((effectColor >> 8) & 0xFF) / 255.0F;
                float blue = (effectColor & 0xFF) / 255.0F;
                for (int i = 0; i < 5; i++) {
                    CoreNetworking.sendToNear(new SendParticlesS2C(
                            ModParticleTypes.CURSED_PARTICLE.get(),
                            pLivingEntity.getX() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            pLivingEntity.getY() + pLivingEntity.getRandom().nextDouble() * pLivingEntity.getBbHeight(),
                            pLivingEntity.getZ() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            red, green, blue), player);
                }
            }

            adjustHunger(player, pAmplifier);
            spoilFoodInInventory(player, pAmplifier);
            checkStarvationDamage(player);
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {

        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    private void adjustHunger(Player player, int amplifier) {
        float extraExhaustion = getHungerMultiplier(amplifier);
        player.causeFoodExhaustion(0.005F * extraExhaustion);
    }

    private float getHungerMultiplier(int pAmplifier) {
        float[] hungerDrainMultiplier = {10.0F, 17.5F, 25.0F};
        int index = Math.min(pAmplifier, hungerDrainMultiplier.length - 1);
        return hungerDrainMultiplier[index];
    }

    private void spoilFoodInInventory(Player player, int pAmplifier) {
        int spoilTime = getSpoilTimer(pAmplifier);
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEdible() && !stack.getItem().equals(ModItems.ROT_CLUMP.get())) {
                if (!stack.hasTag() || !stack.getTag().contains("SpoilTimer")) {
                    stack.getOrCreateTag().putInt("SpoilTimer", 0);
                }

                int currentSpoilTime = stack.getTag().getInt("SpoilTimer");
                if (currentSpoilTime >= spoilTime) {
                    stack.shrink(1);
                    player.getInventory().add(new ItemStack(ModItems.ROT_CLUMP.get()));
                } else {
                    stack.getTag().putInt("SpoilTimer", currentSpoilTime + 1);
                }
            }
        }
    }

    private int getSpoilTimer(int pAmplifier) {
        int[] spoilTimeValues = {2400, 1200, 600};
        int index = Math.min(pAmplifier, spoilTimeValues.length - 1);
        return spoilTimeValues[index];
    }

    private int starvationCounter = 0;

    private void checkStarvationDamage(Player player) {
        FoodData foodData = player.getFoodData();
        Level level = player.level();
        int hungerLevel = foodData.getFoodLevel();
        if (hungerLevel <= 6) {
            player.displayClientMessage(Component.literal("You feel ravenous...").withStyle(ChatFormatting.RED), true);
            starvationCounter++;
            int starvationInterval = 80;
            if (starvationCounter >= starvationInterval) {
                starvationCounter = 0;
                int starvationDamage = 1 + ((6 - hungerLevel) / 2);
                player.hurt(level.damageSources().starve(), starvationDamage);
            }
        } else {
            starvationCounter = 0;
        }
    }

    public static void modifyFoodRestoration(Player player, ItemStack foodStack, int pAmplifier) {
        FoodProperties foodProperties = foodStack.getItem().getFoodProperties(foodStack, player);

        if (foodProperties != null) {
            int baseHunger = foodProperties.getNutrition();
            float baseSaturation = foodProperties.getSaturationModifier();
            float[] newRestorationValues = {1.0F, 0.66F, 0.33F};
            int index = Math.min(pAmplifier, newRestorationValues.length - 1);

            int newHunger = Math.round(baseHunger * newRestorationValues[index]);
            float newSaturation = baseSaturation * newRestorationValues[index];

            player.getFoodData().eat(newHunger, newSaturation);
        }
    }
}
