package net.turtleboi.ancientcurses.enchantment.goldenfeatherenchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.MendingEnchantment;
import net.turtleboi.ancientcurses.item.ModItems;

public class QuickDashEnchantment extends Enchantment {
    public QuickDashEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot[] pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }




    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack pStack) {
        return pStack.getItem()== ModItems.GOLDEN_FEATHER.get();
    }

    public boolean checkCompatibility(Enchantment pEnch) {
        return pEnch instanceof FurtherDashEnchantment ? false : super.checkCompatibility(pEnch);
    }
}
