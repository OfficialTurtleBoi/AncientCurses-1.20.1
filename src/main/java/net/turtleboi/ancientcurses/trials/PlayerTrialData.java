package net.turtleboi.ancientcurses.trials;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class PlayerTrialData {
    private static final String IS_CURSED_KEY = "isCursed";
    private static final String CURSE_ID_KEY = "CurseId";
    private static final String CURSE_AMP_KEY = "CurseAmplifier";
    private static final String ALTAR_POS_KEY = "AltarPos";
    private static final String TRIAL_COMPLETED_KEY = "CurseTrialCompleted";
    private static final String NBT_TRIALS_COMPLETED = "TrialsCompleted";
    private static final String COMPLETED_ALTARS_KEY = "CompletedAltars";
    private static final String CURRENT_TRIAL_TYPE = "CurrentTrialType";

    public static boolean isPlayerCursed(Player player) {
        return player.getPersistentData().getBoolean(IS_CURSED_KEY);
    }

    public static void setPlayerCursed(Player player, boolean cursed) {
        player.getPersistentData().putBoolean(IS_CURSED_KEY, cursed);
    }

    public static BlockPos getAltarPos(Player player) {
        if (player.getPersistentData().contains(ALTAR_POS_KEY)) {
            long posLong = player.getPersistentData().getLong(ALTAR_POS_KEY);
            return BlockPos.of(posLong);
        }
        return null;
    }

    public static void setAltarPos(Player player, BlockPos altarPos) {
        player.getPersistentData().putLong(ALTAR_POS_KEY, altarPos.asLong());
    }

    public static void clearPlayerCurse(Player player) {
        player.getPersistentData().remove(IS_CURSED_KEY);
        player.getPersistentData().remove(CURSE_ID_KEY);
        player.getPersistentData().remove(CURSE_AMP_KEY);
        player.getPersistentData().remove(ALTAR_POS_KEY);
        player.getPersistentData().remove(TRIAL_COMPLETED_KEY);
        player.getPersistentData().remove(CURRENT_TRIAL_TYPE);
    }

    public static void setTrialCompleted(Player player, BlockPos altarPos) {
        CompoundTag completedAltars = player.getPersistentData().getCompound(COMPLETED_ALTARS_KEY);
        completedAltars.putBoolean(altarPos.asLong() + "", true);
        player.getPersistentData().put(COMPLETED_ALTARS_KEY, completedAltars);
    }

    public static boolean hasCompletedTrial(Player player, BlockPos altarPos) {
        CompoundTag completedAltars = player.getPersistentData().getCompound(COMPLETED_ALTARS_KEY);
        return completedAltars.getBoolean(altarPos.asLong() + "");
    }

    public static int getPlayerTrialsCompleted(Player player) {
        return player.getPersistentData().getInt(NBT_TRIALS_COMPLETED);
    }

    public static void incrementPlayerTrialsCompleted(Player player) {
        int currentTrials = getPlayerTrialsCompleted(player);
        player.getPersistentData().putInt(NBT_TRIALS_COMPLETED, currentTrials + 1);
    }

    public static void setCurseAmplifier(Player player, int amplifier) {
        player.getPersistentData().putInt(CURSE_AMP_KEY, amplifier);
    }

    public static int getCurseAmplifier(Player player) {
        return player.getPersistentData().getInt(CURSE_AMP_KEY);
    }

    public static void setCurrentTrialType(Player player, String trialType) {
        player.getPersistentData().putString(CURRENT_TRIAL_TYPE, trialType);
    }

    public static String getCurrentTrialType(Player player) {
        return player.getPersistentData().getString(CURRENT_TRIAL_TYPE);
    }

    public static void clearCurrentTrialType(Player player) {
        player.getPersistentData().remove(CURRENT_TRIAL_TYPE);
    }
}
