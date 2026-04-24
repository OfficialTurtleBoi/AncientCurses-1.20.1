package net.turtleboi.ancientcurses.rite;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.rite.rites.CarnageRite;
import net.turtleboi.ancientcurses.rite.rites.EmbersRite;
import net.turtleboi.ancientcurses.rite.rites.FamineRite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ModRites {
    public static final ResourceLocation CARNAGE = id("carnage");
    public static final ResourceLocation EMBERS = id("embers");
    public static final ResourceLocation FAMINE = id("famine");
    public static final ResourceLocation ODYSSEY = id("odyssey");
    public static final ResourceLocation SACRIFICE = id("sacrifice");
    public static final ResourceLocation ALACRITY = id("alacrity");

    private static final Map<ResourceLocation, List<CurseRiteEntry>> CURSES_BY_RITE = new LinkedHashMap<>();
    private static final Map<ResourceLocation, SavedRiteFactory> SAVED_RITES = new LinkedHashMap<>();
    private static boolean initialized;

    private ModRites() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        registerRiteType(FAMINE, FamineRite::new);
        registerRiteType(EMBERS, EmbersRite::new);
        registerRiteType(CARNAGE, CarnageRite::new);

        registerDefaultCurse(ModEffects.CURSE_OF_AVARICE, FAMINE, 1,
                (player, curse, amplifier, duration, altar) -> new FamineRite(player, curse, amplifier, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_ENDING, EMBERS, 1,
                (player, curse, amplifier, duration, altar) -> new EmbersRite(player, curse, amplifier, duration, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_ENVY, CARNAGE, 1,
                (player, curse, amplifier, duration, altar) -> new CarnageRite(player, curse, amplifier, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_FRAILTY, CARNAGE, 1,
                (player, curse, amplifier, duration, altar) -> new CarnageRite(player, curse, amplifier, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_GLUTTONY, EMBERS, 1,
                (player, curse, amplifier, duration, altar) -> new EmbersRite(player, curse, amplifier, duration, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_NATURE, FAMINE, 1,
                (player, curse, amplifier, duration, altar) -> new FamineRite(player, curse, amplifier, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_OBESSSION, EMBERS, 1,
                (player, curse, amplifier, duration, altar) -> new EmbersRite(player, curse, amplifier, duration, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_PESTILENCE, EMBERS, 1,
                (player, curse, amplifier, duration, altar) -> new EmbersRite(player, curse, amplifier, duration, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_PRIDE, CARNAGE, 1,
                (player, curse, amplifier, duration, altar) -> new CarnageRite(player, curse, amplifier, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_SHADOWS, FAMINE, 1,
                (player, curse, amplifier, duration, altar) -> new FamineRite(player, curse, amplifier, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_SLOTH, FAMINE, 1,
                (player, curse, amplifier, duration, altar) -> new FamineRite(player, curse, amplifier, altar));
        registerDefaultCurse(ModEffects.CURSE_OF_WRATH, CARNAGE, 1,
                (player, curse, amplifier, duration, altar) -> new CarnageRite(player, curse, amplifier, altar));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(AncientCurses.MOD_ID, path);
    }

    public static ResourceLocation parse(String value) {
        if (value == null || value.isBlank() || !value.contains(":")) {
            return null;
        }

        return ResourceLocation.tryParse(value);
    }

    public static CurseRiteEntry registerRiteCurse(ResourceLocation riteId,
                                                   RegistryObject<MobEffect> curse,
                                                   int weight,
                                                   RiteFactory riteFactory) {
        return registerRiteCurse(riteId, curse.get(), weight, riteFactory);
    }

    public static CurseRiteEntry registerRiteCurse(ResourceLocation riteId,
                                                   MobEffect curse,
                                                   int weight,
                                                   RiteFactory riteFactory) {
        Objects.requireNonNull(riteId, "riteId");
        Objects.requireNonNull(curse, "curse");
        Objects.requireNonNull(riteFactory, "riteFactory");
        if (weight <= 0) {
            throw new IllegalArgumentException("Curse weight must be greater than 0.");
        }

        ModEffects.registerCurseEffect(curse);
        CurseRiteEntry entry = new CurseRiteEntry(riteId, curse, weight, riteFactory);
        CURSES_BY_RITE.computeIfAbsent(riteId, key -> new ArrayList<>()).add(entry);
        return entry;
    }

    public static void registerRiteType(ResourceLocation riteId, SavedRiteFactory savedRiteFactory) {
        Objects.requireNonNull(riteId, "riteId");
        Objects.requireNonNull(savedRiteFactory, "savedRiteFactory");
        SAVED_RITES.putIfAbsent(riteId, savedRiteFactory);
    }

    public static List<CurseRiteEntry> getCursesForRite(ResourceLocation riteId) {
        List<CurseRiteEntry> entries = CURSES_BY_RITE.get(riteId);
        if (entries == null) {
            return List.of();
        }
        return Collections.unmodifiableList(entries);
    }

    public static CurseRiteEntry getEntryForCurse(MobEffect curse) {
        for (List<CurseRiteEntry> entries : CURSES_BY_RITE.values()) {
            for (CurseRiteEntry entry : entries) {
                if (entry.curse() == curse) {
                    return entry;
                }
            }
        }

        return null;
    }

    public static Collection<ResourceLocation> getRegisteredRites() {
        return Collections.unmodifiableSet(CURSES_BY_RITE.keySet());
    }

    public static CurseRiteEntry getRandomCurse(RandomSource random) {
        List<CurseRiteEntry> entries = new ArrayList<>();
        for (List<CurseRiteEntry> riteEntries : CURSES_BY_RITE.values()) {
            entries.addAll(riteEntries);
        }

        if (entries.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (CurseRiteEntry entry : entries) {
            totalWeight += entry.weight();
        }

        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        for (CurseRiteEntry entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry;
            }
        }

        return entries.get(entries.size() - 1);
    }

    public static CurseRiteEntry getRandomCurseForRite(ResourceLocation riteId, RandomSource random) {
        List<CurseRiteEntry> entries = CURSES_BY_RITE.get(riteId);
        if (entries == null || entries.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (CurseRiteEntry entry : entries) {
            totalWeight += entry.weight();
        }

        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        for (CurseRiteEntry entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry;
            }
        }

        return entries.get(entries.size() - 1);
    }

    public static Rite createRite(ResourceLocation riteId,
                                  Player player,
                                  MobEffect curse,
                                  int curseAmplifier,
                                  int curseDuration,
                                  CursedAltarBlockEntity altar) {
        List<CurseRiteEntry> entries = CURSES_BY_RITE.get(riteId);
        if (entries == null) {
            return null;
        }

        for (CurseRiteEntry entry : entries) {
            if (entry.curse() == curse) {
                return entry.riteFactory().create(player, curse, curseAmplifier, curseDuration, altar);
            }
        }

        return null;
    }

    public static Rite loadRite(ResourceLocation riteId, CursedAltarBlockEntity altar, CompoundTag tag) {
        SavedRiteFactory savedRiteFactory = SAVED_RITES.get(riteId);
        if (savedRiteFactory == null) {
            return null;
        }

        Rite rite = savedRiteFactory.create(altar);
        rite.loadFromNBT(tag);
        return rite;
    }

    private static void registerDefaultCurse(RegistryObject<MobEffect> curse,
                                             ResourceLocation riteId,
                                             int weight,
                                             RiteFactory riteFactory) {
        registerRiteCurse(riteId, curse, weight, riteFactory);
    }

    public record CurseRiteEntry(ResourceLocation riteId, MobEffect curse, int weight, RiteFactory riteFactory) {
    }

    @FunctionalInterface
    public interface RiteFactory {
        Rite create(Player player, MobEffect curse, int curseAmplifier, int curseDuration, CursedAltarBlockEntity altar);
    }

    @FunctionalInterface
    public interface SavedRiteFactory {
        Rite create(CursedAltarBlockEntity altar);
    }
}
