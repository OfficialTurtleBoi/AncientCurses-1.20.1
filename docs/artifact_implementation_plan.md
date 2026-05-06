# Ancient Curses Artifact Implementation Plan

Source reference: `C:/Users/cmartinez/Downloads/ancient_curses_artifacts.md`

This file tracks the artifact concepts against the current Forge 1.20.1 codebase.

## Current Status

| Artifact | Status | Current Code/Assets | Notes |
| --- | --- | --- | --- |
| First Beacon | Implemented, needs tuning | `FirstBeaconItem`, beacon networking, first-person renderer, item models/textures | Current beam damages during use ticks once charge passes 35%, not only on release at full charge. It does pierce entities, has range, client beam info, durability use, and release cooldown. Aim slowdown/harder control is not implemented. |
| Golden Feather | Implemented, needs redesign toward spec | `GoldenFeatherItem`, feather particles, item model/property, TurtleCore spell JSON | Current item performs a short charged dash, uses durability as remaining uses, and supports extra enchantment-driven landing spells. Spec wants repeat-use heat, overheat stall, brief mid-flight steering, and no mention of durability/enchantments. |
| Ice Spark | Initial mechanics implemented | `IceSparkItem`; `ThrownIceSpark`; `ModEvents` shatter hook | Right-click throws the spark without consuming it. On first impact it deploys roughly 3 blocks above the hit point for 10 seconds, pulsing TurtleCore's Chilled mob effect in a 5 block radius. Repeated pulses increase Chilled amplifier until the target receives TurtleCore's Frozen mob effect. The next damaging hit shatters Frozen for bonus damage. Right-click while deployed recalls it early. |
| Plague Idol | Missing | None | Needs placeable one-hit block/entity, 60 second lifetime, status spread whitelist, duration scaling, particles, model/texture. |
| Voodoo Doll | Missing | None | Needs target selection, soul visual travel, soul clone/entity, mirrored damage, body debuffs, boss scaling, cooldown, and renderer/model strategy. This is one of the heaviest artifacts. |
| Fathomless Cauldron | Missing | None | Needs item NBT storage, inventory-click handling for potion add/remove, tooltip bundle display, right-click application of stored potion effects, splash/lingering behavior decisions, model/texture. |
| Hollow Lantern | Initial mechanics implemented | `HollowLanternItem`; `cursed_lantern` block/item remains separate | Right-click reveals nearby living entities with Glowing for 5 seconds, limited to 4 solid blocks of penetration, with 30 second vanilla cooldown. Held item pulses when invisible/sneaking entities are nearby. |
| Bone Flute | Initial mechanics implemented | `BoneFluteItem`; `ModEvents` tick/attack hooks | Right-click charms nearby undead for 10 seconds at 50% chance, or 75% for skeletons. Wither skeletons and Wither are immune. Charmed undead retarget nearby hostile monsters and charm breaks when the owner attacks them. Uses 45 second vanilla cooldown. |
| Echo Stone | Initial mechanics implemented | `EchoStoneItem` | Stores a snapshot on item NBT every 10 seconds while carried. Right-click teleports the player back to the saved position/dimension and restores saved health, then applies 25 second vanilla cooldown. Inventory is untouched. |
| Gilded Tome | Initial mechanics implemented | `GildedTomeItem`; `EnrichmentEffect`; `ModEvents` XP hook | Passively banks 30% of positive XP gains on the first carried tome. Sneak right-click applies the Enrichment mob effect for 2 minutes, doubling incoming XP before banking. Right-click releases banked XP without re-banking its own payout. |
| Soul Compass | Initial mechanics implemented | `SoulCompassItem`; `ModEvents` kill hook; `ModItemProperties` angle property | When held in the offhand, kills attune the item to the defeated mob type. The item uses vanilla compass assets for now and its needle points toward the nearest living mob of that type within 128 blocks on the client, spinning when unattuned or no match is nearby. |
| Exodus Totem | Initial mechanics implemented | `ExodusTotemItem`; `ModEvents` lodestone/damage hooks | Sneak right-click a lodestone to bind the totem. Right-click and hold for 4 seconds to teleport the user and nearby TurtleCore party members within 8 blocks to the bound position, including cross-dimensional destinations. Taking damage cancels the channel and successful use applies a 60 second vanilla cooldown. |
| Crystal Ball | Missing | None | Needs custom Crystal Heart resource, damage interception ordering, HUD overlay, empty/breaking heart state, cooldown, model/texture. |
| Bloodprice Sigil | Missing | None | Needs damage interception window, debt tracking, kill debt reduction, delayed nonlethal burst, visual outline, cooldown, model/texture. |
| Thorn Crown | Missing | None | Needs Curios head slot support, passive melee reflection, artifact ability keybind, thorn projectile entity, cooldown, model/texture. |
| Ruination Brand | Missing | None | Needs offhand/Curio support, per-target stack tracking, sigil visuals over entities, incoming damage vulnerability from stacked mob, death/full-stack detonation, ally healing, model/texture. |

## Implementation Surfaces To Reuse

- Item registration: `src/main/java/net/turtleboi/ancientcurses/item/ModItems.java`
- Creative tab: `src/main/java/net/turtleboi/ancientcurses/item/ModCreativeModeTabs.java`
- Datagen names/models: `src/main/java/net/turtleboi/ancientcurses/datagen/ModLanguageProvider.java` and `ModItemModelProvider.java`
- Server event hooks: `src/main/java/net/turtleboi/ancientcurses/event/ModEvents.java`
- Client event/render hooks: `src/main/java/net/turtleboi/ancientcurses/event/ModClientEvents.java`
- Networking: `src/main/java/net/turtleboi/ancientcurses/network/ModNetworking.java`
- Particles: `src/main/java/net/turtleboi/ancientcurses/particle/ModParticleTypes.java`
- Entities: `src/main/java/net/turtleboi/ancientcurses/entity/ModEntities.java`
- Curios integration already exists for amulets and can be extended for head/offhand-style artifacts.

## Suggested Build Order

1. Done: Add lightweight item registrations, generated names, simple models, and creative-tab entries for every missing artifact.
2. Done: Add artifacts to Rite/cursed altar rewards as rare bonus loot.
3. Done: Implement lower-risk self-contained artifacts first: `Hollow Lantern`, `Bone Flute`, `Echo Stone`, `Gilded Tome`, `Soul Compass`.
4. In progress: Implement entity/block artifacts next: `Ice Spark`, `Plague Idol`, `Exodus Totem`.
5. Implement damage-pipeline artifacts carefully: `Crystal Ball`, `Bloodprice Sigil`, `Thorn Crown`, `Ruination Brand`.
6. Leave `Voodoo Doll` for its own pass because clone AI, mirrored damage, boss scaling, and soul rendering can touch many systems.

## Settled Design Decisions

- Artifacts should be obtainable as rare loot from Rites/cursed altars.
- Artifact reward chances currently scale by Rite reward tier: 2% / 5% / 10%, awarding one random artifact on success.
- Artifact cooldowns should use the vanilla item cooldown system.
- Party-aware artifacts should use TurtleCore's party system.
- Golden Feather should keep its existing enchantments while adding heat mechanics later. Enchantments can interact with heat, such as lowering overheat or making Seismic convert heat into a fiery landing shockwave.
- `cursed_lantern` and `Hollow Lantern` are separate items with separate textures.
