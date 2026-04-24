package net.turtleboi.ancientcurses.item.items.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.item.items.PreciousGemItem;
import net.turtleboi.ancientcurses.item.items.PreciousGemType;
import net.turtleboi.turtlecore.init.CoreAttributes;
import net.turtleboi.turtlecore.util.CoreAttributeModifiers;

public final class GemBonusUtil {
    private static final String AMULET_PREFIX = "amulet_";
    private static final String ARMOR_PREFIX = "armor_";
    private static final String HEALTH_UPDATED_FLAG = "GearHealthBonusUpdated";
    private static final String CHRYSOBERYL_FLIGHT_FLAG = "ChrysoberylFlight";

    private GemBonusUtil() {
    }

    public static void applyMajorBonus(Player player, PreciousGemType gemType, int slotIndex) {
        String slotSuffix = "_slot_" + slotIndex;

        switch (gemType) {
            case POLISHED_AMETHYST -> {
                applyModifier(player, Attributes.MAX_HEALTH, id(AMULET_PREFIX, gemType, "bonus", slotSuffix), 8.0, AttributeModifier.Operation.ADDITION);
                setHealthUpdated(player, false);
            }
            case PERFECT_AMETHYST -> {
                applyModifier(player, Attributes.MAX_HEALTH, id(AMULET_PREFIX, gemType, "bonus", slotSuffix), 12.0, AttributeModifier.Operation.ADDITION);
                setHealthUpdated(player, false);
                applyTimedEffect(player, ModEffects.LIFEBLOOM.get(), 200, 0, 20);
            }
            case POLISHED_DIAMOND -> {
                applyModifier(player, Attributes.ARMOR, id(AMULET_PREFIX, gemType, "armor_bonus", slotSuffix), 4.0, AttributeModifier.Operation.ADDITION);
                applyModifier(player, Attributes.ARMOR_TOUGHNESS, id(AMULET_PREFIX, gemType, "armortoughness_bonus", slotSuffix), 1.0, AttributeModifier.Operation.ADDITION);
            }
            case PERFECT_DIAMOND -> {
                applyModifier(player, Attributes.ARMOR, id(AMULET_PREFIX, gemType, "bonus", slotSuffix), 8.0, AttributeModifier.Operation.ADDITION);
                applyModifier(player, Attributes.ARMOR_TOUGHNESS, id(AMULET_PREFIX, gemType, "armortoughness_bonus", slotSuffix), 2.0, AttributeModifier.Operation.ADDITION);
                applyTimedEffect(player, ModEffects.CRYSTALLINE_HARDENING.get(), 200, 0, 20);
            }
            case POLISHED_EMERALD ->
                    applyModifier(player, Attributes.LUCK, id(AMULET_PREFIX, gemType, "bonus", slotSuffix), 1.0, AttributeModifier.Operation.ADDITION);
            case PERFECT_EMERALD -> {
                applyModifier(player, Attributes.LUCK, id(AMULET_PREFIX, gemType, "bonus", slotSuffix), 2.0, AttributeModifier.Operation.ADDITION);
                applyTimedEffect(player, ModEffects.FORTUNES_FAVOR.get(), 200, 0, 20);
            }
            case POLISHED_RUBY ->
                    applyModifier(player, Attributes.ATTACK_DAMAGE, id(AMULET_PREFIX, gemType, "bonus", slotSuffix), 2.0, AttributeModifier.Operation.ADDITION);
            case PERFECT_RUBY ->
                    applyModifier(player, Attributes.ATTACK_DAMAGE, id(AMULET_PREFIX, gemType, "bonus", slotSuffix), 4.0, AttributeModifier.Operation.ADDITION);
            case POLISHED_SAPPHIRE ->
                    applyModifier(player, CoreAttributes.MAGIC_AMP.get(), id(AMULET_PREFIX, gemType, "bonus", slotSuffix), 0.4, AttributeModifier.Operation.MULTIPLY_BASE);
            case PERFECT_SAPPHIRE ->
                    applyModifier(player, CoreAttributes.MAGIC_AMP.get(), id(AMULET_PREFIX, gemType, "bonus", slotSuffix), 0.67, AttributeModifier.Operation.MULTIPLY_BASE);
            case POLISHED_TOPAZ -> {
                applyModifier(player, Attributes.ATTACK_SPEED, id(AMULET_PREFIX, gemType, "attackspeed_bonus", slotSuffix), 0.25, AttributeModifier.Operation.MULTIPLY_BASE);
                applyModifier(player, Attributes.MOVEMENT_SPEED, id(AMULET_PREFIX, gemType, "movementspeed_bonus", slotSuffix), 0.25, AttributeModifier.Operation.MULTIPLY_BASE);
            }
            case PERFECT_TOPAZ -> {
                applyModifier(player, Attributes.ATTACK_SPEED, id(AMULET_PREFIX, gemType, "attackspeed_bonus", slotSuffix), 0.67, AttributeModifier.Operation.MULTIPLY_BASE);
                applyModifier(player, Attributes.MOVEMENT_SPEED, id(AMULET_PREFIX, gemType, "movementspeed_bonus", slotSuffix), 0.67, AttributeModifier.Operation.MULTIPLY_BASE);
            }
            case ANCIENT_ALEXANDRITE -> applyAncientAlexandriteMajorBonus(player, slotSuffix);
            case ANCIENT_BISMUTH -> applyTimedEffect(player, ModEffects.ELEMENTAL_CONVERGENCE.get(), 200, 0, 0);
            case ANCIENT_CHRYSOBERYL -> {
                applyModifier(player, Attributes.ATTACK_SPEED, id(AMULET_PREFIX, gemType, "attackspeed_bonus", slotSuffix), 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
                applyModifier(player, Attributes.MOVEMENT_SPEED, id(AMULET_PREFIX, gemType, "movementspeed_bonus", slotSuffix), 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
                applyModifier(player, Attributes.LUCK, id(AMULET_PREFIX, gemType, "luck_bonus", slotSuffix), 3.0, AttributeModifier.Operation.ADDITION);

                if (!player.isCreative() && !player.getPersistentData().getBoolean(CHRYSOBERYL_FLIGHT_FLAG)) {
                    player.getPersistentData().putBoolean(CHRYSOBERYL_FLIGHT_FLAG, false);
                    player.getAbilities().mayfly = false;
                    player.onUpdateAbilities();
                }
            }
            default -> {
            }
        }
    }

    public static void applyAmuletMinorBonus(Player player, PreciousGemType gemType, int slotIndex) {
        applyMinorBonus(player, gemType, AMULET_PREFIX, slotIndex);
    }

    public static void applyArmorMinorBonus(Player player, PreciousGemType gemType, EquipmentSlot armorType, int slotIndex) {
        applyMinorBonus(player, gemType, ARMOR_PREFIX + armorType.getName(), slotIndex);
    }

    private static void applyMinorBonus(Player player, PreciousGemType gemType, String scopePrefix, int slotIndex) {
        String slotSuffix = "_slot_" + slotIndex;
        String prefix = scopePrefix.endsWith("_") ? scopePrefix : scopePrefix + "_";

        switch (gemType) {
            case BROKEN_AMETHYST -> applyHealthBonus(player, prefix, gemType, slotSuffix, 2.0);
            case POLISHED_AMETHYST -> applyHealthBonus(player, prefix, gemType, slotSuffix, 4.0);
            case PERFECT_AMETHYST -> applyHealthBonus(player, prefix, gemType, slotSuffix, 8.0);
            case BROKEN_DIAMOND ->
                    applyModifier(player, Attributes.ARMOR, id(prefix, gemType, "bonus", slotSuffix), 1.0, AttributeModifier.Operation.ADDITION);
            case POLISHED_DIAMOND -> {
                applyModifier(player, Attributes.ARMOR, id(prefix, gemType, "armor_bonus", slotSuffix), 2.0, AttributeModifier.Operation.ADDITION);
                applyModifier(player, Attributes.ARMOR_TOUGHNESS, id(prefix, gemType, "armortoughness_bonus", slotSuffix), 0.5, AttributeModifier.Operation.ADDITION);
            }
            case PERFECT_DIAMOND -> {
                applyModifier(player, Attributes.ARMOR, id(prefix, gemType, "bonus", slotSuffix), 4.0, AttributeModifier.Operation.ADDITION);
                applyModifier(player, Attributes.ARMOR_TOUGHNESS, id(prefix, gemType, "armortoughness_bonus", slotSuffix), 1.0, AttributeModifier.Operation.ADDITION);
            }
            case BROKEN_EMERALD ->
                    applyModifier(player, Attributes.LUCK, id(prefix, gemType, "bonus", slotSuffix), 0.25, AttributeModifier.Operation.ADDITION);
            case POLISHED_EMERALD ->
                    applyModifier(player, Attributes.LUCK, id(prefix, gemType, "bonus", slotSuffix), 0.5, AttributeModifier.Operation.ADDITION);
            case PERFECT_EMERALD ->
                    applyModifier(player, Attributes.LUCK, id(prefix, gemType, "bonus", slotSuffix), 1.0, AttributeModifier.Operation.ADDITION);
            case BROKEN_RUBY ->
                    applyModifier(player, Attributes.ATTACK_DAMAGE, id(prefix, gemType, "bonus", slotSuffix), 0.5, AttributeModifier.Operation.ADDITION);
            case POLISHED_RUBY ->
                    applyModifier(player, Attributes.ATTACK_DAMAGE, id(prefix, gemType, "bonus", slotSuffix), 1.0, AttributeModifier.Operation.ADDITION);
            case PERFECT_RUBY ->
                    applyModifier(player, Attributes.ATTACK_DAMAGE, id(prefix, gemType, "bonus", slotSuffix), 2.0, AttributeModifier.Operation.ADDITION);
            case BROKEN_SAPPHIRE ->
                    applyModifier(player, CoreAttributes.MAGIC_AMP.get(), id(prefix, gemType, "bonus", slotSuffix), 0.1, AttributeModifier.Operation.MULTIPLY_BASE);
            case POLISHED_SAPPHIRE ->
                    applyModifier(player, CoreAttributes.MAGIC_AMP.get(), id(prefix, gemType, "bonus", slotSuffix), 0.2, AttributeModifier.Operation.MULTIPLY_BASE);
            case PERFECT_SAPPHIRE ->
                    applyModifier(player, CoreAttributes.MAGIC_AMP.get(), id(prefix, gemType, "bonus", slotSuffix), 0.33, AttributeModifier.Operation.MULTIPLY_BASE);
            case BROKEN_TOPAZ -> {
                applyModifier(player, Attributes.ATTACK_SPEED, id(prefix, gemType, "attackspeed_bonus", slotSuffix), 0.075, AttributeModifier.Operation.MULTIPLY_BASE);
                applyModifier(player, Attributes.MOVEMENT_SPEED, id(prefix, gemType, "movementspeed_bonus", slotSuffix), 0.075, AttributeModifier.Operation.MULTIPLY_BASE);
            }
            case POLISHED_TOPAZ -> {
                applyModifier(player, Attributes.ATTACK_SPEED, id(prefix, gemType, "attackspeed_bonus", slotSuffix), 0.125, AttributeModifier.Operation.MULTIPLY_BASE);
                applyModifier(player, Attributes.MOVEMENT_SPEED, id(prefix, gemType, "movementspeed_bonus", slotSuffix), 0.125, AttributeModifier.Operation.MULTIPLY_BASE);
            }
            case PERFECT_TOPAZ -> {
                applyModifier(player, Attributes.ATTACK_SPEED, id(prefix, gemType, "attackspeed_bonus", slotSuffix), 0.33, AttributeModifier.Operation.MULTIPLY_BASE);
                applyModifier(player, Attributes.MOVEMENT_SPEED, id(prefix, gemType, "movementspeed_bonus", slotSuffix), 0.33, AttributeModifier.Operation.MULTIPLY_BASE);
            }
            default -> {
            }
        }
    }

    public static void removeAmuletBonus(Player player) {
        removeBonusesByPrefix(player, AMULET_PREFIX.substring(0, AMULET_PREFIX.length() - 1));

        if (!isHealthUpdated(player)) {
            updatePlayerHealth(player);
        }

        if (!player.isCreative() && player.getPersistentData().getBoolean(CHRYSOBERYL_FLIGHT_FLAG)) {
            player.getPersistentData().putBoolean(CHRYSOBERYL_FLIGHT_FLAG, false);
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    public static void applyArmorGemBonuses(Player player, ItemStack armorPiece, EquipmentSlot armorType) {
        if (!armorPiece.hasTag()) {
            return;
        }

        CompoundTag armorTag = armorPiece.getTag();
        if (armorTag == null || !armorTag.contains("MinorGems")) {
            return;
        }

        ListTag minorGemsTag = armorTag.getList("MinorGems", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < minorGemsTag.size(); i++) {
            CompoundTag minorGemTag = minorGemsTag.getCompound(i);
            ItemStack minorGem = ItemStack.of(minorGemTag);
            if (minorGem.getItem() instanceof PreciousGemItem preciousGem) {
                preciousGem.applyArmorMinorBonus(player, armorType, i + 1);
            }
        }
    }

    public static void removeArmorBonus(Player player) {
        removeBonusesByPrefix(player, ARMOR_PREFIX.substring(0, ARMOR_PREFIX.length() - 1));
        setHealthUpdated(player, false);
    }

    public static void updatePlayerHealth(Player player) {
        Level level = player.level();
        if (!level.isClientSide && !isHealthUpdated(player)) {
            float healthBefore = player.getHealth();
            player.setHealth(Math.max(1.0F, healthBefore - 0.1F));
            player.setHealth(healthBefore);
            setHealthUpdated(player, true);
        }
    }

    private static void applyAncientAlexandriteMajorBonus(Player player, String slotSuffix) {
        if (!player.level().isDay()) {
            CoreAttributeModifiers.removeModifier(player, Attributes.ARMOR, "ancient_alexandrite_armor_bonus");
            CoreAttributeModifiers.removeModifier(player, Attributes.ARMOR_TOUGHNESS, "ancient_alexandrite_armortoughness_bonus");
            applyTimedEffect(player, MobEffects.NIGHT_VISION, 300, 0, 220);
            applyModifier(player, Attributes.ATTACK_DAMAGE, id(AMULET_PREFIX, PreciousGemType.ANCIENT_ALEXANDRITE, "attackdamage_bonus", slotSuffix), 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(player, Attributes.ATTACK_SPEED, id(AMULET_PREFIX, PreciousGemType.ANCIENT_ALEXANDRITE, "attackspeed_bonus", slotSuffix), 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(player, CoreAttributes.DODGE_CHANCE.get(), id(AMULET_PREFIX, PreciousGemType.ANCIENT_ALEXANDRITE, "dodgechance_bonus", slotSuffix), -0.33, AttributeModifier.Operation.ADDITION);
        } else {
            CoreAttributeModifiers.removeModifier(player, Attributes.ATTACK_DAMAGE, "ancient_alexandrite_attackdamage_bonus");
            CoreAttributeModifiers.removeModifier(player, Attributes.ATTACK_SPEED, "ancient_alexandrite_attackspeed_bonus");
            CoreAttributeModifiers.removeModifier(player, CoreAttributes.DODGE_CHANCE.get(), "ancient_alexandrite_dodgechance_bonus");
            applyTimedEffect(player, MobEffects.REGENERATION, 200, 0, 0);
            applyModifier(player, Attributes.ARMOR, id(AMULET_PREFIX, PreciousGemType.ANCIENT_ALEXANDRITE, "armor_bonus", slotSuffix), 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(player, Attributes.ARMOR_TOUGHNESS, id(AMULET_PREFIX, PreciousGemType.ANCIENT_ALEXANDRITE, "armortoughness_bonus", slotSuffix), 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);

            int foodInterval = player.level().canSeeSky(BlockPos.containing(player.getX(), player.getEyeY(), player.getZ())) ? 60 : 180;
            if (player.tickCount % foodInterval == 0) {
                player.getFoodData().eat(1, 0.5F);
            }
        }
    }

    private static void applyHealthBonus(Player player, String prefix, PreciousGemType gemType, String slotSuffix, double amount) {
        applyModifier(player, Attributes.MAX_HEALTH, id(prefix, gemType, "bonus", slotSuffix), amount, AttributeModifier.Operation.ADDITION);
        setHealthUpdated(player, false);
    }

    private static void applyTimedEffect(Player player, MobEffect effect, int duration, int amplifier, int refreshThreshold) {
        MobEffectInstance currentEffect = player.getEffect(effect);
        if (currentEffect == null || currentEffect.getDuration() <= refreshThreshold) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, true));
        }
    }

    private static void applyModifier(Player player, Attribute attribute, String id, double amount, AttributeModifier.Operation operation) {
        CoreAttributeModifiers.applyPermanentModifier(player, attribute, id, amount, operation);
    }

    private static String id(String prefix, PreciousGemType gemType, String suffix, String slotSuffix) {
        return prefix + gemType.getItemName() + "_" + suffix + slotSuffix;
    }

    private static void removeBonusesByPrefix(Player player, String prefix) {
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ARMOR, prefix);
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ARMOR_TOUGHNESS, prefix);
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ATTACK_DAMAGE, prefix);
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ATTACK_SPEED, prefix);
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.LUCK, prefix);
        CoreAttributeModifiers.removeModifiersByPrefix(player, CoreAttributes.MAGIC_AMP.get(), prefix);
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.MAX_HEALTH, prefix);
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.MOVEMENT_SPEED, prefix);
        CoreAttributeModifiers.removeModifiersByPrefix(player, CoreAttributes.DODGE_CHANCE.get(), prefix);
    }

    private static boolean isHealthUpdated(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(HEALTH_UPDATED_FLAG);
    }

    private static void setHealthUpdated(Player player, boolean updated) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(HEALTH_UPDATED_FLAG, updated);
    }
}
