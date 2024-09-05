package net.turtleboi.ancientcurses.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.sound.ModSounds;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AncientCurses.MOD_ID);

    public static final RegistryObject<Item> GOLDEN_AMULET = ITEMS.register("golden_amulet",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BROKEN_GEM = ITEMS.register("broken_gem",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> FINE_GEM = ITEMS.register("fine_gem",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PERFECT_GEM = ITEMS.register("perfect_gem",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DEPRECOPHOBIA_MUSIC_DISC = ITEMS.register("deprecophobia_music_disc",
            () -> new RecordItem(13, ModSounds.DEPRECOPHOBIA, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 2780));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
