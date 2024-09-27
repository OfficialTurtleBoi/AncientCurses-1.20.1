package net.turtleboi.ancientcurses.trials;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;

import java.util.Objects;
import java.util.UUID;

public class EliminationTrial implements Trial {
    private UUID playerUUID;
    private int eliminationKills;
    private int eliminationKillsRequired;
    public static final String eliminationCount = "EliminationCount";
    public static final String eliminationRequirement = "EliminationRequirement";
    private CursedAltarBlockEntity altar;
    private MobEffect effect;
    private ServerBossEvent bossEvent;

    public EliminationTrial(Player player, MobEffect effect, int requiredEliminations, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.altar = altar;
        this.effect = effect;
        this.eliminationKillsRequired = requiredEliminations;
        this.eliminationKills = 0;
        PlayerTrialData.setCurseEffect(player, effect);

        //this.bossEvent = new ServerBossEvent(
        //        Component.literal(PlayerTrialData.eliminationTrial),
        //        BossEvent.BossBarColor.RED,
        //        BossEvent.BossBarOverlay.PROGRESS
        //);
        //this.bossEvent.setProgress(0.0f);
        //if (player instanceof ServerPlayer serverPlayer) {
        //    this.bossEvent.addPlayer(serverPlayer);
        //}
    }

    public EliminationTrial(CursedAltarBlockEntity altar) {
        this.altar = altar;
    }

    public boolean isTrialActive() {
        return altar.getPlayerTrial(playerUUID) != null;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putUUID("PlayerUUID", playerUUID);
        tag.putString("Effect", Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getKey(effect)).toString());
        tag.putInt(eliminationCount, eliminationKills);
        tag.putInt(eliminationRequirement, eliminationKillsRequired);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        String effectName = tag.getString("Effect");
        this.effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        this.eliminationKills = tag.getInt(eliminationCount);
        this.eliminationKillsRequired = tag.getInt(eliminationRequirement);
    }

    @Override
    public String getType() {
        return PlayerTrialData.eliminationTrial;
    }

    @Override
    public void setAltar(CursedAltarBlockEntity altar) {
        this.altar = altar;
    }

    @Override
    public MobEffect getEffect() {
        return this.effect;
    }

    public Player getPlayer() {
        if (altar.getLevel() instanceof ServerLevel serverLevel) {
            return serverLevel.getPlayerByUUID(playerUUID);
        }
        return null;
    }


    @Override
    public boolean isTrialCompleted(Player player) {
        return eliminationKills >= eliminationKillsRequired;
    }

    @Override
    public void onEntityKilled(Player player, Entity entity) {
        if (!isTrialActive()) {
            return;
        }

        incrementEliminationCount();
        if (isTrialCompleted(player)) {
            concludeTrial(player);
        } else {
            trackProgress(player);
        }
    }

    @Override
    public void onPlayerTick(Player player) {

    }

    @Override
    public void trackProgress(Player player) {
        if (player != null) {
            float progressPercentage = Math.min((float) eliminationKills / eliminationKillsRequired, 1.0f);
            //player.displayClientMessage(
            //        Component.literal("Eliminations: " + eliminationKills + "/" + eliminationKillsRequired)
            //                .withStyle(ChatFormatting.YELLOW), true);
            ModNetworking.sendToPlayer(
                    new SyncTrialDataS2C(
                            PlayerTrialData.eliminationTrial,
                            eliminationKills,
                            eliminationKillsRequired,
                            0,
                            0),
                    (ServerPlayer) player);
            //if (this.bossEvent != null) {
            //    this.bossEvent.setProgress(progressPercentage);
            //}
        }
    }

    @Override
    public void concludeTrial(Player player) {
        //player.displayClientMessage(Component.literal("You have completed the elimination trial! Collect your reward").withStyle(ChatFormatting.GREEN), true);
        ModNetworking.sendToPlayer(
                new SyncTrialDataS2C(
                        PlayerTrialData.eliminationTrial,
                        eliminationKillsRequired,
                        eliminationKillsRequired,
                        0,
                        0),
                (ServerPlayer) player);
        player.removeEffect(this.effect);

        PlayerTrialData.clearCurseEffect(player);

        CursedPortalEntity.spawnPortalNearPlayer(player, altar.getBlockPos(),  altar.getLevel());
        //removeEventBar(player);
        altar.setPlayerTrialCompleted(player);
        altar.removePlayerTrial(playerUUID);
    }

    //@Override
    //public void removeEventBar(Player player) {
        //System.out.println("removeEventBar called for player: " + player.getName().getString());
//
        //if (this.bossEvent != null) {
        //    System.out.println("Boss event is not null. Removing boss bar for player: " + player.getName().getString());
//
        //    if (player instanceof ServerPlayer serverPlayer) {
        //        this.bossEvent.removePlayer(serverPlayer);
        //        System.out.println("Boss bar removed for player: " + player.getName().getString());
        //    }
        //    this.bossEvent = null;
        //} else {
        //    System.out.println("No boss bar to remove for player: " + player.getName().getString());
        //}
    //}


    public void incrementEliminationCount() {
        eliminationKills++;
    }

    @Override
    public void onPlayerRemoved(Player player) {
        //removeEventBar(player);
    }
}
