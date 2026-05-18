package net.turtleboi.ancientcurses.datagen;

import net.minecraft.data.PackOutput;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.enchantment.ModEnchantments;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.turtlecore.datagen.CoreLanguageProvider;

public class ModLanguageProvider extends CoreLanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, AncientCurses.MOD_ID);
    }

    @Override
    protected void addTranslations() {
        add("creativetab.ancientcurses_tab", "Ancient Curses");
        add("creativetab.ancientcurses_gems_tab", "Ancient Curses - Gems");

        add("sounds.ancientcurses.deprecophobia", "Deprecophobia music plays");

        addSimpleNameEffect(ModEffects.CURSE_OF_ENDING);
        addSimpleNameEffect(ModEffects.CURSE_OF_ENVY);
        addSimpleNameEffect(ModEffects.CURSE_OF_FRAILTY);
        addSimpleNameEffect(ModEffects.CURSE_OF_GLUTTONY);
        addSimpleNameEffect(ModEffects.CURSE_OF_AVARICE);
        addSimpleNameEffect(ModEffects.CURSE_OF_OBESSSION);
        addSimpleNameEffect(ModEffects.CURSE_OF_NATURE);
        addSimpleNameEffect(ModEffects.CURSE_OF_PESTILENCE);
        addSimpleNameEffect(ModEffects.CURSE_OF_PRIDE);
        addSimpleNameEffect(ModEffects.CURSE_OF_SHADOWS);
        addSimpleNameEffect(ModEffects.CURSE_OF_SLOTH);
        addSimpleNameEffect(ModEffects.CURSE_OF_WRATH);
        addSimpleNameEffect(ModEffects.CRITICAL_FURY);
        addSimpleNameEffect(ModEffects.CRYSTALLINE_HARDENING);
        addSimpleNameEffect(ModEffects.ELEMENTAL_CONVERGENCE);
        addSimpleNameEffect(ModEffects.FORTUNES_FAVOR);
        addSimpleNameEffect(ModEffects.FRENZIED_BLOWS);
        addSimpleNameEffect(ModEffects.LIFEBLOOM);
        addSimpleNameEffect(ModEffects.ENRICHMENT);
        add(ModEffects.BLOOD_DEBT.get(), "Blood Debt");
        addSimpleNameEffect(ModEffects.RUINATION_MARK);

        addSimpleNameEnchant(ModEnchantments.SOARING);
        addSimpleNameEnchant(ModEnchantments.TAILWIND);
        addSimpleNameEnchant(ModEnchantments.ZEPHYR_RUSH);
        addSimpleNameEnchant(ModEnchantments.SEISMIC);

        addSimpleNameBlock(ModBlocks.CURSED_ALTAR);
        addSimpleNameBlock(ModBlocks.LAPIDARIST_TABLE);
        addSimpleNameBlock(ModBlocks.SOUL_ROCK);
        addSimpleNameBlock(ModBlocks.SCONCED_TORCH);
        addSimpleNameBlock(ModBlocks.SCONCED_SOUL_TORCH);
        addSimpleNameBlock(ModBlocks.SCONCED_REDSTONE_TORCH);
        addSimpleNameBlock(ModBlocks.SCONCED_CURSED_TORCH);
        addSimpleNameBlock(ModBlocks.SCONCED_UNLIT_TORCH);
        addSimpleNameBlock(ModBlocks.SCONCED_UNLIT_SOUL_TORCH);

        addSimpleItemName(ModItems.GOLDEN_AMULET);
        add(ModItems.DEPRECOPHOBIA_MUSIC_DISC.get(), "Music Disc");
        add("item.ancientcurses.deprecophobia_music_disc.desc", "KindaGlumm - Deprecophobia");

        addSimpleItemName(ModItems.BROKEN_AMETHYST);
        addSimpleItemName(ModItems.BROKEN_DIAMOND);
        addSimpleItemName(ModItems.BROKEN_EMERALD);
        addSimpleItemName(ModItems.BROKEN_RUBY);
        addSimpleItemName(ModItems.BROKEN_SAPPHIRE);
        addSimpleItemName(ModItems.BROKEN_TOPAZ);

        addSimpleItemName(ModItems.POLISHED_AMETHYST);
        addSimpleItemName(ModItems.POLISHED_DIAMOND);
        addSimpleItemName(ModItems.POLISHED_EMERALD);
        addSimpleItemName(ModItems.POLISHED_RUBY);
        addSimpleItemName(ModItems.POLISHED_SAPPHIRE);
        addSimpleItemName(ModItems.POLISHED_TOPAZ);

        addSimpleItemName(ModItems.PERFECT_AMETHYST);
        addSimpleItemName(ModItems.PERFECT_DIAMOND);
        addSimpleItemName(ModItems.PERFECT_EMERALD);
        addSimpleItemName(ModItems.PERFECT_RUBY);
        addSimpleItemName(ModItems.PERFECT_SAPPHIRE);
        addSimpleItemName(ModItems.PERFECT_TOPAZ);

        addSimpleItemName(ModItems.ANCIENT_ALEXANDRITE);
        addSimpleItemName(ModItems.ANCIENT_BISMUTH);
        addSimpleItemName(ModItems.ANCIENT_CHRYSOBERYL);
        addSimpleItemName(ModItems.CURSED_PEARL);
        addSimpleItemName(ModItems.ROT_CLUMP);
        addSimpleItemName(ModItems.SMOKY_QUARTZ);
        addSimpleItemName(ModItems.SOUL_SHARD);
        addSimpleItemName(ModItems.CURSED_SOUL_SHARD);
        addSimpleItemName(ModItems.GOLDEN_FEATHER);
        addSimpleItemName(ModItems.DOWSING_ROD);
        addSimpleItemName(ModItems.FIRST_BEACON);
        addSimpleItemName(ModItems.ICE_SPARK);
        addSimpleItemName(ModItems.PLAGUE_IDOL);
        addSimpleItemName(ModItems.VOODOO_DOLL);
        addSimpleItemName(ModItems.FATHOMLESS_CAULDRON);
        addSimpleItemName(ModItems.HOLLOW_LANTERN);
        addSimpleItemName(ModItems.BONE_FLUTE);
        addSimpleItemName(ModItems.ECHO_STONE);
        addSimpleItemName(ModItems.GILDED_TOME);
        addSimpleItemName(ModItems.SOUL_COMPASS);
        addSimpleItemName(ModItems.EXODUS_TOTEM);
        addSimpleItemName(ModItems.CRYSTAL_BALL);
        addSimpleItemName(ModItems.BLOODPRICE_SIGIL);
        addSimpleItemName(ModItems.THORN_CROWN);
        addSimpleItemName(ModItems.RUINATION_BRAND);
        add("item.ancientcurses.fathomless_cauldron.tooltip", "Holds up to 4 potions (16 uses each). Add gunpowder to throw splash, or dragon's breath for lingering.");
        add("item.ancientcurses.fathomless_cauldron.potion_entry", "%s (%s uses)");
        add("item.ancientcurses.fathomless_cauldron.gunpowder", "Gunpowder: %s");
        add("item.ancientcurses.fathomless_cauldron.dragons_breath", "Dragon's Breath: %s");
        add("item.ancientcurses.fathomless_cauldron.empty", "The cauldron is empty.");
        add("item.ancientcurses.fathomless_cauldron.controls", "[Ctrl] Click to insert/extract catalysts");
        add("key.categories.ancientcurses", "Ancient Curses");
        add("key.ancientcurses.artifact_ability", "Artifact Ability");
        add("item.ancientcurses.ice_spark.tooltip", "Right-click to throw. Right-click again to recall the deployed spark.");
        add("item.ancientcurses.ice_spark.deployed", "Deployed");
        add("item.ancientcurses.plague_idol.tooltip", "Place to spread harmful effects between nearby mobs.");
        add("item.ancientcurses.voodoo_doll.tooltip", "Right-click a mob to pull its soul loose.");
        add("item.ancientcurses.hollow_lantern.tooltip", "Pulses when hidden things are close. Right-click to reveal nearby entities.");
        add("item.ancientcurses.hollow_lantern.no_entities", "The dark gives nothing back.");
        add("item.ancientcurses.echo_stone.tooltip", "Right-click to rewind to your last saved echo.");
        add("item.ancientcurses.echo_stone.no_snapshot", "No echo answers.");
        add("item.ancientcurses.bone_flute.tooltip", "Right-click to briefly charm nearby undead.");
        add("item.ancientcurses.bone_flute.no_charm", "The dead refuse the song.");
        add("item.ancientcurses.gilded_tome.tooltip", "Right-click to release banked XP. Sneak right-click to trigger Enrichment.");
        add("item.ancientcurses.gilded_tome.banked", "Banked XP: %s");
        add("item.ancientcurses.gilded_tome.enriched", "The gilded pages brighten.");
        add("item.ancientcurses.gilded_tome.empty", "The tome is empty.");
        add("item.ancientcurses.bloodprice_sigil.tooltip", "Right-click in hand, or press Artifact Ability while worn as a Curio, to defer incoming damage for a short time.");
        add("item.ancientcurses.bloodprice_sigil.debt", "Blood Debt: %s");
        add("item.ancientcurses.bloodprice_sigil.active", "The sigil is hungry.");
        add("item.ancientcurses.bloodprice_sigil.pending", "The price comes due.");
        add("item.ancientcurses.ruination_brand.tooltip", "Brand foes while equipped in your offhand or a Curios charm slot. Stacked brands amplify your damage and detonate into healing.");
        add("item.ancientcurses.soul_compass.tracking", "Tracking: %s");
        add("item.ancientcurses.soul_compass.unattuned", "Unattuned");
        add("item.ancientcurses.exodus_totem.unbound", "Unbound");
        add("item.ancientcurses.exodus_totem.bound", "Bound to %s, %s, %s");
        add("item.ancientcurses.exodus_totem.bound_message", "The totem remembers this lodestone.");

        add(ModEntities.CAULDRON_POTION.get(), "Cauldron Potion");
        add(ModEntities.CURSED_PEARL.get(), "Cursed Pearl");
        add(ModEntities.ICE_SPARK.get(), "Ice Spark");
        add(ModEntities.PLAGUE_IDOL.get(), "Plague Idol");
        add(ModEntities.VOODOO_SOUL.get(), "Voodoo Soul");
        add(ModEntities.CURSED_NODE.get(), "Cursed Node");
        add(ModEntities.CURSED_PORTAL.get(), "Cursed Portal");

        add("item.ancientcurses.gem.socket", "When socketed:");
        add("item.ancientcurses.amulet.minor_gems", "Minor Gems:");
        add("item.ancientcurses.amulet.minor_gem_entry", "  - %s");
        add("item.ancientcurses.amulet.main_gem", "Main Gem: %s");
        add("item.ancientcurses.amulet.main_gem_none", "Main Gem: None");
        add("item.ancientcurses.amulet.minor_gems_none", "Minor Gems: None");
        add("item.ancientcurses.soul_shard.energy", "Soul Energy: %s/%s");
        add("item.ancientcurses.soul_shard.charged", "Charged with harvested souls");
        add("item.ancientcurses.soul_shard.uncharged", "Charge in your off hand by slaying mobs");

        add("item.ancientcurses.broken_amethyst.tooltip", "+2 Health");
        add("item.ancientcurses.polished_amethyst.tooltip", "+4 Health");
        add("item.ancientcurses.perfect_amethyst.tooltip",
                "Lifebloom: When slotted in Main Gem socket, when you are below half health, you gradually regenerate health over time until you are full health|+8 Health");

        add("item.ancientcurses.broken_diamond.tooltip", "+1 Armor");
        add("item.ancientcurses.polished_diamond.tooltip", "+2 Armor|+0.5 Armor Toughness");
        add("item.ancientcurses.perfect_diamond.tooltip",
                "Crystalline Hardening: When slotted in Main Gem socket, when hit or blocking an attack with a shield, you have a 20% chance to completely negate damage|+4 Armor|+1 Armor Toughness");

        add("item.ancientcurses.broken_emerald.tooltip", "+0.25 Luck");
        add("item.ancientcurses.polished_emerald.tooltip", "+0.5 Luck");
        add("item.ancientcurses.perfect_emerald.tooltip",
                "Fortune's Favor: When slotted in Main Gem socket, all loot drops from killing enemies or mining ore are doubled with a 20% to triple the drops|+1 Luck");

        add("item.ancientcurses.broken_ruby.tooltip", "+0.5 Attack Damage");
        add("item.ancientcurses.polished_ruby.tooltip", "+1 Attack Damage");
        add("item.ancientcurses.perfect_ruby.tooltip",
                "Critical Fury: When slotted in Main Gem socket, when you deal critical damage, your total Attack Damage increases by 25% for 5 seconds. This effect refreshes with each critical hit but does not stack|+2 Attack Damage");

        add("item.ancientcurses.broken_sapphire.tooltip", "+10% Magic Amplification");
        add("item.ancientcurses.polished_sapphire.tooltip", "+20% Magic Amplification");
        add("item.ancientcurses.perfect_sapphire.tooltip",
                "Arcane Amplification: When slotted in Main Gem socket, when applying enchantments on your equipment there is a chance that they are randomly increased by 1 or 2 levels, even beyond the natural maximum|+40% Magic Amplification");

        add("item.ancientcurses.broken_topaz.tooltip", "+7.5% Attack Speed|+7.5% Movement Speed");
        add("item.ancientcurses.polished_topaz.tooltip", "+12.5% Attack Speed|+12.5% Movement Speed");
        add("item.ancientcurses.perfect_topaz.tooltip",
                "Frenzied Blows: When slotted in Main Gem socket, each successful melee attack increases your Attack Speed and Movement Speed by 10% for 5 seconds, stacking up to 3 times. The buff is refreshed on each hit|+33% Attack Speed|+33% Movement Speed");

        add("item.ancientcurses.ancient_alexandrite.tooltip",
                "Dawn's Grace: When slotted in Main Gem socket, during the day, gain Regeneration, 25% increased total Armor and 25% increased total Armor Toughness. Additionally, you benefit from Solar Nourishment, which restores hunger passively and is amplified while standing in direct sunlight|Twilight's Edge: When slotted in Main Gem socket, during the night, gain Night Vision, 25% increased total Attack Speed and 25% increased total Attack Damage. Additionally, you gain a 33% chance to dodge all incoming attacks");

        add("item.ancientcurses.ancient_bismuth.tooltip",
                "Elemental Convergence: Every 10 seconds you unleash a magical pulse, granting either Regeneration II, Strength II, or Speed II for 5 seconds, along with a random elemental aura|Fire Aura- Burn nearby enemies|Lightning Aura- Strike foes with lightning|Ice Aura- Slow and weaken enemies|After each pulse, an elemental burst deals 5 magic damage to nearby enemies within 5 blocks, pushing them back and briefly levitating them");

        add("item.ancientcurses.ancient_chrysoberyl.tooltip",
                "Electric Skies: When slotted in Main Gem socket, gain the ability to fly|+50% Attack Speed|+50% Movement Speed|+3 Luck");

        add("trial.ancientcurses.survival", "Feed the souls: %s%%");
        add("trial.ancientcurses.elimination", "Eliminate %s | Wave: %d | Remaining: %d");
        add("trial.ancientcurses.fetch", "Feed the altar: %s/%s %s");
        add("trial.ancientcurses.complete", "The altar beckons your return...");

        add("trial.ancientcurses.unlucky", "How unlucky...");
        add("trial.ancientcurses.pride.better", "You are better than them!");
        add("trial.ancientcurses.death", "The Altars Feed on your soul...");
        add("trial.ancientcurses.sleep", "It's too dark to sleep...");
        add("trial.ancientcurses.pride.help", "Help is for the weak!");
        add("trial.ancientcurses.pride.run", "Running is for the pathetic!");
    }
}
