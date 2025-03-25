package net.turtleboi.ancientcurses.trials;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.trials.PlayerTrialDataCapability;
import net.turtleboi.ancientcurses.capabilities.trials.PlayerTrialProvider;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EliminationTrial implements Trial {
    private UUID playerUUID;
    private MobEffect effect;
    private int amplifier;
    private CursedAltarBlockEntity altar;
    private boolean completed;

    private int currentWave;
    private int waveDelay;

    private EntityType<?> eliminationTarget;
    private String eliminationTargetString;
    private int eliminationKills;

    private List<Entity> activeMobs = new ArrayList<>();

    private final int firstDegreeThreshold = 5;
    private boolean completedFirstDegree;
    private final int secondDegreeThreshold = 10;
    private boolean completedSecondDegree;
    private final int thirdDegreeThreshold = 20;
    private boolean completedThirdDegree;

    public static final String eliminationCount = "EliminationCount";
    public static final String eliminationRequirement = "EliminationRequirement";

    public EliminationTrial(Player player, MobEffect effect, int amplifier, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.effect = effect;
        this.amplifier = amplifier + 1;
        this.altar = altar;
        this.eliminationTarget = selectRandomTargetMob();
        this.currentWave = 0;
        this.waveDelay = getDefaultWaveDelay();
        this.completedFirstDegree = false;
        this.completedSecondDegree = false;
        this.completedThirdDegree = false;
        if (this.eliminationTarget != null) {
            this.eliminationTargetString = this.eliminationTarget.getDescription().getString();
        }

        this.completed = false;
        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
            trialData.setCurseEffect(effect);
            trialData.setActiveTrial(this);
            if (trialData.getEliminationKills() == 0) {
                trialData.setEliminationKills(0);
            }
            this.eliminationKills = trialData.getEliminationKills();
        });
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
        tag.putString("EliminationTargetString", eliminationTargetString != null ? eliminationTargetString : "");
        tag.putInt(eliminationCount, eliminationKills);
        tag.putBoolean("Completed", completed);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        String effectName = tag.getString("Effect");
        this.effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        String targetName = tag.getString("EliminationTarget");
        this.eliminationTarget = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(targetName));
        if (tag.contains("EliminationTargetString")) {
            this.eliminationTargetString = tag.getString("EliminationTargetString");
        } else if (this.eliminationTarget != null) {
            this.eliminationTargetString = this.eliminationTarget.getDescription().getString();
        } else {
            this.eliminationTargetString = "";
        }
        this.eliminationKills = tag.getInt(eliminationCount);
        this.completed = tag.getBoolean("Completed");
    }

    @Override
    public String getType() {
        return Trial.eliminationTrial;
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
        return (currentWave >= firstDegreeThreshold && activeMobs.isEmpty());
    }

    @Override
    public void onEntityKilled(Player player, Entity entity) {
        if (!isTrialActive()) {
            return;
        }
        if (entity.getType() == eliminationTarget) {
            player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
                int newKills = trialData.getEliminationKills() + 1;
                trialData.setEliminationKills(newKills);
                this.eliminationKills = newKills;
                activeMobs.removeIf(mob -> mob == entity);
            });

            if (isTrialCompleted(player)) {
                concludeTrial(player);
            } else {
                trackProgress(player);
            }
        }
    }


    @Override
    public void onPlayerTick(Player player) {
        if (!completedFirstDegree) {
            if (waveDelay > 0 ) {
                waveDelay--;
            }

            if (currentWave < firstDegreeThreshold) {
                if (waveDelay <= 0) {
                    if (currentWave != 0) {
                        System.out.println("[Elimination Trial] Wave " + currentWave + ": Timed out! Spawning wave: " + (currentWave + 1));
                    }
                    spawnWave(player);
                    currentWave++;
                }
            } else if (currentWave == firstDegreeThreshold && waveDelay <= 0) {
                Iterator<Entity> mobIterator = activeMobs.iterator();
                while (mobIterator.hasNext()) {
                    Entity mob = mobIterator.next();
                    if (mob instanceof LivingEntity livingMob) {
                        if (!livingMob.isAlive()) {
                            mobIterator.remove();
                        } else if (!livingMob.hasEffect(MobEffects.GLOWING)) {
                            livingMob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0));
                            System.out.println("Come kill this guy! At: " + livingMob.getX() + ", " + livingMob.getY() + ", " + livingMob.getZ());
                        }
                    }
                }

                if (activeMobs.isEmpty()) {
                    completedFirstDegree = true;
                    System.out.println("[Elimination Trial] Wave " + currentWave + ": Done! First degree complete!");
                }
            }
        }

        if (isTrialCompleted(player)) {
            concludeTrial(player);
        }
    }

    @Override
    public void trackProgress(Player player) {
        if (player != null) {
            float progressPercentage = Math.min((float) eliminationKills / 256, 1.0f);
            //player.displayClientMessage(
            //        Component.literal("Eliminations: " + eliminationKills + "/" + eliminationKillsRequired)
            //                .withStyle(ChatFormatting.YELLOW), true);

            //System.out.println("Trial Type: " + Trial.eliminationTrial);
            //System.out.println("Elimination Target: " + eliminationTarget);
            //System.out.println("Elimination Target String: " + eliminationTargetString);
            //System.out.println("Elimination Kills: " + eliminationKills);
            //System.out.println("Elimination Kills Required: " + eliminationKillsRequired);
            //System.out.println("Progress Percentage: " + progressPercentage);

            player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
                trialData.setEliminationKills(eliminationKills);

                BlockPos altarPos = trialData.getCurrentAltarPos();
                ResourceKey<Level> altarDimension = trialData.getAltarDimension();
                if (altarPos != null && altarDimension != null) {
                    MinecraftServer server = player.getServer();
                    if (server != null) {
                        ServerLevel altarLevel = server.getLevel(altarDimension);
                        if (altarLevel != null) {
                            BlockEntity blockEntity = altarLevel.getBlockEntity(altarPos);
                            if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                                CompoundTag altarNBT = new CompoundTag();
                                altarEntity.saveAdditional(altarNBT);
                                //System.out.println("Altar at " + altarPos + " in dimension "
                                //        + altarDimension.location() + " reloaded trial data for player "
                                //        + player.getName().getString());
                            } else {
                                //System.out.println("No altar found at " + altarPos + " in dimension "
                                //        + altarDimension.location() + " for player " + player.getName().getString());
                            }
                        } else {
                            //System.out.println("Could not get ServerLevel for dimension " + altarDimension.location());
                        }
                    } else {
                        //System.out.println("Player server is null.");
                    }
                }
            });

            ModNetworking.sendToPlayer(
                    new SyncTrialDataS2C(
                            Trial.eliminationTrial,
                            eliminationTargetString,
                            eliminationKills,
                            256,
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
                        Trial.eliminationTrial,
                        eliminationTargetString,
                        256,
                        265,
                        0,
                        0,
                        "",
                        0,
                        0),
                (ServerPlayer) player);

        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(PlayerTrialDataCapability::clearCurseEffect);

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

        CoreNetworking.sendToNear((new CameraShakeS2C(0.05F, 1000)), player);
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
        double variation = ThreadLocalRandom.current().nextDouble(0.75, 1.26);
        int base = 4 * (1 << (amplifier - 1));
        double waveMultiplier = 1.0 + ((double) currentWave / 10.0);
        int baseKillCount = (int) Math.round(base * variation * waveMultiplier);
        if (eliminationTarget.equals(EntityType.GHAST) ||
                eliminationTarget.equals(EntityType.WITHER_SKELETON) ||
                eliminationTarget.equals(EntityType.WITCH) ||
                eliminationTarget.equals(EntityType.EVOKER) ||
                eliminationTarget.equals(EntityType.PIGLIN_BRUTE)) {
            baseKillCount = Math.max(1, baseKillCount / 2);
        } else if (eliminationTarget.equals(EntityType.RAVAGER)) {
            baseKillCount = Math.max(1, baseKillCount / 4);
        } else if (eliminationTarget.equals(EntityType.CAVE_SPIDER)) {
            baseKillCount = baseKillCount * 2;
        }

        if (ThreadLocalRandom.current().nextDouble() <= 0.01) {
            eliminationTarget = EntityType.VILLAGER;
            baseKillCount = baseKillCount * 2;
        }

        return baseKillCount;
    }

    private List<Entity> buildWaveMobList(ServerLevel level, int mobCount) {
        List<Entity> mobs = new ArrayList<>();
        for (int i = 0; i < mobCount; i++) {
            Entity mob = eliminationTarget.create(level);
            if (mob != null) {
                mobs.add(mob);
            }
        }
        return mobs;
    }

    private void spawnWave(Player player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            ServerLevel level = (ServerLevel) player.level();
            BlockPos portalPos = altar.getBlockPos().above(1);
            int mobCount = calculateRequiredKillCount(amplifier);
            List<Entity> mobsToSpawn = buildWaveMobList(level, mobCount);
            activeMobs.addAll(mobsToSpawn);
            CursedPortalEntity.spawnSummoningPortalAtPos(level, altar, portalPos, mobsToSpawn);
            System.out.println("[EliminationTrial] Spawned wave with " + mobCount + " enemies via portal at " + portalPos);
            addWaveDelay((mobsToSpawn.size() + 1) * CursedPortalEntity.spawnDelay);
            System.out.println("[EliminationTrial] Time until next wave: " + (mobsToSpawn.size() + 1) * CursedPortalEntity.spawnDelay);
        }
    }

    private int getDefaultWaveDelay(){
        return 200 * (4 - amplifier);
    }

    private void addWaveDelay(int delayTicks) {
        waveDelay = getDefaultWaveDelay() + delayTicks;
    }

    public static class MobList {
        public static final List<WeightedMob> ELIMINATION_TRIAL_MOBS = Arrays.asList(
                new WeightedMob(EntityType.ZOMBIE, 20.0),
                new WeightedMob(EntityType.SKELETON, 20.0),
                new WeightedMob(EntityType.CREEPER, 15.0),
                new WeightedMob(EntityType.SPIDER, 15.0),
                new WeightedMob(EntityType.PILLAGER, 10.0),
                new WeightedMob(EntityType.VINDICATOR, 10.0),
                new WeightedMob(EntityType.RAVAGER, 2.0),
                new WeightedMob(EntityType.ZOMBIFIED_PIGLIN, 10.0),
                new WeightedMob(EntityType.PIGLIN_BRUTE, 10.0),
                new WeightedMob(EntityType.DROWNED, 10.0),
                new WeightedMob(EntityType.GUARDIAN, 5.0),
                new WeightedMob(EntityType.PHANTOM, 12.0),
                new WeightedMob(EntityType.SLIME, 15.0),
                new WeightedMob(EntityType.MAGMA_CUBE, 15.0),
                new WeightedMob(EntityType.CAVE_SPIDER, 5.0),
                new WeightedMob(EntityType.WITCH, 5.0),
                new WeightedMob(EntityType.EVOKER, 3.0),
                new WeightedMob(EntityType.WITHER_SKELETON, 2.0),
                new WeightedMob(EntityType.GHAST, 1.0),
                new WeightedMob(EntityType.BLAZE, 5.0),
                new WeightedMob(EntityType.HUSK, 15.0),
                new WeightedMob(EntityType.STRAY, 15.0)
        );
    }
}
