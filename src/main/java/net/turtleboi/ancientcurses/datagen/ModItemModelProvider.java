package net.turtleboi.ancientcurses.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AncientCurses.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.DEPRECOPHOBIA_MUSIC_DISC);
        simpleItem(ModItems.SCONCED_TORCH_ITEM);
        simpleItem(ModItems.SCONCED_SOUL_TORCH_ITEM);
        simpleItem(ModItems.SCONCED_REDSTONE_TORCH_ITEM);
        simpleItem(ModItems.SCONCED_CURSED_TORCH_ITEM);
        simpleItem(ModItems.SCONCED_UNLIT_TORCH_ITEM);
        simpleItem(ModItems.SCONCED_UNLIT_SOUL_TORCH_ITEM);
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
        simpleItem(ModItems.ANCIENT_ALEXANDRITE);
        simpleItem(ModItems.ANCIENT_BISMUTH);
        simpleItem(ModItems.ANCIENT_CHRYSOBERYL);
        simpleItem(ModItems.CURSED_PEARL);
        simpleItem(ModItems.DOWSING_ROD);
        simpleItem(ModItems.ROT_CLUMP);
        withExistingParent(ModBlocks.SOUL_ROCK.getId().getPath(), modLoc("block/soul_rock"));
        simpleItem(ModItems.SMOKY_QUARTZ);
        simpleItem(ModItems.SOUL_SHARD);
        simpleItem(ModItems.CURSED_SOUL_SHARD);
        simpleItem(ModItems.ICE_SPARK);
        simpleItem(ModItems.VOODOO_DOLL);
        tintedCauldronItem(ModItems.FATHOMLESS_CAULDRON);
        simpleItem(ModItems.HOLLOW_LANTERN);
        simpleItem(ModItems.BONE_FLUTE);
        simpleItem(ModItems.ECHO_STONE);
        simpleItem(ModItems.GILDED_TOME);
        compassItem(ModItems.SOUL_COMPASS);
        simpleItem(ModItems.EXODUS_TOTEM);
        simpleItem(ModItems.CRYSTAL_BALL);
        simpleItem(ModItems.BLOODPRICE_SIGIL);
        simpleItem(ModItems.THORN_CROWN);
        simpleItem(ModItems.RUINATION_BRAND);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(AncientCurses.MOD_ID,"item/" + item.getId().getPath()));
    }

    private void tintedCauldronItem(RegistryObject<Item> item) {
        String name = item.getId().getPath();

        // Filled variant: layer0 = base cauldron, layer1 = tintable overlay
        ItemModelBuilder filled = withExistingParent(name + "_filled", new ResourceLocation("item/generated"))
                .texture("layer0", modLoc("item/" + name))
                .texture("layer1", modLoc("item/" + name + "_fill"));

        // Base model: no overlay; swaps to filled when has_contents >= 1
        withExistingParent(name, new ResourceLocation("item/generated"))
                .texture("layer0", modLoc("item/" + name))
                .override()
                    .predicate(new ResourceLocation(AncientCurses.MOD_ID, "has_contents"), 1.0f)
                    .model(filled)
                .end();
    }

    private ItemModelBuilder compassItem(RegistryObject<Item> item) {
        for (int i = 0; i < 32; i++) {
            String frameName = String.format("%s_%02d", item.getId().getPath(), i);
            String vanillaTextureName = String.format("item/compass_%02d", i);
            withExistingParent(frameName, new ResourceLocation("item/generated"))
                    .texture("layer0", new ResourceLocation("minecraft", vanillaTextureName));
        }

        ItemModelBuilder model = withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation("minecraft", "item/compass_16"));

        for (int i = 0; i < 32; i++) {
            String modelName = String.format("item/%s_%02d", item.getId().getPath(), i);
            model.override()
                    .predicate(new ResourceLocation("angle"), i / 32.0F)
                    .model(getExistingFile(new ResourceLocation(AncientCurses.MOD_ID, modelName)))
                    .end();
        }

        return model;
    }
}
