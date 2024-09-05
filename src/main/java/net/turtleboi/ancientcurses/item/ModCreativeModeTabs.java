package net.turtleboi.ancientcurses.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AncientCurses.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ANCIENTCURSES_TAB = CREATIVE_MODE_TABS.register("ancientcurses_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.GOLDEN_AMULET.get()))
                    .title(Component.translatable("creativetab.ancientcurses_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.GOLDEN_AMULET.get());
                        pOutput.accept(ModItems.DEPRECOPHOBIA_MUSIC_DISC.get());

                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
