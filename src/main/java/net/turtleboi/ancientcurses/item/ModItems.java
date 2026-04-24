package net.turtleboi.ancientcurses.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.item.items.CursedPearlItem;
import net.turtleboi.ancientcurses.item.items.DowsingRod;
import net.turtleboi.ancientcurses.item.items.FirstBeaconItem;
import net.turtleboi.ancientcurses.item.items.GoldenAmuletItem;
import net.turtleboi.ancientcurses.item.items.GoldenFeatherItem;
import net.turtleboi.ancientcurses.item.items.PreciousGemItem;
import net.turtleboi.ancientcurses.item.items.PreciousGemType;
import net.turtleboi.ancientcurses.item.items.SconcedCursedTorchItem;
import net.turtleboi.ancientcurses.item.items.SconcedRedstoneTorchItem;
import net.turtleboi.ancientcurses.item.items.SconcedSoulTorchItem;
import net.turtleboi.ancientcurses.item.items.SconcedTorchItem;
import net.turtleboi.ancientcurses.item.items.SconcedUnlitSoulTorchItem;
import net.turtleboi.ancientcurses.item.items.SconcedUnlitTorchItem;
import net.turtleboi.ancientcurses.item.items.SoulShardItem;
import net.turtleboi.ancientcurses.sound.ModSounds;
import net.turtleboi.turtlecore.item.CoreItems;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AncientCurses.MOD_ID);

    public static final RegistryObject<Item> GOLDEN_AMULET = ITEMS.register("golden_amulet",
            () -> new GoldenAmuletItem(new Item.Properties()));

    public static final RegistryObject<Item> BROKEN_AMETHYST = registerGem(PreciousGemType.BROKEN_AMETHYST, Rarity.UNCOMMON);
    public static final RegistryObject<Item> BROKEN_DIAMOND = registerGem(PreciousGemType.BROKEN_DIAMOND, Rarity.UNCOMMON);
    public static final RegistryObject<Item> BROKEN_EMERALD = registerGem(PreciousGemType.BROKEN_EMERALD, Rarity.UNCOMMON);
    public static final RegistryObject<Item> BROKEN_RUBY = registerGem(PreciousGemType.BROKEN_RUBY, Rarity.UNCOMMON);
    public static final RegistryObject<Item> BROKEN_SAPPHIRE = registerGem(PreciousGemType.BROKEN_SAPPHIRE, Rarity.UNCOMMON);
    public static final RegistryObject<Item> BROKEN_TOPAZ = registerGem(PreciousGemType.BROKEN_TOPAZ, Rarity.UNCOMMON);

    public static final RegistryObject<Item> POLISHED_AMETHYST = registerGem(PreciousGemType.POLISHED_AMETHYST, Rarity.RARE);
    public static final RegistryObject<Item> POLISHED_DIAMOND = registerGem(PreciousGemType.POLISHED_DIAMOND, Rarity.RARE);
    public static final RegistryObject<Item> POLISHED_EMERALD = registerGem(PreciousGemType.POLISHED_EMERALD, Rarity.RARE);
    public static final RegistryObject<Item> POLISHED_RUBY = registerGem(PreciousGemType.POLISHED_RUBY, Rarity.RARE);
    public static final RegistryObject<Item> POLISHED_SAPPHIRE = registerGem(PreciousGemType.POLISHED_SAPPHIRE, Rarity.RARE);
    public static final RegistryObject<Item> POLISHED_TOPAZ = registerGem(PreciousGemType.POLISHED_TOPAZ, Rarity.RARE);

    public static final RegistryObject<Item> PERFECT_AMETHYST = registerGem(PreciousGemType.PERFECT_AMETHYST, Rarity.EPIC);
    public static final RegistryObject<Item> PERFECT_DIAMOND = registerGem(PreciousGemType.PERFECT_DIAMOND, Rarity.EPIC);
    public static final RegistryObject<Item> PERFECT_EMERALD = registerGem(PreciousGemType.PERFECT_EMERALD, Rarity.EPIC);
    public static final RegistryObject<Item> PERFECT_RUBY = registerGem(PreciousGemType.PERFECT_RUBY, Rarity.EPIC);
    public static final RegistryObject<Item> PERFECT_SAPPHIRE = registerGem(PreciousGemType.PERFECT_SAPPHIRE, Rarity.EPIC);
    public static final RegistryObject<Item> PERFECT_TOPAZ = registerGem(PreciousGemType.PERFECT_TOPAZ, Rarity.EPIC);

    public static final RegistryObject<Item> ANCIENT_ALEXANDRITE = registerGem(PreciousGemType.ANCIENT_ALEXANDRITE, CoreItems.LEGENDARY);
    public static final RegistryObject<Item> ANCIENT_BISMUTH = registerGem(PreciousGemType.ANCIENT_BISMUTH, CoreItems.LEGENDARY);
    public static final RegistryObject<Item> ANCIENT_CHRYSOBERYL = registerGem(PreciousGemType.ANCIENT_CHRYSOBERYL, CoreItems.LEGENDARY);

    public static final RegistryObject<Item> DEPRECOPHOBIA_MUSIC_DISC = ITEMS.register("deprecophobia_music_disc",
            () -> new RecordItem(13, ModSounds.DEPRECOPHOBIA, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 2780));

    public static final RegistryObject<Item> ROT_CLUMP = ITEMS.register("rot_clump",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SMOKY_QUARTZ = ITEMS.register("smoky_quartz",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SOUL_SHARD = ITEMS.register("soul_shard",
            () -> new SoulShardItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

    public static final RegistryObject<Item> CURSED_SOUL_SHARD = ITEMS.register("cursed_soul_shard",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).stacksTo(16)));

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

    public static final RegistryObject<Item> SCONCED_UNLIT_SOUL_TORCH_ITEM = ITEMS.register("sconced_unlit_soul_torch",
            () -> new SconcedUnlitSoulTorchItem(ModBlocks.SCONCED_UNLIT_SOUL_TORCH.get(), new Item.Properties()));

    public static final RegistryObject<Item> CURSED_LANTERN = ITEMS.register("cursed_lantern",
            () -> new BlockItem(ModBlocks.CURSED_LANTERN.get(), new Item.Properties()));

    public static final RegistryObject<Item> CURSED_PEARL = ITEMS.register("cursed_pearl",
            () -> new CursedPearlItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> DOWSING_ROD = ITEMS.register("dowsing_rod",
            () -> new DowsingRod(new Item.Properties()));

    public static final RegistryObject<Item> GOLDEN_FEATHER = ITEMS.register("golden_feather",
            () -> new GoldenFeatherItem(new Item.Properties().rarity(CoreItems.LEGENDARY).stacksTo(1).durability(32)));

    public static final RegistryObject<Item> FIRST_BEACON = ITEMS.register("first_beacon",
            () -> new FirstBeaconItem(new Item.Properties().rarity(CoreItems.LEGENDARY).stacksTo(1).durability(600)));

    private static RegistryObject<Item> registerGem(PreciousGemType gemType, Rarity rarity) {
        return ITEMS.register(gemType.getItemName(),
                () -> new PreciousGemItem(new Item.Properties().rarity(rarity), gemType));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
