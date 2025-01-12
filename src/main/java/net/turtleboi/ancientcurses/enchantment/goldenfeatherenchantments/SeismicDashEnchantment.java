package net.turtleboi.ancientcurses.enchantment.goldenfeatherenchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.turtleboi.ancientcurses.item.ModItems;

public class SeismicDashEnchantment extends Enchantment {
    public SeismicDashEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot[] pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }




    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean canEnchant(ItemStack pStack) {
        return pStack.getItem()== ModItems.GOLDEN_FEATHER.get();
    }
    public boolean checkCompatibility(Enchantment pEnch) {
        return pEnch instanceof SpeedDashEnchantment ? false : super.checkCompatibility(pEnch);
    }
}
