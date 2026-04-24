package net.turtleboi.ancientcurses.block.altar;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.item.items.SoulShardItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class RitualUtil {
    private static final int CATALYST_SLOT = 0;
    private static final int FIRST_INGREDIENT_SLOT = 1;
    private static final int LAST_INGREDIENT_SLOT = 8;
    private static final int INGREDIENT_SLOT_COUNT = LAST_INGREDIENT_SLOT;
    private static final List<RitualRecipe> RITUAL_RECIPES = buildRitualRecipes();

    private static List<RitualRecipe> buildRitualRecipes() {
        List<RitualRecipe> recipes = new ArrayList<>();

        recipes.add(replayRecipe("replay_tier_1", 0, List.of(
                ingredient(ModItems.SMOKY_QUARTZ.get(), 4),
                ingredient(ModItems.ROT_CLUMP.get(), 2),
                ingredient(Items.IRON_INGOT, 2)
        )));
        recipes.add(replayRecipe("replay_tier_2", 1, List.of(
                ingredient(ModItems.CURSED_PEARL.get(), 4),
                ingredient(ModItems.SOUL_SHARD.get(), 2, RitualUtil::isFilledSoulShard),
                ingredient(Items.GOLD_INGOT, 2)
        )));
        recipes.add(replayRecipe("replay_tier_3", 2, List.of(
                ingredient(ModItems.SOUL_SHARD.get(), 4, RitualUtil::isFilledSoulShard),
                ingredient(ModItems.CURSED_PEARL.get(), 2),
                ingredient(Items.DIAMOND, 2)
        )));

        addCurseRecipeSet(recipes, "curse_avarice", ModEffects.CURSE_OF_AVARICE.get(), List.of(
                ingredient(Items.GOLD_INGOT, 1),
                ingredient(Items.RAW_GOLD, 1),
                ingredient(Items.GOLD_NUGGET, 1),
                ingredient(Items.CLOCK, 1)
        ));
        addCurseRecipeSet(recipes, "curse_ending", ModEffects.CURSE_OF_ENDING.get(), List.of(
                ingredient(Items.OBSIDIAN, 1),
                ingredient(Items.BONE, 1),
                ingredient(Items.SOUL_SAND, 1),
                ingredient(Items.ROTTEN_FLESH, 1)
        ));
        addCurseRecipeSet(recipes, "curse_envy", ModEffects.CURSE_OF_ENVY.get(), List.of(
                ingredient(Items.GOLD_INGOT, 1),
                ingredient(Items.LAPIS_LAZULI, 1),
                ingredient(Items.AMETHYST_SHARD, 1),
                ingredient(Items.REDSTONE, 1)
        ));
        addCurseRecipeSet(recipes, "curse_frailty", ModEffects.CURSE_OF_FRAILTY.get(), List.of(
                ingredient(Items.GLASS, 1),
                ingredient(Items.BONE, 1),
                ingredient(Items.BONE_MEAL, 1),
                ingredient(Items.STRING, 1)
        ));
        addCurseRecipeSet(recipes, "curse_gluttony", ModEffects.CURSE_OF_GLUTTONY.get(), List.of(
                ingredient(Items.COOKED_BEEF, 1),
                ingredient(Items.COOKED_PORKCHOP, 1),
                ingredient(Items.PUMPKIN_PIE, 1),
                ingredient(Items.BREAD, 1)
        ));
        addCurseRecipeSet(recipes, "curse_nature", ModEffects.CURSE_OF_NATURE.get(), List.of(
                ingredient(Items.VINE, 1),
                ingredient(Items.WHEAT_SEEDS, 1),
                ingredient(Items.OAK_SAPLING, 1),
                ingredient(Items.FERN, 1)
        ));
        addCurseRecipeSet(recipes, "curse_obsession", ModEffects.CURSE_OF_OBESSSION.get(), List.of(
                ingredient(Items.POPPY, 1),
                ingredient(Items.PINK_DYE, 1),
                ingredient(Items.SPIDER_EYE, 1),
                ingredient(Items.CHAIN, 1)
        ));
        addCurseRecipeSet(recipes, "curse_pestilence", ModEffects.CURSE_OF_PESTILENCE.get(), List.of(
                ingredient(Items.ROTTEN_FLESH, 1),
                ingredient(Items.SPIDER_EYE, 1),
                ingredient(Items.SLIME_BALL, 1),
                ingredient(Items.BONE, 1)
        ));
        addCurseRecipeSet(recipes, "curse_pride", ModEffects.CURSE_OF_PRIDE.get(), List.of(
                ingredient(Items.FEATHER, 1),
                ingredient(Items.GOLD_INGOT, 1),
                ingredient(Items.CLOCK, 1),
                ingredient(Items.GOLD_NUGGET, 1)
        ));
        addCurseRecipeSet(recipes, "curse_shadows", ModEffects.CURSE_OF_SHADOWS.get(), List.of(
                ingredient(Items.COAL, 1),
                ingredient(Items.BLACK_DYE, 1),
                ingredient(Items.INK_SAC, 1),
                ingredient(Items.AMETHYST_SHARD, 1)
        ));
        addCurseRecipeSet(recipes, "curse_sloth", ModEffects.CURSE_OF_SLOTH.get(), List.of(
                ingredient(Items.COBWEB, 1),
                ingredient(Items.SLIME_BALL, 1),
                ingredient(Items.WHITE_WOOL, 1),
                ingredient(Items.SNOWBALL, 1)
        ));
        addCurseRecipeSet(recipes, "curse_wrath", ModEffects.CURSE_OF_WRATH.get(), List.of(
                ingredient(Items.BLAZE_POWDER, 1),
                ingredient(Items.GUNPOWDER, 1),
                ingredient(Items.NETHER_BRICK, 1),
                ingredient(Items.RED_DYE, 1)
        ));

        return List.copyOf(recipes);
    }

    private static void addCurseRecipeSet(List<RitualRecipe> recipes, String idPrefix, MobEffect curse, List<RitualIngredient> supportIngredients) {
        recipes.add(curseRecipe(idPrefix + "_tier_1", curse, 0, List.of(
                ingredient(ModItems.SMOKY_QUARTZ.get(), 3)
        ), supportIngredients));
        recipes.add(curseRecipe(idPrefix + "_tier_2", curse, 1, List.of(
                ingredient(ModItems.CURSED_PEARL.get(), 3)
        ), supportIngredients));
        recipes.add(curseRecipe(idPrefix + "_tier_3", curse, 2, List.of(
                ingredient(ModItems.SOUL_SHARD.get(), 3, RitualUtil::isFilledSoulShard)
        ), supportIngredients));
    }

    public static boolean isRitualItem(CursedAltarBlockEntity altarEntity, Player player, ItemStack stack) {
        return getNextRitualSlot(altarEntity, player, stack) != -1;
    }

    public static int getNextRitualSlot(CursedAltarBlockEntity altarEntity, Player player, ItemStack stack) {
        if (stack.isEmpty() || !altarEntity.hasPlayerCompletedRite(player)) {
            return -1;
        }

        ItemStack catalyst = altarEntity.getRitualItemInSlot(CATALYST_SLOT);

        if (catalyst.isEmpty()) {
            return CATALYST_SLOT;
        }

        for (int slot = FIRST_INGREDIENT_SLOT; slot <= LAST_INGREDIENT_SLOT; slot++) {
            if (altarEntity.getRitualItemInSlot(slot).isEmpty()) {
                return slot;
            }
        }

        return -1;
    }

    public static boolean matchesRitual(CursedAltarBlockEntity altarEntity, Player player) {
        if (!altarEntity.hasPlayerCompletedRite(player)) {
            return false;
        }

        return hasCompleteRecipe(altarEntity);
    }

    public static boolean hasCompleteRecipe(CursedAltarBlockEntity altarEntity) {
        ItemStack catalyst = altarEntity.getRitualItemInSlot(CATALYST_SLOT);
        ItemStack[] ingredients = getIngredientStacks(altarEntity);
        return getMatchingRecipe(catalyst, ingredients) != null;
    }

    public static boolean tryStartMatchingRitual(Player player, CursedAltarBlockEntity altarEntity) {
        ItemStack catalyst = altarEntity.getRitualItemInSlot(CATALYST_SLOT);
        ItemStack[] ingredients = getIngredientStacks(altarEntity);
        RitualRecipe matchingRecipe = getMatchingRecipe(catalyst, ingredients);
        if (matchingRecipe == null) {
            return false;
        }

        for (int slot = CATALYST_SLOT; slot <= LAST_INGREDIENT_SLOT; slot++) {
            altarEntity.setRitualItemInSlot(slot, ItemStack.EMPTY);
        }

        matchingRecipe.start(player, altarEntity, catalyst, ingredients);
        altarEntity.setChanged();
        return true;
    }

    private static RitualRecipe getMatchingRecipe(ItemStack catalyst, ItemStack[] ingredients) {
        for (RitualRecipe recipe : RITUAL_RECIPES) {
            if (recipe.matches(catalyst, ingredients)) {
                return recipe;
            }
        }
        return null;
    }

    private static ItemStack[] getIngredientStacks(CursedAltarBlockEntity altarEntity) {
        ItemStack[] ingredients = new ItemStack[INGREDIENT_SLOT_COUNT];
        for (int slot = FIRST_INGREDIENT_SLOT; slot <= LAST_INGREDIENT_SLOT; slot++) {
            ingredients[slot - 1] = altarEntity.getRitualItemInSlot(slot);
        }
        return ingredients;
    }

    private static RitualRecipe replayRecipe(String id, int amplifier, List<RitualIngredient> ingredients) {
        return new RitualRecipe(
                id,
                catalyst -> catalyst.is(ModItems.SOUL_SHARD.get()) && SoulShardItem.isCharged(catalyst),
                ingredients,
                (player, altarEntity, catalyst, ritualItems) -> {
                    player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData ->
                            riteData.resetAltarAtPos(altarEntity.getBlockPos()));

                    RiteUtil.startRite(player, altarEntity, (ResourceLocation) null, amplifier);
                }
        );
    }

    private static RitualRecipe curseRecipe(String id, MobEffect curse, int amplifier, List<RitualIngredient> tierIngredients, List<RitualIngredient> supportIngredients) {
        List<RitualIngredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient(ModItems.SOUL_SHARD.get(), 1, RitualUtil::isFilledSoulShard));
        ingredients.addAll(tierIngredients);
        ingredients.addAll(supportIngredients);

        return new RitualRecipe(
                id,
                catalyst -> catalyst.is(ModItems.CURSED_SOUL_SHARD.get()),
                ingredients,
                (player, altarEntity, catalyst, ritualItems) -> {
                    player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData ->
                            riteData.resetAltarAtPos(altarEntity.getBlockPos()));

                    RiteUtil.startRite(player, altarEntity, curse, amplifier);
                }
        );
    }

    private static RitualIngredient ingredient(Item item, int count) {
        return new RitualIngredient(item, count, stack -> stack.is(item));
    }

    private static RitualIngredient ingredient(Item item, int count, Predicate<ItemStack> matcher) {
        return new RitualIngredient(item, count, matcher);
    }

    private static boolean isFilledSoulShard(ItemStack stack) {
        return stack.is(ModItems.SOUL_SHARD.get()) && SoulShardItem.isCharged(stack);
    }

    private record RitualRecipe(String id, Predicate<ItemStack> catalystPredicate, List<RitualIngredient> ingredients, RitualAction action) {
        boolean acceptsCatalyst(ItemStack catalyst) {
            return catalystPredicate.test(catalyst);
        }

        boolean acceptsIngredient(ItemStack catalyst, ItemStack[] ingredients, ItemStack candidate) {
            if (!acceptsCatalyst(catalyst)) {
                return false;
            }

            int requiredCount = getRequiredMatchCount(candidate);
            if (requiredCount <= 0) {
                return false;
            }

            int currentCount = 0;
            for (ItemStack ingredient : ingredients) {
                if (matchesAnyIngredient(ingredient) && matchesAnyIngredient(candidate) && ingredient.getItem() == candidate.getItem()) {
                    currentCount++;
                }
            }

            return currentCount < requiredCount;
        }

        boolean matches(ItemStack catalyst, ItemStack[] ingredients) {
            if (!acceptsCatalyst(catalyst) || ingredients.length != getRequiredIngredientCount()) {
                return false;
            }

            for (ItemStack ingredient : ingredients) {
                if (ingredient.isEmpty()) {
                    return false;
                }
            }

            List<ItemStack> checkedStacks = new ArrayList<>();
            for (ItemStack ingredient : ingredients) {
                if (containsMatchingStack(checkedStacks, ingredient)) {
                    continue;
                }

                if (!matchesAnyIngredient(ingredient)) {
                    return false;
                }

                int currentCount = 0;
                for (ItemStack otherIngredient : ingredients) {
                    if (otherIngredient.getItem() == ingredient.getItem()) {
                        currentCount++;
                    }
                }

                if (currentCount != getRequiredMatchCount(ingredient)) {
                    return false;
                }

                checkedStacks.add(ingredient.copyWithCount(1));
            }

            return true;
        }

        private int getRequiredIngredientCount() {
            int count = 0;
            for (RitualIngredient ingredient : ingredients) {
                count += ingredient.count();
            }
            return count;
        }

        private boolean matchesAnyIngredient(ItemStack stack) {
            for (RitualIngredient ingredient : ingredients) {
                if (ingredient.matches(stack)) {
                    return true;
                }
            }
            return false;
        }

        private int getRequiredMatchCount(ItemStack stack) {
            int total = 0;
            for (RitualIngredient ingredient : ingredients) {
                if (ingredient.matches(stack)) {
                    total += ingredient.count();
                }
            }
            return total;
        }

        private boolean containsMatchingStack(List<ItemStack> checkedStacks, ItemStack stack) {
            for (ItemStack checkedStack : checkedStacks) {
                if (checkedStack.getItem() == stack.getItem()) {
                    return true;
                }
            }
            return false;
        }

        void start(Player player, CursedAltarBlockEntity altarEntity, ItemStack catalyst, ItemStack[] ingredients) {
            action.start(player, altarEntity, catalyst, ingredients);
        }
    }

    private record RitualIngredient(Item item, int count, Predicate<ItemStack> matcher) {
        boolean matches(ItemStack stack) {
            return matcher.test(stack);
        }
    }

    @FunctionalInterface
    private interface RitualAction {
        void start(Player player, CursedAltarBlockEntity altarEntity, ItemStack catalyst, ItemStack[] ingredients);
    }
}
