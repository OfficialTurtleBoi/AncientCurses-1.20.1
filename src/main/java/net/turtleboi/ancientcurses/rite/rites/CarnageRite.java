package net.turtleboi.ancientcurses.rite.rites;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import net.turtleboi.ancientcurses.entity.entities.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.ancientcurses.rite.AbstractRite;
import net.turtleboi.ancientcurses.rite.ModRites;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CarnageRite extends AbstractRite {
    private static final String AMPLIFIER_KEY = "Amplifier";
    private static final String CURRENT_DEGREE_KEY = "CurrentDegree";
    private static final String CURRENT_WAVE_KEY = "CurrentWave";
    private static final String WAVE_DELAY_KEY = "WaveDelay";
    private static final String WAVE_DELAY_TOTAL_KEY = "WaveDelayTotal";
    private static final String WAVE_KILL_TOTAL_KEY = "WaveKillTotal";
    private static final String REFILL_START_MAIN_PROGRESS_KEY = "RefillStartMainProgress";
    private static final String REFILL_START_SUB_PROGRESS_KEY = "RefillStartSubProgress";
    private static final String ELIMINATION_TARGET_KEY = "EliminationTarget";
    private static final String ELIMINATION_TARGET_STRING_KEY = "EliminationTargetString";

    private int amplifier;

    private int currentDegree;
    private int currentWave;
    private int waveDelay;
    private int waveDelayTotal;
    private float refillStartMainProgress;
    private float refillStartSubProgress;

    private EntityType<?> eliminationTarget;
    private String eliminationTargetString;
    private int waveKillTotal;

    private List<Entity> activeMobs = new ArrayList<>();

    public static final String eliminationCount = "EliminationCount";
    public static final String eliminationRequirement = "EliminationRequirement";

    public CarnageRite(Player player, MobEffect effect, int amplifier, CursedAltarBlockEntity altar) {
        super(altar);
        this.playerUUID = player.getUUID();
        this.effect = effect;
        this.amplifier = amplifier + 1;
        setMaxDegrees(getMaxDegreesForTier(this.amplifier));
        this.eliminationTarget = selectRandomTargetMob();
        this.currentDegree = 0;
        this.currentWave = 0;
        setWaveDelay(getDefaultWaveDelay());
        this.refillStartMainProgress = 0.0F;
        this.refillStartSubProgress = 0.0F;
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
        tag.putInt(CURRENT_DEGREE_KEY, currentDegree);
        tag.putInt(CURRENT_WAVE_KEY, currentWave);
        tag.putInt(WAVE_DELAY_KEY, waveDelay);
        tag.putInt(WAVE_DELAY_TOTAL_KEY, waveDelayTotal);
        tag.putInt(WAVE_KILL_TOTAL_KEY, waveKillTotal);
        tag.putFloat(REFILL_START_MAIN_PROGRESS_KEY, refillStartMainProgress);
        tag.putFloat(REFILL_START_SUB_PROGRESS_KEY, refillStartSubProgress);
        if (eliminationTarget != null) {
            tag.putString(ELIMINATION_TARGET_KEY, Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(eliminationTarget)).toString());
        }
        tag.putString(ELIMINATION_TARGET_STRING_KEY, eliminationTargetString != null ? eliminationTargetString : "");
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        loadBaseData(tag);
        this.amplifier = tag.getInt(AMPLIFIER_KEY);
        if (!tag.contains(MAX_DEGREES_KEY)) {
            setMaxDegrees(getMaxDegreesForTier(this.amplifier));
        }
        this.currentDegree = tag.getInt(CURRENT_DEGREE_KEY);
        this.currentWave = tag.getInt(CURRENT_WAVE_KEY);
        this.waveDelay = tag.getInt(WAVE_DELAY_KEY);
        this.waveDelayTotal = tag.contains(WAVE_DELAY_TOTAL_KEY)
                ? tag.getInt(WAVE_DELAY_TOTAL_KEY)
                : Math.max(this.waveDelay, getDefaultWaveDelay());
        this.waveKillTotal = tag.getInt(WAVE_KILL_TOTAL_KEY);
        this.refillStartMainProgress = tag.contains(REFILL_START_MAIN_PROGRESS_KEY)
                ? tag.getFloat(REFILL_START_MAIN_PROGRESS_KEY)
                : 0.0F;
        this.refillStartSubProgress = tag.contains(REFILL_START_SUB_PROGRESS_KEY)
                ? tag.getFloat(REFILL_START_SUB_PROGRESS_KEY)
                : 0.0F;
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
        return getCompletionDegree() >= getMaxDegrees() && activeMobs.isEmpty();
    }

    @Override
    public boolean canConcludeAtAltar() {
        int completionDegree = getCompletionDegree();
        return completionDegree >= getMinimumCompletionDegrees() && completionDegree < getMaxDegrees();
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

                    if (isDegreeThresholdWave(currentWave) && waveDelay <= 0) {
                        if (!livingMob.hasEffect(MobEffects.GLOWING)) {
                            livingMob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 72000, 0));
                            //System.out.println("Come kill this guy! At: " + livingMob.getX() + ", " + livingMob.getY() + ", " + livingMob.getZ());
                        }
                    }
                }
            }
        }

        if (getCompletionDegree() < getMaxDegrees()) {
            if (waveDelay > 0) {
                waveDelay--;
            }

            if (!isWaveActiveForCurrentDegree()) {
                if (waveDelay <= 0) {
                    spawnWave(player);
                    currentWave++;
                }
            } else {
                boolean waveCleared = activeMobs.isEmpty();
                boolean isOptionalDegree = currentDegree >= getMinimumCompletionDegrees();
                boolean isFinalDegree = currentDegree == getMaxDegrees() - 1;
                boolean optionalWaveExpired = isOptionalDegree && !isFinalDegree && waveDelay <= 0;

                if (waveCleared || optionalWaveExpired) {
                    finishCurrentDegreeWave();
                    currentDegree++;
                    if (getCompletionDegree() >= getMaxDegrees()) {
                        concludeRite(player);
                        return;
                    }
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
        return Math.min(currentDegree, getMaxDegrees());
    }

    @Override
    public boolean shouldClearOnPlayerExit() {
        return true;
    }

    @Override
    protected SyncRiteDataS2C buildSyncPacket(Player player) {
        int completedDegrees = getCompletionDegree();
        int totalDegrees = getDisplayDegreeCount();
        int activeDegreeIndex = isRiteCompleted(player) ? -1 : Math.min(completedDegrees, totalDegrees - 1);
        return SyncRiteDataS2C.fromState(new CarnageClientRiteState(
                isRiteCompleted(player),
                eliminationTargetString,
                currentWave,
                activeMobs.size(),
                waveKillTotal,
                getMainBarProgress(),
                getSubBarProgress(),
                getCurrentDegreeWaveProgress(),
                totalDegrees,
                getMinimumCompletionDegrees(),
                completedDegrees,
                activeDegreeIndex
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
            refillStartMainProgress = 0.0F;
            refillStartSubProgress = 0.0F;
            CursedPortalEntity.spawnSummoningPortalAtPos(level, altar, portalPos, mobsToSpawn);
            //System.out.println("[CarnageRite] Spawned wave with " + mobCount + " enemies via portal at " + portalPos);
            addWaveDelay((mobsToSpawn.size() + 1) * CursedPortalEntity.spawnDelay);
            //System.out.println("[CarnageRite] Time until next wave: " + (mobsToSpawn.size() + 1) * CursedPortalEntity.spawnDelay);
        }
    }

    private int getDefaultWaveDelay(){
        return switch (Math.max(1, amplifier)) {
            case 1 -> 240;
            case 2 -> 140;
            default -> 60;
        };
    }

    private void addWaveDelay(int delayTicks) {
        setWaveDelay(getDefaultWaveDelay() + delayTicks);
    }

    private void setWaveDelay(int delayTicks) {
        waveDelay = Math.max(0, delayTicks);
        waveDelayTotal = waveDelay;
    }

    private boolean isWaveActiveForCurrentDegree() {
        return currentWave > currentDegree;
    }

    private void finishCurrentDegreeWave() {
        refillStartMainProgress = getCurrentDegreeWaveProgress();
        refillStartSubProgress = getCurrentDelayProgress();
        setWaveDelay(getDefaultWaveDelay());
    }

    private float getMainBarProgress() {
        if (isRiteCompleted(getPlayer())) {
            return 1.0F;
        }

        if (!isWaveActiveForCurrentDegree()) {
            return Mth.clamp(Mth.lerp(getIntermissionProgress(), refillStartMainProgress, 1.0F), 0.0F, 1.0F);
        }

        return getCurrentDegreeWaveProgress();
    }

    private float getSubBarProgress() {
        if (!isWaveActiveForCurrentDegree()) {
            return Mth.clamp(Mth.lerp(getIntermissionProgress(), refillStartSubProgress, 1.0F), 0.0F, 1.0F);
        }

        return getCurrentDelayProgress();
    }

    private float getCurrentDegreeWaveProgress() {
        if (waveKillTotal <= 0) {
            return 0.0F;
        }
        return Mth.clamp((float) activeMobs.size() / (float) waveKillTotal, 0.0F, 1.0F);
    }

    private float getCurrentDelayProgress() {
        if (waveDelayTotal <= 0) {
            return 0.0F;
        }
        return Mth.clamp((float) waveDelay / (float) waveDelayTotal, 0.0F, 1.0F);
    }

    private float getIntermissionProgress() {
        if (waveDelayTotal <= 0) {
            return 1.0F;
        }
        return Mth.clamp(1.0F - ((float) waveDelay / (float) waveDelayTotal), 0.0F, 1.0F);
    }

    private int getWaveThresholdForDegree(int degreeIndex) {
        return degreeIndex + 1;
    }

    private boolean isDegreeThresholdWave(int wave) {
        return wave > 0 && wave <= getMaxDegrees();
    }

    private int getMinimumCompletionDegrees() {
        return getMinimumCompletionDegreesForTier(amplifier);
    }

    private static int getMaxDegreesForTier(int tier) {
        return switch (Math.max(1, tier)) {
            case 1 -> 3;
            case 2 -> 4;
            default -> 5;
        };
    }

    private static int getMinimumCompletionDegreesForTier(int tier) {
        return switch (Math.max(1, tier)) {
            case 1 -> 1;
            case 2 -> 2;
            default -> 3;
        };
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
