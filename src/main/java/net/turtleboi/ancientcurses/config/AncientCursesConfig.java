package net.turtleboi.ancientcurses.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class AncientCursesConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Double> CURSED_ALTAR_SPAWN_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CURSED_ALTAR_BIOME_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<Double> CURSED_ALTAR_SURFACE_BIAS;
    public static final ForgeConfigSpec.ConfigValue<Integer> CURSED_ALTAR_WATER_CHECK_RADIUS;

    public static final ForgeConfigSpec.ConfigValue<Double> CURSED_RITE_TIER1_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> CURSED_RITE_TIER2_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> CURSED_RITE_TIER3_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Integer> CURSED_RITE_MAX;
    public static final ForgeConfigSpec.ConfigValue<Integer> CURSED_RITE_TIER2_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Integer> CURSED_RITE_TIER3_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Double> CARNAGE_RITE_SPAWN_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Integer> CURSE_TIME_MIN;
    public static final ForgeConfigSpec.ConfigValue<Integer> CURSE_TIME_MAX;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FAMINE_RITE_ITEMS;


    static {
        BUILDER.push("Structures");

        CURSED_ALTAR_SPAWN_CHANCE = BUILDER
                .comment("Secondary spawn chance for the Cursed Altar after structure placement picks a candidate chunk.")
                .comment("Keep this at 1.0 for fully data-driven, predictable spacing; lower it to make altars rarer.")
                .defineInRange("cursedAltarSpawnChance", 1.0, 0.0, 1.0);

        CURSED_ALTAR_BIOME_BLACKLIST = BUILDER
                .comment("Biomes where the Cursed Altar should NOT spawn.")
                .comment("Use biome IDs (e.g. \"minecraft:ocean\") or tags with a leading # (e.g. \"#minecraft:is_river\")")
                .comment("Examples: [\"minecraft:ocean\", \"minecraft:river\", \"#minecraft:is_beach\"]")
                .defineListAllowEmpty(
                        "cursedAltarBiomeBlacklist",
                        List.of(
                                "#minecraft:is_ocean",
                                "#minecraft:is_river",
                                "#minecraft:is_beach"
                        ),
                        object -> object instanceof String
                );

        CURSED_ALTAR_SURFACE_BIAS = BUILDER
                .comment("Chance for a valid cursed altar candidate to use a surface position instead of an underground one.")
                .comment("Higher values favor surface altars while still allowing underground spawns.")
                .defineInRange("cursedAltarSurfaceBias", 0.92, 0.0, 1.0);

        CURSED_ALTAR_WATER_CHECK_RADIUS = BUILDER
                .comment("Horizontal radius used to reject altar placements near fluids such as water or lava.")
                .comment("Set to 0 to disable the fluid proximity check.")
                .defineInRange("cursedAltarWaterCheckRadius", 3, 0, 32);

        BUILDER.pop();

        BUILDER.push("Curse Modifiers");

        CURSE_TIME_MIN = BUILDER
                .comment("Minimum number of seconds that a Survival Rite can curse the player for")
                .comment("If value is set to 0, all Survival Rites will result in an instant completion")
                .defineInRange("curseTimeMinimum", 180, 0, 12000);

        CURSE_TIME_MAX = BUILDER
                .comment("Maximum number of seconds that a Survival Rite can curse the player for")
                .comment("If value is set to 0, all Survival Rites will result in an instant completion")
                .defineInRange("curseTimeMaximum", 240, 0, 18000);

        BUILDER.pop();

        BUILDER.push("Rite Modifiers");

        CURSED_RITE_TIER1_CHANCE = BUILDER
                .comment("Base chance for player to be cursed with a Tier 1 rite")
                .comment("All rite chance values should ideally add up to 10 to avoid bugs")
                .defineInRange("cursedRiteTier1Chance", 6.0, 0.0, 10.0);

        CURSED_RITE_TIER2_CHANCE = BUILDER
                .comment("Base chance for player to be cursed with a Tier 2 rite")
                .comment("All rite chance values should ideally add up to 10 to avoid bugs")
                .defineInRange("cursedRiteTier2Chance", 3.0, 0.0, 10.0);

        CURSED_RITE_TIER3_CHANCE = BUILDER
                .comment("Base chance for player to be cursed with a Tier 3 rite")
                .comment("All rite chance values should ideally add up to 10 to avoid bugs")
                .defineInRange("cursedRiteTier3Chance", 1.0, 0.0, 10.0);

        CURSED_RITE_TIER2_THRESHOLD = BUILDER
                .comment("Amount of rites for player to complete before Tier 2 rites become available")
                .defineInRange("cursedRiteTier2Threshold", 5, 1, 256);

        CURSED_RITE_TIER3_THRESHOLD = BUILDER
                .comment("Amount of rites for player to complete before Tier 3 rites become available")
                .defineInRange("cursedRiteTier3Threshold", 15, 1, 256);

        CURSED_RITE_MAX = BUILDER
                .comment("Max amount of rites for player to complete before Tier 3 rites are guaranteed")
                .defineInRange("cursedRiteMaxChance", 25, 1, 256);

        BUILDER.pop();

        BUILDER.push("Curse Modifiers");

        FAMINE_RITE_ITEMS = BUILDER
                .comment("List of valid items (by ID) that can be required in the Famine Rite.")
                .comment("Example: [\"minecraft:iron_ingot\", \"minecraft:string\"]")
                .defineListAllowEmpty("famineRiteItems",
                        java.util.List.of(
                                "minecraft:iron_ingot",
                                "minecraft:gold_ingot",
                                "minecraft:copper_ingot",
                                "minecraft:raw_iron",
                                "minecraft:raw_gold",
                                "minecraft:raw_copper",
                                "minecraft:lapis_lazuli",
                                "minecraft:redstone",
                                "minecraft:rotten_flesh",
                                "minecraft:bone",
                                "minecraft:ender_pearl",
                                "minecraft:gunpowder",
                                "minecraft:spider_eye",
                                "minecraft:glowstone_dust",
                                "minecraft:sugar",
                                "minecraft:string"
                        ),
                        object -> object instanceof String
                );

        CARNAGE_RITE_SPAWN_MULTIPLIER = BUILDER
                .comment("Multiplier applied to Carnage Rite wave sizes.")
                .comment("Higher values spawn more enemies per wave while keeping the kill target in sync.")
                .defineInRange("carnageRiteSpawnMultiplier", 1.5, 0.1, 10.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
