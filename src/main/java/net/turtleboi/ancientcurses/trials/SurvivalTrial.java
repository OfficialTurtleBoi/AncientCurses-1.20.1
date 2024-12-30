package net.turtleboi.ancientcurses.trials;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.CameraShakeS2C;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SurvivalTrial implements Trial {
    private UUID playerUUID;
    private long elapsedTime;
    private long trialDuration;
    private CursedAltarBlockEntity altar;
    private MobEffect effect;
    private int pAmplifier;
    private boolean completed;
    public static final String trialDurationTotal = "TrialDuration";
    public static final String trialDurationElapsed = "TrialElapsedTime";
    private int portalCooldown = 0;
    public SurvivalTrial(Player player, MobEffect effect, int pAmplifier, long trialDuration, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.altar = altar;
        this.effect = effect;
        this.trialDuration = trialDuration;
        //System.out.println(Component.literal("Setting trial duration to: " + this.trialDuration));
        this.elapsedTime = 0;
        //System.out.println(Component.literal("Setting elapsed time to: " + this.elapsedTime));
        this.pAmplifier = pAmplifier + 1;
        this.completed = false;
        this.portalCooldown = 0;
        PlayerTrialData.setCurseEffect(player, effect);
    }

    public SurvivalTrial(CursedAltarBlockEntity altar) {
        this.altar = altar;
        this.completed = false;
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
        tag.putBoolean("Completed", completed);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        String effectName = tag.getString("Effect");
        this.effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        this.elapsedTime = tag.getLong(trialDurationElapsed);
        this.trialDuration = tag.getLong(trialDurationTotal);
        this.completed = tag.getBoolean("Completed");
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
        //System.out.println(Component.literal("Completed trial!"));
        return elapsedTime >= trialDuration;
    }

    @Override
    public void onPlayerTick(Player player) {
        if (!isTrialActive()) {
            return;
        }

        if (effect != ModEffects.CURSE_OF_PESTILENCE.get()) {
            portalCooldown++;
            if (portalCooldown >= 600 / pAmplifier) {
                portalCooldown = 0;
                //System.out.println(Component.literal("Spawning new summoning portal"));
                CursedPortalEntity.spawnSummoningPortalOnPlayer(player, altar.getBlockPos(), player.level(), altar);
            }
        }

        trackProgress(player);
        if (isTrialCompleted(player)) {
            concludeTrial(player);
        }
    }

    @Override
    public void trackProgress(Player player) {
        if (!isTrialActive()) {
            return;
        }

        if (player != null) {
            elapsedTime++;
            //System.out.println("Setting elapsed time to: " + elapsedTime);
            float progressPercentage = Math.min((float) elapsedTime / trialDuration, 1.0f);
            ModNetworking.sendToPlayer(
                    new SyncTrialDataS2C(
                            PlayerTrialData.survivalTrial,
                            "",
                            0,
                            0,
                            elapsedTime,
                            trialDuration,
                            "",
                            0,
                            0),
                    (ServerPlayer) player);
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
                        "",
                        0,
                        0,
                        trialDuration,
                        trialDuration,
                        "",
                        0,
                        0),
                (ServerPlayer) player);
        PlayerTrialData.clearCurseEffect(player);

        List<MobEffect> cursesToRemove = new ArrayList<>();
        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            MobEffect effect = effectInstance.getEffect();
            if (CurseRegistry.getCurses().contains(effect)) {
                cursesToRemove.add(effect);
            }
        }

        for (MobEffect effect : cursesToRemove) {
            player.removeEffect(effect);
        }

        ModNetworking.sendToPlayer(new CameraShakeS2C(0.125F, 1000), (ServerPlayer) player);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.get(),
                    SoundSource.AMBIENT,
                    1.00f,
                    0.25f
            );
        }

        CursedPortalEntity.spawnPortalNearPlayer(player, altar.getBlockPos(),  altar.getLevel(), altar);
        //System.out.println(Component.literal("Spawning new portal to altar"));
        altar.setPlayerTrialCompleted(player);
        this.completed = true;
    }

    @Override
    public void onEntityKilled(Player player, Entity entity) {

    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
