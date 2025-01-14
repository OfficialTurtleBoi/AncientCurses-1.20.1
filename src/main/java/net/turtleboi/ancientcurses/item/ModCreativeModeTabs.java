package net.turtleboi.ancientcurses.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.enchantment.ModEnchantments;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AncientCurses.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ANCIENTCURSES_TAB = CREATIVE_MODE_TABS.register("ancientcurses_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.GOLDEN_AMULET.get()))
                    .title(Component.translatable("creativetab.ancientcurses_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModBlocks.CURSED_ALTAR.get());
                        pOutput.accept(ModBlocks.LAPIDARIST_TABLE.get());

                        pOutput.accept(ModBlocks.SCONCED_TORCH.get());
                        pOutput.accept(ModBlocks.SCONCED_SOUL_TORCH.get());
                        pOutput.accept(ModBlocks.SCONCED_REDSTONE_TORCH.get());
                        pOutput.accept(ModBlocks.SCONCED_CURSED_TORCH.get());
                        pOutput.accept(ModItems.GOLDEN_AMULET.get());
                        pOutput.accept(ModItems.DEPRECOPHOBIA_MUSIC_DISC.get());
                        pOutput.accept(ModItems.CURSED_PEARL.get());
                        pOutput.accept(ModItems.ROT_CLUMP.get());
                        pOutput.accept(ModItems.GOLDEN_FEATHER.get());
                        Enchantment enchantment = ModEnchantments.FURTHER_DASH.get();
                        EnchantmentInstance instance = new EnchantmentInstance(enchantment, enchantment.getMaxLevel());
                        pOutput.accept(EnchantedBookItem.createForEnchantment(instance));
                        enchantment = ModEnchantments.QUICK_DASH.get();
                        instance = new EnchantmentInstance(enchantment, enchantment.getMaxLevel());
                        pOutput.accept(EnchantedBookItem.createForEnchantment(instance));
                        enchantment = ModEnchantments.SPEED_DASH.get();
                        instance = new EnchantmentInstance(enchantment, enchantment.getMaxLevel());
                        pOutput.accept(EnchantedBookItem.createForEnchantment(instance));
                        enchantment = ModEnchantments.SEISMIC_DASH.get();
                        instance = new EnchantmentInstance(enchantment, enchantment.getMaxLevel());
                        pOutput.accept(EnchantedBookItem.createForEnchantment(instance));
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> ANCIENTCURSES_GEMS_TAB = CREATIVE_MODE_TABS.register("ancientcurses_gems_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.ANCIENT_ALEXANDRITE.get()))
                    .title(Component.translatable("creativetab.ancientcurses_gems_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.BROKEN_AMETHYST.get());
                        pOutput.accept(ModItems.BROKEN_DIAMOND.get());
                        pOutput.accept(ModItems.BROKEN_EMERALD.get());
                        pOutput.accept(ModItems.BROKEN_RUBY.get());
                        pOutput.accept(ModItems.BROKEN_SAPPHIRE.get());
                        pOutput.accept(ModItems.BROKEN_TOPAZ.get());
                        pOutput.accept(ModItems.POLISHED_AMETHYST.get());
                        pOutput.accept(ModItems.POLISHED_DIAMOND.get());
                        pOutput.accept(ModItems.POLISHED_EMERALD.get());
                        pOutput.accept(ModItems.POLISHED_RUBY.get());
                        pOutput.accept(ModItems.POLISHED_SAPPHIRE.get());
                        pOutput.accept(ModItems.POLISHED_TOPAZ.get());
                        pOutput.accept(ModItems.PERFECT_AMETHYST.get());
                        pOutput.accept(ModItems.PERFECT_DIAMOND.get());
                        pOutput.accept(ModItems.PERFECT_EMERALD.get());
                        pOutput.accept(ModItems.PERFECT_RUBY.get());
                        pOutput.accept(ModItems.PERFECT_SAPPHIRE.get());
                        pOutput.accept(ModItems.PERFECT_TOPAZ.get());
                        pOutput.accept(ModItems.ANCIENT_CHRYSOBERYL.get());
                        pOutput.accept(ModItems.ANCIENT_BISMUTH.get());
                        pOutput.accept(ModItems.ANCIENT_ALEXANDRITE.get());
                    })
                    .build());
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
