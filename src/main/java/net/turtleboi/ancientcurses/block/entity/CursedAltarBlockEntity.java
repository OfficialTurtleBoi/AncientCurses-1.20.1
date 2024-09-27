package net.turtleboi.ancientcurses.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.trials.EliminationTrial;
import net.turtleboi.ancientcurses.trials.PlayerTrialData;
import net.turtleboi.ancientcurses.trials.SurvivalTrial;
import net.turtleboi.ancientcurses.trials.Trial;
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
    private final Map<UUID, Trial> playerTrials = new HashMap<>();
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private boolean isAnimating;
    private long animationStartTime;
    private static final Map<Item, Item> gemUpgradeMap = new HashMap<>();

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

        if (PlayerTrialData.isPlayerCursed(player)) {
            //player.sendSystemMessage(Component.literal("You're cursed!").withStyle(ChatFormatting.RED));
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

    public void cursePlayer(Player player, MobEffect curse, int amplifier) {
        UUID playerUUID = player.getUUID();
        int duration = 6000;

        Trial trial = createTrialForCurse(player, curse, duration, amplifier);
        addPlayerTrial(playerUUID, trial);

        PlayerTrialData.setCurseEffect(player, curse);
        PlayerTrialData.setCurseAmplifier(player, amplifier);
        PlayerTrialData.setCurrentAltarPos(player, this.getBlockPos());
        PlayerTrialData.addAltarToTrialList(player, this.getBlockPos(), false);

        player.addEffect(new MobEffectInstance(curse, trial instanceof SurvivalTrial ? duration : MobEffectInstance.INFINITE_DURATION, amplifier, false, false, true));

        trial.trackProgress(player);
    }

    public boolean hasPlayerCompletedTrial(Player player) {
        BlockPos altarPos = this.getBlockPos();
        return PlayerTrialData.hasCompletedTrial(player, altarPos);
    }

    public void setPlayerTrialCompleted(Player player) {
        UUID playerUUID = player.getUUID();
        Trial trial = playerTrials.get(playerUUID);
        if (trial != null) {
            PlayerTrialData.setTrialCompleted(player, this.getBlockPos());
            PlayerTrialData.clearCurrentAltarPos(player);
            playerTrials.remove(playerUUID);
            setChanged();
        }
    }

    public boolean hasCollectedReward(Player player) {
        BlockPos altarPos = this.getBlockPos();
        return PlayerTrialData.hasCollectedReward(player, altarPos);
    }

    public void markRewardCollected(Player player) {
        PlayerTrialData.setRewardCollected(player, this.getBlockPos());
        setChanged();
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
            trial.onPlayerRemoved(player);
            removePlayerTrial(playerUUID);
            setChanged();
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
        int trialsCompleted = PlayerTrialData.getPlayerTrialsCompleted(player);
        List<Integer> weightedAmplifiers = getWeightedAmplifier(trialsCompleted);
        return weightedAmplifiers.get(new Random().nextInt(weightedAmplifiers.size()));
    }

    private static @NotNull List<Integer> getWeightedAmplifier(int trialsCompleted) {
        double completionFactor = Math.min(trialsCompleted / (double) 25, 1.0);

        int amplifier0Weight = (int) Math.max(6 * (1.0 - completionFactor), 0);
        int amplifier1Weight = (int) Math.max(3 * (1.0 - completionFactor), 0);
        int amplifier2Weight = (int) Math.max(1 + (10 * completionFactor), 1);
        List<Integer> weightedAmplifiers = new ArrayList<>();
        for (int i = 0; i < amplifier0Weight; i++) weightedAmplifiers.add(0);
        for (int i = 0; i < amplifier1Weight; i++) weightedAmplifiers.add(1);
        for (int i = 0; i < amplifier2Weight; i++) weightedAmplifiers.add(2);

        return weightedAmplifiers;
    }

    public Trial createTrialForCurse(Player player, MobEffect curseType, int curseDuration, int curseAmplifier) {
        if (curseType == ModEffects.CURSE_OF_AVARICE.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), this);
        } else if (curseType == ModEffects.CURSE_OF_ENDING.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), this);
        } else if (curseType == ModEffects.CURSE_OF_ENVY.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), this);
        } else if (curseType == ModEffects.CURSE_OF_FRAILTY.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), this);
        } else if (curseType == ModEffects.CURSE_OF_GLUTTONY.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), this);
        } else if (curseType == ModEffects.CURSE_OF_NATURE.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), this);
        } else if (curseType == ModEffects.CURSE_OF_OBESSSION.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), this);
        } else if (curseType == ModEffects.CURSE_OF_PESTILENCE.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), this);
        } else if (curseType == ModEffects.CURSE_OF_PRIDE.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), this);
        } else if (curseType == ModEffects.CURSE_OF_SHADOWS.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), this);
        } else if (curseType == ModEffects.CURSE_OF_SLOTH.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), this);
        } else if (curseType == ModEffects.CURSE_OF_WRATH.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), this);
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
            long animationDuration = 7800;
            if (currentTime - getAnimationStartTime() >= animationDuration) {
                performGemUpgrade();
                stopAnimation();
            }
        }
    }

    private Trial reconstructTrialFromNBT(String trialType, CompoundTag trialData) {
        if (trialType.equals(PlayerTrialData.survivalTrial)) {
            SurvivalTrial trial = new SurvivalTrial(this);
            trial.loadFromNBT(trialData);
            return trial;
        } else if (trialType.equals(PlayerTrialData.eliminationTrial)) {
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
