package net.turtleboi.ancientcurses.enchantment.feather;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.turtleboi.ancientcurses.item.ModItems;
import org.jetbrains.annotations.NotNull;

public class SeismicEnchantment extends Enchantment {
    public SeismicEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot[] pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }

    @Override
    public boolean canEnchant(ItemStack pStack) {
        return pStack.getItem()== ModItems.GOLDEN_FEATHER.get();
    }

    public boolean checkCompatibility(@NotNull Enchantment pEnch) {
        return !(pEnch instanceof ZephyrRushEnchantment) && super.checkCompatibility(pEnch);
    }
}
