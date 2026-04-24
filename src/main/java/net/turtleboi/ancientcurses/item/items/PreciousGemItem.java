package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.item.items.util.GemBonusUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class PreciousGemItem extends Item {
    private final PreciousGemType gemType;

    public PreciousGemItem(Properties pProperties, PreciousGemType gemType) {
        super(pProperties);
        this.gemType = gemType;
    }

    public PreciousGemType getGemType() {
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
        GemBonusUtil.applyMajorBonus(player, gemType, slotIndex);
    }

    public void applyAmuletMinorBonus(Player player, int slotIndex) {
        GemBonusUtil.applyAmuletMinorBonus(player, gemType, slotIndex);
    }

    public void applyArmorMinorBonus(Player player, EquipmentSlot armorType, int slotIndex) {
        GemBonusUtil.applyArmorMinorBonus(player, gemType, armorType, slotIndex);
    }

    public static void removeAmuletBonus(Player player) {
        GemBonusUtil.removeAmuletBonus(player);
    }

    public static void applyArmorGemBonuses(Player player, ItemStack armorPiece, EquipmentSlot armorType) {
        GemBonusUtil.applyArmorGemBonuses(player, armorPiece, armorType);
    }


    public static void removeArmorBonus(Player player) {
        GemBonusUtil.removeArmorBonus(player);
    }

    public static void updatePlayerHealth(Player player) {
        GemBonusUtil.updatePlayerHealth(player);
    }
}


