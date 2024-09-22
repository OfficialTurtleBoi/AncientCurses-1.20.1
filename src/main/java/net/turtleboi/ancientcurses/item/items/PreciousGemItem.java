package net.turtleboi.ancientcurses.item.items;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.turtleboi.ancientcurses.init.ModAttributes;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.util.AttributeModifierUtil;

public class PreciousGemItem extends Item {
    public PreciousGemItem(Properties pProperties) {
        super(pProperties);
    }

    public void applyMajorBonus(Player player, int slotIndex) {
        String slotName = "_slot_" + slotIndex;
        String amulet = "amulet_";
        if (this == ModItems.POLISHED_AMETHYST.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    amulet + "polished_amethyst_bonus" + slotName,
                    8.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_AMETHYST.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    amulet + "perfect_amethyst_bonus" + slotName,
                    12.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_DIAMOND.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    amulet + "polished_diamond_armor_bonus" + slotName,
                    4.0,
                    AttributeModifier.Operation.ADDITION);
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ARMOR_TOUGHNESS,
                    amulet + "polished_diamond_armortoughness_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_DIAMOND.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    amulet + "perfect_diamond_bonus" + slotName,
                    8.0,
                    AttributeModifier.Operation.ADDITION);
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ARMOR_TOUGHNESS,
                    amulet + "perfect_diamond_armortoughness_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_EMERALD.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "polished_emerald_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_EMERALD.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "perfect_emerald_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_RUBY.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    amulet + "polished_ruby_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_RUBY.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    amulet + "perfect_ruby_bonus" + slotName,
                    4.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_SAPPHIRE.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    ModAttributes.MAGIC_AMP.get(),
                    amulet + "polished_sapphire_bonus" + slotName,
                    0.4,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.PERFECT_SAPPHIRE.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    ModAttributes.MAGIC_AMP.get(),
                    amulet + "perfect_sapphire_bonus" + slotName,
                    0.67,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.POLISHED_TOPAZ.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "polished_topaz_attackspeed_bonus" + slotName,
                    0.25,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "polished_topaz_movementspeed_bonus" + slotName,
                    0.25,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.PERFECT_TOPAZ.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "perfect_topaz_attackspeed_bonus" + slotName,
                    0.67,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "perfect_topaz_movementspeed_bonus" + slotName,
                    0.67,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        }
    }

    public void applyMinorBonus(Player player, int slotIndex) {
        String slotName = "_slot_" + slotIndex;
        String amulet = "amulet_";
        if (this == ModItems.BROKEN_AMETHYST.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    amulet + "broken_amethyst_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_AMETHYST.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    amulet + "polished_amethyst_bonus" + slotName,
                    4.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_AMETHYST.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    amulet + "perfect_amethyst_bonus" + slotName,
                    8.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.BROKEN_DIAMOND.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    amulet + "broken_diamond_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_DIAMOND.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    amulet + "polished_diamond_armor_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ARMOR_TOUGHNESS,
                    amulet + "polished_diamond_armortoughness_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_DIAMOND.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    amulet + "perfect_diamond_bonus" + slotName,
                    4.0,
                    AttributeModifier.Operation.ADDITION);
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ARMOR_TOUGHNESS,
                    amulet + "perfect_diamond_armortoughness_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.BROKEN_EMERALD.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "broken_emerald_bonus" + slotName,
                    0.25,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_EMERALD.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "polished_emerald_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_EMERALD.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "perfect_emerald_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.BROKEN_RUBY.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    amulet + "broken_ruby_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_RUBY.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    amulet + "polished_ruby_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_RUBY.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    amulet + "perfect_ruby_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.BROKEN_SAPPHIRE.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    ModAttributes.MAGIC_AMP.get(),
                    amulet + "broken_sapphire_bonus" + slotName,
                    0.1,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.POLISHED_SAPPHIRE.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    ModAttributes.MAGIC_AMP.get(),
                    amulet + "polished_sapphire_bonus" + slotName,
                    0.2,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.PERFECT_SAPPHIRE.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    ModAttributes.MAGIC_AMP.get(),
                    amulet + "perfect_sapphire_bonus" + slotName,
                    0.33,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.BROKEN_TOPAZ.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "broken_topaz_attackspeed_bonus" + slotName,
                    0.075,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "broken_topaz_movementspeed_bonus" + slotName,
                    0.075,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.POLISHED_TOPAZ.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "polished_topaz_attackspeed_bonus" + slotName,
                    0.125,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "polished_topaz_movementspeed_bonus" + slotName,
                    0.125,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.PERFECT_TOPAZ.get()) {
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "perfect_topaz_attackspeed_bonus" + slotName,
                    0.33,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            AttributeModifierUtil.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "perfect_topaz_movementspeed_bonus" + slotName,
                    0.33,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        }
    }

    public static void removeBonus(Player player) {
        AttributeModifierUtil.removeModifiersByPrefix(player, Attributes.ARMOR, "amulet");
        AttributeModifierUtil.removeModifiersByPrefix(player, Attributes.ARMOR_TOUGHNESS, "amulet");
        AttributeModifierUtil.removeModifiersByPrefix(player, Attributes.ATTACK_DAMAGE, "amulet");
        AttributeModifierUtil.removeModifiersByPrefix(player, Attributes.ATTACK_SPEED, "amulet");
        AttributeModifierUtil.removeModifiersByPrefix(player, Attributes.LUCK, "amulet");
        AttributeModifierUtil.removeModifiersByPrefix(player, ModAttributes.MAGIC_AMP.get(), "amulet");
        AttributeModifierUtil.removeModifiersByPrefix(player, Attributes.MAX_HEALTH, "amulet");
        AttributeModifierUtil.removeModifiersByPrefix(player, Attributes.MOVEMENT_SPEED, "amulet");
    }
}
