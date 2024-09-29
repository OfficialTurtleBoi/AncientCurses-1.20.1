package net.turtleboi.ancientcurses.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.item.items.*;
import net.turtleboi.ancientcurses.sound.ModSounds;

public class ModItems {
    public static final Rarity LEGENDARY = Rarity.create("LEGENDARY", ChatFormatting.GOLD);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AncientCurses.MOD_ID);

    public static final RegistryObject<Item> GOLDEN_AMULET = ITEMS.register("golden_amulet",
            () -> new GoldenAmuletItem(new Item.Properties()));

    public static final RegistryObject<Item> BROKEN_AMETHYST = ITEMS.register("broken_amethyst",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.UNCOMMON), PreciousGemType.BROKEN_AMETHYST));

    public static final RegistryObject<Item> BROKEN_DIAMOND = ITEMS.register("broken_diamond",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.UNCOMMON), PreciousGemType.BROKEN_DIAMOND));

    public static final RegistryObject<Item> BROKEN_EMERALD = ITEMS.register("broken_emerald",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.UNCOMMON), PreciousGemType.BROKEN_EMERALD));

    public static final RegistryObject<Item> BROKEN_RUBY = ITEMS.register("broken_ruby",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.UNCOMMON), PreciousGemType.BROKEN_RUBY));

    public static final RegistryObject<Item> BROKEN_SAPPHIRE = ITEMS.register("broken_sapphire",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.UNCOMMON), PreciousGemType.BROKEN_SAPPHIRE));

    public static final RegistryObject<Item> BROKEN_TOPAZ = ITEMS.register("broken_topaz",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.UNCOMMON), PreciousGemType.BROKEN_TOPAZ));

    public static final RegistryObject<Item> POLISHED_AMETHYST = ITEMS.register("polished_amethyst",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.RARE), PreciousGemType.POLISHED_AMETHYST));

    public static final RegistryObject<Item> POLISHED_DIAMOND = ITEMS.register("polished_diamond",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.RARE), PreciousGemType.POLISHED_DIAMOND));

    public static final RegistryObject<Item> POLISHED_EMERALD = ITEMS.register("polished_emerald",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.RARE), PreciousGemType.POLISHED_EMERALD));

    public static final RegistryObject<Item> POLISHED_RUBY = ITEMS.register("polished_ruby",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.RARE), PreciousGemType.POLISHED_RUBY));

    public static final RegistryObject<Item> POLISHED_SAPPHIRE = ITEMS.register("polished_sapphire",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.RARE), PreciousGemType.POLISHED_SAPPHIRE));

    public static final RegistryObject<Item> POLISHED_TOPAZ = ITEMS.register("polished_topaz",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.RARE), PreciousGemType.POLISHED_TOPAZ));

    public static final RegistryObject<Item> PERFECT_AMETHYST = ITEMS.register("perfect_amethyst",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.EPIC), PreciousGemType.PERFECT_AMETHYST));

    public static final RegistryObject<Item> PERFECT_DIAMOND = ITEMS.register("perfect_diamond",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.EPIC), PreciousGemType.PERFECT_DIAMOND));

    public static final RegistryObject<Item> PERFECT_EMERALD = ITEMS.register("perfect_emerald",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.EPIC), PreciousGemType.PERFECT_EMERALD));

    public static final RegistryObject<Item> PERFECT_RUBY = ITEMS.register("perfect_ruby",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.EPIC), PreciousGemType.PERFECT_RUBY));

    public static final RegistryObject<Item> PERFECT_SAPPHIRE = ITEMS.register("perfect_sapphire",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.EPIC), PreciousGemType.PERFECT_SAPPHIRE));

    public static final RegistryObject<Item> PERFECT_TOPAZ = ITEMS.register("perfect_topaz",
            () -> new PreciousGemItem(new Item.Properties().rarity(Rarity.EPIC), PreciousGemType.PERFECT_TOPAZ));

    public static final RegistryObject<Item> ANCIENT_ALEXANDRITE = ITEMS.register("ancient_alexandrite",
            () -> new PreciousGemItem(new Item.Properties().rarity(ModItems.LEGENDARY), PreciousGemType.ANCIENT_ALEXANDRITE));

    public static final RegistryObject<Item> ANCIENT_BISMUTH = ITEMS.register("ancient_bismuth",
            () -> new PreciousGemItem(new Item.Properties().rarity(ModItems.LEGENDARY), PreciousGemType.ANCIENT_BISMUTH));

    public static final RegistryObject<Item> ANCIENT_CHRYSOBERYL = ITEMS.register("ancient_chrysoberyl",
            () -> new PreciousGemItem(new Item.Properties().rarity(ModItems.LEGENDARY), PreciousGemType.ANCIENT_CHRYSOBERYL));

    public static final RegistryObject<Item> DEPRECOPHOBIA_MUSIC_DISC = ITEMS.register("deprecophobia_music_disc",
            () -> new RecordItem(13, ModSounds.DEPRECOPHOBIA, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 2780));

    public static final RegistryObject<Item> ROT_CLUMP = ITEMS.register("rotclump",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SCONCED_TORCH_ITEM = ITEMS.register("sconced_torch",
            () -> new SconcedTorchItem(ModBlocks.SCONCED_TORCH.get(), new Item.Properties()));

    public static final RegistryObject<Item> SCONCED_SOUL_TORCH_ITEM = ITEMS.register("sconced_soul_torch",
            () -> new SconcedSoulTorchItem(ModBlocks.SCONCED_SOUL_TORCH.get(), new Item.Properties()));

    public static final RegistryObject<Item> SCONCED_REDSTONE_TORCH_ITEM = ITEMS.register("sconced_redstone_torch",
            () -> new SconcedRedstoneTorchItem(ModBlocks.SCONCED_REDSTONE_TORCH.get(), new Item.Properties()));

    public static final RegistryObject<Item> SCONCED_CURSED_TORCH_ITEM = ITEMS.register("sconced_cursed_torch",
            () -> new SconcedCursedTorchItem(ModBlocks.SCONCED_CURSED_TORCH.get(), new Item.Properties()));

    public static final RegistryObject<Item> SCONCED_UNLIT_TORCH_ITEM = ITEMS.register("sconced_unlit_torch",
            () -> new SconcedUnlitTorchItem(ModBlocks.SCONCED_UNLIT_TORCH.get(), new Item.Properties()));

    public static final RegistryObject<Item> CURSED_PEARL = ITEMS.register("cursed_pearl",
            () -> new CursedPearlItem(new Item.Properties()));
    public static final RegistryObject<Item> SCONCED_UNLIT_SOUL_TORCH_ITEM = ITEMS.register("sconced_unlit_soul_torch",
            () -> new SconcedUnlitSoulTorchItem(ModBlocks.SCONCED_UNLIT_SOUL_TORCH.get(), new Item.Properties()));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
