package net.turtleboi.ancientcurses.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.enchantment.feather.SoaringEnchantment;
import net.turtleboi.ancientcurses.enchantment.feather.TailwindEnchantment;
import net.turtleboi.ancientcurses.enchantment.feather.SeismicEnchantment;
import net.turtleboi.ancientcurses.enchantment.feather.ZephyrRushEnchantment;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, AncientCurses.MOD_ID);

    public static RegistryObject<Enchantment> SOARING =
            ENCHANTMENTS.register("soaring",
                    ()-> new SoaringEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,EquipmentSlot.values()));

    public static RegistryObject<Enchantment> TAILWIND =
            ENCHANTMENTS.register("tailwind",
                    ()-> new TailwindEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,EquipmentSlot.values()));

    public static RegistryObject<Enchantment> ZEPHYR_RUSH =
            ENCHANTMENTS.register("zephyr_rush",
                    ()-> new ZephyrRushEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,EquipmentSlot.values()));

    public static RegistryObject<Enchantment> SEISMIC =
            ENCHANTMENTS.register("seismic",
                    ()-> new SeismicEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,EquipmentSlot.values()));

    public static void register(IEventBus eventBus){
        ENCHANTMENTS.register(eventBus);
    }

}
