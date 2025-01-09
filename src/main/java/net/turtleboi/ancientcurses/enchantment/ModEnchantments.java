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

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, AncientCurses.MOD_ID);

    public static RegistryObject<Enchantment> FURTHER_DASH =
            ENCHANTMENTS.register("further_dash",
                    ()-> new FurtherDashEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON,new EquipmentSlot[]{EquipmentSlot.MAINHAND}));

    public static void register(IEventBus eventBus){
        ENCHANTMENTS.register(eventBus);
    }

}
