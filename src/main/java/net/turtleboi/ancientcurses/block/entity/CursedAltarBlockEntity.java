package net.turtleboi.ancientcurses.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.turtleboi.ancientcurses.block.altar.GemUtil;
import net.turtleboi.ancientcurses.block.altar.LifecycleUtil;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.config.AncientCursesConfig;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.rites.*;
import net.turtleboi.ancientcurses.util.AltarSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CursedAltarBlockEntity extends BlockEntity {
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final RandomSource RANDOM = RandomSource.create();
    private final Map<UUID, ActiveRiteSession> playerRites = new HashMap<>();
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private boolean isAnimating;
    private long animationStartTime;
    private UUID occupantUuid;
    private boolean chunkLoaded;
    private ItemStack pendingGemFusionResult = ItemStack.EMPTY;
    private boolean resolvingPendingGemFusion;
    public static String CURSED_SPAWN = "cursed_spawn";

    public final ItemStackHandler itemStackHandler = new ItemStackHandler(3){
        @Override
        protected void onContentsChanged(int slot){
            setChanged();
            if(level != null && !level.isClientSide){
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.getCount() > 1) {
                stack.setCount(1);
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    public final ItemStackHandler ritualStackHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.getCount() > 1) {
                stack.setCount(1);
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    public CursedAltarBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.CURSED_ALTAR_BE.get(), pPos, pBlockState);
        this.isAnimating = false;
        this.animationStartTime = 0;
        this.chunkLoaded = false;
    }

    public static void bookAnimationTick(Level pLevel, BlockPos pPos, BlockState pState, CursedAltarBlockEntity pBlockEntity) {
        pBlockEntity.oOpen = pBlockEntity.open;
        pBlockEntity.oRot = pBlockEntity.rot;
        Player v1 = pLevel.getNearestPlayer((double)pPos.getX() + 0.5, (double)pPos.getY() + 0.5, (double)pPos.getZ() + 0.5, 3.0, false);
        if (v1 != null) {
            double v2 = v1.getX() - ((double)pPos.getX() + 0.5);
            double v3 = v1.getZ() - ((double)pPos.getZ() + 0.5);
            pBlockEntity.tRot = (float) Mth.atan2(v3, v2);
            pBlockEntity.open += 0.1F;
            if (pBlockEntity.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float v4 = pBlockEntity.flipT;

                do {
                    pBlockEntity.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while(v4 == pBlockEntity.flipT);
            }
        } else {
            pBlockEntity.tRot += 0.02F;
            pBlockEntity.open -= 0.1F;
        }

        while(pBlockEntity.rot >= 3.1415927F) {
            pBlockEntity.rot -= 6.2831855F;
        }

        while(pBlockEntity.rot < -3.1415927F) {
            pBlockEntity.rot += 6.2831855F;
        }

        while(pBlockEntity.tRot >= 3.1415927F) {
            pBlockEntity.tRot -= 6.2831855F;
        }

        while(pBlockEntity.tRot < -3.1415927F) {
            pBlockEntity.tRot += 6.2831855F;
        }

        float v5;
        for(v5 = pBlockEntity.tRot - pBlockEntity.rot; v5 >= 3.1415927F; v5 -= 6.2831855F) {
        }

        while(v5 < -3.1415927F) {
            v5 += 6.2831855F;
        }

        pBlockEntity.rot += v5 * 0.4F;
        pBlockEntity.open = Mth.clamp(pBlockEntity.open, 0.0F, 1.0F);
        ++pBlockEntity.time;
        pBlockEntity.oFlip = pBlockEntity.flip;
        float v6 = (pBlockEntity.flipT - pBlockEntity.flip) * 0.4F;
        v6 = Mth.clamp(v6, -0.2F, 0.2F);
        pBlockEntity.flipA += (v6 - pBlockEntity.flipA) * 0.9F;
        pBlockEntity.flip += pBlockEntity.flipA;
    }

    public void startAnimation() {
        this.isAnimating = true;
        setAnimationStartTime(System.currentTimeMillis());
        setChanged();
        if (level != null){
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void stopAnimation() {
        this.isAnimating = false;
        this.animationStartTime = 0;
        setChanged();
        if (level != null){
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public boolean hasPendingGemFusion() {
        return !pendingGemFusionResult.isEmpty();
    }

    public boolean isResolvingPendingGemFusion() {
        return resolvingPendingGemFusion;
    }

    public ItemStack getPendingGemFusionResult() {
        return pendingGemFusionResult;
    }

    public void setPendingGemFusionResult(ItemStack stack) {
        this.pendingGemFusionResult = stack.copy();
        this.resolvingPendingGemFusion = false;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void startPendingGemFusionResolution() {
        if (!hasPendingGemFusion()) {
            return;
        }

        this.resolvingPendingGemFusion = true;
        startAnimation();
    }

    public void clearPendingGemFusion() {
        this.pendingGemFusionResult = ItemStack.EMPTY;
        this.resolvingPendingGemFusion = false;
        stopAnimation();
    }

    public void finishPendingGemFusion() {
        ItemStack fusedGem = pendingGemFusionResult.copy();
        for (int slot = 0; slot < itemStackHandler.getSlots(); slot++) {
            setGemInSlot(slot, ItemStack.EMPTY);
        }
        setRitualItemInSlot(0, fusedGem);
        spawnFusionParticles();
        clearPendingGemFusion();
    }

    private void spawnFusionParticles() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        double centerX = worldPosition.getX() + 0.5D;
        double centerY = worldPosition.getY() + 1.25D;
        double centerZ = worldPosition.getZ() + 0.5D;
        serverLevel.sendParticles(
                ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                centerX,
                centerY,
                centerZ,
                64,
                0.18D,
                0.12D,
                0.18D,
                0.03D
        );
        serverLevel.playSound(
                null,
                centerX,
                centerY,
                centerZ,
                SoundEvents.GHAST_SHOOT,
                SoundSource.BLOCKS,
                1.0f,
                0.5f
        );
    }

    public void setAnimationStartTime (Long startTime) {
        this.getPersistentData().putLong("AnimationStartTime", startTime);
    }

    public long getAnimationStartTime() {
        return this.getPersistentData().getLong("AnimationStartTime");
    }

    public ItemStack getGemInSlot(int slot) {
        return itemStackHandler.getStackInSlot(slot);
    }

    public void setGemInSlot(int slot, ItemStack stack) {
        itemStackHandler.setStackInSlot(slot, stack);
    }

    public ItemStack getRitualItemInSlot(int slot) {
        return ritualStackHandler.getStackInSlot(slot);
    }

    public void setRitualItemInSlot(int slot, ItemStack stack) {
        ritualStackHandler.setStackInSlot(slot, stack);
    }

    public UUID getOccupantUuid() {
        return occupantUuid;
    }

    public void setOccupantUuid(UUID occupantUuid) {
        this.occupantUuid = occupantUuid;
    }

    public void setChunkLoaded(boolean chunkLoaded) {
        this.chunkLoaded = chunkLoaded;
    }

    public boolean canPlayerUse(Player player) {
        UUID playerUUID = player.getUUID();

        if (player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                .map(PlayerRiteDataCapability::isPlayerCursed)
                .orElse(false)) {
            // player.sendSystemMessage(Component.literal("You're cursed!").withStyle(ChatFormatting.RED));
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownTime = 250;
        if (playerCooldowns.containsKey(playerUUID)) {
            long lastUseTime = playerCooldowns.get(playerUUID);
            long timePassed = currentTime - lastUseTime;
            //player.sendSystemMessage(Component.literal("Current time: " + currentTime).withStyle(ChatFormatting.RED));
            //player.sendSystemMessage(Component.literal("Last used: " + lastUseTime).withStyle(ChatFormatting.RED));
            //player.sendSystemMessage(Component.literal("Time passed since last use: " + timePassed).withStyle(ChatFormatting.RED));
            if (timePassed < cooldownTime) {
                long timeRemaining = cooldownTime - timePassed;
                //player.sendSystemMessage(Component.literal("Altar is recharging... Time remaining: " + timeRemaining).withStyle(ChatFormatting.RED));
                return false;
            }
        }
        playerCooldowns.put(playerUUID, currentTime);
        return true;
    }

    public void cursePlayer(Player player, ModRites.CurseRiteEntry curseEntry, int curseAmplifier) {
        LifecycleUtil.forceDimensionActive(this);

        MobEffect curse = curseEntry.curse();
        BlockPos altarPos = this.getBlockPos();
        UUID playerUUID = player.getUUID();
        Random random = new Random();
        int minMultiple = AncientCursesConfig.CURSE_TIME_MIN.get();
        int maxMultiple = AncientCursesConfig.CURSE_TIME_MAX.get();
        int range = maxMultiple - minMultiple + 1;
        int randomMultiple = random.nextInt(range) + minMultiple;
        int calculatedDuration = randomMultiple * 20;
        int curseDuration = calculatedDuration * (curseAmplifier + 1);

        Rite rite = ModRites.createRite(curseEntry.riteId(), player, curse, curseAmplifier, curseDuration, this);
        if (rite == null) {
            throw new IllegalStateException("No rite registered for curse " + curse + " in rite id " + curseEntry.riteId());
        }
        addPlayerRite(playerUUID, rite);

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setCurseEffect(curse);
            riteData.setCurseAmplifier(curseAmplifier);
            riteData.setCurrentAltarPos(altarPos);
            //System.out.println("Setting altar dimension to: " + this.getLevel().dimension());
            riteData.setAltarDimension(Objects.requireNonNull(this.getLevel()).dimension());

            RiteRecord riteRecord = new RiteRecord(altarPos, rite.getId(), false, false);
            riteData.addOrUpdateRiteRecord(riteRecord);
        });

        player.addEffect(new MobEffectInstance(curse, rite.getCurseEffectDurationTicks(curseDuration), curseAmplifier, false, false, true));

        rite.syncToClient(player);
    }

    public boolean hasPlayerCompletedRite(Player player) {
        BlockPos altarPos = this.getBlockPos();
        return player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                .map(riteData -> riteData.hasCompletedRite(altarPos))
                .orElse(false);
    }

    private boolean anyActiveRitesRemaining() {
        for (ActiveRiteSession session : playerRites.values()) {
            if (!session.getRite().isCompleted()) {
                return true;
            }
        }
        return false;
    }

    public void setPlayerRiteCompleted(Player player) {
        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            UUID playerUUID = player.getUUID();
            Rite rite = getPlayerRite(playerUUID);
            if (rite != null) {
                BlockPos altarPos = this.getBlockPos();
                riteData.setRiteCompleted(altarPos);
                rite.setCompleted(true);

                if (!anyActiveRitesRemaining()) {
                    releaseDimensionActive();
                }
                setChanged();
            }
        });
    }

    public boolean hasCollectedReward(Player player) {
        BlockPos altarPos = this.getBlockPos();
        return player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                .map(riteData -> riteData.hasCollectedReward(altarPos))
                .orElse(false);
    }

    public void markRewardCollected(Player player) {
        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            BlockPos altarPos = this.getBlockPos();
            riteData.setRewardCollected(altarPos);
            riteData.clearCurrentAltarPos();
            setChanged();
        });
    }

    public Rite getPlayerRite(UUID playerUUID) {
        ActiveRiteSession session = playerRites.get(playerUUID);
        return session != null ? session.getRite() : null;
    }

    public void addPlayerRite(UUID playerUUID, Rite rite) {
        playerRites.put(playerUUID, new ActiveRiteSession(playerUUID, rite));
    }

    public void removePlayerRite(UUID playerUUID) {
        playerRites.remove(playerUUID);
    }

    public void removePlayerFromRite(Player player) {
        UUID playerUUID = player.getUUID();
        Rite rite = getPlayerRite(playerUUID);
        if (rite != null) {
            if (!rite.isRiteCompleted(player)) {
                removePlayerRite(playerUUID);
                setChanged();
                //System.out.println("Removed rite for player: " + player.getName().getString()); //debug code
            } else {
                //System.out.println("Attempted to remove a completed rite for player: " + player.getName().getString()); //debug code
            }
        }
    }

    public void performGemUpgrade() {
        ItemStack gem1 = getGemInSlot(0);
        ItemStack gem2 = getGemInSlot(1);
        ItemStack gem3 = getGemInSlot(2);
        if (GemUtil.canUpgrade(gem1, gem2, gem3)) {
            ItemStack upgradedGem = GemUtil.getUpgradedGem(gem1);
            if (upgradedGem.isEmpty()) {
                return;
            }

            setGemInSlot(0, ItemStack.EMPTY);
            setGemInSlot(1, ItemStack.EMPTY);
            setGemInSlot(2, ItemStack.EMPTY);
            setGemInSlot(0, upgradedGem);
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                GemUtil.playGemPlaceSound(level, worldPosition, 1);
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CursedAltarBlockEntity blockEntity) {
        blockEntity.serverTick();
    }

    public void serverTick() {
        if (isAnimating && !hasPendingGemFusion()) {
            long currentTime = System.currentTimeMillis();
            long animationDuration = 7600;
            if (currentTime - getAnimationStartTime() >= animationDuration) {
                performGemUpgrade();
                stopAnimation();
            }
        } else if (isAnimating && hasPendingGemFusion() && resolvingPendingGemFusion) {
            long currentTime = System.currentTimeMillis();
            long animationDuration = 2400;
            if (currentTime - getAnimationStartTime() >= animationDuration) {
                finishPendingGemFusion();
            }
        }

        if (!anyActiveRitesRemaining()) {
            if (this.level instanceof ServerLevel serverLevel) {
                if (LifecycleUtil.hasOccupantEntity(serverLevel, occupantUuid)){
                    Entity occupant = serverLevel.getEntity(occupantUuid);
                    occupant.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    public boolean isChunkLoaded() {
        return chunkLoaded;
    }

    public void forceLoadChunk() {
        LifecycleUtil.forceLoadChunk(this);
    }

    public void releaseChunkLoad() {
        LifecycleUtil.releaseChunkLoad(this);
    }

    public void forceDimensionActive() {
        LifecycleUtil.forceDimensionActive(this);
    }

    public void releaseDimensionActive() {
        LifecycleUtil.releaseDimensionActive(this);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsAnimating", this.isAnimating);
        tag.putLong("AnimationStartTime", this.animationStartTime);
        tag.put("gems", itemStackHandler.serializeNBT());
        tag.put("ritual_items", ritualStackHandler.serializeNBT());
        if (!this.pendingGemFusionResult.isEmpty()) {
            tag.put("PendingGemFusionResult", this.pendingGemFusionResult.save(new CompoundTag()));
        }
        tag.putBoolean("ResolvingPendingGemFusion", this.resolvingPendingGemFusion);

        tag.putBoolean("ChunkLoaded", this.chunkLoaded);
        if (this.occupantUuid != null) {
            tag.putUUID("OccupantUUID", this.occupantUuid);
        }

        ListTag activeRitesList = new ListTag();
        for (ActiveRiteSession session : playerRites.values()) {
            activeRitesList.add(session.save());
        }
        tag.put("ActiveRites", activeRitesList);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.isAnimating = tag.getBoolean("IsAnimating");
        this.animationStartTime = tag.getLong("AnimationStartTime");
        if (tag.contains("gems")) {
            itemStackHandler.deserializeNBT(tag.getCompound("gems"));
        }
        if (tag.contains("ritual_items")) {
            ritualStackHandler.deserializeNBT(tag.getCompound("ritual_items"));
        }
        if (tag.contains("PendingGemFusionResult")) {
            this.pendingGemFusionResult = ItemStack.of(tag.getCompound("PendingGemFusionResult"));
        } else {
            this.pendingGemFusionResult = ItemStack.EMPTY;
        }
        this.resolvingPendingGemFusion = tag.getBoolean("ResolvingPendingGemFusion");

        this.chunkLoaded = tag.getBoolean("ChunkLoaded");
        if (tag.hasUUID("OccupantUUID")) {
            this.occupantUuid = tag.getUUID("OccupantUUID");
        } else {
            this.occupantUuid = null;
        }

        ListTag activeRitesList = tag.getList("ActiveRites", Tag.TAG_COMPOUND);
        playerRites.clear();
        for (int i = 0; i < activeRitesList.size(); i++) {
            CompoundTag playerTag = activeRitesList.getCompound(i);
            ActiveRiteSession session = ActiveRiteSession.load(playerTag, this);
            if (session != null) {
                playerRites.put(session.getPlayerUuid(), session);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.level.isClientSide) {
            if (this.chunkLoaded) {
                forceDimensionActive();
            }

            if (level instanceof ServerLevel serverLevel) {
                AltarSavedData.get(serverLevel).addAltar(worldPosition);
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!this.level.isClientSide && level instanceof ServerLevel serverLevel) {
            AltarSavedData.get(serverLevel).removeAltar(worldPosition);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}


