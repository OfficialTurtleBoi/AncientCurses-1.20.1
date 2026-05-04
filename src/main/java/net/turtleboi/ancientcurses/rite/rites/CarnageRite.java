package net.turtleboi.ancientcurses.rite.rites;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.client.rites.CarnageClientRiteState;
import net.turtleboi.ancientcurses.config.AncientCursesConfig;
import net.turtleboi.ancientcurses.entity.entities.CursedPortalEntity;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.rite.AbstractRite;
import net.turtleboi.ancientcurses.rite.ModRites;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.SendParticlesS2C;

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
    private static final String ACTIVE_MOB_UUIDS_KEY = "ActiveMobUuids";
    private static final int BOUNDARY_PARTICLE_INTERVAL = 10;
    private static final double BOUNDARY_WARNING_BAND = 3.0D;

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
    private final Set<UUID> activeMobUuids = new LinkedHashSet<>();

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
        ListTag activeMobUuidTags = new ListTag();
        for (UUID activeMobUuid : activeMobUuids) {
            CompoundTag activeMobTag = new CompoundTag();
            activeMobTag.putUUID("UUID", activeMobUuid);
            activeMobUuidTags.add(activeMobTag);
        }
        tag.put(ACTIVE_MOB_UUIDS_KEY, activeMobUuidTags);
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
        this.activeMobs.clear();
        this.activeMobUuids.clear();
        ListTag activeMobUuidTags = tag.getList(ACTIVE_MOB_UUIDS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < activeMobUuidTags.size(); i++) {
            CompoundTag activeMobTag = activeMobUuidTags.getCompound(i);
            if (activeMobTag.hasUUID("UUID")) {
                this.activeMobUuids.add(activeMobTag.getUUID("UUID"));
            }
        }
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
        return getCompletionDegree() >= getMaxDegrees() && activeMobUuids.isEmpty();
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
            activeMobUuids.remove(entity.getUUID());

            if (isRiteCompleted(player)) {
                concludeRite(player);
            } else {
                syncToClient(player);
            }
        }
    }

    @Override
    public void onPlayerTick(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            renderDistanceBoundary(serverLevel, player);
            if (isOutsideRiteBoundary(player)) {
                failForLeavingArena(player);
                return;
            }
        }

        resolveActiveMobs();
        trackProgress(player);

        Iterator<Entity> mobIterator = activeMobs.iterator();
        while (mobIterator.hasNext()) {
            Entity mob = mobIterator.next();
            if (mob instanceof LivingEntity livingMob) {
                if (!livingMob.isAlive()) {
                    activeMobUuids.remove(livingMob.getUUID());
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
                boolean waveCleared = activeMobUuids.isEmpty();
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
    protected float getRiteEndShakeAmount() {
        return 0.05F;
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
                activeMobUuids.size(),
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
        return Math.max(1, Mth.ceil(baseKillCount * AncientCursesConfig.CARNAGE_RITE_SPAWN_MULTIPLIER.get()));
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
            for (Entity mobToSpawn : mobsToSpawn) {
                activeMobUuids.add(mobToSpawn.getUUID());
            }
            waveKillTotal = activeMobUuids.size();
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
        return Mth.clamp((float) activeMobUuids.size() / (float) waveKillTotal, 0.0F, 1.0F);
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

    private double getMaxRiteDistance() {
        return switch (Math.max(1, amplifier)) {
            case 1 -> 18.0D;
            case 2 -> 24.0D;
            default -> 30.0D;
        };
    }

    private boolean isOutsideRiteBoundary(Player player) {
        return getHorizontalDistanceSqrToAltar(player) > getMaxRiteDistance() * getMaxRiteDistance();
    }

    private void renderDistanceBoundary(ServerLevel level, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.tickCount % BOUNDARY_PARTICLE_INTERVAL != 0) {
            return;
        }

        double maxDistance = getMaxRiteDistance();
        double horizontalDistance = Math.sqrt(getHorizontalDistanceSqrToAltar(player));
        if (horizontalDistance < maxDistance - BOUNDARY_WARNING_BAND) {
            return;
        }

        double cx = altar.getBlockPos().getX() + 0.5D;
        double cz = altar.getBlockPos().getZ() + 0.5D;
        int baseY = altar.getBlockPos().getY();
        int particleCount = Math.max(24, Mth.ceil(maxDistance * 8.0D));

        for (int i = 0; i < particleCount; i++) {
            double angle = Mth.TWO_PI * i / particleCount;
            double px = cx + Math.cos(angle) * maxDistance;
            double pz = cz + Math.sin(angle) * maxDistance;
            Double py = findBoundaryParticleY(level, baseY, px, pz, maxDistance);
            if (py == null) {
                continue;
            }

            CoreNetworking.sendToNear(new SendParticlesS2C(
                    ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                    px, py, pz,
                    0, 0.1D, 0
            ), serverPlayer);
        }
    }

    private Double findBoundaryParticleY(ServerLevel level, int baseY, double particleX, double particleZ, double radius) {
        int ix = Mth.floor(particleX);
        int iz = Mth.floor(particleZ);
        int yBottom = Math.max(baseY - Mth.ceil(radius), level.getMinBuildHeight());
        int yTop = Math.min(baseY + Mth.ceil(radius), level.getMaxBuildHeight() - 1);

        for (int y = baseY; y >= yBottom; y--) {
            if (level.getBlockState(new BlockPos(ix, y, iz)).isSolid()) {
                return (double) (y + 1);
            }
        }

        for (int y = baseY + 1; y <= yTop; y++) {
            BlockPos checkPos = new BlockPos(ix, y, iz);
            if (level.getBlockState(checkPos).isSolid() && !level.getBlockState(checkPos.above()).isSolid()) {
                return (double) (y + 1);
            }
        }

        return null;
    }

    private double getHorizontalDistanceSqrToAltar(Player player) {
        double dx = player.getX() - (altar.getBlockPos().getX() + 0.5D);
        double dz = player.getZ() - (altar.getBlockPos().getZ() + 0.5D);
        return (dx * dx) + (dz * dz);
    }

    private void failForLeavingArena(Player player) {
        altar.removePlayerFromRite(player);
        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> riteData.clearPlayerCurse());

        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(SyncRiteDataS2C.none(), serverPlayer);
        }

        for (MobEffectInstance effectInstance : new ArrayList<>(player.getActiveEffects())) {
            MobEffect effect = effectInstance.getEffect();
            if (ModEffects.isCurseEffect(effect)) {
                player.removeEffect(effect);
            }
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            onRiteFailed(player);
        }
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

    private void resolveActiveMobs() {
        activeMobs.clear();
        if (!(altar.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        Iterator<UUID> trackedMobIterator = activeMobUuids.iterator();
        while (trackedMobIterator.hasNext()) {
            UUID trackedMobUuid = trackedMobIterator.next();
            Entity trackedMob = serverLevel.getEntity(trackedMobUuid);
            if (trackedMob instanceof LivingEntity livingMob) {
                if (!livingMob.isAlive()) {
                    trackedMobIterator.remove();
                } else {
                    activeMobs.add(trackedMob);
                }
            }
        }
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
