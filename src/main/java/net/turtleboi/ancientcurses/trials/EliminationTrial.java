package net.turtleboi.ancientcurses.trials;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.effect.ModEffects;

import java.util.UUID;

public class EliminationTrial implements Trial {
    private static final String ELIMINATION_COUNT_KEY = "EliminationCount";
    private static final String ELIMINATION_REQUIREMENT_KEY = "EliminationRequirement";
    private CursedAltarBlockEntity altar;
    private final MobEffect effect;

    public EliminationTrial(Player player, MobEffect effect, int requiredEliminations, CursedAltarBlockEntity altar) {
        this.altar = altar;
        this.effect = effect;
        saveRequiredEliminations(player, requiredEliminations);
    }

    @Override
    public void setAltar(CursedAltarBlockEntity altar) {
        this.altar = altar;
    }

    @Override
    public MobEffect getEffect() {
        return this.effect;
    }

    @Override
    public boolean isTrialCompleted(Player player) {
        int eliminations = getEliminationCount(player);
        int requiredEliminations = getRequiredEliminations(player);
        return eliminations >= requiredEliminations;
    }

    @Override
    public void trackProgress(Player player) {
        int eliminations = getEliminationCount(player);
        int requiredEliminations = getRequiredEliminations(player);
        player.sendSystemMessage(Component.literal("Eliminations: " + eliminations + "/" + requiredEliminations).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void rewardPlayer(Player player) {
        UUID playerUUID = player.getUUID();
        player.sendSystemMessage(Component.literal("You have completed the elimination trial!").withStyle(ChatFormatting.GREEN));
        player.removeEffect(this.effect);
        clearEliminationData(player);
        PlayerTrialData.clearPlayerCurse(player);
        PlayerTrialData.clearCurrentTrialType(player);
        PlayerTrialData.incrementPlayerTrialsCompleted(player);
        altar.setPlayerTrialStatus(playerUUID, true);
        altar.setPlayerTrialCompleted(player);
        altar.removePlayerTrial(playerUUID);
    }

    public void incrementEliminationCount(Player player) {
        int eliminations = getEliminationCount(player);
        eliminations++;
        saveEliminationCount(player, eliminations);
    }

    public static void saveEliminationCount(Player player, int count) {
        player.getPersistentData().putInt(ELIMINATION_COUNT_KEY, count);
    }

    // Load the player's elimination count from their NBT data
    public static int getEliminationCount(Player player) {
        return player.getPersistentData().getInt(ELIMINATION_COUNT_KEY);
    }

    public static void saveRequiredEliminations(Player player, int requiredEliminations) {
        player.getPersistentData().putInt(ELIMINATION_REQUIREMENT_KEY, requiredEliminations);
    }

    // Load the required eliminations from the player's NBT data
    public static int getRequiredEliminations(Player player) {
        return player.getPersistentData().getInt(ELIMINATION_REQUIREMENT_KEY);
    }

    // Clear the player's elimination count and required eliminations
    public static void clearEliminationData(Player player) {
        player.getPersistentData().remove(ELIMINATION_COUNT_KEY);
        player.getPersistentData().remove(ELIMINATION_REQUIREMENT_KEY);
    }
}
