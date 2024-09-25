package net.turtleboi.ancientcurses.trials;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;

import java.util.UUID;

public class EliminationTrial implements Trial {
    public static final String eliminationCount = "EliminationCount";
    public static final String eliminationRequirement = "EliminationRequirement";
    private CursedAltarBlockEntity altar;
    private final MobEffect effect;
    private ServerBossEvent bossEvent;

    public EliminationTrial(Player player, MobEffect effect, int requiredEliminations, CursedAltarBlockEntity altar) {
        this.altar = altar;
        this.effect = effect;
        saveRequiredEliminations(player, requiredEliminations);
        PlayerTrialData.setCurseEffect(player, effect);

        this.bossEvent = new ServerBossEvent(
                Component.literal(PlayerTrialData.eliminationTrial),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );
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
        int eliminations = getEliminationCount(player);
        int requiredEliminations = getRequiredEliminations(player);
        return eliminations >= requiredEliminations;
    }

    @Override
    public void trackProgress(Player player) {
        int eliminations = getEliminationCount(player);
        int requiredEliminations = getRequiredEliminations(player);
        float progressPercentage = Math.min((float) eliminations / requiredEliminations, 1.0f);
        player.displayClientMessage(Component.literal("Eliminations: " + eliminations + "/" + requiredEliminations).withStyle(ChatFormatting.YELLOW), true);

        ModNetworking.sendToPlayer(new SyncTrialDataS2C(PlayerTrialData.eliminationTrial, eliminations,requiredEliminations, 0, 0), (ServerPlayer) player);

        if (this.bossEvent != null) {
            this.bossEvent.setProgress(progressPercentage);
        }
    }

    @Override
    public void concludeTrial(Player player) {
        UUID playerUUID = player.getUUID();
        player.displayClientMessage(Component.literal("You have completed the elimination trial! Collect your reward").withStyle(ChatFormatting.GREEN), true);
        player.removeEffect(this.effect);
        resetEliminationData(player);

        PlayerTrialData.clearCurseEffect(player);
        PlayerTrialData.clearCurrentTrialType(player);

        CursedPortalEntity.spawnPortalNearPlayer(player, altar.getBlockPos(),  altar.getLevel());

        altar.setPlayerTrialStatus(playerUUID, true, false);
        altar.setPlayerTrialCompleted(player);
        altar.removePlayerTrial(playerUUID);
    }

    public void resetEventProgress(){
        this.bossEvent.setProgress(0.0f);
    }

    @Override
    public void removeEventBar(Player player) {
        if (this.bossEvent != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                this.bossEvent.removePlayer(serverPlayer);
                ModNetworking.sendToPlayer(new SyncTrialDataS2C("None", 0, 0, 0, 0), serverPlayer);
                System.out.println("Boss bar removed for player: " + player.getName().getString());
            }
            this.bossEvent = null;
        } else {
            System.out.println("No boss bar to remove for player: " + player.getName().getString());
        }
    }


    public void incrementEliminationCount(Player player) {
        int eliminations = getEliminationCount(player);
        eliminations++;
        saveEliminationCount(player, eliminations);
        trackProgress(player);
    }

    public static void saveEliminationCount(Player player, int count) {
        player.getPersistentData().putInt(eliminationCount, count);
    }

    public static int getEliminationCount(Player player) {
        return player.getPersistentData().getInt(eliminationCount);
    }

    public static void saveRequiredEliminations(Player player, int requiredEliminations) {
        player.getPersistentData().putInt(eliminationRequirement, requiredEliminations);
    }

    public static int getRequiredEliminations(Player player) {
        return player.getPersistentData().getInt(eliminationRequirement);
    }

    public static void resetEliminationData(Player player) {
        player.getPersistentData().remove(eliminationCount);
        player.getPersistentData().remove(eliminationRequirement);
    }
}
