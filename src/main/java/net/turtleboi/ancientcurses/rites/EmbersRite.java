package net.turtleboi.ancientcurses.rites;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;
import net.turtleboi.turtlecore.network.packet.util.SendParticlesS2C;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EmbersRite implements Rite {
    private UUID playerUUID;
    private MobEffect effect;
    private int amplifier;
    private CursedAltarBlockEntity altar;
    private boolean completed;

    private long elapsedTime;
    private long riteDuration = 3600;
    private int portalCooldown = 0;
    private int nodeRadius;
    private static final int spawnInterval = 200;
    private static final int feedTicks = 400;
    private final List<BlockPos> nodePositions  = new ArrayList<>();
    private final Map<BlockPos,Integer> nodeProgress = new HashMap<>();
    private int currentDegree = 0;
    public boolean completedFirstDegree;
    public boolean completedSecondDegree;
    public boolean completedThirdDegree;

    public static final String riteDurationTotal = "RiteDuration";
    public static final String riteDurationElapsed = "RiteElapsedTime";

    public EmbersRite(Player player, MobEffect effect, int amplifier, long riteDuration, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.altar = altar;
        this.effect = effect;
        this.riteDuration = riteDuration;
        //System.out.println(Component.literal("Setting rite duration to: " + this.riteDuration));
        //System.out.println(Component.literal("Setting elapsed time to: " + this.elapsedTime));
        this.amplifier = amplifier + 1;
        this.completed = false;
        this.portalCooldown = 0;

        this.nodeRadius = Math.max(1, 4 - amplifier);
        Random random = new Random();
        BlockPos center = altar.getBlockPos();
        //int sectors = 3 + amplifier;
        int sectors = 32;//+ amplifier;
        double minDistance = 8.0 * (1 + amplifier);
        double maxDistance = 64.0 * (1 + amplifier);
        for (int i = 0; i < sectors; i++) {
            double sectorStart = i * (360.0 / sectors);
            double randomOffset = random.nextDouble() * (360.0 / sectors);
            double degreeOffset = sectorStart + randomOffset;
            double radians = Math.toRadians(degreeOffset);

            double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);

            int dx = (int) Math.round(Math.cos(radians) * distance);
            int dz = (int) Math.round(Math.sin(radians) * distance);

            BlockPos node = new BlockPos(
                    center.getX() + dx,
                    center.getY(),
                    center.getZ() + dz);
            nodePositions.add(node);
            nodeProgress.put(node, 0);
        }

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setCurseEffect(effect);
            riteData.setActiveRite(this);
        });
    }

    public EmbersRite(CursedAltarBlockEntity altar) {
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
        tag.putLong(riteDurationElapsed, elapsedTime);
        tag.putLong(riteDurationTotal, riteDuration);
        tag.putBoolean("Completed", completed);

        ListTag nodesTag = new ListTag();
        for (BlockPos pos : nodePositions) {
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.putLong("Pos", pos.asLong());
            nodeTag.putInt("Progress", nodeProgress.getOrDefault(pos, 0));
            nodesTag.add(nodeTag);
        }
        tag.put("Nodes", nodesTag);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        String effectName = tag.getString("Effect");
        this.effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        this.elapsedTime = tag.getLong(riteDurationElapsed);
        this.riteDuration = tag.getLong(riteDurationTotal);
        this.completed = tag.getBoolean("Completed");

        this.nodePositions.clear();
        this.nodeProgress.clear();
        ListTag nodesTag = tag.getList("Nodes", Tag.TAG_COMPOUND);
        for (int i = 0; i < nodesTag.size(); i++) {
            CompoundTag nodeTag = nodesTag.getCompound(i);
            BlockPos pos = BlockPos.of(nodeTag.getLong("Pos"));
            int    prog = nodeTag.getInt("Progress");
            nodePositions.add(pos);
            nodeProgress.put(pos, prog);
        }

        if (nodePositions.isEmpty() && amplifier > 0) {
            Random rnd = new Random();
            BlockPos center = altar.getBlockPos();
            for (int i = 0; i < amplifier; i++) {
                int dx = rnd.nextInt(61) - 30;
                int dz = rnd.nextInt(61) - 30;
                BlockPos pos = new BlockPos(center.getX() + dx, center.getY(), center.getZ() + dz);
                nodePositions.add(pos);
                nodeProgress.put(pos, 0);
            }
        }
    }

    @Override
    public String getType() {
        return Rite.embersRite;
    }

    @Override
    public void setAltar(CursedAltarBlockEntity altar) {
        this.altar = altar;
    }

    @Override
    public MobEffect getEffect() {
        return this.effect;
    }

    public ServerPlayer getServerPlayer() {
        if (altar.getLevel() instanceof ServerLevel serverLevel) {
            return (ServerPlayer) serverLevel.getPlayerByUUID(playerUUID);
        }
        return null;
    }

    @Override
    public boolean isRiteCompleted(Player player) {
        //System.out.println(Component.literal("Completed rite!"));
        return currentDegree >= 3;
    }

    @Override
    public void onPlayerTick(Player player) {
        if (!isRiteActive()) {
            return;
        }

        if(nodePositions.isEmpty()){
            return;
        }

        ServerLevel level = (ServerLevel) getServerPlayer().level();
        elapsedTime++;
        portalCooldown++;

        if (portalCooldown >= spawnInterval) {
            portalCooldown = 0;
            for (BlockPos node : nodePositions) {
                Mob placeholder = EntityType.VEX.create(level);
                if (placeholder != null) {
                    placeholder.moveTo(node.getX()+0.5, node.getY()+1, node.getZ()+0.5, 0, 0);
                    level.addFreshEntity(placeholder);
                }
            }
        }

        for (BlockPos node : nodePositions) {
            int progress = nodeProgress.getOrDefault(node, 0);
            if (progress >= feedTicks) continue;

            double cx = node.getX() + 0.5;
            double cy = node.getY() + 1.0;
            double cz = node.getZ() + 0.5;
            for (int i = 0; i < 20; i++) {
                double angle = 2 * Math.PI * i / 20;
                double px = cx + Math.cos(angle) * nodeRadius;
                double pz = cz + Math.sin(angle) * nodeRadius;
                CoreNetworking.sendToNear(new SendParticlesS2C(
                        ModParticleTypes.CURSED_FLAME_PARTICLE.get(), px, cy, pz, 0, 0.1, 0
                ), player);
            }
        }


        double feedRadiusSq = nodeRadius * nodeRadius;
        for (BlockPos node : nodePositions) {
            int progress = nodeProgress.getOrDefault(node, 0);
            if (progress >= feedTicks) continue;

            double dx = player.getX() - (node.getX() + 0.5);
            double dz = player.getZ() - (node.getZ() + 0.5);
            if (dx*dx + dz*dz <= feedRadiusSq) {
                progress = nodeProgress.merge(node, 1, Integer::sum);
                if (progress >= feedTicks) {
                    currentDegree++;
                    CoreNetworking.sendToNear(new CameraShakeS2C(0.125F, 1000), player);
                }
            }
        }

        if (elapsedTime % 100 == 0) {
            for (int i = 0; i < nodePositions.size(); i++) {
                BlockPos blockPos = nodePositions.get(i);
                int progress = nodeProgress.getOrDefault(blockPos, 0);
                float percentage = 100f * progress / (float) feedTicks;

                if (progress <= 0 || progress >= feedTicks) {
                    continue;
                }

                String line = String.format(
                        "Node %d @ [x=%d, y=%d, z=%d]: %d / %d (%.0f%%)",
                        i + 1,
                        blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                        progress, feedTicks,
                        percentage
                );

                getServerPlayer().sendSystemMessage(Component.literal(line));
            }
        }

        boolean allMature = nodeProgress.values().stream()
                .allMatch(p -> p >= feedTicks);

        if (allMature) {
            concludeRite(player);
            return;
        }

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setSurvivalTicks((int) elapsedTime);
        });

        if (isRiteCompleted(player)) {
            concludeRite(player);
        }

        trackProgress(player);
    }

    @Override
    public void trackProgress(Player player) {
        if (!isRiteActive()) {
            return;
        }

        ServerPlayer serverPlayer = getServerPlayer();
        if (serverPlayer == null) return;

        if (player != null) {
            elapsedTime++;
            //System.out.println("Setting elapsed time to: " + elapsedTime);
            float progressPercentage = Math.min((float) elapsedTime / riteDuration, 1.0f);
            ModNetworking.sendToPlayer(
                    new SyncRiteDataS2C(
                            Rite.embersRite,
                            isRiteCompleted(player),
                            "",
                            0,
                            0,
                            0,
                            elapsedTime,
                            riteDuration,
                            "",
                            0,
                            0),
                    serverPlayer);
            //player.displayClientMessage(Component.literal(String.format("Rite progress: %.2f%% complete", progressPercentage * 100))
            //        .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    @Override
    public void concludeRite(Player player) {
        ServerPlayer serverPlayer = getServerPlayer();
        if (serverPlayer == null) return;

        //player.displayClientMessage(Component.literal("You have survived the rite! Collect your reward").withStyle(ChatFormatting.GREEN), true);
        ModNetworking.sendToPlayer(
                new SyncRiteDataS2C(
                        Rite.embersRite,
                        isRiteCompleted(player),
                        "",
                        0,
                        0,
                        0,
                        riteDuration,
                        riteDuration,
                        "",
                        0,
                        0),
                serverPlayer);
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

        CoreNetworking.sendToNear((new CameraShakeS2C(0.125F, 1000)), player);
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
        altar.setPlayerRiteCompleted(player);
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

    private List<Entity> buildPortalSpawnList(ServerLevel level, int mobCount) {
        List<Entity> mobs = new ArrayList<>();
        for (int i = 0; i < mobCount; i++) {
            Entity mob = selectRandomTargetMob().create(level);
            if (mob != null) {
                mobs.add(mob);
            }
        }
        return mobs;
    }

    private EntityType<?> selectRandomTargetMob() {
        List<CarnageRite.WeightedMob> mobList = CarnageRite.MobList.ELIMINATION_TRIAL_MOBS;
        double totalWeight = mobList.stream().mapToDouble(CarnageRite.WeightedMob::weight).sum();
        double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cumulativeWeight = 0.0;
        for (CarnageRite.WeightedMob weightedMob : mobList) {
            cumulativeWeight += weightedMob.weight();
            if (randomValue <= cumulativeWeight) {
                return weightedMob.mobType();
            }
        }
        return mobList.get(mobList.size() - 1).mobType();
    }
}
