package net.turtleboi.ancientcurses.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AncientCurses.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.GOLDEN_AMULET);
        simpleItem(ModItems.DEPRECOPHOBIA_MUSIC_DISC);
        simpleItem(ModItems.BROKEN_AMETHYST);
        simpleItem(ModItems.BROKEN_DIAMOND);
        simpleItem(ModItems.BROKEN_EMERALD);
        simpleItem(ModItems.BROKEN_RUBY);
        simpleItem(ModItems.BROKEN_SAPPHIRE);
        simpleItem(ModItems.BROKEN_TOPAZ);
        simpleItem(ModItems.POLISHED_AMETHYST);
        simpleItem(ModItems.POLISHED_DIAMOND);
        simpleItem(ModItems.POLISHED_EMERALD);
        simpleItem(ModItems.POLISHED_RUBY);
        simpleItem(ModItems.POLISHED_SAPPHIRE);
        simpleItem(ModItems.POLISHED_TOPAZ);
        simpleItem(ModItems.PERFECT_AMETHYST);
        simpleItem(ModItems.PERFECT_DIAMOND);
        simpleItem(ModItems.PERFECT_EMERALD);
        simpleItem(ModItems.PERFECT_RUBY);
        simpleItem(ModItems.PERFECT_SAPPHIRE);
        simpleItem(ModItems.PERFECT_TOPAZ);
        simpleItem(ModItems.ROT_CLUMP);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(AncientCurses.MOD_ID,"item/" + item.getId().getPath()));
    }
}
