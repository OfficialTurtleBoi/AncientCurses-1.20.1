package net.turtleboi.ancientcurses.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.capabilities.trials.PlayerTrialDataCapability;
import net.turtleboi.ancientcurses.capabilities.trials.PlayerTrialProvider;
import net.turtleboi.ancientcurses.config.AncientCursesConfig;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.sound.ModSounds;
import net.turtleboi.ancientcurses.trials.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
    private final Map<UUID, Trial> playerTrials = new HashMap<>();
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private boolean isAnimating;
    private long animationStartTime;
    private static final Map<Item, Item> gemUpgradeMap = new HashMap<>();
    private UUID occupantUuid;
    private boolean chunkLoaded;

    static {
        gemUpgradeMap.put(ModItems.BROKEN_AMETHYST.get(), ModItems.POLISHED_AMETHYST.get());
        gemUpgradeMap.put(ModItems.POLISHED_AMETHYST.get(), ModItems.PERFECT_AMETHYST.get());
        gemUpgradeMap.put(ModItems.BROKEN_DIAMOND.get(), ModItems.POLISHED_DIAMOND.get());
        gemUpgradeMap.put(ModItems.POLISHED_DIAMOND.get(), ModItems.PERFECT_DIAMOND.get());
        gemUpgradeMap.put(ModItems.BROKEN_EMERALD.get(), ModItems.POLISHED_EMERALD.get());
        gemUpgradeMap.put(ModItems.POLISHED_EMERALD.get(), ModItems.PERFECT_EMERALD.get());
        gemUpgradeMap.put(ModItems.BROKEN_RUBY.get(), ModItems.POLISHED_RUBY.get());
        gemUpgradeMap.put(ModItems.POLISHED_RUBY.get(), ModItems.PERFECT_RUBY.get());
        gemUpgradeMap.put(ModItems.BROKEN_SAPPHIRE.get(), ModItems.POLISHED_SAPPHIRE.get());
        gemUpgradeMap.put(ModItems.POLISHED_SAPPHIRE.get(), ModItems.PERFECT_SAPPHIRE.get());
        gemUpgradeMap.put(ModItems.BROKEN_TOPAZ.get(), ModItems.POLISHED_TOPAZ.get());
        gemUpgradeMap.put(ModItems.POLISHED_TOPAZ.get(), ModItems.PERFECT_TOPAZ.get());
    }


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
        setChanged();
        if (level != null){
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isAnimating() {
        return isAnimating;
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

    public boolean canPlayerUse(Player player) {
        UUID playerUUID = player.getUUID();

        if (player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA)
                .map(PlayerTrialDataCapability::isPlayerCursed)
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

    public void cursePlayer(Player player, MobEffect curse, int curseAmplifier) {
        forceDimensionActive();

        BlockPos altarPos = this.getBlockPos();
        UUID playerUUID = player.getUUID();
        Random random = new Random();
        int minMultiple = AncientCursesConfig.CURSE_TIME_MIN.get();
        int maxMultiple = AncientCursesConfig.CURSE_TIME_MAX.get();
        int range = maxMultiple - minMultiple + 1;
        int randomMultiple = random.nextInt(range) + minMultiple;
        int calculatedDuration = randomMultiple * 20;
        int curseDuration = calculatedDuration * (curseAmplifier + 1);

        Trial trial = createTrialForCurse(player, curse, curseDuration, curseAmplifier);
        addPlayerTrial(playerUUID, trial);

        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
            trialData.setCurseEffect(curse);
            trialData.setCurseAmplifier(curseAmplifier);
            trialData.setCurrentAltarPos(altarPos);
            //System.out.println("Setting altar dimension to: " + this.getLevel().dimension());
            trialData.setAltarDimension(Objects.requireNonNull(this.getLevel()).dimension());

            String trialType = trial.getType();
            TrialRecord trialRecord = new TrialRecord(altarPos, trialType, false, false);
            trialData.addOrUpdateTrialRecord(trialRecord);
        });

        player.addEffect(new MobEffectInstance(curse, trial instanceof SurvivalTrial ? curseDuration : MobEffectInstance.INFINITE_DURATION, curseAmplifier, false, false, true));

        trial.trackProgress(player);
    }

    public boolean hasPlayerCompletedTrial(Player player) {
        BlockPos altarPos = this.getBlockPos();
        return player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA)
                .map(trialData -> trialData.hasCompletedTrial(altarPos))
                .orElse(false);
    }

    private boolean anyActiveTrialsRemaining() {
        for (Trial trial : playerTrials.values()) {
            if (!trial.isCompleted()) {
                return true;
            }
        }
        return false;
    }

    public void setPlayerTrialCompleted(Player player) {
        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
            UUID playerUUID = player.getUUID();
            Trial trial = playerTrials.get(playerUUID);
            if (trial != null) {
                BlockPos altarPos = this.getBlockPos();
                trialData.setTrialCompleted(altarPos);
                trial.setCompleted(true);

                if (!anyActiveTrialsRemaining()) {
                    releaseDimensionActive();
                }
                setChanged();
            }
        });
    }

    public boolean hasCollectedReward(Player player) {
        BlockPos altarPos = this.getBlockPos();
        return player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA)
                .map(trialData -> trialData.hasCollectedReward(altarPos))
                .orElse(false);
    }

    public void markRewardCollected(Player player) {
        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
            BlockPos altarPos = this.getBlockPos();
            trialData.setRewardCollected(altarPos);
            trialData.clearCurrentAltarPos();
            setChanged();
        });
    }

    public Trial getPlayerTrial(UUID playerUUID) {
        return playerTrials.get(playerUUID);
    }

    public void addPlayerTrial(UUID playerUUID, Trial trial) {
        playerTrials.put(playerUUID, trial);
    }

    public void removePlayerTrial(UUID playerUUID) {
        playerTrials.remove(playerUUID);
    }

    public void removePlayerFromTrial(Player player) {
        UUID playerUUID = player.getUUID();
        Trial trial = playerTrials.get(playerUUID);
        if (trial != null) {
            if (!trial.isTrialCompleted(player)) {
                removePlayerTrial(playerUUID);
                setChanged();
                //System.out.println("Removed trial for player: " + player.getName().getString()); //debug code
            } else {
                //System.out.println("Attempted to remove a completed trial for player: " + player.getName().getString()); //debug code
            }
        }
    }

    public static MobEffect getRandomCurse() {
        List<MobEffect> curses = Arrays.asList(
                ModEffects.CURSE_OF_SLOTH.get(),
                ModEffects.CURSE_OF_WRATH.get(),
                ModEffects.CURSE_OF_OBESSSION.get(),
                ModEffects.CURSE_OF_SHADOWS.get(),
                ModEffects.CURSE_OF_GLUTTONY.get(),
                ModEffects.CURSE_OF_ENDING.get(),
                ModEffects.CURSE_OF_ENVY.get(),
                ModEffects.CURSE_OF_FRAILTY.get(),
                ModEffects.CURSE_OF_PESTILENCE.get(),
                ModEffects.CURSE_OF_PRIDE.get(),
                ModEffects.CURSE_OF_NATURE.get(),
                ModEffects.CURSE_OF_AVARICE.get()
        );
        return curses.get(new Random().nextInt(curses.size()));
    }

    public static int getRandomAmplifier(Player player) {
        int trialsCompleted = player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA)
                .map(PlayerTrialDataCapability::getPlayerTrialsCompleted)
                .orElse(0);

        List<Integer> weightedAmplifiers = getWeightedAmplifier(trialsCompleted);

        if (weightedAmplifiers.isEmpty()) {
            System.out.println(
                    Component.literal("No amplifiers available at this time.")
                            .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        int count0 = 0;
        int count1 = 0;
        int count2 = 0;
        for (int amplifier : weightedAmplifiers) {
            switch (amplifier) {
                case 0 -> count0++;
                case 1 -> count1++;
                case 2 -> count2++;
                default -> {}
            }
        }

        int total = weightedAmplifiers.size();
        double chance0 = count0 * 100.0 / total;
        double chance1 = count1 * 100.0 / total;
        double chance2 = count2 * 100.0 / total;

        Component message = Component.literal("Trials Completed: ")
                .append(Component.literal(String.valueOf(trialsCompleted)).withStyle(ChatFormatting.GREEN))
                .append(Component.literal("\nAmplifier Chances:\n"))
                .append(Component.literal("• Amplifier 0: ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(String.format("%.2f%%", chance0)).withStyle(ChatFormatting.GOLD))
                .append(Component.literal("\n• Amplifier 1: ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(String.format("%.2f%%", chance1)).withStyle(ChatFormatting.GOLD))
                .append(Component.literal("\n• Amplifier 2: ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(String.format("%.2f%%", chance2)).withStyle(ChatFormatting.GOLD));

        //System.out.println(message);
        return weightedAmplifiers.get(ThreadLocalRandom.current().nextInt(weightedAmplifiers.size()));
    }

    private static @NotNull List<Integer> getWeightedAmplifier(int trialsCompleted) {
        int thresholdTier2 = AncientCursesConfig.CURSED_TRIAL_TIER2_THRESHOLD.get();
        int thresholdTier3 = AncientCursesConfig.CURSED_TRIAL_TIER3_THRESHOLD.get();
        int maxTrials = AncientCursesConfig.CURSED_TRIAL_MAX.get();

        double weight0, weight1, weight2;

        if (trialsCompleted < thresholdTier2) {
            weight0 = AncientCursesConfig.CURSED_TRIAL_TIER1_CHANCE.get();
            weight1 = 0;
            weight2 = 0;
        } else if (trialsCompleted < thresholdTier3) {
            double factor = (trialsCompleted - thresholdTier2) / (double)(thresholdTier3 - thresholdTier2);
            weight0 = AncientCursesConfig.CURSED_TRIAL_TIER1_CHANCE.get() * (1.0 - factor);
            weight1 = AncientCursesConfig.CURSED_TRIAL_TIER2_CHANCE.get() * factor;
            weight2 = 0;
        } else if (trialsCompleted < maxTrials) {
            double factor = (trialsCompleted - thresholdTier3) / (double)(maxTrials - thresholdTier3);
            weight0 = AncientCursesConfig.CURSED_TRIAL_TIER1_CHANCE.get() * (1.0 - factor);
            weight1 = AncientCursesConfig.CURSED_TRIAL_TIER2_CHANCE.get() * (1.0 - factor);
            weight2 = AncientCursesConfig.CURSED_TRIAL_TIER3_CHANCE.get() * factor;
        } else {
            weight0 = 0;
            weight1 = 0;
            weight2 = 1;
        }

        List<Integer> weightedAmplifiers = new ArrayList<>();
        for (int i = 0; i < (int)Math.round(weight0); i++) {
            weightedAmplifiers.add(0);
        }
        for (int i = 0; i < (int)Math.round(weight1); i++) {
            weightedAmplifiers.add(1);
        }
        for (int i = 0; i < (int)Math.round(weight2); i++) {
            weightedAmplifiers.add(2);
        }

        if (weightedAmplifiers.isEmpty()) {
            weightedAmplifiers.add(0);
        }
        return weightedAmplifiers;
    }

    public Trial createTrialForCurse(Player player, MobEffect curseType, int curseDuration, int curseAmplifier) {
        if (curseType == ModEffects.CURSE_OF_AVARICE.get()) {
            return new FetchTrial(player, curseType, curseAmplifier, this);
        } else if (curseType == ModEffects.CURSE_OF_ENDING.get()) {
            //return new SurvivalTrial(player, curseType, curseAmplifier, curseDuration, this);
            return new EliminationTrial(player, curseType, curseAmplifier, this);
        } else if (curseType == ModEffects.CURSE_OF_ENVY.get()) {
            return new EliminationTrial(player, curseType, curseAmplifier, this);
        } else if (curseType == ModEffects.CURSE_OF_FRAILTY.get()) {
            return new EliminationTrial(player, curseType, curseAmplifier, this);
        } else if (curseType == ModEffects.CURSE_OF_GLUTTONY.get()) {
           //return new SurvivalTrial(player, curseType, curseAmplifier, curseDuration, this);
            return new EliminationTrial(player, curseType, curseAmplifier, this);
        } else if (curseType == ModEffects.CURSE_OF_NATURE.get()) {
            return new FetchTrial(player, curseType, curseAmplifier, this);
       } else if (curseType == ModEffects.CURSE_OF_OBESSSION.get()) {
           //return new SurvivalTrial(player, curseType, curseAmplifier, curseDuration, this);
            return new EliminationTrial(player, curseType, curseAmplifier, this);
       } else if (curseType == ModEffects.CURSE_OF_PESTILENCE.get()) {
           //return new SurvivalTrial(player, curseType, curseAmplifier, curseDuration, this);
            return new EliminationTrial(player, curseType, curseAmplifier, this);
        } else if (curseType == ModEffects.CURSE_OF_PRIDE.get()) {
            return new EliminationTrial(player, curseType, curseAmplifier, this);
        } else if (curseType == ModEffects.CURSE_OF_SHADOWS.get()) {
            return new FetchTrial(player, curseType, curseAmplifier, this);
        } else if (curseType == ModEffects.CURSE_OF_SLOTH.get()) {
            return new FetchTrial(player, curseType, curseAmplifier, this);
        } else if (curseType == ModEffects.CURSE_OF_WRATH.get()) {
          return new EliminationTrial(player, curseType, curseAmplifier, this);
        }
        return null;
    }

    public void performGemUpgrade() {
        ItemStack gem1 = getGemInSlot(0);
        ItemStack gem2 = getGemInSlot(1);
        ItemStack gem3 = getGemInSlot(2);
        if (!gem1.isEmpty() && gem1.is(gem2.getItem()) && gem1.is(gem3.getItem())) {
            ItemStack upgradedGem = getUpgradedGem(gem1);
            if (upgradedGem != null) {
                setGemInSlot(0, ItemStack.EMPTY);
                setGemInSlot(1, ItemStack.EMPTY);
                setGemInSlot(2, ItemStack.EMPTY);
                setGemInSlot(0, upgradedGem);
                setChanged();
                if (level != null){
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    level.playSound(
                            null,
                            worldPosition,
                            ModSounds.GEM_PLACE.get(),
                            SoundSource.BLOCKS,
                            1.0F + 0.07f,
                            0.9F + 0.1f + (float) level.getRandom().
                                    nextIntBetweenInclusive(0, 3) / 100
                    );
                }
            }
        }
    }

    public ItemStack getUpgradedGem(ItemStack gem) {
        Item upgradedItem = gemUpgradeMap.get(gem.getItem());
        if (upgradedItem != null) {
            return new ItemStack(upgradedItem);
        } else {
            return null;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CursedAltarBlockEntity blockEntity) {
        blockEntity.serverTick();
    }

    public void serverTick() {
        if (isAnimating) {
            long currentTime = System.currentTimeMillis();
            long animationDuration = 7600;
            if (currentTime - getAnimationStartTime() >= animationDuration) {
                performGemUpgrade();
                stopAnimation();
            }
        }

        if (!anyActiveTrialsRemaining()) {
            if (this.level instanceof ServerLevel serverLevel) {
                if (hasOccupantEntity(serverLevel)){
                    Entity occupant = serverLevel.getEntity(occupantUuid);
                    occupant.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    public static final TicketType<ChunkPos> CURSED_ALTAR_TICKET = TicketType.create("cursed_altar_ticket", Comparator.comparingLong(ChunkPos::toLong),33);

    public boolean isChunkLoaded() {
        return chunkLoaded;
    }

    public void forceLoadChunk() {
        if (!(this.level instanceof ServerLevel serverLevel)) return;
        ChunkPos chunkPos = new ChunkPos(this.worldPosition);
        int ticketDistance = 1;
        serverLevel.getChunkSource().addRegionTicket(
                CURSED_ALTAR_TICKET,
                chunkPos,
                ticketDistance,
                chunkPos
        );
        this.chunkLoaded = true;
        setChanged();
    }

    public void releaseChunkLoad() {
        if (!(this.level instanceof ServerLevel serverLevel)) return;
        ChunkPos chunkPos = new ChunkPos(this.worldPosition);
        int ticketDistance = 1;
        serverLevel.getChunkSource().removeRegionTicket(
                CURSED_ALTAR_TICKET,
                chunkPos,
                ticketDistance,
                chunkPos
        );
        this.chunkLoaded = false;
        setChanged();
    }

    private static final EntityType<ArmorStand> OCCUPANT_TYPE = EntityType.ARMOR_STAND;

    public void forceDimensionActive() {
        if (!(this.level instanceof ServerLevel serverLevel)) return;
        forceLoadChunk();
        if (!hasOccupantEntity(serverLevel)) {
            ArmorStand occupant = OCCUPANT_TYPE.create(serverLevel);
            if (occupant != null) {
                occupant.setPos(this.worldPosition.getX() + 0.5, this.worldPosition.getY() - 2, this.worldPosition.getZ() + 0.5);
                occupant.setInvulnerable(true);
                occupant.setInvisible(true);
                occupant.setCustomName(Component.literal("Cursed Altar Occupant"));
                occupant.setCustomNameVisible(false);
                occupant.setNoGravity(true);
                serverLevel.addFreshEntity(occupant);
                occupantUuid = occupant.getUUID();
            }
        }
    }

    public void releaseDimensionActive() {
        if (!(this.level instanceof ServerLevel serverLevel)) return;
        if (occupantUuid != null) {
            Entity occupantEntity = serverLevel.getEntity(occupantUuid);
            if (occupantEntity != null) {
                occupantEntity.remove(Entity.RemovalReason.DISCARDED);
            }
            occupantUuid = null;
        }
        releaseChunkLoad();
    }

    private boolean hasOccupantEntity(ServerLevel serverLevel) {
        if (occupantUuid != null) {
            Entity occupantEntity = serverLevel.getEntity(occupantUuid);
            return occupantEntity != null;
        }
        return false;
    }

    private Trial reconstructTrialFromNBT(String trialType, CompoundTag trialData) {
        if (trialType.equals(Trial.survivalTrial)) {
            SurvivalTrial trial = new SurvivalTrial(this);
            trial.loadFromNBT(trialData);
            return trial;
        } else if (trialType.equals(Trial.eliminationTrial)) {
            EliminationTrial trial = new EliminationTrial(this);
            trial.loadFromNBT(trialData);
            return trial;
        }
        return null;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsAnimating", this.isAnimating);
        tag.putLong("AnimationStartTime", this.animationStartTime);
        tag.put("gems", itemStackHandler.serializeNBT());

        tag.putBoolean("ChunkLoaded", this.chunkLoaded);
        if (this.occupantUuid != null) {
            tag.putUUID("OccupantUUID", this.occupantUuid);
        }

        ListTag activeTrialsList = new ListTag();
        for (Map.Entry<UUID, Trial> entry : playerTrials.entrySet()) {
            UUID playerUUID = entry.getKey();
            Trial trial = entry.getValue();

            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("PlayerUUID", playerUUID);
            playerTag.putString("TrialType", trial.getType());

            CompoundTag trialData = new CompoundTag();
            trial.saveToNBT(trialData);
            playerTag.put("TrialData", trialData);

            activeTrialsList.add(playerTag);
        }
        tag.put("ActiveTrials", activeTrialsList);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.isAnimating = tag.getBoolean("IsAnimating");
        this.animationStartTime = tag.getLong("AnimationStartTime");
        if (tag.contains("gems")) {
            itemStackHandler.deserializeNBT(tag.getCompound("gems"));
        }

        this.chunkLoaded = tag.getBoolean("ChunkLoaded");
        if (tag.hasUUID("OccupantUUID")) {
            this.occupantUuid = tag.getUUID("OccupantUUID");
        } else {
            this.occupantUuid = null;
        }

        ListTag activeTrialsList = tag.getList("ActiveTrials", Tag.TAG_COMPOUND);
        playerTrials.clear();
        for (int i = 0; i < activeTrialsList.size(); i++) {
            CompoundTag playerTag = activeTrialsList.getCompound(i);
            UUID playerUUID = playerTag.getUUID("PlayerUUID");
            String trialType = playerTag.getString("TrialType");
            CompoundTag trialData = playerTag.getCompound("TrialData");

            Trial trial = reconstructTrialFromNBT(trialType, trialData);
            if (trial != null) {
                trial.setAltar(this);
                playerTrials.put(playerUUID, trial);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.level.isClientSide && this.chunkLoaded) {
            forceDimensionActive();
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


