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
import net.turtleboi.turtlecore.item.CoreItems;

import java.util.*;

public class ItemValueMap {

    private static final Map<Item, Integer> ITEM_VALUE_MAP = new IdentityHashMap<>();
    private static final Map<Item, Integer> ITEM_BASE_VALUE_CACHE = new IdentityHashMap<>();
    private static final Map<RecipeManager, RecipeValueCache> RECIPE_VALUE_CACHES = new IdentityHashMap<>();

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

    public static void clearCaches() {
        ITEM_BASE_VALUE_CACHE.clear();
        RECIPE_VALUE_CACHES.clear();
    }

    public static int getItemValue(ItemStack itemStack, Level level) {
        Item item = itemStack.getItem();
        Integer predefinedValue = ITEM_VALUE_MAP.get(item);
        if (predefinedValue != null) {
            return predefinedValue + calculateEnchantmentValue(itemStack);
        }

        RecipeValueCache recipeValueCache = getRecipeValueCache(level);
        int recipeValue = recipeValueCache.getRecipeValue(item, level);
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

    private static RecipeValueCache getRecipeValueCache(Level level) {
        RecipeManager recipeManager = level.getRecipeManager();
        RecipeValueCache cache = RECIPE_VALUE_CACHES.get(recipeManager);
        if (cache == null) {
            cache = new RecipeValueCache(recipeManager);
            RECIPE_VALUE_CACHES.put(recipeManager, cache);
        }
        return cache;
    }

    private static int calculateCraftingRecipeValue(Item item, Level level, List<CraftingRecipe> recipes) {
        if (recipes.isEmpty()) {
            return 0;
        }

        int totalValue = 0;
        int baseValue = getBaseValue(item);

        for (CraftingRecipe recipe : recipes) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                ItemStack[] matchingStacks = ingredient.getItems();
                if (matchingStacks.length > 0) {
                    totalValue += getItemValue(matchingStacks[0], level);
                }
            }
            int resultCount = recipe.getResultItem(level.registryAccess()).getCount();
            totalValue = (totalValue / Math.max(resultCount, 1)) + baseValue;
        }
        return totalValue;
    }

    private static int calculateSmithingRecipeValue(Item item, Level level, List<SmithingRecipeValue> recipes) {
        if (recipes.isEmpty()) {
            return 0;
        }

        int totalValue = 0;
        int baseValue = getBaseValue(item);
        for (SmithingRecipeValue recipe : recipes) {
            totalValue += getItemValue(recipe.baseItem(), level);
            totalValue += getItemValue(recipe.additionItem(), level);
            totalValue += baseValue;
        }
        return totalValue;
    }

    private static int getBaseValue(ItemStack itemStack) {
        return getBaseValue(itemStack.getItem(), itemStack.getRarity());
    }

    private static int getBaseValue(Item item) {
        Integer cachedValue = ITEM_BASE_VALUE_CACHE.get(item);
        if (cachedValue != null) {
            return cachedValue;
        }

        int baseValue = getBaseValue(item, new ItemStack(item).getRarity());
        ITEM_BASE_VALUE_CACHE.put(item, baseValue);
        return baseValue;
    }

    private static int getBaseValue(Item item, Rarity rarity) {
        if (rarity == Rarity.COMMON) return 1;
        if (rarity == Rarity.UNCOMMON) return 20;
        if (rarity == Rarity.RARE) return 50;
        if (rarity == Rarity.EPIC) return 100;
        if (rarity == CoreItems.LEGENDARY) return 500;
        return 0;
    }

    private static ItemStack findFirstMatchingItem(Ingredient ingredient) {
        ItemStack[] matchingStacks = ingredient.getItems();
        if (matchingStacks.length > 0) {
            return matchingStacks[0];
        }
        return ItemStack.EMPTY;
    }

    private record SmithingRecipeValue(ItemStack baseItem, ItemStack additionItem) {}

    private static final class RecipeValueCache {
        private static final List<CraftingRecipe> NO_CRAFTING_RECIPES = Collections.emptyList();
        private static final List<SmithingRecipeValue> NO_SMITHING_RECIPES = Collections.emptyList();

        private final RecipeManager recipeManager;
        private final Map<Item, Integer> itemValueCache = new IdentityHashMap<>();
        private final Set<Item> currentlyProcessingItems = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<Item, List<CraftingRecipe>> craftingRecipesByResult = new IdentityHashMap<>();
        private final Map<Item, List<SmithingRecipeValue>> smithingRecipesByResult = new IdentityHashMap<>();
        private boolean craftingRecipesIndexed;
        private boolean smithingRecipesIndexed;

        private RecipeValueCache(RecipeManager recipeManager) {
            this.recipeManager = recipeManager;
        }

        private int getRecipeValue(Item item, Level level) {
            Integer cachedValue = itemValueCache.get(item);
            if (cachedValue != null) {
                return cachedValue;
            }
            if (!currentlyProcessingItems.add(item)) {
                return itemValueCache.getOrDefault(item, 1);
            }

            try {
                int totalValue = calculateCraftingRecipeValue(item, level, getCraftingRecipesFor(item, level));
                totalValue += calculateSmithingRecipeValue(item, level, getSmithingRecipesFor(item, level));
                totalValue = Math.max(totalValue, 1);

                itemValueCache.put(item, totalValue);
                return totalValue;
            } finally {
                currentlyProcessingItems.remove(item);
            }
        }

        private List<CraftingRecipe> getCraftingRecipesFor(Item item, Level level) {
            if (!craftingRecipesIndexed) {
                indexCraftingRecipes(level);
            }
            return craftingRecipesByResult.getOrDefault(item, NO_CRAFTING_RECIPES);
        }

        private List<SmithingRecipeValue> getSmithingRecipesFor(Item item, Level level) {
            if (!smithingRecipesIndexed) {
                indexSmithingRecipes(level);
            }
            return smithingRecipesByResult.getOrDefault(item, NO_SMITHING_RECIPES);
        }

        private void indexCraftingRecipes(Level level) {
            for (CraftingRecipe recipe : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
                Item resultItem = recipe.getResultItem(level.registryAccess()).getItem();
                craftingRecipesByResult
                        .computeIfAbsent(resultItem, ignored -> new ArrayList<>())
                        .add(recipe);
            }
            craftingRecipesIndexed = true;
        }

        private void indexSmithingRecipes(Level level) {
            for (SmithingRecipe recipe : recipeManager.getAllRecipesFor(RecipeType.SMITHING)) {
                if (!(recipe instanceof SmithingTransformRecipe)) {
                    continue;
                }

                Item resultItem = recipe.getResultItem(level.registryAccess()).getItem();
                ItemStack baseItem = resolveSmithingIngredient(recipe, true);
                ItemStack additionItem = resolveSmithingIngredient(recipe, false);
                if (baseItem.isEmpty() || additionItem.isEmpty()) {
                    continue;
                }

                smithingRecipesByResult
                        .computeIfAbsent(resultItem, ignored -> new ArrayList<>())
                        .add(new SmithingRecipeValue(baseItem, additionItem));
            }
            smithingRecipesIndexed = true;
        }

        private ItemStack resolveSmithingIngredient(SmithingRecipe recipe, boolean baseIngredient) {
            for (Item item : ForgeRegistries.ITEMS) {
                ItemStack candidate = new ItemStack(item);
                boolean matches = baseIngredient
                        ? recipe.isBaseIngredient(candidate)
                        : recipe.isAdditionIngredient(candidate);
                if (matches) {
                    return candidate;
                }
            }

            return ItemStack.EMPTY;
        }
    }

}
