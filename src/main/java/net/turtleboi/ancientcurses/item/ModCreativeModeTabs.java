package net.turtleboi.ancientcurses.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.ModBlocks;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AncientCurses.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ANCIENTCURSES_TAB = CREATIVE_MODE_TABS.register("ancientcurses_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.GOLDEN_AMULET.get()))
                    .title(Component.translatable("creativetab.ancientcurses_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModBlocks.CURSED_ALTAR.get());
                        pOutput.accept(ModBlocks.SCONCED_TORCH.get());
                        pOutput.accept(ModBlocks.SCONCED_SOUL_TORCH.get());
                        pOutput.accept(ModBlocks.SCONCED_REDSTONE_TORCH.get());
                        pOutput.accept(ModBlocks.SCONCED_CURSED_TORCH.get());
                        pOutput.accept(ModBlocks.SCONCED_UNLIT_TORCH.get());

                        pOutput.accept(ModItems.GOLDEN_AMULET.get());
                        pOutput.accept(ModItems.DEPRECOPHOBIA_MUSIC_DISC.get());
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
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
