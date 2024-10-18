package net.turtleboi.ancientcurses.item.items;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum PreciousGemType {
    BROKEN_AMETHYST("item.ancientcurses.broken_amethyst.tooltip"),
    POLISHED_AMETHYST("item.ancientcurses.polished_amethyst.tooltip"),
    PERFECT_AMETHYST("item.ancientcurses.perfect_amethyst.tooltip"),
    BROKEN_DIAMOND("item.ancientcurses.broken_diamond.tooltip"),
    POLISHED_DIAMOND("item.ancientcurses.polished_diamond.tooltip"),
    PERFECT_DIAMOND("item.ancientcurses.perfect_diamond.tooltip"),
    BROKEN_EMERALD("item.ancientcurses.broken_emerald.tooltip"),
    POLISHED_EMERALD("item.ancientcurses.polished_emerald.tooltip"),
    PERFECT_EMERALD("item.ancientcurses.perfect_emerald.tooltip"),
    BROKEN_RUBY("item.ancientcurses.broken_ruby.tooltip"),
    POLISHED_RUBY("item.ancientcurses.polished_ruby.tooltip"),
    PERFECT_RUBY("item.ancientcurses.perfect_ruby.tooltip"),
    BROKEN_SAPPHIRE("item.ancientcurses.broken_sapphire.tooltip"),
    POLISHED_SAPPHIRE("item.ancientcurses.polished_sapphire.tooltip"),
    PERFECT_SAPPHIRE("item.ancientcurses.perfect_sapphire.tooltip"),
    BROKEN_TOPAZ("item.ancientcurses.broken_topaz.tooltip"),
    POLISHED_TOPAZ("item.ancientcurses.polished_topaz.tooltip"),
    PERFECT_TOPAZ("item.ancientcurses.perfect_topaz.tooltip"),
    ANCIENT_ALEXANDRITE("item.ancientcurses.ancient_alexandrite.tooltip"),
    ANCIENT_BISMUTH("item.ancientcurses.ancient_bismuth.tooltip"),
    ANCIENT_CHRYSOBERYL("item.ancientcurses.ancient_chrysoberyl.tooltip");

    private final String translationKey;

    PreciousGemType(String translationKey) {
        this.translationKey = translationKey;
    }

    public MutableComponent getBonuses() {
        return Component.translatable(translationKey);
    }
}
