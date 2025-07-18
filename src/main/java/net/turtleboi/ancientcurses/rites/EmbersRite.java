package net.turtleboi.ancientcurses.rites;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.entity.CursedNodeEntity;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.item.items.DowsingRod;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.turtlecore.client.util.ParticleSpawnQueue;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;
import net.turtleboi.turtlecore.network.packet.util.SendParticlesS2C;
import net.turtleboi.turtlecore.particle.CoreParticles;

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
    public static final int feedTicks = 400;
    private final List<BlockPos> nodePositions  = new ArrayList<>();
    private final Map<BlockPos,Integer> nodeProgress = new HashMap<>();

    private final List<CursedNodeEntity> cursedNodes = new ArrayList<>();

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

        this.nodeRadius = Math.max(1, 9 / this.amplifier);
        Random random = new Random();
        BlockPos center = altar.getBlockPos();
        int sectors = 3 + amplifier;
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

            ServerLevel level = (ServerLevel) getServerPlayer().level();
            CursedNodeEntity  cursedNode = ModEntities.CURSED_NODE.get().create(level);
            if (cursedNode != null) {
                double x = node.getX() + 0.5;
                double z = node.getZ() + 0.5;
                double initialY = node.getY();
                BlockPos spawnPos = new BlockPos(node.getX(), node.getY(), node.getZ());
                BlockState spawnState = level.getBlockState(spawnPos);
                if (spawnState.isSolid() && !spawnState.is(BlockTags.LEAVES)) {
                    cursedNode.moveTo(x, initialY + 0.75, z, 0, 0);
                } else {
                    int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ());
                    cursedNode.moveTo(x, groundY + + 0.75, z, 0, 0);
                }

                cursedNode.setProgress(0);
                cursedNode.setNodeLifetime((int) riteDuration);
                cursedNode.setOwner(altar);
                level.addFreshEntity(cursedNode);
                cursedNodes.add(cursedNode);
            }
        }

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setCurseEffect(effect);
            riteData.setActiveRite(this);
        });

        if ((player.getMainHandItem().getItem() instanceof DowsingRod) && PlayerClientData.getItemUsed()) {
            ServerPlayer serverPlayer = getServerPlayer();
            serverPlayer.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
                Rite activeRite = riteData.getActiveRite();
                if (activeRite instanceof EmbersRite activeEmbersRite) {
                    DowsingRod.findNearestNode(serverPlayer, activeEmbersRite);
                }
            });
        }
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
    public CursedAltarBlockEntity getAltar() {
        return this.altar;
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

        for (CursedNodeEntity cursedNode : cursedNodes) {
            int progress = cursedNode.getProgress();
            if (progress >= feedTicks) continue;

            double cx = cursedNode.getBlockX() + 0.5;
            double cz = cursedNode.getBlockZ() + 0.5;
            int baseY  = cursedNode.getBlockY();

            for (int i = 0; i < (nodeRadius * 8); i++) {
                double angle = 2 * Math.PI * i / (nodeRadius * 8);
                double px = cx + Math.cos(angle) * nodeRadius;
                double py;
                double pz = cz + Math.sin(angle) * nodeRadius;
                int ix = Mth.floor(px);
                int iz = Mth.floor(pz);

                int particleY = Integer.MIN_VALUE;
                int yTop = Math.min(baseY + nodeRadius, level.getMaxBuildHeight() - 1);
                int yBot = Math.max(baseY - nodeRadius, level.getMinBuildHeight());

                for (int y = yBot; y <= yTop; y++) {
                    BlockPos belowPos = new BlockPos(ix, y, iz);
                    BlockPos abovePos = belowPos.above();
                    BlockState blockStateBelow = level.getBlockState(belowPos);
                    BlockState blockStateAbove = level.getBlockState(abovePos);
                    if (!blockStateBelow.isSolid() && !blockStateAbove.isSolid()) {
                        particleY = y;
                        break;
                    }
                }

                if (particleY != Integer.MIN_VALUE) {
                    py = particleY;
                } else {
                    py = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ix, iz);
                }

                CoreNetworking.sendToNear(new SendParticlesS2C(
                        ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                        px, py, pz,
                        0, 0.1, 0
                ), player);
            }
        }

        double feedRadiusSq = nodeRadius * nodeRadius;
        for (CursedNodeEntity cursedNode : cursedNodes) {
            int progress = cursedNode.getProgress();
            if (progress >= feedTicks) continue;

            double dx = player.getX() - (cursedNode.getX() + 0.5);
            double dz = player.getZ() - (cursedNode.getZ() + 0.5);
            if (dx*dx + dz*dz <= feedRadiusSq) {
                if (progress % 20 == 0) {
                    DamageSource damageSource = level.damageSources().indirectMagic(cursedNode, cursedNode);
                    int damageAmount = this.amplifier;
                    Vec3 preDelta = player.getDeltaMovement();
                    player.hurt(damageSource, damageAmount);
                    player.setDeltaMovement(preDelta);
                    spawnLifeDrainParticles(player, cursedNode, damageAmount * 2);
                }

                cursedNode.setProgress(progress + 1);
                if (progress + 1 >= feedTicks) {
                    currentDegree++;
                    CoreNetworking.sendToNear(new CameraShakeS2C(0.125F, 1000), player);
                    cursedNode.discard();

                    ServerPlayer serverPlayer = getServerPlayer();
                    serverPlayer.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
                        Rite activeRite = riteData.getActiveRite();
                        if (activeRite instanceof EmbersRite activeEmbersRite) {
                            DowsingRod.findNearestNode(serverPlayer, activeEmbersRite);
                        }
                    });
                }
            }
        }

        if (elapsedTime % 100 == 0) {
            for (int i = 0; i < cursedNodes.size(); i++) {
                CursedNodeEntity node = cursedNodes.get(i);
                int progress = node.getProgress();
                if (progress <= 0 || progress >= feedTicks) continue;

                float percentage = 100f * progress / feedTicks;
                BlockPos pos = node.blockPosition();
                String line = String.format(
                        "Node %d @ [x=%d, y=%d, z=%d]: %d / %d (%.0f%%)",
                        i + 1,
                        pos.getX(), pos.getY(), pos.getZ(),
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

    private static final Random RANDOM = new Random();
    private static final double SPEED = 0;//0.0125;
    private static final double SIZE = 0.025;
    private static final double SIZE_VARIATION = 0.025;
    private static final double POSITION_VARIATION = 0.1;
    private static final double SPEED_VARIATION = 0;//0.0125;

    private static void spawnLifeDrainParticles(LivingEntity targetEntity, CursedNodeEntity originEntity, float healAmount) {
        if (!originEntity.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) originEntity.level();
            double targetX = targetEntity.getX();
            double targetY = targetEntity.getY() + targetEntity.getEyeHeight() / 2.0;
            double targetZ = targetEntity.getZ();
            double originX = originEntity.getX();
            double originY = originEntity.getY() + originEntity.getEyeHeight() / 2.0;
            double originZ = originEntity.getZ();
            double dx = originX - targetX;
            double dy = originY - targetY;
            double dz = originZ - targetZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance <= 0) return;
            int particlesPerHealth = 4;
            int numberOfParticles = (int)(healAmount * particlesPerHealth * Math.max(1, distance));
            if (numberOfParticles <= 0) return;

            double interval = distance / numberOfParticles;

            for (int i = 0; i < numberOfParticles; i++) {
                final int idx = i;
                long delay = idx * 10L;
                double size = SIZE + RANDOM.nextDouble() * SIZE_VARIATION;
                ParticleSpawnQueue.schedule(delay, () -> {
                    double progress = interval * idx;
                    double xPos = targetX + dx * (progress / distance) + (RANDOM.nextDouble() - 0.5) * POSITION_VARIATION;
                    double yPos = targetY + dy * (progress / distance) + (RANDOM.nextDouble() - 0.5) * POSITION_VARIATION;
                    double zPos = targetZ + dz * (progress / distance) + (RANDOM.nextDouble() - 0.5) * POSITION_VARIATION;

                    double speedFactor = SPEED / distance;
                    double xSpeed = dx * speedFactor + (RANDOM.nextDouble() - 0.5) * SPEED_VARIATION;
                    double ySpeed = dy * speedFactor + (RANDOM.nextDouble() - 0.5) * SPEED_VARIATION;
                    double zSpeed = dz * speedFactor + (RANDOM.nextDouble() - 0.5) * SPEED_VARIATION;
                    serverLevel.sendParticles(CoreParticles.LIFE_DRAIN_PARTICLES.get(),
                            xPos, yPos, zPos,
                            1, xSpeed, ySpeed, zSpeed, size);
                });
            }
        }
    }

    public List<CursedNodeEntity> getNodes() {
        return Collections.unmodifiableList(cursedNodes);
    }

    public BlockPos findNearestIncompleteNode(ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        CursedNodeEntity nearestNode = null;
        double bestDistSq = Double.MAX_VALUE;
        for (CursedNodeEntity cursedNode : this.cursedNodes) {
            if (cursedNode.getProgress() >= feedTicks) continue;
            double d2 = cursedNode.blockPosition().distSqr(playerPos);
            if (d2 < bestDistSq) {
                bestDistSq = d2;
                nearestNode = cursedNode;
            }
        }
        return nearestNode != null ? nearestNode.blockPosition() : null;
    }

}
