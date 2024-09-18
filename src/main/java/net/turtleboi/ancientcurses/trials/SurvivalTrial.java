package net.turtleboi.ancientcurses.trials;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.entity.ModEntities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SurvivalTrial implements Trial {
    private final long trialDuration;
    private final Map<UUID, Long> startTimes = new HashMap<>();
    private CursedAltarBlockEntity altar;
    private final MobEffect effect;
    public static final String TRIAL_DURATION_KEY = "TrialDuration";
    public static final String TRIAL_ELAPSED_TIME_KEY = "TrialElapsedTime";

    public SurvivalTrial(Player player, MobEffect effect, long trialDuration, CursedAltarBlockEntity altar) {
        this.trialDuration = trialDuration;
        this.altar = altar;
        this.effect = effect;
        setTrialDuration(player, trialDuration);
        PlayerTrialData.setEffect(player, effect);

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
        long elapsedTime = SurvivalTrial.getTrialElapsedTime(player);
        return elapsedTime >= trialDuration;
    }

    @Override
    public void trackProgress(Player player) {
        long elapsedTime = SurvivalTrial.getTrialElapsedTime(player);
        elapsedTime += 1;
        SurvivalTrial.setTrialElapsedTime(player, elapsedTime);
        double progressPercentage = (double) elapsedTime / trialDuration * 100.0;
        progressPercentage = Math.min(progressPercentage, 100.0);

        player.displayClientMessage(Component.literal(String.format("Trial progress: %.2f%% complete", progressPercentage))
                .withStyle(ChatFormatting.YELLOW), true);
    }

    @Override
    public void rewardPlayer(Player player) {
        UUID playerUUID = player.getUUID();
        player.displayClientMessage(Component.literal("You have survived the trial!").withStyle(ChatFormatting.GREEN), true);
        player.removeEffect(this.effect);
        resetTrialData(player);

        PlayerTrialData.clearPlayerCurse(player);
        PlayerTrialData.clearCurrentTrialType(player);

        CursedPortalEntity.spawnPortalNearPlayer(player, altar.getBlockPos(),  altar.getLevel());

        altar.setPlayerTrialStatus(playerUUID, true, false);
        altar.setPlayerTrialCompleted(player);
        altar.removePlayerTrial(playerUUID);
    }

    public static void setTrialDuration(Player player, long duration) {
        player.getPersistentData().putLong(TRIAL_DURATION_KEY, duration);
    }

    public static long getTrialDuration(Player player) {
        return player.getPersistentData().getLong(TRIAL_DURATION_KEY);
    }

    public static void setTrialElapsedTime(Player player, long elapsedTime) {
        player.getPersistentData().putLong(TRIAL_ELAPSED_TIME_KEY, elapsedTime);
    }

    public static long getTrialElapsedTime(Player player) {
        return player.getPersistentData().getLong(TRIAL_ELAPSED_TIME_KEY);
    }

    private static void resetTrialData(Player player){
        player.getPersistentData().remove(TRIAL_DURATION_KEY);
        player.getPersistentData().remove(TRIAL_ELAPSED_TIME_KEY);
    }
}
