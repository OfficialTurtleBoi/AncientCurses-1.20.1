package net.turtleboi.ancientcurses.rites;

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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CarnageRite implements Rite {
    private UUID playerUUID;
    private MobEffect effect;
    private int amplifier;
    private CursedAltarBlockEntity altar;
    private boolean completed;

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
        this.playerUUID = player.getUUID();
        this.effect = effect;
        this.amplifier = amplifier + 1;
        this.altar = altar;
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
        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setCurseEffect(effect);
            riteData.setActiveRite(this);
            if (riteData.getCurrentWave() == 0) {
                riteData.setCurrentWave(0);
            }
            this.currentWave = riteData.getCurrentWave();
        });
    }

    public CarnageRite(CursedAltarBlockEntity altar) {
        this.altar = altar;
        this.completed = false;
    }

    public boolean isRiteActive() {
        return altar.getPlayerRite(playerUUID) != null;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putUUID("PlayerUUID", playerUUID);
        tag.putString("Effect", Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getKey(effect)).toString());
        tag.putString("EliminationTarget", Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(eliminationTarget)).toString());
        tag.putString("EliminationTargetString", eliminationTargetString != null ? eliminationTargetString : "");
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
        this.completed = tag.getBoolean("Completed");
    }

    @Override
    public String getType() {
        return Rite.carnageRite;
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
    public boolean isRiteCompleted(Player player) {
        return (completedThirdDegree && activeMobs.isEmpty());
    }

    @Override
    public void onEntityKilled(Player player, Entity entity) {
        if (!isRiteActive()) {
            return;
        }
        if (entity.getType() == eliminationTarget) {
            player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
                int newKills = riteData.getCurrentWave() + 1;
                riteData.setCurrentWave(newKills);
                activeMobs.removeIf(mob -> mob == entity);
            });

            if (isRiteCompleted(player)) {
                concludeRite(player);
            } else {
                trackProgress(player);
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
            player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
                BlockPos altarPos = riteData.getCurrentAltarPos();
                ResourceKey<Level> altarDimension = riteData.getAltarDimension();
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
                                //        + altarDimension.location() + " reloaded rite data for player "
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

            //System.out.println("Rite Type: " + Rite.eliminationRite);
            //System.out.println("Rite Completed: " + isRiteCompleted(player));
            //System.out.println("Elimination Target: " + eliminationTarget);
            //System.out.println("Elimination Target String: " + eliminationTargetString);
            //System.out.println("Current Wave: " + currentWave);
            //System.out.println("Kills Remaining: " + activeMobs.size());
            //System.out.println("Total kill required this wave: " + waveKillTotal);
            //System.out.println("Time until next wave: " + waveDelay);
            //System.out.println("Total wave delay: " + getDefaultWaveDelay());

            ModNetworking.sendToPlayer(
                    new SyncRiteDataS2C(
                            Rite.carnageRite,
                            isRiteCompleted(player),
                            eliminationTargetString,
                            currentWave,
                            activeMobs.size(),
                            waveKillTotal,
                            waveDelay,
                            200,
                            "",
                            0,
                            0),
                    (ServerPlayer) player);

        }
    }

    @Override
    public void concludeRite(Player player) {
        //player.displayClientMessage(Component.literal("You have completed the elimination rite! Collect your reward").withStyle(ChatFormatting.GREEN), true);
        ModNetworking.sendToPlayer(
                new SyncRiteDataS2C(
                        Rite.carnageRite,
                        isRiteCompleted(player),
                        eliminationTargetString,
                        0,
                        0,
                        0,
                        0,
                        0,
                        "",
                        0,
                        0),
                (ServerPlayer) player);

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(PlayerRiteDataCapability::clearCurseEffect);

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
        altar.setPlayerRiteCompleted(player);
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
