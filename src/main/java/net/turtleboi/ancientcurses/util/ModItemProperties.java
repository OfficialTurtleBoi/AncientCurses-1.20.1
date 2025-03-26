package net.turtleboi.ancientcurses.util;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.item.items.GoldenFeatherItem;

public class ModItemProperties {
    public static void addCustomItemProperties() {
        ItemProperties.register(ModItems.GOLDEN_FEATHER.get(), new  ResourceLocation(AncientCurses.MOD_ID, "broken"),
                (itemStack, clientLevel, livingEntity, i) -> {
                    return GoldenFeatherItem.canDash(itemStack) ? 0.0F : 1.0F;
                });

    }
}
