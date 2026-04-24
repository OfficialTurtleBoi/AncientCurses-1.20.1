package net.turtleboi.ancientcurses.item.items;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PreciousGemType {
    BROKEN_AMETHYST("broken_amethyst", 0.0F),
    POLISHED_AMETHYST("polished_amethyst", 1.0F),
    PERFECT_AMETHYST("perfect_amethyst", 1.0F),
    BROKEN_DIAMOND("broken_diamond", 0.0F),
    POLISHED_DIAMOND("polished_diamond", 2.0F),
    PERFECT_DIAMOND("perfect_diamond", 2.0F),
    BROKEN_EMERALD("broken_emerald", 0.0F),
    POLISHED_EMERALD("polished_emerald", 3.0F),
    PERFECT_EMERALD("perfect_emerald", 3.0F),
    BROKEN_RUBY("broken_ruby", 0.0F),
    POLISHED_RUBY("polished_ruby", 4.0F),
    PERFECT_RUBY("perfect_ruby", 4.0F),
    BROKEN_SAPPHIRE("broken_sapphire", 0.0F),
    POLISHED_SAPPHIRE("polished_sapphire", 5.0F),
    PERFECT_SAPPHIRE("perfect_sapphire", 5.0F),
    BROKEN_TOPAZ("broken_topaz", 0.0F),
    POLISHED_TOPAZ("polished_topaz", 6.0F),
    PERFECT_TOPAZ("perfect_topaz", 6.0F),
    ANCIENT_ALEXANDRITE("ancient_alexandrite", 7.0F),
    ANCIENT_BISMUTH("ancient_bismuth", 8.0F),
    ANCIENT_CHRYSOBERYL("ancient_chrysoberyl", 9.0F);

    private final String itemName;
    private final String translationKey;
    private final float amuletModelValue;

    PreciousGemType(String itemName, float amuletModelValue) {
        this.itemName = itemName;
        this.translationKey = "item.ancientcurses." + itemName + ".tooltip";
        this.amuletModelValue = amuletModelValue;
    }

    public String getItemName() {
        return itemName;
    }

    public float getAmuletModelValue() {
        return amuletModelValue;
    }

    public List<MutableComponent> getBonuses() {
        String translated = Component.translatable(translationKey).getString();
        String[] lines = translated.split("\\|");
        return Arrays.stream(lines)
                .map(Component::literal)
                .collect(Collectors.toList());
    }
}
