package net.turtleboi.ancientcurses.trials;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SurvivalTrial implements Trial {
    private final long trialDuration;
    private final Map<UUID, Long> startTimes = new HashMap<>();
    private CursedAltarBlockEntity altar;
    private final MobEffect effect;

    public SurvivalTrial(MobEffect effect, long trialDuration, CursedAltarBlockEntity altar) {
        this.trialDuration = trialDuration;
        this.altar = altar;
        this.effect = effect;
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
        UUID playerUUID = player.getUUID();
        if (!startTimes.containsKey(playerUUID)) {
            startTimes.put(playerUUID, player.level().getGameTime());
        }

        long timeElapsed = player.level().getGameTime() - startTimes.get(playerUUID);
        return timeElapsed >= trialDuration;
    }

    @Override
    public void trackProgress(Player player) {
        UUID playerUUID = player.getUUID();

        if (!startTimes.containsKey(playerUUID)) {
            startTimes.put(playerUUID, player.level().getGameTime());
        }

        long startTime = startTimes.get(playerUUID);
        long currentTime = player.level().getGameTime();
        long timeElapsed = currentTime - startTime;

        double progressPercentage = (double) timeElapsed / trialDuration * 100.0;
        progressPercentage = Math.min(progressPercentage, 100.0);
        player.sendSystemMessage(Component.literal(String.format("Trial progress: %.2f%% complete", progressPercentage))
                .withStyle(ChatFormatting.YELLOW));
    }


    @Override
    public void rewardPlayer(Player player) {
        UUID playerUUID = player.getUUID();
        player.sendSystemMessage(Component.literal("You have survived the trial!").withStyle(ChatFormatting.GREEN));
        player.removeEffect(this.effect);
        PlayerTrialData.clearPlayerCurse(player);
        PlayerTrialData.clearCurrentTrialType(player);
        PlayerTrialData.incrementPlayerTrialsCompleted(player);
        altar.setPlayerTrialStatus(playerUUID, true);
        altar.setPlayerTrialCompleted(player);
        altar.removePlayerTrial(playerUUID);
    }

}
