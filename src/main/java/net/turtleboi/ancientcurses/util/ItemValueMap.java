package net.turtleboi.ancientcurses.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.turtlecore.item.CoreItems;

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
    private static final ThreadLocal<Set<Item>> CURRENTLY_PROCESSING_ITEMS = ThreadLocal.withInitial(HashSet::new);
    private static final int MAX_RECURSION_DEPTH = 64;
    private static final ThreadLocal<Integer> CURRENT_RECURSION_DEPTH = ThreadLocal.withInitial(() -> 0);

    private static Object currentRecipeManagerIdentity = null;
    private static final Map<Item, List<CraftingRecipe>> resultToCraftingRecipes = new HashMap<>();
    private static final Map<Item, List<SmithingRecipe>> resultToSmithingRecipes = new HashMap<>();
    private static final Map<SmithingRecipe, ItemStack[]> smithingBaseCandidates = new HashMap<>();
    private static final Map<SmithingRecipe, ItemStack[]> smithingAdditionCandidates = new HashMap<>();
    private static ItemStack[] allSingletonItemStacks = null;

    public static int getItemValue(ItemStack itemStack, Level level) {
        ensureIndexesBuilt(level);

        Item item = itemStack.getItem();

        Integer predefinedValue = ITEM_VALUE_MAP.get(item);
        if (predefinedValue != null) {
            return predefinedValue + calculateEnchantmentValue(itemStack);
        }

        Integer cachedValue = ITEM_VALUE_CACHE.get(item);
        if (cachedValue != null) {
            return cachedValue + calculateEnchantmentValue(itemStack);
        }

        Set<Item> recursionSet = CURRENTLY_PROCESSING_ITEMS.get();
        if (!recursionSet.add(item)) {
            int cycleSafe = Math.max(1, getBaseValue(itemStack));
            ITEM_VALUE_CACHE.put(item, cycleSafe);
            recursionSet.remove(item);
            return cycleSafe + calculateEnchantmentValue(itemStack);
        }

        int previousDepth = CURRENT_RECURSION_DEPTH.get();
        CURRENT_RECURSION_DEPTH.set(previousDepth + 1);

        int computedBase;
        try {
            if (previousDepth >= MAX_RECURSION_DEPTH) {
                computedBase = Math.max(1, getBaseValue(itemStack));
            } else {
                computedBase = computeItemBaseValue(itemStack, level);
            }
        } finally {
            CURRENT_RECURSION_DEPTH.set(previousDepth);
            recursionSet.remove(item);
        }

        ITEM_VALUE_CACHE.put(item, computedBase);
        return computedBase + calculateEnchantmentValue(itemStack);
    }

    private static void ensureIndexesBuilt(Level level) {
        RecipeManager recipeManager = level.getRecipeManager();
        if (recipeManager != currentRecipeManagerIdentity) {
            rebuildIndexes(recipeManager, level);
            currentRecipeManagerIdentity = recipeManager;
            ITEM_VALUE_CACHE.clear();
        }
    }

    private static void rebuildIndexes(RecipeManager recipeManager, Level level) {
        resultToCraftingRecipes.clear();
        resultToSmithingRecipes.clear();
        smithingBaseCandidates.clear();
        smithingAdditionCandidates.clear();

        if (allSingletonItemStacks == null) {
            List<ItemStack> list = new ArrayList<>(ForgeRegistries.ITEMS.getValues().size());
            for (Item registryItem : ForgeRegistries.ITEMS) {
                list.add(new ItemStack(registryItem));
            }
            allSingletonItemStacks = list.toArray(ItemStack[]::new);
        }

        for (CraftingRecipe craftingRecipe : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
            Item resultItem = craftingRecipe.getResultItem(level.registryAccess()).getItem();
            resultToCraftingRecipes.computeIfAbsent(resultItem, k -> new ArrayList<>()).add(craftingRecipe);
        }

        for (SmithingRecipe smithingRecipe : recipeManager.getAllRecipesFor(RecipeType.SMITHING)) {
            Item resultItem = smithingRecipe.getResultItem(level.registryAccess()).getItem();
            resultToSmithingRecipes.computeIfAbsent(resultItem, k -> new ArrayList<>()).add(smithingRecipe);

            List<ItemStack> baseCandidates = new ArrayList<>();
            List<ItemStack> additionCandidates = new ArrayList<>();
            for (ItemStack candidate : allSingletonItemStacks) {
                if (smithingRecipe.isBaseIngredient(candidate)) {
                    baseCandidates.add(candidate);
                }
                if (smithingRecipe.isAdditionIngredient(candidate)) {
                    additionCandidates.add(candidate);
                }
            }
            smithingBaseCandidates.put(smithingRecipe, baseCandidates.toArray(ItemStack[]::new));
            smithingAdditionCandidates.put(smithingRecipe, additionCandidates.toArray(ItemStack[]::new));
        }
    }

    private static int computeItemBaseValue(ItemStack itemStack, Level level) {
        int baseValue = getBaseValue(itemStack);
        int recipeValue = calculateRecipeCost(itemStack, level);
        int total = baseValue + recipeValue;
        return Math.max(total, 1);
    }

    private static int calculateRecipeCost(ItemStack itemStack, Level level) {
        int craftingCost = calculateCraftingRecipe(itemStack, level);
        int smithingCost = calculateSmithingRecipe(itemStack, level);

        boolean hasCrafting = craftingCost != Integer.MAX_VALUE;
        boolean hasSmithing = smithingCost != Integer.MAX_VALUE;

        if (hasCrafting && hasSmithing) {
            return Math.max(craftingCost, smithingCost);
        } else if (hasCrafting) {
            return craftingCost;
        } else if (hasSmithing) {
            return smithingCost;
        } else {
            return 0;
        }
    }

    private static int calculateCraftingRecipe(ItemStack itemStack, Level level) {
        List<CraftingRecipe> recipes = resultToCraftingRecipes.get(itemStack.getItem());
        if (recipes == null || recipes.isEmpty()) return Integer.MAX_VALUE;

        int bestCost = Integer.MAX_VALUE;
        for (CraftingRecipe recipe : recipes) {
            ItemStack result = recipe.getResultItem(level.registryAccess());
            int resultCount = Math.max(1, result.getCount());

            int ingredientsCost = 0;
            for (Ingredient ingredient : recipe.getIngredients()) {
                ItemStack[] matchingStacks = ingredient.getItems();
                if (matchingStacks.length == 0) continue;

                int cheapest = Integer.MAX_VALUE;
                for (ItemStack candidate : matchingStacks) {
                    int cost = getItemValue(candidate, level);
                    if (cost < cheapest) cheapest = cost;
                }
                if (cheapest != Integer.MAX_VALUE) {
                    ingredientsCost += cheapest;
                }
            }

            int totalForThisRecipe = (ingredientsCost / resultCount) + getBaseValue(itemStack);
            if (totalForThisRecipe < bestCost) bestCost = totalForThisRecipe;
        }
        return bestCost;
    }

    private static int calculateSmithingRecipe(ItemStack itemStack, Level level) {
        List<SmithingRecipe> recipes = resultToSmithingRecipes.get(itemStack.getItem());
        if (recipes == null || recipes.isEmpty()) return Integer.MAX_VALUE;

        int bestCost = Integer.MAX_VALUE;
        for (SmithingRecipe recipe : recipes) {
            ItemStack[] baseCandidates = smithingBaseCandidates.getOrDefault(recipe, new ItemStack[0]);
            ItemStack[] additionCandidates = smithingAdditionCandidates.getOrDefault(recipe, new ItemStack[0]);

            int baseCost = cheapestCost(baseCandidates, level);
            int additionCost = cheapestCost(additionCandidates, level);

            if (baseCost == Integer.MAX_VALUE || additionCost == Integer.MAX_VALUE) continue;

            int totalForThisRecipe = baseCost + additionCost + getBaseValue(itemStack);
            if (totalForThisRecipe < bestCost) bestCost = totalForThisRecipe;
        }
        return bestCost;
    }

    private static int cheapestCost(ItemStack[] candidates, Level level) {
        if (candidates == null || candidates.length == 0) return Integer.MAX_VALUE;
        int cheapest = Integer.MAX_VALUE;
        for (ItemStack candidate : candidates) {
            int cost = getItemValue(candidate, level);
            if (cost < cheapest) cheapest = cost;
        }
        return cheapest;
    }

    private static int calculateEnchantmentValue(ItemStack itemStack) {
        if (!itemStack.isEnchanted()) return 0;
        int enchantmentValue = 0;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            int enchantmentLevel = entry.getValue();
            enchantmentValue += enchantmentLevel * 20;
        }
        return enchantmentValue;
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
        } else if (rarity == CoreItems.LEGENDARY) {
            return 500;
        } else {
            return 0;
        }
    }
}
