package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.particle.ModParticles;
import net.turtleboi.ancientcurses.util.AttributeModifierUtil;
import net.turtleboi.ancientcurses.util.ItemValueMap;

public class CurseOfAvariceEffect extends MobEffect {
    private static final ResourceLocation COGrhealthUpdateTag = new ResourceLocation("ancientcurses", "cogr_healthupdated");
    private static final ResourceLocation COGrlastInventoryValueTag = new ResourceLocation("ancientcurses", "cogr_last_inventory_value");
    public CurseOfAvariceEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) {
            if (pLivingEntity.tickCount % 20 == 0) {
                int effectColor = this.getColor();
                float red = ((effectColor >> 16) & 0xFF) / 255.0F;
                float green = ((effectColor >> 8) & 0xFF) / 255.0F;
                float blue = (effectColor & 0xFF) / 255.0F;
                for (int i = 0; i < 5; i++) {
                    pLivingEntity.level().addParticle(
                            ModParticles.CURSED_PARTICLES.get(),
                            pLivingEntity.getX() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            pLivingEntity.getY() + pLivingEntity.getRandom().nextDouble() * pLivingEntity.getBbHeight(),
                            pLivingEntity.getZ() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            red, green, blue);
                }
            }
        }

        Level level = pLivingEntity.level();
        if (!level.isClientSide && pLivingEntity instanceof Player player) {
            if (pAmplifier >= 2) {
                int currentInventoryValue = calculatePlayerInventoryValue(player, level);
                int lastInventoryValue = getLastInventoryValue(player);

                if (currentInventoryValue != lastInventoryValue) {
                    resetInventoryValue(player);
                    updatePlayerPenalties(player, currentInventoryValue);
                    setLastInventoryValue(player, currentInventoryValue);
                }
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            resetInventoryValue(player);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    public static double getItemDestroyChance(int pAmplifier) {
        double[] itemDestroyChanceValues = {0.0, 0.075, 0.15};
        int index = Math.min(pAmplifier, itemDestroyChanceValues.length - 1);
        return itemDestroyChanceValues[index];
    }

    private void updatePlayerPenalties(Player player, int inventoryValue) {
        int penaltyMultiplier = inventoryValue / 1000;

        if (penaltyMultiplier > 0) {
            double healthReduction = 2.0 * penaltyMultiplier;
            AttributeModifierUtil.applyTransientModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    "COGrMaxHealth",
                    -healthReduction,
                    AttributeModifier.Operation.ADDITION
            );

            double attackReduction = 1.0 * penaltyMultiplier;
            AttributeModifierUtil.applyTransientModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    "COGrAttackDamage",
                    -attackReduction,
                    AttributeModifier.Operation.ADDITION
            );

            if (!isHealthUpdated(player)) {
                updatePlayerHealth(player);
            }
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
        return data.getBoolean(COGrhealthUpdateTag.toString());
    }

    private static void setHealthUpdated(Player player, boolean updated) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(COGrhealthUpdateTag.toString(), updated);
    }

    public static void resetInventoryValue(Player player){
        AttributeModifierUtil.removeModifier(player, Attributes.MAX_HEALTH, "COGrMaxHealth");
        AttributeModifierUtil.removeModifier(player, Attributes.ATTACK_DAMAGE, "COGrAttackDamage");
        setHealthUpdated(player, false);
    }

    private void setLastInventoryValue(Player player, int value) {
        CompoundTag data = player.getPersistentData();
        data.putInt(COGrlastInventoryValueTag.toString(), value);
    }

    private int getLastInventoryValue(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getInt(COGrlastInventoryValueTag.toString());
    }

    private static int calculatePlayerInventoryValue(Player player, Level level) {
        int totalValue = 0;
        for (ItemStack itemStack : player.getInventory().items) {
            if (!itemStack.isEmpty()) {
                int stackValue = ItemValueMap.getItemValue(itemStack, level);
                totalValue += stackValue * itemStack.getCount();
            }
        }
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (!armorStack.isEmpty()) {
                int armorValue = ItemValueMap.getItemValue(armorStack, level);
                totalValue += armorValue * armorStack.getCount();
            }
        }
        ItemStack offhandStack = player.getOffhandItem();
        if (!offhandStack.isEmpty()) {
            int offhandValue = ItemValueMap.getItemValue(offhandStack, level);
            totalValue += offhandValue * offhandStack.getCount();
        }
        return totalValue;
    }

}