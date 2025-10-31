package net.turtleboi.ancientcurses.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.util.ModTags;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DOWSING_ROD.get())
                .pattern("SS")
                .pattern(" S")
                .define('S', Items.STICK)
                .unlockedBy("has_sticks", has(Items.STICK))
                .save(pWriter);
    }
}
