package net.turtleboi.ancientcurses.item.items;

public enum PreciousGemType {
    BROKEN_AMETHYST(new String[]{"+2 Health"}),
    POLISHED_AMETHYST(new String[]{"+4 Health"}),
    PERFECT_AMETHYST(new String[]{
            "Lifebloom: When slotted in Main Gem socket, when you are below half health, you gradually regenerate health over time until you are full health",
            "",
            "+8 Health"}),
    BROKEN_DIAMOND(new String[]{"+1 Armor"}),
    POLISHED_DIAMOND(new String[]{"+2 Armor", "+0.5 Armor Toughness"}),
    PERFECT_DIAMOND(new String[]{
            "Crystalline Hardening: When slotted in Main Gem socket, when hit or blocking an attack with a shield, you have a 20% chance to completely negate damage",
            "",
            "+4 Armor",
            "+1 Armor Toughness"}),
    BROKEN_EMERALD(new String[]{"+0.25 Luck"}),
    POLISHED_EMERALD(new String[]{"+0.5 Luck"}),
    PERFECT_EMERALD(new String[]{
            "Fortune's Favor: When slotted in Main Gem socket, all loot drops from killing enemies or mining ore are doubled with a 20% to triple the drops",
            "",
            "+1 Luck"}),
    BROKEN_RUBY(new String[]{"+0.5 Attack Damage"}),
    POLISHED_RUBY(new String[]{"+1 Attack Damage"}),
    PERFECT_RUBY(new String[]{
            "Critical Fury: When slotted in Main Gem socket, when you deal critical damage, your total Attack Damage increases by 25% for 5 seconds. This effect refreshes with each critical hit but does not stack",
            "",
            "+2 Attack Damage"}),
    BROKEN_SAPPHIRE(new String[]{"+10% Magic Amplification"}),
    POLISHED_SAPPHIRE(new String[]{"+20% Magic Amplification"}),
    PERFECT_SAPPHIRE(new String[]{
            "Arcane Amplification: When slotted in Main Gem socket, when apply enchantments on your equipment there is a chance that they are randomly increased by 1 or 2 levels, even beyond the natural maximum",
            "",
            "+40% Magic Amplification"}),
    BROKEN_TOPAZ(new String[]{"+7.5% Attack Speed", "+7.5% Movement Speed"}),
    POLISHED_TOPAZ(new String[]{"+12.5% Attack Speed", "+12.5% Movement Speed"}),
    PERFECT_TOPAZ(new String[]{
            "Arcane Amplification: When slotted in Main Gem socket, each successful melee attack increases your Attack Speed and Movement Speed by 10% for 5 seconds, stacking up to 3 times. The buff is refreshed on each hit",
            "",
            "+33% Attack Speed",
            "+33% Movement Speed"}),
    ANCIENT_ALEXANDRITE(new String[]{
            "Dawn's Grace: When slotted in Main Gem socket, during the day, gain Regeneration, 25% increased total Armor and 25% increased total Armor Toughness. Additionally, you benefit from Solar Nourishment, which restores hunger passively and is amplified while standing in direct sunlight",
            "",
            "Twilight's Edge: When slotted in Main Gem socket, during the night, gain Night Vision, 25% increased total Attack Speed and 25% increased total Attack Damage. Additionally, you gain a 33% chance to all incoming dodge attacks"}),
    ANCIENT_BISMUTH(new String[]{
            "Elemental Convergence: When slotted in Main Gem socket, every 10 seconds you unleash a magical pulse, granting either Regeneration II, Strength II, or Speed II for 5 seconds, along with a random elemental aura",
            "",
            "Fire Aura- Burn nearby enemies",
            "Lightning Aura- Strike foes with lightning",
            "Ice Aura- Slow and weaken enemies",
            "",
            "After each pulse, an elemental burst deals 5 magic damage to nearby enemies within 5 blocks, pushing them back and briefly levitating them"}),
    ANCIENT_CHRYSOBERYL(new String[]{
            "Electric Skies: When slotted in Main Gem socket, gain the ability to fly",
            "",
            "+50% Attack Speed",
            "+50% Movement Speed",
            "+3 Luck"});

    private final String[] bonuses;

    PreciousGemType(String[] bonuses) {
        this.bonuses = bonuses;
    }

    public String[] getBonuses() {
        return bonuses;
    }
}
