package net.turtleboi.ancientcurses.trials;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.effect.effects.CurseOfObessionEffect;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.LustedPacketS2C;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.UUID;

public class SurvivalTrial implements Trial {
    private final long trialDuration;
    private CursedAltarBlockEntity altar;
    private final MobEffect effect;
    public static final String trialDurationTotal = "TrialDuration";
    public static final String trialDurationElapsed = "TrialElapsedTime";
    private ServerBossEvent bossEvent;

    public SurvivalTrial(Player player, MobEffect effect, long trialDuration, CursedAltarBlockEntity altar) {
        this.trialDuration = trialDuration;
        this.altar = altar;
        this.effect = effect;
        setTrialDuration(player, trialDuration);
        PlayerTrialData.setCurseEffect(player, effect);

        this.bossEvent = new ServerBossEvent(
                Component.literal(PlayerTrialData.survivalTrial),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS);

        this.bossEvent.setProgress(0.0f);
        if (player instanceof ServerPlayer serverPlayer) {
            this.bossEvent.addPlayer(serverPlayer);
        }
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
        float progressPercentage = Math.min((float) elapsedTime / trialDuration, 1.0f);

        ModNetworking.sendToPlayer(new SyncTrialDataS2C(0,0, elapsedTime, getTrialDuration(player)), (ServerPlayer) player);

        if (this.bossEvent != null) {
            this.bossEvent.setProgress(progressPercentage);
        }

        //player.displayClientMessage(Component.literal(String.format("Trial progress: %.2f%% complete", progressPercentage * 100))
        //        .withStyle(ChatFormatting.YELLOW), true);
    }

    @Override
    public void rewardPlayer(Player player) {
        UUID playerUUID = player.getUUID();
        //player.displayClientMessage(Component.literal("You have survived the trial!").withStyle(ChatFormatting.GREEN), true);
        player.removeEffect(this.effect);
        resetTrialData(player);

        PlayerTrialData.clearCurseEffect(player);
        PlayerTrialData.clearCurrentTrialType(player);

        CursedPortalEntity.spawnPortalNearPlayer(player, altar.getBlockPos(),  altar.getLevel());

        altar.setPlayerTrialStatus(playerUUID, true, false);
        altar.setPlayerTrialCompleted(player);
    }

    public void resetEventProgress(){
        this.bossEvent.setProgress(0.0f);
    }

    public void removeEventBar(Player player){
        UUID playerUUID = player.getUUID();
        altar.removePlayerTrial(playerUUID);
        if (player instanceof ServerPlayer serverPlayer) {
            this.bossEvent.removePlayer(serverPlayer);
        }
        this.bossEvent = null;
    }

    public static void setTrialDuration(Player player, long duration) {
        player.getPersistentData().putLong(trialDurationTotal, duration);
    }

    public static long getTrialDuration(Player player) {
        return player.getPersistentData().getLong(trialDurationTotal);
    }

    public static void setTrialElapsedTime(Player player, long elapsedTime) {
        player.getPersistentData().putLong(trialDurationElapsed, elapsedTime);
    }

    public static long getTrialElapsedTime(Player player) {
        return player.getPersistentData().getLong(trialDurationElapsed);
    }

    private static void resetTrialData(Player player){
        player.getPersistentData().remove(trialDurationTotal);
        player.getPersistentData().remove(trialDurationElapsed);
    }
}
