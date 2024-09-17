package net.turtleboi.ancientcurses.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.turtleboi.ancientcurses.effect.ModEffects;
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
    private static final Map<UUID, Boolean> playerTrialCompletion = new HashMap<>();
    private static final Map<UUID, Trial> playerTrials = new HashMap<>();
    private static final long cooldownTime = 250;
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();

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
    }

    public static void bookAnimationTick(Level pLevel, BlockPos pPos, BlockState pState, CursedAltarBlockEntity pBlockEntity) {
        pBlockEntity.oOpen = pBlockEntity.open;
        pBlockEntity.oRot = pBlockEntity.rot;
        Player $$4 = pLevel.getNearestPlayer((double)pPos.getX() + 0.5, (double)pPos.getY() + 0.5, (double)pPos.getZ() + 0.5, 3.0, false);
        if ($$4 != null) {
            double $$5 = $$4.getX() - ((double)pPos.getX() + 0.5);
            double $$6 = $$4.getZ() - ((double)pPos.getZ() + 0.5);
            pBlockEntity.tRot = (float) Mth.atan2($$6, $$5);
            pBlockEntity.open += 0.1F;
            if (pBlockEntity.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float $$7 = pBlockEntity.flipT;

                do {
                    pBlockEntity.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while($$7 == pBlockEntity.flipT);
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

        float $$8;
        for($$8 = pBlockEntity.tRot - pBlockEntity.rot; $$8 >= 3.1415927F; $$8 -= 6.2831855F) {
        }

        while($$8 < -3.1415927F) {
            $$8 += 6.2831855F;
        }

        pBlockEntity.rot += $$8 * 0.4F;
        pBlockEntity.open = Mth.clamp(pBlockEntity.open, 0.0F, 1.0F);
        ++pBlockEntity.time;
        pBlockEntity.oFlip = pBlockEntity.flip;
        float $$9 = (pBlockEntity.flipT - pBlockEntity.flip) * 0.4F;
        float $$10 = 0.2F;
        $$9 = Mth.clamp($$9, -0.2F, 0.2F);
        pBlockEntity.flipA += ($$9 - pBlockEntity.flipA) * 0.9F;
        pBlockEntity.flip += pBlockEntity.flipA;
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
            player.sendSystemMessage(Component.literal("You're cursed!").withStyle(ChatFormatting.RED));
            return false;
        }

        long currentTime = System.currentTimeMillis();
        if (playerCooldowns.containsKey(playerUUID)) {
            long lastUseTime = playerCooldowns.get(playerUUID);
            if (currentTime - lastUseTime < cooldownTime) {
                player.sendSystemMessage(Component.literal("Altar is recharging...").withStyle(ChatFormatting.RED));
                return false;
            }
        }
        return true;
    }

    public void setPlayerCooldown(Player player) {
        UUID playerUUID = player.getUUID();
        playerCooldowns.put(playerUUID, System.currentTimeMillis());
    }

    public void cursePlayer(Player player, MobEffect curse, int pAmplifier, CursedAltarBlockEntity altar) {
        UUID playerUUID = player.getUUID();
        int duration = 6000;

        player.addEffect(new MobEffectInstance(curse, duration, pAmplifier, false, false, true));
        Trial trial = createTrialForCurse(player, curse, duration, pAmplifier, altar);
        altar.addPlayerTrial(playerUUID, trial);

        if (trial instanceof EliminationTrial) {
            PlayerTrialData.setCurrentTrialType(player, PlayerTrialData.ELIMINATION_TRIAL);
        }

        if (trial instanceof SurvivalTrial) {
            PlayerTrialData.setCurrentTrialType(player, PlayerTrialData.SURVIVAL_TRIAL);
        }

        PlayerTrialData.setCurseAmplifier(player, pAmplifier);
        PlayerTrialData.addAltarToTrialList(player, altar.worldPosition, false);
    }

    public boolean hasPlayerCompletedTrial(Player player) {
        BlockPos altarPos = this.getBlockPos();
        return PlayerTrialData.hasCompletedTrial(player, altarPos);
    }

    public void setPlayerTrialCompleted(Player player) {
        BlockPos altarPos = this.getBlockPos();
        PlayerTrialData.setTrialCompleted(player, altarPos);
    }

    public void setPlayerTrialStatus(UUID playerUUID, boolean completed) {
        playerTrialCompletion.put(playerUUID, completed);
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

    public Trial createTrialForCurse(Player player, MobEffect curseType, int curseDuration, int curseAmplifier, CursedAltarBlockEntity altar) {
        if (curseType == ModEffects.CURSE_OF_AVARICE.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), altar);
        } else if (curseType == ModEffects.CURSE_OF_ENDING.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), altar);
        } else if (curseType == ModEffects.CURSE_OF_ENVY.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), altar);
        } else if (curseType == ModEffects.CURSE_OF_FRAILTY.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), altar);
        } else if (curseType == ModEffects.CURSE_OF_GLUTTONY.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), altar);
        } else if (curseType == ModEffects.CURSE_OF_NATURE.get()) {
            MobEffectInstance curseInstance = new MobEffectInstance(curseType, curseDuration);
            return new SurvivalTrial(player, curseType, curseInstance.getDuration(), altar);
        } else if (curseType == ModEffects.CURSE_OF_OBESSSION.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), altar);
        } else if (curseType == ModEffects.CURSE_OF_PESTILENCE.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), altar);
        } else if (curseType == ModEffects.CURSE_OF_PRIDE.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), altar);
        } else if (curseType == ModEffects.CURSE_OF_SHADOWS.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), altar);
        } else if (curseType == ModEffects.CURSE_OF_SLOTH.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), altar);
        } else if (curseType == ModEffects.CURSE_OF_WRATH.get()) {
            return new EliminationTrial(player, curseType, 10 * (curseAmplifier + 1), altar);
        }
        return null;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("gems", itemStackHandler.serializeNBT());
        ListTag completedPlayerList = new ListTag();
        for (UUID playerUUID : playerTrialCompletion.keySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("PlayerUUID", playerUUID);
            playerTag.putBoolean("Completed", playerTrialCompletion.get(playerUUID));
            completedPlayerList.add(playerTag);
        }
        tag.put("PlayerTrialCompletion", completedPlayerList);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("gems")) {
            itemStackHandler.deserializeNBT(tag.getCompound("gems"));
        }
        ListTag completedPlayerList = tag.getList("PlayerTrialCompletion", Tag.TAG_COMPOUND);
        for (int i = 0; i < completedPlayerList.size(); i++) {
            CompoundTag playerTag = completedPlayerList.getCompound(i);
            UUID playerUUID = playerTag.getUUID("PlayerUUID");
            boolean completed = playerTag.getBoolean("Completed");
            playerTrialCompletion.put(playerUUID, completed);
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
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}
