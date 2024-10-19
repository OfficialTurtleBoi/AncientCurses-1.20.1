package net.turtleboi.ancientcurses.trials;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.CameraShakeS2C;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EliminationTrial implements Trial {
    private UUID playerUUID;
    private EntityType<?> eliminationTarget;
    private String eliminationTargetString;
    private int eliminationKills;
    private int eliminationKillsRequired;
    public static final String eliminationCount = "EliminationCount";
    public static final String eliminationRequirement = "EliminationRequirement";
    private CursedAltarBlockEntity altar;
    private MobEffect effect;
    private boolean completed;

    public EliminationTrial(Player player, MobEffect effect, int amplifier, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.altar = altar;
        this.effect = effect;
        this.eliminationTarget = selectRandomTargetMob();
        this.eliminationKillsRequired = calculateRequiredKillCount(amplifier);
        this.eliminationKills = 0;
        this.completed = false;
        PlayerTrialData.setCurseEffect(player, effect);
    }

    public EliminationTrial(CursedAltarBlockEntity altar) {
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
        tag.putString("EliminationTarget", Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(eliminationTarget)).toString());
        tag.putInt(eliminationCount, eliminationKills);
        tag.putInt(eliminationRequirement, eliminationKillsRequired);
        tag.putBoolean("Completed", completed);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        String effectName = tag.getString("Effect");
        this.effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        String targetName = tag.getString("EliminationTarget");
        this.eliminationTarget = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(targetName));
        this.eliminationKills = tag.getInt(eliminationCount);
        this.eliminationKillsRequired = tag.getInt(eliminationRequirement);
        this.completed = tag.getBoolean("Completed");
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

        if (entity.getType() == eliminationTarget) {
            incrementEliminationCount();
            if (isTrialCompleted(player)) {
                concludeTrial(player);
            } else {
                trackProgress(player);
            }
        }
    }

    @Override
    public void onPlayerTick(Player player) {

    }

    private EntityType<?> selectRandomTargetMob() {
        List<WeightedMob> mobList = MobList.ELIMINATION_TRIAL_MOBS;
        double totalWeight = mobList.stream().mapToDouble(WeightedMob::getWeight).sum();
        double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cumulativeWeight = 0.0;
        for (WeightedMob wm : mobList) {
            cumulativeWeight += wm.getWeight();
            if (randomValue <= cumulativeWeight) {
                this.eliminationTargetString = wm.getMobType().getDescription().getString();
                return wm.getMobType();
            }
        }
        return mobList.get(mobList.size() - 1).getMobType();
    }

    private int calculateRequiredKillCount(int amplifier) {
        int randomMultiplier = ThreadLocalRandom.current().nextInt(8, 13);
        int baseKillCount = randomMultiplier * (amplifier + 1);
        if (eliminationTarget.equals(EntityType.GHAST) ||
                eliminationTarget.equals(EntityType.WITHER_SKELETON) ||
                eliminationTarget.equals(EntityType.WITCH) ||
                eliminationTarget.equals(EntityType.EVOKER)) {
            baseKillCount = Math.max(1, baseKillCount / 2);
        } else if (eliminationTarget.equals(EntityType.CAVE_SPIDER) ||
                eliminationTarget.equals(EntityType.SLIME) ||
                eliminationTarget.equals(EntityType.MAGMA_CUBE)) {
            baseKillCount = baseKillCount * 2;
        }

        if (ThreadLocalRandom.current().nextDouble() <= 0.01) {
            eliminationTarget = EntityType.VILLAGER;
            baseKillCount = baseKillCount * 2;
        }

        return baseKillCount;
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
                            eliminationTargetString,
                            eliminationKills,
                            eliminationKillsRequired,
                            0,
                            0,
                            "",
                            0,
                            0),
                    (ServerPlayer) player);
        }
    }

    @Override
    public void concludeTrial(Player player) {
        //player.displayClientMessage(Component.literal("You have completed the elimination trial! Collect your reward").withStyle(ChatFormatting.GREEN), true);
        ModNetworking.sendToPlayer(
                new SyncTrialDataS2C(
                        PlayerTrialData.eliminationTrial,
                        eliminationTargetString,
                        eliminationKillsRequired,
                        eliminationKillsRequired,
                        0,
                        0,
                        "",
                        0,
                        0),
                (ServerPlayer) player);
        PlayerTrialData.clearCurseEffect(player);

        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            MobEffect effect = effectInstance.getEffect();
            if (CurseRegistry.getCurses().contains(effect)) {
                player.removeEffect(effect);
            }
        }

        ModNetworking.sendToPlayer(new CameraShakeS2C(0.05F, 1000), (ServerPlayer) player);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.get(),
                    SoundSource.AMBIENT,
                    1.0f,
                    0.25f
            );
        }

        CursedPortalEntity.spawnPortalNearPlayer(player, altar.getBlockPos(),  altar.getLevel(), altar);
        altar.setPlayerTrialCompleted(player);
        setCompleted(true);
    }

    public void incrementEliminationCount() {
        eliminationKills++;
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public static class WeightedMob {
        private final EntityType<?> mobType;
        private final double weight;

        public WeightedMob(EntityType<?> mobType, double weight) {
            this.mobType = mobType;
            this.weight = weight;
        }

        public EntityType<?> getMobType() {
            return mobType;
        }

        public double getWeight() {
            return weight;
        }
    }

    public static class MobList {
        public static final List<WeightedMob> ELIMINATION_TRIAL_MOBS = Arrays.asList(
                new WeightedMob(EntityType.ZOMBIE, 20.0),
                new WeightedMob(EntityType.SKELETON, 20.0),
                new WeightedMob(EntityType.CREEPER, 15.0),
                new WeightedMob(EntityType.SPIDER, 15.0),
                new WeightedMob(EntityType.PILLAGER, 10.0),
                new WeightedMob(EntityType.VINDICATOR, 10.0),
                new WeightedMob(EntityType.HOGLIN, 10.0),
                new WeightedMob(EntityType.ZOMBIFIED_PIGLIN, 10.0),
                new WeightedMob(EntityType.PIGLIN_BRUTE, 10.0),
                new WeightedMob(EntityType.DROWNED, 10.0),
                new WeightedMob(EntityType.GUARDIAN, 5.0),
                new WeightedMob(EntityType.PHANTOM, 5.0),
                new WeightedMob(EntityType.SLIME, 15.0),
                new WeightedMob(EntityType.MAGMA_CUBE, 15.0),
                new WeightedMob(EntityType.CAVE_SPIDER, 5.0),
                new WeightedMob(EntityType.WITCH, 3.0),
                new WeightedMob(EntityType.EVOKER, 3.0),
                new WeightedMob(EntityType.WITHER_SKELETON, 2.0),
                new WeightedMob(EntityType.GHAST, 1.0),
                new WeightedMob(EntityType.BLAZE, 2.0),
                new WeightedMob(EntityType.HUSK, 3.0),
                new WeightedMob(EntityType.STRAY, 3.0)
        );
        public WeightedMob selectRandomTargetMobWithWeight() {
            List<WeightedMob> mobList = MobList.ELIMINATION_TRIAL_MOBS;
            double totalWeight = mobList.stream().mapToDouble(WeightedMob::getWeight).sum();
            double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
            double cumulativeWeight = 0.0;

            for (WeightedMob wm : mobList) {
                cumulativeWeight += wm.getWeight();
                if (randomValue <= cumulativeWeight) {
                    return wm;
                }
            }

            return mobList.get(mobList.size() - 1);
        }
    }
}
