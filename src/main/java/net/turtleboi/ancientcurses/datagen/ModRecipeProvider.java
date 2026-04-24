package net.turtleboi.ancientcurses.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.item.ModItems;
import org.jetbrains.annotations.NotNull;

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

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SMOKY_QUARTZ.get())
                .requires(Items.QUARTZ)
                .requires(Items.COAL)
                .unlockedBy("has_quartz", has(Items.QUARTZ))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SOUL_SHARD.get())
                .pattern(" Q ")
                .pattern("QSQ")
                .pattern(" Q ")
                .define('Q', ModItems.SMOKY_QUARTZ.get())
                .define('S', ModBlocks.SOUL_ROCK.get())
                .unlockedBy("has_smoky_quartz", has(ModItems.SMOKY_QUARTZ.get()))
                .save(pWriter);

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(Items.SOUL_SAND), RecipeCategory.BUILDING_BLOCKS, ModBlocks.SOUL_ROCK.get(), 0.1F, 200)
                .unlockedBy("has_soul_sand", has(Items.SOUL_SAND))
                .save(pWriter, AncientCurses.MOD_ID + ":soul_rock_from_soul_sand_smelting");

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(Items.SOUL_SOIL), RecipeCategory.BUILDING_BLOCKS, ModBlocks.SOUL_ROCK.get(), 0.1F, 200)
                .unlockedBy("has_soul_soil", has(Items.SOUL_SOIL))
                .save(pWriter, AncientCurses.MOD_ID + ":soul_rock_from_soul_soil_smelting");
    }
}
