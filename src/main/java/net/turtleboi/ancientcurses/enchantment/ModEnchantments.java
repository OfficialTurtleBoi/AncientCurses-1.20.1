package net.turtleboi.ancientcurses.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.enchantment.goldenfeatherenchantments.FurtherDashEnchantment;
import net.turtleboi.ancientcurses.enchantment.goldenfeatherenchantments.QuickDashEnchantment;
import net.turtleboi.ancientcurses.enchantment.goldenfeatherenchantments.SeismicDashEnchantment;
import net.turtleboi.ancientcurses.enchantment.goldenfeatherenchantments.SpeedDashEnchantment;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, AncientCurses.MOD_ID);

    public static RegistryObject<Enchantment> FURTHER_DASH =
            ENCHANTMENTS.register("further_dash",
                    ()-> new FurtherDashEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,EquipmentSlot.values()));
    public static RegistryObject<Enchantment> QUICK_DASH =
            ENCHANTMENTS.register("quick_dash",
                    ()-> new QuickDashEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,EquipmentSlot.values()));
    public static RegistryObject<Enchantment> SPEED_DASH =
            ENCHANTMENTS.register("speed_dash",
                    ()-> new SpeedDashEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,EquipmentSlot.values()));
    public static RegistryObject<Enchantment> SEISMIC_DASH =
            ENCHANTMENTS.register("seismic_dash",
                    ()-> new SeismicDashEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,EquipmentSlot.values()));
    public static void register(IEventBus eventBus){
        ENCHANTMENTS.register(eventBus);
    }

}
