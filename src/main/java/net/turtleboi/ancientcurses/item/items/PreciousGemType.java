package net.turtleboi.ancientcurses.item.items;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;

public enum PreciousGemType {
    BROKEN_AMETHYST(Component.translatable("item.ancientcurses.broken_amethyst.tooltip").toString()),
    POLISHED_AMETHYST(Component.translatable("item.ancientcurses.polished_amethyst.tooltip").toString()),
    PERFECT_AMETHYST(Component.translatable("item.ancientcurses.perfect_amethyst.tooltip").toString()),
    BROKEN_DIAMOND(Component.translatable("item.ancientcurses.broken_diamond.tooltip").toString()),
    POLISHED_DIAMOND(Component.translatable("item.ancientcurses.polished_diamond.tooltip").toString()),
    PERFECT_DIAMOND(Component.translatable("item.ancientcurses.perfect_diamond.tooltip").toString()),
    BROKEN_EMERALD(Component.translatable("item.ancientcurses.broken_emerald.tooltip").toString()),
    POLISHED_EMERALD(Component.translatable("item.ancientcurses.polished_emerald.tooltip").toString()),
    PERFECT_EMERALD(Component.translatable("item.ancientcurses.perfect_emerald.tooltip").toString()),
    BROKEN_RUBY(Component.translatable("item.ancientcurses.broken_ruby.tooltip").toString()),
    POLISHED_RUBY(Component.translatable("item.ancientcurses.polished_ruby.tooltip").toString()),
    PERFECT_RUBY(Component.translatable("item.ancientcurses.perfect_ruby.tooltip").toString()),
    BROKEN_SAPPHIRE(Component.translatable("item.ancientcurses.broken_sapphire.tooltip").toString()),
    POLISHED_SAPPHIRE(Component.translatable("item.ancientcurses.polished_sapphire.tooltip").toString()),
    PERFECT_SAPPHIRE(Component.translatable("item.ancientcurses.perfect_sapphire.tooltip").toString()),
    BROKEN_TOPAZ(Component.translatable("item.ancientcurses.broken_topaz.tooltip").toString()),
    POLISHED_TOPAZ(Component.translatable("item.ancientcurses.polished_topaz.tooltip").toString()),
    PERFECT_TOPAZ(Component.translatable("item.ancientcurses.perfect_topaz.tooltip").toString()),
    ANCIENT_ALEXANDRITE(Component.translatable("item.ancientcurses.ancient_alexandrite.tooltip").toString()),
    ANCIENT_BISMUTH(Component.translatable("item.ancientcurses.ancient_bismuth.tooltip").toString()),
    ANCIENT_CHRYSOBERYL(Component.translatable("item.ancientcurses.ancient_chrysoberyl.tooltip").toString());


    private final String bonuses;

    PreciousGemType(String bonuses) {
        this.bonuses = bonuses;
    }

    public String getBonuses() {
        return bonuses;
    }
}
