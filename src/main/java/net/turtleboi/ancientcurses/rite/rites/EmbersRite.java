package net.turtleboi.ancientcurses.rite.rites;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.client.rites.EmbersClientRiteState;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.entity.entities.CursedNodeEntity;
import net.turtleboi.ancientcurses.item.items.DowsingRod;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.rite.AbstractRite;
import net.turtleboi.ancientcurses.rite.ModRites;
import net.turtleboi.turtlecore.client.util.ParticleSpawnQueue;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;
import net.turtleboi.turtlecore.network.packet.util.SendParticlesS2C;
import net.turtleboi.turtlecore.particle.CoreParticles;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EmbersRite extends AbstractRite {
    private static final String ELAPSED_TIME_KEY = "ElapsedTime";
    private static final String RITE_DURATION_KEY = "RiteDuration";
    private static final String PORTAL_COOLDOWN_KEY = "PortalCooldown";
    private static final String NODE_RADIUS_KEY = "NodeRadius";
    private static final String CURRENT_DEGREE_KEY = "CurrentDegree";

    private int amplifier;

    private long elapsedTime;
    private long riteDuration = 3600;
    private int portalCooldown = 0;
    private int nodeRadius;
    private static final int spawnInterval = 200;
    public static final int feedTicks = 400;
    private final List<BlockPos> nodePositions  = new ArrayList<>();
    private final Map<BlockPos,Integer> nodeProgress = new HashMap<>();

    private final List<CursedNodeEntity> cursedNodes = new ArrayList<>();
    private final Map<BlockPos, BlockState> savedBlocks = new HashMap<>();
    private final Map<BlockPos, CompoundTag> savedBlockData = new HashMap<>();

    private final List<List<BlockPos>> nodeClearQueues = new ArrayList<>();
    private final List<Integer> nodeClearIndices = new ArrayList<>();
    private final List<List<BlockPos>> nodeOwnedBlocks = new ArrayList<>();
    private final Map<Integer, List<BlockPos>> pendingNodeRestoreQueues = new LinkedHashMap<>();
    private final Map<Integer, Integer> pendingNodeRestoreIndices = new HashMap<>();
    private final Set<BlockPos> clearQueueSet = new HashSet<>();
    private final Set<Integer> restoredNodeIndices = new HashSet<>();
    private boolean isRestoring = false;
    private List<BlockPos> restoreQueue = new ArrayList<>();
    private int restoreQueueIndex = 0;
    private static final int CLEAR_PER_TICK = 10;
    private static final int RESTORE_PER_TICK = 2;
    private static final double NODE_RESTORE_MARGIN = 1.0D;

    private int currentDegree = 0;

    public static final String riteDurationTotal = "RiteDuration";
    public static final String riteDurationElapsed = "RiteElapsedTime";

    public EmbersRite(Player player, MobEffect effect, int amplifier, long riteDuration, CursedAltarBlockEntity altar) {
        super(altar);
        this.playerUUID = player.getUUID();
        this.effect = effect;
        this.riteDuration = riteDuration;
        this.amplifier = amplifier + 1;
        this.completed = false;
        this.portalCooldown = 0;

        this.nodeRadius = getNodeRadiusForTier(this.amplifier);
        setMaxDegrees(getMaxDegreesForTier(this.amplifier));
        syncNodeCountToMaxDegrees();

        ServerPlayer serverPlayer = getServerPlayer();
        if (serverPlayer != null && (player.getMainHandItem().getItem() instanceof DowsingRod) && DowsingRod.UseState.isActive(serverPlayer)) {
            sendGuidance(serverPlayer, getGuidanceTarget(serverPlayer));
        }
    }

    public EmbersRite(CursedAltarBlockEntity altar) {
        super(altar);
    }

    public static int getMaxDegreesForTier(int tier) {
        return switch (Math.max(1, tier)) {
            case 1 -> 3;
            case 2 -> 4;
            default -> 5;
        };
    }

    private static int getNodeRadiusForTier(int tier) {
        return switch (Math.max(1, tier)) {
            case 1 -> 9;
            case 2 -> 6;
            default -> 4;
        };
    }

    public boolean isRiteActive() {
        return altar.getPlayerRite(playerUUID) != null;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        saveBaseData(tag);
        tag.putInt("Amplifier", amplifier);
        tag.putLong(ELAPSED_TIME_KEY, elapsedTime);
        tag.putLong(RITE_DURATION_KEY, riteDuration);
        tag.putInt(PORTAL_COOLDOWN_KEY, portalCooldown);
        tag.putInt(NODE_RADIUS_KEY, nodeRadius);
        tag.putInt(CURRENT_DEGREE_KEY, currentDegree);

        ListTag nodesTag = new ListTag();
        for (BlockPos pos : nodePositions) {
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.putLong("Pos", pos.asLong());
            nodeTag.putInt("Progress", nodeProgress.getOrDefault(pos, 0));
            nodesTag.add(nodeTag);
        }
        tag.put("Nodes", nodesTag);

        ListTag savedBlocksTag = new ListTag();
        for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
            CompoundTag blockTag = new CompoundTag();
            blockTag.putLong("Pos", entry.getKey().asLong());
            blockTag.put("State", NbtUtils.writeBlockState(entry.getValue()));
            CompoundTag beData = savedBlockData.get(entry.getKey());
            if (beData != null) {
                blockTag.put("BlockEntityData", beData);
            }
            savedBlocksTag.add(blockTag);
        }
        tag.put("SavedBlocks", savedBlocksTag);

        ListTag allQueuesTag = new ListTag();
        for (int n = 0; n < nodeClearQueues.size(); n++) {
            List<BlockPos> queue = nodeClearQueues.get(n);
            int idx = nodeClearIndices.get(n);
            long[] remaining = new long[queue.size() - idx];
            for (int i = idx; i < queue.size(); i++) {
                remaining[i - idx] = queue.get(i).asLong();
            }
            allQueuesTag.add(new LongArrayTag(remaining));
        }
        tag.put("NodeClearQueues", allQueuesTag);

        ListTag nodeOwnedBlocksTag = new ListTag();
        for (List<BlockPos> ownedBlocks : nodeOwnedBlocks) {
            long[] owned = new long[ownedBlocks.size()];
            for (int i = 0; i < ownedBlocks.size(); i++) {
                owned[i] = ownedBlocks.get(i).asLong();
            }
            nodeOwnedBlocksTag.add(new LongArrayTag(owned));
        }
        tag.put("NodeOwnedBlocks", nodeOwnedBlocksTag);

        ListTag restoredNodesTag = new ListTag();
        for (Integer nodeIndex : restoredNodeIndices) {
            restoredNodesTag.add(IntTag.valueOf(nodeIndex));
        }
        tag.put("RestoredNodeIndices", restoredNodesTag);

        ListTag pendingNodeRestoreTag = new ListTag();
        for (Map.Entry<Integer, List<BlockPos>> entry : pendingNodeRestoreQueues.entrySet()) {
            CompoundTag restoreTag = new CompoundTag();
            restoreTag.putInt("NodeIndex", entry.getKey());
            int restoreIndex = pendingNodeRestoreIndices.getOrDefault(entry.getKey(), 0);
            restoreTag.putInt("RestoreIndex", restoreIndex);
            List<BlockPos> queue = entry.getValue();
            long[] remaining = new long[Math.max(0, queue.size() - restoreIndex)];
            for (int i = restoreIndex; i < queue.size(); i++) {
                remaining[i - restoreIndex] = queue.get(i).asLong();
            }
            restoreTag.putLongArray("Queue", remaining);
            pendingNodeRestoreTag.add(restoreTag);
        }
        tag.put("PendingNodeRestoreQueues", pendingNodeRestoreTag);

        tag.putBoolean("IsRestoring", isRestoring);

        if (isRestoring) {
            long[] remainingRestore = new long[restoreQueue.size() - restoreQueueIndex];
            for (int i = restoreQueueIndex; i < restoreQueue.size(); i++) {
                remainingRestore[i - restoreQueueIndex] = restoreQueue.get(i).asLong();
            }
            tag.putLongArray("RestoreQueue", remainingRestore);
        }
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        loadBaseData(tag);
        this.amplifier = tag.getInt("Amplifier");
        this.elapsedTime = tag.getLong(ELAPSED_TIME_KEY);
        this.riteDuration = tag.getLong(RITE_DURATION_KEY);
        this.portalCooldown = tag.getInt(PORTAL_COOLDOWN_KEY);
        this.nodeRadius = tag.getInt(NODE_RADIUS_KEY);
        this.currentDegree = tag.getInt(CURRENT_DEGREE_KEY);

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

        if (nodePositions.isEmpty() && getMaxDegrees() > 0) {
            syncNodeCountToMaxDegrees();
        }
        if (altar.getLevel() instanceof ServerLevel level) {
            ensureActiveNodesPresent(level);
        }

        this.savedBlocks.clear();
        this.savedBlockData.clear();
        ListTag savedBlocksTag = tag.getList("SavedBlocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < savedBlocksTag.size(); i++) {
            CompoundTag blockTag = savedBlocksTag.getCompound(i);
            BlockPos pos = BlockPos.of(blockTag.getLong("Pos"));
            BlockState state = NbtUtils.readBlockState(altar.getLevel().registryAccess().lookupOrThrow(Registries.BLOCK), blockTag.getCompound("State"));
            savedBlocks.put(pos, state);
            if (blockTag.contains("BlockEntityData")) {
                savedBlockData.put(pos, blockTag.getCompound("BlockEntityData"));
            }
        }

        this.nodeClearQueues.clear();
        this.nodeClearIndices.clear();
        this.nodeOwnedBlocks.clear();
        this.clearQueueSet.clear();
        ListTag allQueuesTag = tag.getList("NodeClearQueues", Tag.TAG_LONG_ARRAY);
        for (int n = 0; n < allQueuesTag.size(); n++) {
            long[] positions = ((LongArrayTag) allQueuesTag.get(n)).getAsLongArray();
            List<BlockPos> queue = new ArrayList<>();
            for (long encoded : positions) {
                BlockPos pos = BlockPos.of(encoded);
                queue.add(pos);
                clearQueueSet.add(pos);
            }
            nodeClearQueues.add(queue);
            nodeClearIndices.add(0);
        }

        ListTag nodeOwnedBlocksTag = tag.getList("NodeOwnedBlocks", Tag.TAG_LONG_ARRAY);
        for (int n = 0; n < nodeOwnedBlocksTag.size(); n++) {
            long[] positions = ((LongArrayTag) nodeOwnedBlocksTag.get(n)).getAsLongArray();
            List<BlockPos> ownedBlocks = new ArrayList<>();
            for (long encoded : positions) {
                ownedBlocks.add(BlockPos.of(encoded));
            }
            nodeOwnedBlocks.add(ownedBlocks);
        }
        if (nodeOwnedBlocks.isEmpty() && !nodePositions.isEmpty()) {
            rebuildNodeOwnedBlocks();
        }

        this.restoredNodeIndices.clear();
        ListTag restoredNodesTag = tag.getList("RestoredNodeIndices", Tag.TAG_INT);
        for (int i = 0; i < restoredNodesTag.size(); i++) {
            restoredNodeIndices.add(((IntTag) restoredNodesTag.get(i)).getAsInt());
        }

        this.pendingNodeRestoreQueues.clear();
        this.pendingNodeRestoreIndices.clear();
        ListTag pendingNodeRestoreTag = tag.getList("PendingNodeRestoreQueues", Tag.TAG_COMPOUND);
        for (int i = 0; i < pendingNodeRestoreTag.size(); i++) {
            CompoundTag restoreTag = pendingNodeRestoreTag.getCompound(i);
            int nodeIndex = restoreTag.getInt("NodeIndex");
            List<BlockPos> queue = new ArrayList<>();
            for (long encoded : restoreTag.getLongArray("Queue")) {
                queue.add(BlockPos.of(encoded));
            }
            pendingNodeRestoreQueues.put(nodeIndex, queue);
            pendingNodeRestoreIndices.put(nodeIndex, restoreTag.getInt("RestoreIndex"));
        }

        this.isRestoring = tag.getBoolean("IsRestoring");
        this.restoreQueue = new ArrayList<>();
        this.restoreQueueIndex = 0;
        if (isRestoring) {
            for (long encoded : tag.getLongArray("RestoreQueue")) {
                restoreQueue.add(BlockPos.of(encoded));
            }
        }
    }

    @Override
    public ResourceLocation getId() {
        return ModRites.EMBERS;
    }

    @Override
    public int getCurseEffectDurationTicks(int curseDurationTicks) {
        return curseDurationTicks;
    }

    public ServerPlayer getServerPlayer() {
        if (altar.getLevel() instanceof ServerLevel serverLevel) {
            return (ServerPlayer) serverLevel.getPlayerByUUID(playerUUID);
        }
        return null;
    }

    @Override
    public boolean isRiteCompleted(Player player) {
        return currentDegree >= getMaxDegrees();
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
        ensureActiveNodesPresent(level);

        if (isRestoring) {
            return;
        }

        tryRestoreCompletedNodeAreas(level, player);

        elapsedTime++;
        portalCooldown++;

        if (portalCooldown >= spawnInterval) {
            portalCooldown = 0;
            //for (BlockPos node : nodePositions) {
            //    Mob placeholder = EntityType.VEX.create(level);
            //    if (placeholder != null) {
            //        placeholder.moveTo(node.getX()+0.5, node.getY()+1, node.getZ()+0.5, 0, 0);
            //        level.addFreshEntity(placeholder);
            //    }
            //}
        }

        for (CursedNodeEntity cursedNode : cursedNodes) {
            int progress = cursedNode.getProgress();
            if (progress >= feedTicks) continue;

            double cx = cursedNode.getBlockX() + 0.5;
            double cz = cursedNode.getBlockZ() + 0.5;
            int baseY = cursedNode.getBlockY();

            for (int i = 0; i < (nodeRadius * 8); i++) {
                double angle = 2 * Math.PI * i / (nodeRadius * 8);
                double px = cx + Math.cos(angle) * nodeRadius;
                double pz = cz + Math.sin(angle) * nodeRadius;
                int ix = Mth.floor(px);
                int iz = Mth.floor(pz);

                int yBot = Math.max(baseY - nodeRadius, level.getMinBuildHeight());
                int yAbove = Math.min(baseY + nodeRadius, level.getMaxBuildHeight() - 1);
                Double py = null;
                for (int y = baseY; y >= yBot; y--) {
                    if (level.getBlockState(new BlockPos(ix, y, iz)).isSolid()) {
                        py = (double) (y + 1);
                        break;
                    }
                }
                if (py == null) {
                    for (int y = baseY + 1; y <= yAbove; y++) {
                        BlockPos checkPos = new BlockPos(ix, y, iz);
                        if (level.getBlockState(checkPos).isSolid() && !level.getBlockState(checkPos.above()).isSolid()) {
                            py = (double) (y + 1);
                            break;
                        }
                    }
                }

                if (py == null) {
                    continue;
                }

                CoreNetworking.sendToNear(new SendParticlesS2C(
                        ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                        px, py, pz,
                        0, 0.1, 0
                ), player);
            }
        }

        double feedRadiusSq = nodeRadius * nodeRadius;
        Iterator<CursedNodeEntity> nodeIterator = cursedNodes.iterator();
        while (nodeIterator.hasNext()) {
            CursedNodeEntity cursedNode = nodeIterator.next();
            int progress = cursedNode.getProgress();
            if (progress >= feedTicks) continue;

            double dx = player.getX() - (cursedNode.getX() + 0.5);
            double dz = player.getZ() - (cursedNode.getZ() + 0.5);
            if (dx*dx + dz*dz <= feedRadiusSq && player.isCrouching()) {
                if (progress % 20 == 0) {
                    DamageSource damageSource = level.damageSources().indirectMagic(cursedNode, cursedNode);
                    int damageAmount = this.amplifier;
                    Vec3 preDelta = player.getDeltaMovement();
                    player.hurt(damageSource, damageAmount);
                    player.setDeltaMovement(preDelta);
                    spawnLifeDrainParticles(player, cursedNode, damageAmount * 2);
                }

                int newProgress = progress + 1;
                cursedNode.setProgress(newProgress);
                updateNodeProgress(cursedNode, newProgress);
                if (newProgress >= feedTicks) {
                    currentDegree++;
                    CoreNetworking.sendToNear(new CameraShakeS2C(0.125F, 1000), player);
                    spawnNodeBurst(level, cursedNode.position().add(0.0D, 0.2D, 0.0D), 48, 0.32D, 0.08D);
                    cursedNode.discard();
                    nodeIterator.remove();

                    ServerPlayer serverPlayer = getServerPlayer();
                    sendGuidance(serverPlayer, getGuidanceTarget(serverPlayer));

                    if (getCompletionDegree() >= getMaxDegrees()) {
                        concludeRite(player);
                        return;
                    }
                }
            }
        }

        if (elapsedTime % 100 == 0) {
            for (CursedNodeEntity node : cursedNodes) {
                int progress = node.getProgress();
                if (progress <= 0 || progress >= feedTicks) continue;

                float percentage = 100f * progress / feedTicks;
                BlockPos pos = node.blockPosition();
                //String line = String.format(
                //        "Node %d @ [x=%d, y=%d, z=%d]: %d / %d (%.0f%%)",
                //        i + 1,
                //        pos.getX(), pos.getY(), pos.getZ(),
                //        progress, feedTicks,
                //        percentage
                //);
                //getServerPlayer().sendSystemMessage(Component.literal(line));
            }
        }

        boolean allMature = nodeProgress.values().stream()
                .allMatch(p -> p >= feedTicks);

        if (allMature) {
            concludeRite(player);
            return;
        }

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

        syncToClient(serverPlayer);
    }

    @Override
    public void concludeRite(Player player) {
        if (isRestoring) return;
        discardActiveNodes();
        clearPendingNodeClears();
        isRestoring = true;
        this.elapsedTime = this.riteDuration;
        if (altar.getLevel() instanceof ServerLevel level) {
            queueAllRemainingNodeRestores(level);
            assignRemainingSavedBlocksToNodeSites();
        }
        restoreQueue = new ArrayList<>();
        restoreQueueIndex = 0;
        finishRite(player, true, 0.125F);
    }

    @Override
    public void onAltarTick(ServerLevel level) {
        if (isRestoring) {
            if (!pendingNodeRestoreQueues.isEmpty()) {
                processPendingNodeRestores(level);
                return;
            }
            if (!savedBlocks.isEmpty()) {
                assignRemainingSavedBlocksToNodeSites();
                if (!pendingNodeRestoreQueues.isEmpty()) {
                    processPendingNodeRestores(level);
                    return;
                }
            }
            if (restoreQueue.isEmpty() && !savedBlocks.isEmpty()) {
                restoreQueue = buildRestoreQueue();
                restoreQueueIndex = 0;
            }
            processRestoreQueueFromAltar(level);
            return;
        }
        processPendingNodeRestores(level);
        processClearQueue(level);
    }

    @Override
    public boolean hasPendingAltarWork() {
        return !pendingNodeRestoreQueues.isEmpty()
                || !savedBlocks.isEmpty()
                || !restoreQueue.isEmpty()
                || isRestoring;
    }

    @Override
    public void onEntityKilled(Player player, Entity entity) {

    }

    @Override
    public int getCompletionDegree() {
        return Math.min(currentDegree, getMaxDegrees());
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

    @Override
    public BlockPos getGuidanceTarget(ServerPlayer player) {
        BlockPos nearestNode = findNearestIncompleteNode(player);
        return nearestNode != null ? nearestNode : super.getGuidanceTarget(player);
    }

    @Override
    protected SyncRiteDataS2C buildSyncPacket(Player player) {
        int completedDegrees = getCompletionDegree();
        int totalDegrees = getDisplayDegreeCount();
        int activeDegreeIndex = isRiteCompleted(player) ? -1 : Math.min(currentDegree, totalDegrees - 1);
        int visibleNodeIndex = findVisibleNodeIndex(player);
        float activeNodeProgress = 0.0f;
        boolean showNodeProgress = visibleNodeIndex >= 0;
        if (visibleNodeIndex >= 0 && visibleNodeIndex < nodePositions.size()) {
            BlockPos visibleNodePos = nodePositions.get(visibleNodeIndex);
            activeNodeProgress = Mth.clamp(nodeProgress.getOrDefault(visibleNodePos, 0) / (float) feedTicks, 0.0F, 1.0F);
        }
        return SyncRiteDataS2C.fromState(new EmbersClientRiteState(
                isRiteCompleted(player),
                elapsedTime,
                riteDuration,
                activeNodeProgress,
                showNodeProgress,
                totalDegrees,
                completedDegrees,
                activeDegreeIndex
        ));
    }

    private int findVisibleNodeIndex(Player player) {
        if (player == null || nodePositions.isEmpty()) {
            return -1;
        }

        int closestIndex = -1;
        double closestDistanceSq = Double.MAX_VALUE;
        double radiusSq = nodeRadius * nodeRadius;
        for (int i = 0; i < nodePositions.size(); i++) {
            BlockPos nodePos = nodePositions.get(i);
            if (nodeProgress.getOrDefault(nodePos, 0) >= feedTicks) {
                continue;
            }
            double dx = player.getX() - (nodePos.getX() + 0.5D);
            double dz = player.getZ() - (nodePos.getZ() + 0.5D);
            double distanceSq = (dx * dx) + (dz * dz);
            if (distanceSq <= radiusSq && distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    @Override
    public void setMaxDegrees(int maxDegrees) {
        super.setMaxDegrees(maxDegrees);
        syncNodeCountToMaxDegrees();
    }

    private void ensureActiveNodesPresent(ServerLevel level) {
        Map<Integer, CursedNodeEntity> activeNodesByIndex = new HashMap<>();
        Iterator<CursedNodeEntity> iterator = cursedNodes.iterator();
        while (iterator.hasNext()) {
            CursedNodeEntity cursedNode = iterator.next();
            if (cursedNode == null || !cursedNode.isAlive()) {
                iterator.remove();
                continue;
            }

            int nodeIndex = findNodeIndexFor(cursedNode.blockPosition());
            if (nodeIndex < 0 || nodeIndex >= nodePositions.size()) {
                iterator.remove();
                continue;
            }

            BlockPos nodePos = nodePositions.get(nodeIndex);
            if (nodeProgress.getOrDefault(nodePos, 0) >= feedTicks) {
                if (cursedNode.isAlive()) {
                    cursedNode.discard();
                }
                iterator.remove();
                continue;
            }

            if (activeNodesByIndex.containsKey(nodeIndex)) {
                if (cursedNode.isAlive()) {
                    cursedNode.discard();
                }
                iterator.remove();
                continue;
            }

            cursedNode.setOwner(altar);
            cursedNode.setNodeLifetime(getRemainingNodeLifetime());
            cursedNode.setProgress(nodeProgress.getOrDefault(nodePos, 0));
            activeNodesByIndex.put(nodeIndex, cursedNode);
        }

        Set<CursedNodeEntity> claimedWorldNodes = new HashSet<>(activeNodesByIndex.values());
        for (int nodeIndex = 0; nodeIndex < nodePositions.size(); nodeIndex++) {
            BlockPos nodePos = nodePositions.get(nodeIndex);
            int savedProgress = nodeProgress.getOrDefault(nodePos, 0);
            if (savedProgress >= feedTicks || activeNodesByIndex.containsKey(nodeIndex)) {
                continue;
            }

            CursedNodeEntity existingNode = findExistingNode(level, nodePos, claimedWorldNodes);
            if (existingNode != null) {
                existingNode.setOwner(altar);
                existingNode.setNodeLifetime(getRemainingNodeLifetime());
                existingNode.setProgress(savedProgress);
                cursedNodes.add(existingNode);
                claimedWorldNodes.add(existingNode);
                activeNodesByIndex.put(nodeIndex, existingNode);
                continue;
            }

            CursedNodeEntity spawnedNode = spawnCursedNode(level, nodePos, savedProgress, false);
            if (spawnedNode != null) {
                activeNodesByIndex.put(nodeIndex, spawnedNode);
            }
        }
    }

    private void syncNodeCountToMaxDegrees() {
        if (!(altar.getLevel() instanceof ServerLevel level)) {
            return;
        }

        while (nodePositions.size() > getMaxDegrees()) {
            int lastIndex = nodePositions.size() - 1;
            BlockPos removedPos = nodePositions.remove(lastIndex);
            nodeProgress.remove(removedPos);
            if (lastIndex < nodeOwnedBlocks.size()) {
                nodeOwnedBlocks.remove(lastIndex);
            }
            restoredNodeIndices.remove(lastIndex);
            if (lastIndex < cursedNodes.size()) {
                CursedNodeEntity removedNode = cursedNodes.remove(lastIndex);
                if (removedNode != null && removedNode.isAlive()) {
                    removedNode.discard();
                }
            }
        }

        Random random = new Random();
        while (nodePositions.size() < getMaxDegrees()) {
            int nodeIndex = nodePositions.size();
            BlockPos nodePos = generateNodePosition(random, nodeIndex, getMaxDegrees());
            nodePositions.add(nodePos);
            nodeProgress.put(nodePos, 0);
            spawnCursedNode(level, nodePos, 0, true);
        }
    }

    private BlockPos generateNodePosition(Random random, int nodeIndex, int totalNodes) {
        BlockPos center = altar.getBlockPos();
        double clearRadius = nodeRadius * 1.5;
        double minDistance = clearRadius + 4.0;
        double maxDistance = minDistance + 12.0 + amplifier * 4.0;
        double sectorStart = nodeIndex * (360.0 / totalNodes);
        double randomOffset = random.nextDouble() * (360.0 / totalNodes);
        double degreeOffset = sectorStart + randomOffset;
        double radians = Math.toRadians(degreeOffset);
        double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);

        int dx = (int) Math.round(Math.cos(radians) * distance);
        int dz = (int) Math.round(Math.sin(radians) * distance);
        return new BlockPos(center.getX() + dx, center.getY(), center.getZ() + dz);
    }

    private CursedNodeEntity spawnCursedNode(ServerLevel level, BlockPos node, int progress, boolean initializeClearData) {
        CursedNodeEntity cursedNode = ModEntities.CURSED_NODE.get().create(level);
        if (cursedNode == null) {
            return null;
        }

        double x = node.getX() + 0.5;
        double z = node.getZ() + 0.5;
        int altarY = altar.getBlockPos().getY();
        int searchMin = Math.max(altarY - 8, level.getMinBuildHeight());
        int searchMax = Math.min(altarY + 8, level.getMaxBuildHeight() - 2);

        double spawnY = -1;
        for (int y = searchMax; y >= searchMin; y--) {
            BlockPos checkPos = new BlockPos(node.getX(), y, node.getZ());
            if (level.getBlockState(checkPos).isSolid() && !level.getBlockState(checkPos.above()).isSolid()) {
                spawnY = y + 1;
                break;
            }
        }
        if (spawnY < 0) {
            for (int y = searchMin - 1; y >= level.getMinBuildHeight() + 1; y--) {
                BlockPos checkPos = new BlockPos(node.getX(), y, node.getZ());
                if (level.getBlockState(checkPos).isSolid() && !level.getBlockState(checkPos.above()).isSolid()) {
                    spawnY = y + 1;
                    break;
                }
            }
        }
        if (spawnY < 0) spawnY = altarY;

        cursedNode.moveTo(x, spawnY + 0.75, z, 0, 0);

        cursedNode.setProgress(progress);
        cursedNode.setNodeLifetime(getRemainingNodeLifetime());
        cursedNode.setOwner(altar);
        level.addFreshEntity(cursedNode);
        if (initializeClearData) {
            buildClearQueueForNode(cursedNode);
        }
        cursedNodes.add(cursedNode);
        return cursedNode;
    }

    private int getRemainingNodeLifetime() {
        return Math.max(1, (int) Math.max(1L, riteDuration - elapsedTime));
    }

    private CursedNodeEntity findExistingNode(ServerLevel level, BlockPos nodePos, Set<CursedNodeEntity> claimedNodes) {
        AABB searchBox = new AABB(nodePos).inflate(1.5D, 12.0D, 1.5D);
        for (CursedNodeEntity cursedNode : level.getEntitiesOfClass(CursedNodeEntity.class, searchBox)) {
            if (claimedNodes.contains(cursedNode) || !cursedNode.isAlive()) {
                continue;
            }
            return cursedNode;
        }
        return null;
    }

    private void buildClearQueueForNode(CursedNodeEntity node) {
        List<BlockPos> nodeBlocks = collectNodeBlocks(node.blockPosition(), true);
        nodeClearQueues.add(nodeBlocks);
        nodeClearIndices.add(0);
        List<BlockPos> ownedBlocks = new ArrayList<>(nodeBlocks);
        ownedBlocks.sort(buildRestorationComparator());
        nodeOwnedBlocks.add(ownedBlocks);
    }

    private void processClearQueue(ServerLevel level) {
        for (int n = 0; n < nodeClearQueues.size(); n++) {
            List<BlockPos> queue = nodeClearQueues.get(n);
            int idx = nodeClearIndices.get(n);
            int end = Math.min(idx + CLEAR_PER_TICK, queue.size());
            for (int i = idx; i < end; i++) {
                BlockPos pos = queue.get(i);
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0) {
                    if (!savedBlocks.containsKey(pos)) {
                        savedBlocks.put(pos, state);
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be != null) {
                            savedBlockData.put(pos, be.saveWithFullMetadata());
                        }
                    }
                    level.levelEvent(2001, pos, Block.getId(state));
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
            nodeClearIndices.set(n, end);
        }
    }

    private void discardActiveNodes() {
        for (CursedNodeEntity cursedNode : cursedNodes) {
            if (cursedNode != null && cursedNode.isAlive()) {
                spawnNodeBurst((ServerLevel) altar.getLevel(), cursedNode.position().add(0.0D, 0.2D, 0.0D), 32, 0.26D, 0.06D);
                cursedNode.discard();
            }
        }
        cursedNodes.clear();
    }

    private void clearPendingNodeClears() {
        nodeClearQueues.clear();
        nodeClearIndices.clear();
        clearQueueSet.clear();
    }

    private void processRestoreQueueFromAltar(ServerLevel level) {
        if (restoreQueueIndex >= restoreQueue.size()) {
            savedBlocks.clear();
            savedBlockData.clear();
            altar.removePlayerRite(playerUUID);
            return;
        }
        int end = Math.min(restoreQueueIndex + RESTORE_PER_TICK, restoreQueue.size());
        for (int i = restoreQueueIndex; i < end; i++) {
            restoreSavedBlock(level, restoreQueue.get(i));
        }
        restoreQueueIndex = end;
    }

    private List<BlockPos> buildRestoreQueue() {
        List<BlockPos> queue = new ArrayList<>(savedBlocks.keySet());
        queue.sort(buildRestorationComparator());
        return queue;
    }

    private double nearestNodeDistSq(BlockPos pos) {
        double min = Double.MAX_VALUE;
        for (BlockPos nodePos : nodePositions) {
            double d = pos.distSqr(nodePos);
            if (d < min) min = d;
        }
        return min;
    }

    private void updateNodeProgress(CursedNodeEntity node, int progress) {
        int nodeIndex = findNodeIndexFor(node.blockPosition());
        if (nodeIndex >= 0 && nodeIndex < nodePositions.size()) {
            nodeProgress.put(nodePositions.get(nodeIndex), progress);
        }
    }

    private int findNodeIndexFor(BlockPos nodePos) {
        if (nodePositions.isEmpty()) {
            return -1;
        }

        int bestIndex = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < nodePositions.size(); i++) {
            double distance = nodePositions.get(i).distSqr(nodePos);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private double getNodeClearRadius() {
        return nodeRadius * 1.5D;
    }

    private List<BlockPos> collectNodeBlocks(BlockPos nodeCenter, boolean claimPositions) {
        double clearRadius = getNodeClearRadius();
        double clearRadiusSq = clearRadius * clearRadius;
        int nodeX = nodeCenter.getX();
        int nodeY = nodeCenter.getY();
        int nodeZ = nodeCenter.getZ();
        int r = (int) Math.ceil(clearRadius);
        int bowlDepth = Math.max(1, r / 3);

        List<BlockPos> nodeBlocks = new ArrayList<>();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -bowlDepth; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    double vertFactor = dy >= 0 ? 1.0 : 3.0;
                    double dist = dx * dx + (dy * vertFactor) * (dy * vertFactor) + dz * dz;
                    if (dist > clearRadiusSq) continue;
                    BlockPos pos = new BlockPos(nodeX + dx, nodeY + dy, nodeZ + dz);
                    if (pos.equals(altar.getBlockPos())) continue;
                    if (!claimPositions || clearQueueSet.add(pos)) {
                        nodeBlocks.add(pos);
                    }
                }
            }
        }

        nodeBlocks.sort(Comparator.comparingDouble(pos -> {
            int ddx = pos.getX() - nodeX;
            int ddy = pos.getY() - nodeY;
            int ddz = pos.getZ() - nodeZ;
            return ddx * ddx + ddy * ddy + ddz * ddz;
        }));
        return nodeBlocks;
    }

    private void tryRestoreCompletedNodeAreas(ServerLevel level, Player player) {
        if (nodeOwnedBlocks.isEmpty()) {
            return;
        }

        for (int nodeIndex = 0; nodeIndex < nodePositions.size() && nodeIndex < nodeOwnedBlocks.size(); nodeIndex++) {
            if (restoredNodeIndices.contains(nodeIndex)) {
                continue;
            }

            int progress = nodeProgress.getOrDefault(nodePositions.get(nodeIndex), 0);
            if (progress < feedTicks || !isNodeClearFinished(nodeIndex)) {
                continue;
            }

            if (isPlayerWithinNodeClearArea(player, nodePositions.get(nodeIndex))) {
                continue;
            }

            queueNodeAreaRestore(level, nodeIndex);
        }
    }

    private boolean isNodeClearFinished(int nodeIndex) {
        return nodeIndex < nodeClearQueues.size()
                && nodeIndex < nodeClearIndices.size()
                && nodeClearIndices.get(nodeIndex) >= nodeClearQueues.get(nodeIndex).size();
    }

    private void queueAllRemainingNodeRestores(ServerLevel level) {
        for (int nodeIndex = 0; nodeIndex < nodePositions.size() && nodeIndex < nodeOwnedBlocks.size(); nodeIndex++) {
            if (restoredNodeIndices.contains(nodeIndex) || pendingNodeRestoreQueues.containsKey(nodeIndex)) {
                continue;
            }
            queueNodeAreaRestore(level, nodeIndex);
        }
    }

    private void queueNodeAreaRestore(ServerLevel level, int nodeIndex) {
        List<BlockPos> ownedBlocks = getSavedBlocksForNode(nodeIndex);
        if (ownedBlocks.isEmpty()) {
            restoredNodeIndices.add(nodeIndex);
            return;
        }
        if (pendingNodeRestoreQueues.containsKey(nodeIndex) || restoredNodeIndices.contains(nodeIndex)) {
            return;
        }

        BlockPos nodeCenter = nodePositions.get(nodeIndex);
        spawnNodeBurst(level, Vec3.atCenterOf(nodeCenter).add(0.0D, 0.25D, 0.0D), 56, 0.36D, 0.1D);
        pendingNodeRestoreQueues.put(nodeIndex, ownedBlocks);
        pendingNodeRestoreIndices.put(nodeIndex, 0);
    }

    private boolean isPlayerWithinNodeClearArea(Player player, BlockPos nodeCenter) {
        double clearRadius = getNodeClearRadius() + NODE_RESTORE_MARGIN;
        double dx = player.getX() - (nodeCenter.getX() + 0.5D);
        double dz = player.getZ() - (nodeCenter.getZ() + 0.5D);
        return (dx * dx) + (dz * dz) <= clearRadius * clearRadius;
    }

    private void processPendingNodeRestores(ServerLevel level) {
        if (pendingNodeRestoreQueues.isEmpty()) {
            return;
        }

        List<Integer> completedNodeRestores = new ArrayList<>();
        for (Map.Entry<Integer, List<BlockPos>> entry : pendingNodeRestoreQueues.entrySet()) {
            int nodeIndex = entry.getKey();
            List<BlockPos> queue = entry.getValue();
            int start = pendingNodeRestoreIndices.getOrDefault(nodeIndex, 0);
            int end = Math.min(start + RESTORE_PER_TICK, queue.size());

            for (int i = start; i < end; i++) {
                restoreSavedBlock(level, queue.get(i));
            }

            if (end >= queue.size()) {
                completedNodeRestores.add(nodeIndex);
            } else {
                pendingNodeRestoreIndices.put(nodeIndex, end);
            }
        }

        for (Integer nodeIndex : completedNodeRestores) {
            pendingNodeRestoreQueues.remove(nodeIndex);
            pendingNodeRestoreIndices.remove(nodeIndex);
            restoredNodeIndices.add(nodeIndex);
        }
    }

    private void restoreSavedBlock(ServerLevel level, BlockPos pos) {
        BlockState state = savedBlocks.remove(pos);
        CompoundTag beData = savedBlockData.remove(pos);
        if (state == null) {
            return;
        }

        level.setBlock(pos, state, 3);
        playRestoreEffects(level, pos, state);
        if (beData != null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                be.load(beData);
                be.setChanged();
            }
        }
    }

    private void playRestoreEffects(ServerLevel level, BlockPos pos, BlockState state) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D,
                8,
                0.2D,
                0.2D,
                0.2D,
                0.05D
        );

        SoundType soundType = state.getSoundType();
        level.playSound(
                null,
                pos,
                soundType.getPlaceSound(),
                SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) * 0.5F,
                soundType.getPitch() * 0.8F
        );
    }

    private List<BlockPos> getSavedBlocksForNode(int nodeIndex) {
        if (nodeIndex < 0 || nodeIndex >= nodeOwnedBlocks.size()) {
            return Collections.emptyList();
        }

        List<BlockPos> ownedBlocks = new ArrayList<>();
        for (BlockPos pos : nodeOwnedBlocks.get(nodeIndex)) {
            if (savedBlocks.containsKey(pos)) {
                ownedBlocks.add(pos);
            }
        }
        return ownedBlocks;
    }

    private void assignRemainingSavedBlocksToNodeSites() {
        if (savedBlocks.isEmpty() || nodePositions.isEmpty()) {
            return;
        }

        Map<Integer, List<BlockPos>> additionsByNode = new HashMap<>();
        for (BlockPos pos : savedBlocks.keySet()) {
            if (isAlreadyQueuedForRestore(pos)) {
                continue;
            }

            int nodeIndex = findNearestNodeIndex(pos);
            if (nodeIndex < 0) {
                continue;
            }
            additionsByNode.computeIfAbsent(nodeIndex, ignored -> new ArrayList<>()).add(pos);
        }

        for (Map.Entry<Integer, List<BlockPos>> entry : additionsByNode.entrySet()) {
            int nodeIndex = entry.getKey();
            List<BlockPos> additions = entry.getValue();
            additions.sort(buildRestorationComparator());

            if (pendingNodeRestoreQueues.containsKey(nodeIndex)) {
                List<BlockPos> queue = pendingNodeRestoreQueues.get(nodeIndex);
                queue.addAll(additions);
            } else if (!additions.isEmpty()) {
                pendingNodeRestoreQueues.put(nodeIndex, additions);
                pendingNodeRestoreIndices.put(nodeIndex, 0);
                restoredNodeIndices.remove(nodeIndex);
            }
        }
    }

    private boolean isAlreadyQueuedForRestore(BlockPos pos) {
        for (List<BlockPos> queue : pendingNodeRestoreQueues.values()) {
            if (queue.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    private int findNearestNodeIndex(BlockPos pos) {
        int bestIndex = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < nodePositions.size(); i++) {
            double distance = nodePositions.get(i).distSqr(pos);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private void rebuildNodeOwnedBlocks() {
        Set<BlockPos> claimedBlocks = new HashSet<>();
        nodeOwnedBlocks.clear();
        for (BlockPos nodePos : nodePositions) {
            List<BlockPos> ownedBlocks = collectNodeBlocks(nodePos, claimedBlocks);
            ownedBlocks.sort(buildRestorationComparator());
            nodeOwnedBlocks.add(ownedBlocks);
        }
    }

    private void spawnNodeBurst(ServerLevel level, Vec3 center, int count, double spread, double speed) {
        if (level == null) {
            return;
        }

        level.sendParticles(
                ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                center.x,
                center.y,
                center.z,
                count,
                spread,
                spread,
                spread,
                speed
        );
        level.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.GHAST_SHOOT,
                SoundSource.BLOCKS,
                1.0f,
                0.5f
        );
    }

    private List<BlockPos> collectNodeBlocks(BlockPos nodeCenter, Set<BlockPos> claimedBlocks) {
        double clearRadius = getNodeClearRadius();
        double clearRadiusSq = clearRadius * clearRadius;
        int nodeX = nodeCenter.getX();
        int nodeY = nodeCenter.getY();
        int nodeZ = nodeCenter.getZ();
        int r = (int) Math.ceil(clearRadius);
        int bowlDepth = Math.max(1, r / 3);

        List<BlockPos> nodeBlocks = new ArrayList<>();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -bowlDepth; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    double vertFactor = dy >= 0 ? 1.0 : 3.0;
                    double dist = dx * dx + (dy * vertFactor) * (dy * vertFactor) + dz * dz;
                    if (dist > clearRadiusSq) continue;
                    BlockPos pos = new BlockPos(nodeX + dx, nodeY + dy, nodeZ + dz);
                    if (pos.equals(altar.getBlockPos()) || !claimedBlocks.add(pos)) continue;
                    nodeBlocks.add(pos);
                }
            }
        }

        nodeBlocks.sort(Comparator.comparingDouble(pos -> {
            int ddx = pos.getX() - nodeX;
            int ddy = pos.getY() - nodeY;
            int ddz = pos.getZ() - nodeZ;
            return ddx * ddx + ddy * ddy + ddz * ddz;
        }));
        return nodeBlocks;
    }

    private Comparator<BlockPos> buildRestorationComparator() {
        return Comparator
                .comparingInt((BlockPos pos) -> pos.getY())
                .thenComparingDouble(this::nearestNodeDistSq)
                .thenComparingInt(Vec3i::getX)
                .thenComparingInt(Vec3i::getZ);
    }

}
