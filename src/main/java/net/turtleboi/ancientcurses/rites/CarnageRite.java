package net.turtleboi.ancientcurses.rites;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.client.rites.CarnageClientRiteState;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CarnageRite extends AbstractRite {
    private static final String AMPLIFIER_KEY = "Amplifier";
    private static final String CURRENT_WAVE_KEY = "CurrentWave";
    private static final String WAVE_DELAY_KEY = "WaveDelay";
    private static final String WAVE_KILL_TOTAL_KEY = "WaveKillTotal";
    private static final String ELIMINATION_TARGET_KEY = "EliminationTarget";
    private static final String ELIMINATION_TARGET_STRING_KEY = "EliminationTargetString";
    private static final String COMPLETED_FIRST_KEY = "CompletedFirstDegree";
    private static final String COMPLETED_SECOND_KEY = "CompletedSecondDegree";
    private static final String COMPLETED_THIRD_KEY = "CompletedThirdDegree";

    private int amplifier;

    private int currentWave;
    private int waveDelay;

    private EntityType<?> eliminationTarget;
    private String eliminationTargetString;
    private int waveKillTotal;

    private List<Entity> activeMobs = new ArrayList<>();

    public static final int firstDegreeThreshold = 1;
    public boolean completedFirstDegree;
    public static final int secondDegreeThreshold = 3;
    public boolean completedSecondDegree;
    public static final int thirdDegreeThreshold = 5;
    public boolean completedThirdDegree;

    public static final String eliminationCount = "EliminationCount";
    public static final String eliminationRequirement = "EliminationRequirement";

    public CarnageRite(Player player, MobEffect effect, int amplifier, CursedAltarBlockEntity altar) {
        super(altar);
        this.playerUUID = player.getUUID();
        this.effect = effect;
        this.amplifier = amplifier + 1;
        this.eliminationTarget = selectRandomTargetMob();
        this.currentWave = 0;
        this.waveDelay = 200;
        this.completedFirstDegree = false;
        this.completedSecondDegree = false;
        this.completedThirdDegree = false;
        if (this.eliminationTarget != null) {
            this.eliminationTargetString = this.eliminationTarget.getDescription().getString();
        }

        this.completed = false;
    }

    public CarnageRite(CursedAltarBlockEntity altar) {
        super(altar);
    }

    public boolean isRiteActive() {
        return altar.getPlayerRite(playerUUID) != null;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        saveBaseData(tag);
        tag.putInt(AMPLIFIER_KEY, amplifier);
        tag.putInt(CURRENT_WAVE_KEY, currentWave);
        tag.putInt(WAVE_DELAY_KEY, waveDelay);
        tag.putInt(WAVE_KILL_TOTAL_KEY, waveKillTotal);
        if (eliminationTarget != null) {
            tag.putString(ELIMINATION_TARGET_KEY, Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(eliminationTarget)).toString());
        }
        tag.putString(ELIMINATION_TARGET_STRING_KEY, eliminationTargetString != null ? eliminationTargetString : "");
        tag.putBoolean(COMPLETED_FIRST_KEY, completedFirstDegree);
        tag.putBoolean(COMPLETED_SECOND_KEY, completedSecondDegree);
        tag.putBoolean(COMPLETED_THIRD_KEY, completedThirdDegree);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        loadBaseData(tag);
        this.amplifier = tag.getInt(AMPLIFIER_KEY);
        this.currentWave = tag.getInt(CURRENT_WAVE_KEY);
        this.waveDelay = tag.getInt(WAVE_DELAY_KEY);
        this.waveKillTotal = tag.getInt(WAVE_KILL_TOTAL_KEY);
        if (tag.contains(ELIMINATION_TARGET_KEY)) {
            this.eliminationTarget = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(tag.getString(ELIMINATION_TARGET_KEY)));
        }
        if (tag.contains(ELIMINATION_TARGET_STRING_KEY)) {
            this.eliminationTargetString = tag.getString(ELIMINATION_TARGET_STRING_KEY);
        } else if (this.eliminationTarget != null) {
            this.eliminationTargetString = this.eliminationTarget.getDescription().getString();
        } else {
            this.eliminationTargetString = "";
        }
        this.completedFirstDegree = tag.getBoolean(COMPLETED_FIRST_KEY);
        this.completedSecondDegree = tag.getBoolean(COMPLETED_SECOND_KEY);
        this.completedThirdDegree = tag.getBoolean(COMPLETED_THIRD_KEY);
    }

    @Override
    public ResourceLocation getId() {
        return ModRites.CARNAGE;
    }

    public Player getPlayer() {
        if (altar.getLevel() instanceof ServerLevel serverLevel) {
            return serverLevel.getPlayerByUUID(playerUUID);
        }
        return null;
    }

    @Override
    public boolean isRiteCompleted(Player player) {
        if (altar.hasPendingGemFusion()) {
            return completedFirstDegree;
        }
        return (completedThirdDegree && activeMobs.isEmpty());
    }

    @Override
    public void onEntityKilled(Player player, Entity entity) {
        if (!isRiteActive()) {
            return;
        }
        if (entity.getType() == eliminationTarget) {
            activeMobs.removeIf(mob -> mob == entity);

            if (isRiteCompleted(player)) {
                concludeRite(player);
            } else {
                syncToClient(player);
            }
        }
    }

    @Override
    public void onPlayerTick(Player player) {
        trackProgress(player);

        Iterator<Entity> mobIterator = activeMobs.iterator();
        while (mobIterator.hasNext()) {
            Entity mob = mobIterator.next();
            if (mob instanceof LivingEntity livingMob) {
                if (!livingMob.isAlive()) {
                    mobIterator.remove();
                } else {
                    if (livingMob instanceof Mob riteMob){
                        riteMob.setTarget(player);
                    }

                    if ((currentWave == firstDegreeThreshold || currentWave == secondDegreeThreshold || currentWave == thirdDegreeThreshold) && waveDelay <= 0) {
                        if (!livingMob.hasEffect(MobEffects.GLOWING)) {
                            livingMob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0));
                            //System.out.println("Come kill this guy! At: " + livingMob.getX() + ", " + livingMob.getY() + ", " + livingMob.getZ());
                        }
                    }
                }
            }
        }

        if (!completedFirstDegree) {
            if (waveDelay > 0 ) {
                //System.out.println("[Elimination Rite] Subtracting wave delay");
                waveDelay--;
            }

            if (currentWave < firstDegreeThreshold) {
                if (waveDelay <= 0 || (activeMobs.isEmpty() && currentWave != 0)) {
                    //System.out.println("[Elimination Rite] Wave " + currentWave + ": Timed out! Spawning wave: " + (currentWave + 1));
                    if (waveDelay <= 0) {
                        spawnWave(player);
                        currentWave++;
                    }

                    if (activeMobs.isEmpty()) {
                        if (currentWave != 0 && waveDelay > 200) {
                            waveDelay = 200;
                        }
                    }
                }
            } else if (currentWave == firstDegreeThreshold && (waveDelay <= 0 || activeMobs.isEmpty())) {
                if (activeMobs.isEmpty()) {
                    if (waveDelay > 200) {
                        waveDelay = 200;
                    }
                    completedFirstDegree = true;
                    if (altar.hasPendingGemFusion()) {
                        concludeRite(player);
                        return;
                    }
                    //System.out.println("[Elimination Rite] Wave " + currentWave + ": Done! First degree complete!");
                }
            }
        } else if (!completedSecondDegree) {
            if (waveDelay > 0 ) {
                waveDelay--;
            }

            if (currentWave < secondDegreeThreshold) {
                if (waveDelay <= 0 || activeMobs.isEmpty()) {
                    if (waveDelay <= 0) {
                        spawnWave(player);
                        currentWave++;
                    }

                    if (activeMobs.isEmpty()) {
                        if (waveDelay > 200) {
                            waveDelay = 200;
                        }
                    }
                }
            } else if (currentWave == secondDegreeThreshold && (waveDelay <= 0 || activeMobs.isEmpty())) {
                if (activeMobs.isEmpty()) {
                    if (waveDelay > 200) {
                        waveDelay = 200;
                    }
                    completedSecondDegree = true;
                    //System.out.println("[Elimination Rite] Wave " + currentWave + ": Done! First degree complete!");
                }
            }
        } else if (!completedThirdDegree) {
            if (waveDelay > 0 ) {
                waveDelay--;
            }

            if (currentWave < thirdDegreeThreshold) {
                if (waveDelay <= 0 || activeMobs.isEmpty()) {
                    if (waveDelay <= 0) {
                        spawnWave(player);
                        currentWave++;
                    }

                    if (activeMobs.isEmpty()) {
                        if (waveDelay > 200) {
                            waveDelay = 200;
                        }
                    }
                }
            } else if (currentWave == thirdDegreeThreshold && (waveDelay <= 0 || activeMobs.isEmpty())) {
                if (activeMobs.isEmpty()) {
                    if (waveDelay > 200) {
                        waveDelay = 200;
                    }
                    completedThirdDegree = true;
                    //System.out.println("[Elimination Rite] Wave " + currentWave + ": Done! First degree complete!");
                }
            }
        }

        if (isRiteCompleted(player)) {
            concludeRite(player);
        }
    }

    @Override
    public void trackProgress(Player player) {
        if (player != null) {
            syncToClient(player);
        }
    }

    @Override
    public void concludeRite(Player player) {
        finishRite(player, true, 0.05F);
    }

    @Override
    public int getCompletionDegree() {
        if (completedThirdDegree) {
            return 3;
        }
        if (completedSecondDegree) {
            return 2;
        }
        if (completedFirstDegree) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean canConcludeAtAltar() {
        int completionDegree = getCompletionDegree();
        return completionDegree >= 1 && completionDegree < 3;
    }

    @Override
    public boolean shouldClearOnPlayerExit() {
        return true;
    }

    @Override
    protected SyncRiteDataS2C buildSyncPacket(Player player) {
        return SyncRiteDataS2C.fromState(new CarnageClientRiteState(
                isRiteCompleted(player),
                eliminationTargetString,
                currentWave,
                activeMobs.size(),
                waveKillTotal,
                waveDelay,
                200
        ));
    }

    private EntityType<?> selectRandomTargetMob() {
        List<WeightedMob> mobList = MobList.ELIMINATION_TRIAL_MOBS;
        double totalWeight = mobList.stream().mapToDouble(WeightedMob::weight).sum();
        double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cumulativeWeight = 0.0;
        for (WeightedMob weightedMob : mobList) {
            cumulativeWeight += weightedMob.weight();
            if (randomValue <= cumulativeWeight) {
                this.eliminationTargetString = weightedMob.mobType().getDescription().getString();
                return weightedMob.mobType();
            }
        }
        return mobList.get(mobList.size() - 1).mobType();
    }

    private int calculateRequiredKillCount(int amplifier) {
        double variation = ThreadLocalRandom.current().nextDouble(0.75, 1.26);
        int base = 2 * (1 << (amplifier - 1));
        double waveMultiplier = 1.0 + ((double) currentWave / 4.0);
        int baseKillCount = (int) Math.round(base * variation * waveMultiplier);
        if (eliminationTarget.equals(EntityType.GHAST) ||
                eliminationTarget.equals(EntityType.SLIME) ||
                eliminationTarget.equals(EntityType.MAGMA_CUBE)) {
            baseKillCount = Math.max(1, baseKillCount / 2);
        } else if (eliminationTarget.equals(EntityType.RAVAGER)) {
            baseKillCount = Math.max(1, baseKillCount / 4);
        } else if (eliminationTarget.equals(EntityType.CAVE_SPIDER) ||
                eliminationTarget.equals(EntityType.VILLAGER)) {
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
            waveKillTotal = activeMobs.size();
            CursedPortalEntity.spawnSummoningPortalAtPos(level, altar, portalPos, mobsToSpawn);
            //System.out.println("[CarnageRite] Spawned wave with " + mobCount + " enemies via portal at " + portalPos);
            addWaveDelay((mobsToSpawn.size() + 1) * CursedPortalEntity.spawnDelay);
            //System.out.println("[CarnageRite] Time until next wave: " + (mobsToSpawn.size() + 1) * CursedPortalEntity.spawnDelay);
        }
    }

    private int getDefaultWaveDelay(){
        return 200 * (4 - amplifier);
    }

    private void addWaveDelay(int delayTicks) {
        waveDelay = getDefaultWaveDelay() + delayTicks;
    }

    public record WeightedMob(EntityType<?> mobType, double weight) { }
    
    public static class MobList {
        public static final List<WeightedMob> ELIMINATION_TRIAL_MOBS = Arrays.asList(
                new WeightedMob(EntityType.ZOMBIE, 20.0),
                new WeightedMob(EntityType.SKELETON, 20.0),
                new WeightedMob(EntityType.CREEPER, 15.0),
                new WeightedMob(EntityType.SPIDER, 15.0),
                new WeightedMob(EntityType.ENDERMAN, 15.0),
                new WeightedMob(EntityType.PILLAGER, 10.0),
                new WeightedMob(EntityType.VINDICATOR, 10.0),
                new WeightedMob(EntityType.RAVAGER, 2.0),
                new WeightedMob(EntityType.ZOMBIFIED_PIGLIN, 10.0),
                new WeightedMob(EntityType.DROWNED, 12.0),
                new WeightedMob(EntityType.GUARDIAN, 5.0),
                new WeightedMob(EntityType.PHANTOM, 12.0),
                new WeightedMob(EntityType.SLIME, 10.0),
                new WeightedMob(EntityType.MAGMA_CUBE, 10.0),
                new WeightedMob(EntityType.CAVE_SPIDER, 7.5),
                new WeightedMob(EntityType.WITCH, 5.0),
                new WeightedMob(EntityType.EVOKER, 3.0),
                new WeightedMob(EntityType.WITHER_SKELETON, 5.0),
                new WeightedMob(EntityType.GHAST, 3.0),
                new WeightedMob(EntityType.VILLAGER, 1.0),
                new WeightedMob(EntityType.BLAZE, 5.0),
                new WeightedMob(EntityType.HUSK, 15.0),
                new WeightedMob(EntityType.STRAY, 15.0)
        );
    }
}
