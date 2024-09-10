package net.turtleboi.ancientcurses.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Predicate;

import static net.turtleboi.ancientcurses.item.ModItems.LEGENDARY;

public class ItemValueMap {

    private static final Map<Item, Integer> ITEM_VALUE_MAP = new HashMap<>();

    static {
        ITEM_VALUE_MAP.put(Items.IRON_INGOT, 10);
        ITEM_VALUE_MAP.put(Items.GOLD_INGOT, 33);
        ITEM_VALUE_MAP.put(Items.REDSTONE, 25);
        ITEM_VALUE_MAP.put(Items.LAPIS_LAZULI, 25);
        ITEM_VALUE_MAP.put(Items.DIAMOND, 50);
        ITEM_VALUE_MAP.put(Items.EMERALD, 75);
        ITEM_VALUE_MAP.put(Items.ANCIENT_DEBRIS, 33);
        ITEM_VALUE_MAP.put(Items.NETHERITE_SCRAP, 33);
        ITEM_VALUE_MAP.put(Items.NETHERITE_INGOT, 264);
        ITEM_VALUE_MAP.put(Items.NETHERITE_BLOCK, 2376);
        ITEM_VALUE_MAP.put(Items.TURTLE_HELMET, 44);
        ITEM_VALUE_MAP.put(Items.CHAINMAIL_HELMET, 100);
        ITEM_VALUE_MAP.put(Items.CHAINMAIL_CHESTPLATE, 200);
        ITEM_VALUE_MAP.put(Items.CHAINMAIL_LEGGINGS, 150);
        ITEM_VALUE_MAP.put(Items.CHAINMAIL_BOOTS, 50);
        ITEM_VALUE_MAP.put(Items.TRIDENT, 250);
        ITEM_VALUE_MAP.put(Items.IRON_HORSE_ARMOR, 135);
        ITEM_VALUE_MAP.put(Items.GOLDEN_HORSE_ARMOR, 235);
        ITEM_VALUE_MAP.put(Items.DIAMOND_HORSE_ARMOR, 335);
        ITEM_VALUE_MAP.put(Items.TOTEM_OF_UNDYING, 200);
        ITEM_VALUE_MAP.put(Items.ELYTRA, 200);
        ITEM_VALUE_MAP.put(Items.ENCHANTED_GOLDEN_APPLE, 400);
        ITEM_VALUE_MAP.put(Items.NETHER_STAR, 400);
    }

    private static final Map<Item, Integer> ITEM_VALUE_CACHE = new HashMap<>();
    private static final Set<Item> CURRENTLY_PROCESSING_ITEMS = new HashSet<>();

    public static int getItemValue(ItemStack itemStack, Level level) {
        Item item = itemStack.getItem();
        if (ITEM_VALUE_MAP.containsKey(item)) {
            int predefinedValue = ITEM_VALUE_MAP.get(item);
            return predefinedValue + calculateEnchantmentValue(itemStack);
        }
        int recipeValue = calculateValueFromRecipe(itemStack, level);
        int baseValue = getBaseValue(itemStack);
        int enchantmentValue = calculateEnchantmentValue(itemStack);
        return baseValue + recipeValue + enchantmentValue;
    }

    private static int calculateEnchantmentValue(ItemStack itemStack) {
        int enchantmentValue = 0;
        if (itemStack.isEnchanted()) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                int level = entry.getValue();
                enchantmentValue += level * 20;
            }
        }
        return enchantmentValue;
    }

    private static int calculateValueFromRecipe(ItemStack itemStack, Level level) {
        Item item = itemStack.getItem();
        if (CURRENTLY_PROCESSING_ITEMS.contains(item)) {
            return ITEM_VALUE_CACHE.getOrDefault(item, 1);
        }
        if (ITEM_VALUE_CACHE.containsKey(item)) {
            return ITEM_VALUE_CACHE.get(item);
        }
        CURRENTLY_PROCESSING_ITEMS.add(item);
        RecipeManager recipeManager = level.getRecipeManager();
        int totalValue = 0;
        totalValue += calculateCraftingRecipeValue(itemStack, level, recipeManager);
        totalValue += calculateSmithingRecipeValue(itemStack, level, recipeManager);
        totalValue = Math.max(totalValue, 1);
        ITEM_VALUE_CACHE.put(item, totalValue);
        CURRENTLY_PROCESSING_ITEMS.remove(item);
        return totalValue;
    }

    private static int calculateCraftingRecipeValue(ItemStack itemStack, Level level, RecipeManager recipeManager) {
        int totalValue = 0;
        for (CraftingRecipe recipe : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (ItemStack.isSameItem(recipe.getResultItem(level.registryAccess()), itemStack)) {
                int resultCount = recipe.getResultItem(level.registryAccess()).getCount();
                for (Ingredient ingredient : recipe.getIngredients()) {
                    ItemStack[] matchingStacks = ingredient.getItems();
                    if (matchingStacks.length > 0) {
                        totalValue += getItemValue(matchingStacks[0], level);
                    }
                }
                totalValue = (totalValue / Math.max(resultCount, 1)) + getBaseValue(itemStack);
            }
        }
        return totalValue;
    }

    private static int calculateSmithingRecipeValue(ItemStack itemStack, Level level, RecipeManager recipeManager) {
        int totalValue = 0;

        for (SmithingRecipe recipe : recipeManager.getAllRecipesFor(RecipeType.SMITHING)) {
            if (ItemStack.isSameItem(recipe.getResultItem(level.registryAccess()), itemStack)) {
                ItemStack base = findMatchingItem(recipe::isBaseIngredient);
                ItemStack addition = findMatchingItem(recipe::isAdditionIngredient);
                int baseValue = getItemValue(base, level);
                int additionValue = getItemValue(addition, level);
                totalValue += baseValue + additionValue + getBaseValue(itemStack);
            }
        }
        return totalValue;
    }

    private static ItemStack findMatchingItem(Predicate<ItemStack> predicate){
        for (Item item : ForgeRegistries.ITEMS) {
            ItemStack stack = new ItemStack(item);
            if (predicate.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static int getBaseValue(ItemStack itemStack) {
        Rarity rarity = itemStack.getRarity();
        if (rarity == Rarity.COMMON) {
            return 1;
        } else if (rarity == Rarity.UNCOMMON) {
            return 20;
        } else if (rarity == Rarity.RARE) {
            return 50;
        } else if (rarity == Rarity.EPIC) {
            return 100;
        } else if (rarity == LEGENDARY) {
            return 500;
        } else {
            return 0;
        }
    }

}
