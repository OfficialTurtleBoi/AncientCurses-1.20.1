package net.turtleboi.ancientcurses.block.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.config.AncientCursesConfig;
import net.turtleboi.ancientcurses.rite.ModRites;
import net.turtleboi.ancientcurses.rite.Rite;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RiteUtil {
    private RiteUtil() {
    }

    public static void startRite(Player player, CursedAltarBlockEntity altarEntity) {
        startRite(player, altarEntity, null, null, null);
    }

    public static void startRite(Player player, CursedAltarBlockEntity altarEntity, ResourceLocation forcedRiteId) {
        startRite(player, altarEntity, forcedRiteId, null, null);
    }

    public static void startRite(Player player, CursedAltarBlockEntity altarEntity, ResourceLocation forcedRiteId, Integer forcedAmplifier) {
        startRite(player, altarEntity, forcedRiteId, null, forcedAmplifier);
    }

    public static void startRite(Player player, CursedAltarBlockEntity altarEntity, MobEffect forcedCurse, Integer forcedAmplifier) {
        startRite(player, altarEntity, null, forcedCurse, forcedAmplifier);
    }

    private static void startRite(Player player, CursedAltarBlockEntity altarEntity, ResourceLocation forcedRiteId, MobEffect forcedCurse, Integer forcedAmplifier) {
        if (forcedRiteId == null && forcedCurse == null && altarEntity.hasPlayerCompletedRite(player)) {
            return;
        }

        ModRites.CurseRiteEntry curseEntry;
        if (forcedCurse != null) {
            curseEntry = ModRites.getEntryForCurse(forcedCurse);
        } else if (forcedRiteId != null) {
            curseEntry = ModRites.getRandomCurseForRite(forcedRiteId, player.level().getRandom());
        } else {
            curseEntry = ModRites.getRandomCurse(player.level().getRandom());
        }

        if (curseEntry == null) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        Level level = player.level();
        int randomAmplifier = forcedAmplifier != null ? forcedAmplifier : getRandomAmplifier(player);

        level.playSound(null, playerPos, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.AMBIENT, 1.0F, 0.25F);
        level.playSound(null, playerPos, SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.get(), SoundSource.AMBIENT, 1.0F, 0.5F);

        CoreNetworking.sendToNear(new CameraShakeS2C(0.125F, 1000), player);
        altarEntity.cursePlayer(player, curseEntry, randomAmplifier);
    }

    public static boolean tryConcludeActiveRite(Player player, Rite rite) {
        if (rite.canConcludeAtAltar()) {
            rite.concludeRite(player);
            return true;
        }

        return false;
    }

    public static int getRandomAmplifier(Player player) {
        int ritesCompleted = player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                .map(PlayerRiteDataCapability::getPlayerRitesCompleted)
                .orElse(0);

        List<Integer> weightedAmplifiers = getWeightedAmplifier(ritesCompleted);
        if (weightedAmplifiers.isEmpty()) {
            System.out.println(Component.literal("No amplifiers available at this time."));
            return 0;
        }

        return weightedAmplifiers.get(ThreadLocalRandom.current().nextInt(weightedAmplifiers.size()));
    }

    private static List<Integer> getWeightedAmplifier(int ritesCompleted) {
        int thresholdTier2 = AncientCursesConfig.CURSED_RITE_TIER2_THRESHOLD.get();
        int thresholdTier3 = AncientCursesConfig.CURSED_RITE_TIER3_THRESHOLD.get();
        int maxRites = AncientCursesConfig.CURSED_RITE_MAX.get();

        double weight0;
        double weight1;
        double weight2;

        if (ritesCompleted < thresholdTier2) {
            weight0 = AncientCursesConfig.CURSED_RITE_TIER1_CHANCE.get();
            weight1 = 0;
            weight2 = 0;
        } else if (ritesCompleted < thresholdTier3) {
            double factor = (ritesCompleted - thresholdTier2) / (double) (thresholdTier3 - thresholdTier2);
            weight0 = AncientCursesConfig.CURSED_RITE_TIER1_CHANCE.get() * (1.0 - factor);
            weight1 = AncientCursesConfig.CURSED_RITE_TIER2_CHANCE.get() * factor;
            weight2 = 0;
        } else if (ritesCompleted < maxRites) {
            double factor = (ritesCompleted - thresholdTier3) / (double) (maxRites - thresholdTier3);
            weight0 = AncientCursesConfig.CURSED_RITE_TIER1_CHANCE.get() * (1.0 - factor);
            weight1 = AncientCursesConfig.CURSED_RITE_TIER2_CHANCE.get() * (1.0 - factor);
            weight2 = AncientCursesConfig.CURSED_RITE_TIER3_CHANCE.get() * factor;
        } else {
            weight0 = 0;
            weight1 = 0;
            weight2 = 1;
        }

        List<Integer> weightedAmplifiers = new ArrayList<>();
        addAmplifierWeight(weightedAmplifiers, 0, weight0);
        addAmplifierWeight(weightedAmplifiers, 1, weight1);
        addAmplifierWeight(weightedAmplifiers, 2, weight2);

        if (weightedAmplifiers.isEmpty()) {
            weightedAmplifiers.add(0);
        }

        return weightedAmplifiers;
    }

    private static void addAmplifierWeight(List<Integer> weightedAmplifiers, int amplifier, double weight) {
        for (int i = 0; i < (int) Math.round(weight); i++) {
            weightedAmplifiers.add(amplifier);
        }
    }
}
