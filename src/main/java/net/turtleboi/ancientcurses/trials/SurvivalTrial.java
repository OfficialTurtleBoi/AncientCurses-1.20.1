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
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.effect.effects.CurseOfObessionEffect;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.LustedPacketS2C;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.Objects;
import java.util.UUID;

public class SurvivalTrial implements Trial {
    private UUID playerUUID;
    private long elapsedTime;
    private long trialDuration;
    private CursedAltarBlockEntity altar;
    private MobEffect effect;
    public static final String trialDurationTotal = "TrialDuration";
    public static final String trialDurationElapsed = "TrialElapsedTime";
    //private ServerBossEvent bossEvent;

    public SurvivalTrial(Player player, MobEffect effect, long trialDuration, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.altar = altar;
        this.effect = effect;
        this.trialDuration = trialDuration;
        this.elapsedTime = 0;

        PlayerTrialData.setCurseEffect(player, effect);

        //this.bossEvent = new ServerBossEvent(
        //        Component.literal(PlayerTrialData.survivalTrial),
        //        BossEvent.BossBarColor.RED,
        //        BossEvent.BossBarOverlay.PROGRESS);
//
        //this.bossEvent.setProgress(0.0f);
        //if (player instanceof ServerPlayer serverPlayer) {
        //    this.bossEvent.addPlayer(serverPlayer);
        //}
    }

    public SurvivalTrial(CursedAltarBlockEntity altar) {
        this.altar = altar;
    }

    public boolean isTrialActive() {
        return altar.getPlayerTrial(playerUUID) != null;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putUUID("PlayerUUID", playerUUID);
        tag.putString("Effect", Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getKey(effect)).toString());
        tag.putLong(trialDurationElapsed, elapsedTime);
        tag.putLong(trialDurationTotal, trialDuration);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        String effectName = tag.getString("Effect");
        this.effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        this.elapsedTime = tag.getLong(trialDurationElapsed);
        this.trialDuration = tag.getLong(trialDurationTotal);
    }

    @Override
    public String getType() {
        return PlayerTrialData.survivalTrial;
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
        return elapsedTime >= trialDuration;
    }

    @Override
    public void onPlayerTick(Player player) {
        if (!isTrialActive()) {
            return;
        }

        trackProgress(player);
        if (isTrialCompleted(player)) {
            concludeTrial(player);
        }
    }

    @Override
    public void trackProgress(Player player) {
        if (player != null) {
            elapsedTime += 1;
            float progressPercentage = Math.min((float) elapsedTime / trialDuration, 1.0f);
            ModNetworking.sendToPlayer(
                    new SyncTrialDataS2C(
                            PlayerTrialData.survivalTrial,
                            0,
                            0,
                            elapsedTime,
                            trialDuration),
                    (ServerPlayer) player);
            //if (this.bossEvent != null) {
            //    this.bossEvent.setProgress(progressPercentage);
            //}
            //player.displayClientMessage(Component.literal(String.format("Trial progress: %.2f%% complete", progressPercentage * 100))
            //        .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    @Override
    public void concludeTrial(Player player) {
        //player.displayClientMessage(Component.literal("You have survived the trial! Collect your reward").withStyle(ChatFormatting.GREEN), true);
        ModNetworking.sendToPlayer(
                new SyncTrialDataS2C(
                        PlayerTrialData.survivalTrial,
                        0,
                        0,
                        trialDuration,
                        trialDuration),
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


    @Override
    public void onEntityKilled(Player player, Entity entity) {

    }

    @Override
    public void onPlayerRemoved(Player player) {
        //removeEventBar(player);
    }
}
