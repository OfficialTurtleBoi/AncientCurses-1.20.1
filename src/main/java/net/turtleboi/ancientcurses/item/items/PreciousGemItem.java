package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.turtlecore.init.CoreAttributeModifiers;
import net.turtleboi.turtlecore.init.CoreAttributes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class PreciousGemItem extends Item {
    private final PreciousGemType gemType;
    public PreciousGemItem(Properties pProperties, PreciousGemType gemType) {
        super(pProperties);
        this.gemType = gemType;
    }

    public PreciousGemType getGemType (){
        return this.gemType;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.ancientcurses.gem.socket").withStyle(ChatFormatting.GRAY));

        List<MutableComponent> bonuses = gemType.getBonuses();
        for (MutableComponent bonus : bonuses) {
            tooltip.add(bonus.withStyle(ChatFormatting.BLUE));
        }
    }

    public void applyMajorBonus(Player player, int slotIndex) {
        String slotName = "_slot_" + slotIndex;
        String amulet = "amulet_";
        if (this == ModItems.POLISHED_AMETHYST.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    amulet + "polished_amethyst_bonus" + slotName,
                    8.0,
                    AttributeModifier.Operation.ADDITION);
            setHealthUpdated(player, false);
        } else if (this == ModItems.PERFECT_AMETHYST.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    amulet + "perfect_amethyst_bonus" + slotName,
                    12.0,
                    AttributeModifier.Operation.ADDITION);
            setHealthUpdated(player, false);
            if (!player.hasEffect(ModEffects.LIFEBLOOM.get()) ||
                    (player.hasEffect(ModEffects.LIFEBLOOM.get()) && Objects.requireNonNull(player.getEffect(ModEffects.LIFEBLOOM.get())).getDuration() <= 20)) {
                player.addEffect(new MobEffectInstance(ModEffects.LIFEBLOOM.get(), 200, 0, true, true));
            }
        } else if (this == ModItems.POLISHED_DIAMOND.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    amulet + "polished_diamond_armor_bonus" + slotName,
                    4.0,
                    AttributeModifier.Operation.ADDITION);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR_TOUGHNESS,
                    amulet + "polished_diamond_armortoughness_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_DIAMOND.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    amulet + "perfect_diamond_bonus" + slotName,
                    8.0,
                    AttributeModifier.Operation.ADDITION);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR_TOUGHNESS,
                    amulet + "perfect_diamond_armortoughness_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
            if (!player.hasEffect(ModEffects.CRYSTALLINE_HARDENING.get()) ||
                    (player.hasEffect(ModEffects.CRYSTALLINE_HARDENING.get()) && Objects.requireNonNull(player.getEffect(ModEffects.CRYSTALLINE_HARDENING.get())).getDuration() <= 20)) {
                player.addEffect(new MobEffectInstance(ModEffects.CRYSTALLINE_HARDENING.get(), 200, 0, true, true));
            }
        } else if (this == ModItems.POLISHED_EMERALD.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "polished_emerald_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_EMERALD.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "perfect_emerald_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
            if (!player.hasEffect(ModEffects.FORTUNES_FAVOR.get()) ||
                    (player.hasEffect(ModEffects.FORTUNES_FAVOR.get()) && Objects.requireNonNull(player.getEffect(ModEffects.FORTUNES_FAVOR.get())).getDuration() <= 20)) {
                player.addEffect(new MobEffectInstance(ModEffects.FORTUNES_FAVOR.get(), 200, 0, true, true));
            }
        } else if (this == ModItems.POLISHED_RUBY.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    amulet + "polished_ruby_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_RUBY.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    amulet + "perfect_ruby_bonus" + slotName,
                    4.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_SAPPHIRE.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    CoreAttributes.MAGIC_AMP.get(),
                    amulet + "polished_sapphire_bonus" + slotName,
                    0.4,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.PERFECT_SAPPHIRE.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    CoreAttributes.MAGIC_AMP.get(),
                    amulet + "perfect_sapphire_bonus" + slotName,
                    0.67,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.POLISHED_TOPAZ.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "polished_topaz_attackspeed_bonus" + slotName,
                    0.25,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "polished_topaz_movementspeed_bonus" + slotName,
                    0.25,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.PERFECT_TOPAZ.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "perfect_topaz_attackspeed_bonus" + slotName,
                    0.67,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "perfect_topaz_movementspeed_bonus" + slotName,
                    0.67,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.ANCIENT_ALEXANDRITE.get()) {
            if (!player.level().isDay()) {
                CoreAttributeModifiers.removeModifier(
                        player,
                        Attributes.ARMOR,
                        "ancient_alexandrite_armor_bonus"
                );
                CoreAttributeModifiers.removeModifier(
                        player,
                        Attributes.ARMOR_TOUGHNESS,
                        "ancient_alexandrite_armortoughness_bonus"
                );
                if (!player.hasEffect(MobEffects.NIGHT_VISION) ||
                        (player.hasEffect(MobEffects.NIGHT_VISION) && Objects.requireNonNull(player.getEffect(MobEffects.NIGHT_VISION)).getDuration() <= 220)) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
                }
                CoreAttributeModifiers.applyPermanentModifier(
                        player,
                        Attributes.ATTACK_DAMAGE,
                        amulet + "ancient_alexandrite_attackdamage_bonus" + slotName,
                        0.25,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
                CoreAttributeModifiers.applyPermanentModifier(
                        player,
                        Attributes.ATTACK_SPEED,
                        amulet + "ancient_alexandrite_attackspeed_bonus" + slotName,
                        0.25,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
                CoreAttributeModifiers.applyPermanentModifier(
                        player,
                        CoreAttributes.DODGE_CHANCE.get(),
                        amulet + "ancient_alexandrite_dodgechance_bonus" + slotName,
                        -0.33,
                        AttributeModifier.Operation.ADDITION);
            } else {
                CoreAttributeModifiers.removeModifier(
                        player,
                        Attributes.ATTACK_DAMAGE,
                        "ancient_alexandrite_attackdamage_bonus"
                );
                CoreAttributeModifiers.removeModifier(
                        player,
                        Attributes.ATTACK_SPEED,
                        "ancient_alexandrite_attackspeed_bonus"
                );
                CoreAttributeModifiers.removeModifier(
                        player,
                        CoreAttributes.DODGE_CHANCE.get(),
                        "ancient_alexandrite_dodgechance_bonus"
                );
                if (!player.hasEffect(MobEffects.REGENERATION)) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, true));
                }
                CoreAttributeModifiers.applyPermanentModifier(
                        player,
                        Attributes.ARMOR,
                        amulet + "ancient_alexandrite_armor_bonus" + slotName,
                        0.25,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
                CoreAttributeModifiers.applyPermanentModifier(
                        player,
                        Attributes.ARMOR_TOUGHNESS,
                        amulet + "ancient_alexandrite_armortoughness_bonus" + slotName,
                        0.25,
                        AttributeModifier.Operation.MULTIPLY_TOTAL);
                if (player.level().canSeeSky(BlockPos.containing(player.getX(), player.getEyeY(), player.getZ()))) {
                    if (player.tickCount % 60 == 0) {
                        player.getFoodData().eat(1, 0.5F);
                    }
                } else {
                    if (player.tickCount % 180 == 0) {
                        player.getFoodData().eat(1, 0.5F);
                    }
                }
            }
        } else if (this == ModItems.ANCIENT_BISMUTH.get()) {
            if (!player.hasEffect(ModEffects.ELEMENTAL_CONVERGENCE.get())) {
                player.addEffect(new MobEffectInstance(ModEffects.ELEMENTAL_CONVERGENCE.get(), 200, 0, true, true));
            }
        } else if (this == ModItems.ANCIENT_CHRYSOBERYL.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "ancient_chrysoberyl_attackspeed_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "ancient_chrysoberyl_movementspeed_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "ancient_chrysoberyl_luck_bonus" + slotName,
                    3,
                    AttributeModifier.Operation.ADDITION);

            if(!player.isCreative() && !player.getPersistentData().getBoolean("ChrysoberylFlight")) {
                player.getPersistentData().putBoolean("ChrysoberylFlight", false);
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
            }
        }
    }

    public void applyAmuletMinorBonus(Player player, int slotIndex) {
        String slotName = "_slot_" + slotIndex;
        String amulet = "amulet_";
        if (this == ModItems.BROKEN_AMETHYST.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    amulet + "broken_amethyst_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
            setHealthUpdated(player, false);
        } else if (this == ModItems.POLISHED_AMETHYST.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    amulet + "polished_amethyst_bonus" + slotName,
                    4.0,
                    AttributeModifier.Operation.ADDITION);
            setHealthUpdated(player, false);
        } else if (this == ModItems.PERFECT_AMETHYST.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    amulet + "perfect_amethyst_bonus" + slotName,
                    8.0,
                    AttributeModifier.Operation.ADDITION);
            setHealthUpdated(player, false);
        } else if (this == ModItems.BROKEN_DIAMOND.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    amulet + "broken_diamond_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_DIAMOND.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    amulet + "polished_diamond_armor_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR_TOUGHNESS,
                    amulet + "polished_diamond_armortoughness_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_DIAMOND.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    amulet + "perfect_diamond_bonus" + slotName,
                    4.0,
                    AttributeModifier.Operation.ADDITION);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR_TOUGHNESS,
                    amulet + "perfect_diamond_armortoughness_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.BROKEN_EMERALD.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "broken_emerald_bonus" + slotName,
                    0.25,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_EMERALD.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "polished_emerald_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_EMERALD.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    amulet + "perfect_emerald_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.BROKEN_RUBY.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    amulet + "broken_ruby_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_RUBY.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    amulet + "polished_ruby_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_RUBY.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    amulet + "perfect_ruby_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.BROKEN_SAPPHIRE.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    CoreAttributes.MAGIC_AMP.get(),
                    amulet + "broken_sapphire_bonus" + slotName,
                    0.1,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.POLISHED_SAPPHIRE.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    CoreAttributes.MAGIC_AMP.get(),
                    amulet + "polished_sapphire_bonus" + slotName,
                    0.2,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.PERFECT_SAPPHIRE.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    CoreAttributes.MAGIC_AMP.get(),
                    amulet + "perfect_sapphire_bonus" + slotName,
                    0.33,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.BROKEN_TOPAZ.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "broken_topaz_attackspeed_bonus" + slotName,
                    0.075,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "broken_topaz_movementspeed_bonus" + slotName,
                    0.075,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.POLISHED_TOPAZ.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "polished_topaz_attackspeed_bonus" + slotName,
                    0.125,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "polished_topaz_movementspeed_bonus" + slotName,
                    0.125,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.PERFECT_TOPAZ.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    amulet + "perfect_topaz_attackspeed_bonus" + slotName,
                    0.33,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    amulet + "perfect_topaz_movementspeed_bonus" + slotName,
                    0.33,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        }
    }

    public void applyArmorMinorBonus(Player player, EquipmentSlot armorType, int slotIndex) {
        String slotName = "_slot_" + slotIndex;
        String armorSlot = "armor_" + armorType.getName();
        if (this == ModItems.BROKEN_AMETHYST.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    armorSlot + "broken_amethyst_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
            setHealthUpdated(player, false);
        } else if (this == ModItems.POLISHED_AMETHYST.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    armorSlot + "polished_amethyst_bonus" + slotName,
                    4.0,
                    AttributeModifier.Operation.ADDITION);
            setHealthUpdated(player, false);
        } else if (this == ModItems.PERFECT_AMETHYST.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MAX_HEALTH,
                    armorSlot + "perfect_amethyst_bonus" + slotName,
                    8.0,
                    AttributeModifier.Operation.ADDITION);
            setHealthUpdated(player, false);
        } else if (this == ModItems.BROKEN_DIAMOND.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    armorSlot + "broken_diamond_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_DIAMOND.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    armorSlot + "polished_diamond_armor_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR_TOUGHNESS,
                    armorSlot + "polished_diamond_armortoughness_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_DIAMOND.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR,
                    armorSlot + "perfect_diamond_bonus" + slotName,
                    4.0,
                    AttributeModifier.Operation.ADDITION);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ARMOR_TOUGHNESS,
                    armorSlot + "perfect_diamond_armortoughness_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.BROKEN_EMERALD.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    armorSlot + "broken_emerald_bonus" + slotName,
                    0.25,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_EMERALD.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    armorSlot + "polished_emerald_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_EMERALD.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.LUCK,
                    armorSlot + "perfect_emerald_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.BROKEN_RUBY.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    armorSlot + "broken_ruby_bonus" + slotName,
                    0.5,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.POLISHED_RUBY.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    armorSlot + "polished_ruby_bonus" + slotName,
                    1.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.PERFECT_RUBY.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_DAMAGE,
                    armorSlot + "perfect_ruby_bonus" + slotName,
                    2.0,
                    AttributeModifier.Operation.ADDITION);
        } else if (this == ModItems.BROKEN_SAPPHIRE.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    CoreAttributes.MAGIC_AMP.get(),
                    armorSlot + "broken_sapphire_bonus" + slotName,
                    0.1,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.POLISHED_SAPPHIRE.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    CoreAttributes.MAGIC_AMP.get(),
                    armorSlot + "polished_sapphire_bonus" + slotName,
                    0.2,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.PERFECT_SAPPHIRE.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    CoreAttributes.MAGIC_AMP.get(),
                    armorSlot + "perfect_sapphire_bonus" + slotName,
                    0.33,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.BROKEN_TOPAZ.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    armorSlot + "broken_topaz_attackspeed_bonus" + slotName,
                    0.075,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    armorSlot + "broken_topaz_movementspeed_bonus" + slotName,
                    0.075,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.POLISHED_TOPAZ.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    armorSlot + "polished_topaz_attackspeed_bonus" + slotName,
                    0.125,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    armorSlot + "polished_topaz_movementspeed_bonus" + slotName,
                    0.125,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (this == ModItems.PERFECT_TOPAZ.get()) {
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    armorSlot + "perfect_topaz_attackspeed_bonus" + slotName,
                    0.33,
                    AttributeModifier.Operation.MULTIPLY_BASE);
            CoreAttributeModifiers.applyPermanentModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    armorSlot + "perfect_topaz_movementspeed_bonus" + slotName,
                    0.33,
                    AttributeModifier.Operation.MULTIPLY_BASE);
        }
    }

    public static void removeAmuletBonus(Player player) {
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ARMOR, "amulet");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ARMOR_TOUGHNESS, "amulet");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ATTACK_DAMAGE, "amulet");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ATTACK_SPEED, "amulet");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.LUCK, "amulet");
        CoreAttributeModifiers.removeModifiersByPrefix(player, CoreAttributes.MAGIC_AMP.get(), "amulet");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.MAX_HEALTH, "amulet");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.MOVEMENT_SPEED, "amulet");
        CoreAttributeModifiers.removeModifiersByPrefix(player, CoreAttributes.DODGE_CHANCE.get(), "amulet");

        if (!isHealthUpdated(player)) {
            updatePlayerHealth(player);
        }

        if(!player.isCreative() && player.getPersistentData().getBoolean("ChrysoberylFlight")) {
            player.getPersistentData().putBoolean("ChrysoberylFlight", false);
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    public static void applyArmorGemBonuses(Player player, ItemStack armorPiece, EquipmentSlot armorType) {
        if (!armorPiece.hasTag()) return;

        CompoundTag armorTag = armorPiece.getTag();
        if (armorTag == null) return;

        if (armorTag.contains("MinorGems")) {
            ListTag minorGemsTag = armorTag.getList("MinorGems", 10);
            for (int i = 0; i < minorGemsTag.size(); i++) {
                CompoundTag minorGemTag = minorGemsTag.getCompound(i);
                ItemStack minorGem = ItemStack.of(minorGemTag);
                if (minorGem.getItem() instanceof PreciousGemItem preciousGem) {
                    preciousGem.applyArmorMinorBonus(player, armorType, i + 1);
                }
            }
        }
    }


    public static void removeArmorBonus(Player player) {
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ARMOR, "armor");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ARMOR_TOUGHNESS, "armor");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ATTACK_DAMAGE, "armor");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.ATTACK_SPEED, "armor");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.LUCK, "armor");
        CoreAttributeModifiers.removeModifiersByPrefix(player, CoreAttributes.MAGIC_AMP.get(), "armor");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.MAX_HEALTH, "armor");
        CoreAttributeModifiers.removeModifiersByPrefix(player, Attributes.MOVEMENT_SPEED, "armor");
        CoreAttributeModifiers.removeModifiersByPrefix(player, CoreAttributes.DODGE_CHANCE.get(), "armor");

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

    private static boolean isHealthUpdated(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean("GearHealthBonusUpdated");
    }

    private static void setHealthUpdated(Player player, boolean updated) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean("GearHealthBonusUpdated", updated);
    }
}


