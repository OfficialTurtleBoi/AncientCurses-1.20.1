package net.turtleboi.ancientcurses.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.item.ModItems;

import java.util.*;

public class ItemValueMap {

    private static final Map<Item, Integer> ITEM_VALUE_MAP = new HashMap<>();

    static {
        ITEM_VALUE_MAP.put(Items.IRON_INGOT, 10);
        ITEM_VALUE_MAP.put(Items.GOLD_INGOT, 33);
        ITEM_VALUE_MAP.put(Items.DIAMOND, 50);
        ITEM_VALUE_MAP.put(Items.NETHERITE_INGOT, 100);
    }

    public static int getItemValue(ItemStack itemStack, Level level) {
        Item item = itemStack.getItem();

        if (ITEM_VALUE_MAP.containsKey(item)) {
            return ITEM_VALUE_MAP.get(item);
        }

        int craftingValue = calculateValueFromRecipe(itemStack, level);
        return craftingValue > 0 ? craftingValue : getBaseValue(item.getDefaultInstance());
    }

    private static final Map<Item, Integer> ITEM_VALUE_CACHE = new HashMap<>();
    private static final Set<Item> CURRENTLY_PROCESSING_ITEMS = new HashSet<>();

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

        for (CraftingRecipe recipe : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (ItemStack.isSameItem(recipe.getResultItem(level.registryAccess()), itemStack)) {
                int resultCount = recipe.getResultItem(level.registryAccess()).getCount();

                for (Ingredient ingredient : recipe.getIngredients()) {
                    ItemStack[] matchingStacks = ingredient.getItems();
                    if (matchingStacks.length > 0) {
                        totalValue += getItemValue(matchingStacks[0], level);
                    }
                }

                totalValue = totalValue / Math.max(resultCount, 1);
            }
        }

        totalValue = Math.max(totalValue, 1);
        ITEM_VALUE_CACHE.put(item, totalValue);
        CURRENTLY_PROCESSING_ITEMS.remove(item);

        return totalValue;
    }


    private static int getBaseValue(ItemStack itemStack) {
        Rarity rarity = itemStack.getRarity();
        if (rarity == Rarity.COMMON) {
            return 1;
        } else if (rarity == Rarity.UNCOMMON) {
            return 5;
        } else if (rarity == Rarity.RARE) {
            return 10;
        } else if (rarity == Rarity.EPIC) {
            return 20;
        } else if (rarity == ModItems.LEGENDARY) {
            return 100;
        } else {
            return 0;
        }
    }

}
