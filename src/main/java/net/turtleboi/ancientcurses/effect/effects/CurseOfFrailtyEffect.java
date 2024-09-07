package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.turtleboi.ancientcurses.init.ModAttributes;
import net.turtleboi.ancientcurses.util.AttributeModifierUtil;

import java.util.Objects;
import java.util.Random;


public class CurseOfFrailtyEffect extends MobEffect {
    private static final Random random = new Random();
    private int tickCounter = 0;
    private static final ResourceLocation healthUpdateTag = new ResourceLocation("ancientcurses", "health_updated");
    public CurseOfFrailtyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            double healthReduction = getHealthReduction(pAmplifier);
            AttributeModifierUtil.applyTransientModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    "COFMaxHealth",
                    healthReduction,
                    AttributeModifier.Operation.ADDITION);

            if (!isHealthUpdated(player)) {
                updatePlayerHealth(player);
            }

            double damageReduction = getAttackDamageReduction(pAmplifier);
            AttributeModifierUtil.applyTransientModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    "COFAttackDamage",
                    damageReduction,
                    AttributeModifier.Operation.MULTIPLY_BASE);

            if (pAmplifier >= 1) {
                double hitChanceReduction = getHitChanceReduction(pAmplifier);
                AttributeModifierUtil.applyTransientModifier(
                        player,
                        ModAttributes.HIT_CHANCE.get(),
                        "COFHitChance",
                        hitChanceReduction,
                        AttributeModifier.Operation.ADDITION);
            }

            tickCounter++;
            int randomTicks = 5 + random.nextInt(16);
            if (tickCounter >= randomTicks) {
                reduceDurability(player);
                tickCounter = 0;
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            AttributeModifierUtil.removeModifier(player, Attributes.MAX_HEALTH, "COFMaxHealth");
            AttributeModifierUtil.removeModifier(player, Attributes.ATTACK_DAMAGE, "COFAttackDamage");
            AttributeModifierUtil.removeModifier(player, ModAttributes.HIT_CHANCE.get(), "COFHitChance");
            setHealthUpdated(player, false);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    private double getHealthReduction(int pAmplifier) {
        switch (pAmplifier) {
            case 0: return -4.0;
            case 1: return -8.0;
            case 2: return -16.0;
            default: return 0;
        }
    }

    private void updatePlayerHealth(Player player) {
        Level level = player.level();
        player.hurt(level.damageSources().generic(), 1);
        player.heal(1);
        setHealthUpdated(player, true);
    }

    private boolean isHealthUpdated(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(healthUpdateTag.toString());
    }

    private void setHealthUpdated(Player player, boolean updated) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(healthUpdateTag.toString(), updated);
    }

    private double getAttackDamageReduction(int pAmplifier) {
        switch (pAmplifier) {
            case 0: return -0.25;
            case 1: return -0.33;
            case 2: return -0.50;
            default: return 0;
        }
    }

    private double getHitChanceReduction(int pAmplifier) {
        switch (pAmplifier) {
            case 1: return -0.33;
            case 2: return -0.66;
            default: return 1;
        }
    }

    private void reduceDurability(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            }
        }

        for (ItemStack armor : player.getInventory().armor) {
            if (!armor.isEmpty() && armor.isDamageableItem()) {
                armor.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            }
        }

        ItemStack offhandItem = player.getOffhandItem();
        if (!offhandItem.isEmpty() && offhandItem.isDamageableItem()) {
            offhandItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(InteractionHand.OFF_HAND));
        }
    }
}
