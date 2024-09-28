package net.turtleboi.ancientcurses.trials;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class PlayerTrialData {
    private static final String curseEffect = "CurseEffect";
    private static final String curseAmplifier = "CurseAmplifier";

    private static final String currentAltarPos = "CurrentAltarPos";
    private static final String trialRecords = "TrialRecords";

    public static final String eliminationTrial = "EliminationTrial";
    public static final String survivalTrial = "SurvivalTrial";
    public static final String fetchTrial = "FetchTrial";
    public static final String seekTrial = "SeekTrial";

    public static boolean isPlayerCursed(Player player) {
        return player.getPersistentData().contains(curseEffect) && player.getPersistentData().contains(curseAmplifier);
    }

    public static void clearPlayerCurse(Player player) {
        clearCurseEffect(player);
        clearCurseAmplifier(player);
        clearCurrentAltarPos(player);
    }

    public static void setCurrentAltarPos(Player player, BlockPos altarPos) {
        player.getPersistentData().putLong(currentAltarPos, altarPos.asLong());
    }

    public static BlockPos getCurrentAltarPos(Player player) {
        if (player.getPersistentData().contains(currentAltarPos)) {
            long posLong = player.getPersistentData().getLong(currentAltarPos);
            return BlockPos.of(posLong);
        }
        return null;
    }

    public static void clearCurrentAltarPos(Player player) {
        player.getPersistentData().remove(currentAltarPos);
    }

    public static void addOrUpdateTrialRecord(Player player, TrialRecord trialRecord) {
        ListTag trialRecordsList = player.getPersistentData().getList(trialRecords, 10);
        boolean found = false;

        for (int i = 0; i < trialRecordsList.size(); i++) {
            CompoundTag existingTag = trialRecordsList.getCompound(i);
            TrialRecord existingRecord = new TrialRecord();
            existingRecord.deserializeNBT(existingTag);
            if (existingRecord.getAltarPos().equals(trialRecord.getAltarPos())) {
                existingRecord.setTrialType(trialRecord.getTrialType());
                existingRecord.setCompleted(trialRecord.isCompleted());
                existingRecord.setRewardCollected(trialRecord.isRewardCollected());
                trialRecordsList.set(i, existingRecord.serializeNBT());
                found = true;
                break;
            }
        }

        if (!found) {
            trialRecordsList.add(trialRecord.serializeNBT());
        }

        player.getPersistentData().put(trialRecords, trialRecordsList);
    }

    public static void setTrialCompleted(Player player, BlockPos altarPos) {
        ListTag trialRecordsList = player.getPersistentData().getList(trialRecords, 10);
        for (int i = 0; i < trialRecordsList.size(); i++) {
            CompoundTag existingTag = trialRecordsList.getCompound(i);
            TrialRecord existingRecord = new TrialRecord();
            existingRecord.deserializeNBT(existingTag);
            if (existingRecord.getAltarPos().equals(altarPos)) {
                existingRecord.setCompleted(true);
                trialRecordsList.set(i, existingRecord.serializeNBT());
                //System.out.println("Player " + player.getName().getString() + " completed trial at altar " + altarPos);
                break;
            }
        }
        player.getPersistentData().put(trialRecords, trialRecordsList);
    }

    public static boolean hasCompletedTrial(Player player, BlockPos altarPos) {
        ListTag trialRecordsList = player.getPersistentData().getList(trialRecords, 10);
        for (int i = 0; i < trialRecordsList.size(); i++) {
            CompoundTag existingTag = trialRecordsList.getCompound(i);
            TrialRecord existingRecord = new TrialRecord();
            existingRecord.deserializeNBT(existingTag);
            if (existingRecord.getAltarPos().equals(altarPos)) {
                return existingRecord.isCompleted();
            }
        }
        return false;
    }

    public static void setRewardCollected(Player player, BlockPos altarPos) {
        ListTag trialRecordsList = player.getPersistentData().getList(trialRecords, 10);
        for (int i = 0; i < trialRecordsList.size(); i++) {
            CompoundTag existingTag = trialRecordsList.getCompound(i);
            TrialRecord existingRecord = new TrialRecord();
            existingRecord.deserializeNBT(existingTag);
            if (existingRecord.getAltarPos().equals(altarPos)) {
                existingRecord.setRewardCollected(true);
                trialRecordsList.set(i, existingRecord.serializeNBT());
                //System.out.println("Player " + player.getName().getString() + " collected reward at altar " + altarPos);
                break;
            }
        }
        player.getPersistentData().put(trialRecords, trialRecordsList);
    }

    public static boolean hasCollectedReward(Player player, BlockPos altarPos) {
        ListTag trialRecordsList = player.getPersistentData().getList(trialRecords, 10);
        for (int i = 0; i < trialRecordsList.size(); i++) {
            CompoundTag existingTag = trialRecordsList.getCompound(i);
            TrialRecord existingRecord = new TrialRecord();
            existingRecord.deserializeNBT(existingTag);
            if (existingRecord.getAltarPos().equals(altarPos)) {
                return existingRecord.isRewardCollected();
            }
        }
        return false;
    }

    public static int getPlayerTrialsCompleted(Player player) {
        ListTag trialRecordsList = player.getPersistentData().getList(trialRecords, 10);
        int count = 0;
        for (int i = 0; i < trialRecordsList.size(); i++) {
            CompoundTag existingTag = trialRecordsList.getCompound(i);
            TrialRecord existingRecord = new TrialRecord();
            existingRecord.deserializeNBT(existingTag);
            if (existingRecord.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public static void resetAltarAtPos(Player player, BlockPos altarPos) {
        ListTag trialRecordsList = player.getPersistentData().getList(trialRecords, 10);
        ListTag updatedTrialRecords = new ListTag();
        for (int i = 0; i < trialRecordsList.size(); i++) {
            CompoundTag existingTag = trialRecordsList.getCompound(i);
            TrialRecord existingRecord = new TrialRecord();
            existingRecord.deserializeNBT(existingTag);
            if (!existingRecord.getAltarPos().equals(altarPos)) {
                updatedTrialRecords.add(existingTag.copy());
            } else {
                existingRecord.setCompleted(false);
                existingRecord.setRewardCollected(false);
                updatedTrialRecords.add(existingRecord.serializeNBT());
            }
        }
        player.getPersistentData().put(trialRecords, updatedTrialRecords);
        //System.out.println("Trial at altar " + altarPos + " has been reset for player " + player.getName().getString());
    }

    public static TrialRecord getTrialRecord(Player player, BlockPos altarPos) {
        ListTag trialRecordsList = player.getPersistentData().getList(trialRecords, 10);
        for (int i = 0; i < trialRecordsList.size(); i++) {
            CompoundTag existingTag = trialRecordsList.getCompound(i);
            TrialRecord existingRecord = new TrialRecord();
            existingRecord.deserializeNBT(existingTag);
            if (existingRecord.getAltarPos().equals(altarPos)) {
                return existingRecord;
            }
        }
        return null;
    }

    public static List<TrialRecord> getActiveTrialsByType(Player player, String trialType) {
        List<TrialRecord> activeTrials = new ArrayList<>();
        ListTag trialRecordsList = player.getPersistentData().getList(trialRecords, 10);

        for (int i = 0; i < trialRecordsList.size(); i++) {
            CompoundTag trialTag = trialRecordsList.getCompound(i);
            TrialRecord trialRecord = new TrialRecord();
            trialRecord.deserializeNBT(trialTag);

            if (trialRecord.getTrialType().equals(trialType) && !trialRecord.isCompleted()) {
                activeTrials.add(trialRecord);
            }
        }

        return activeTrials;
    }

    public static void clearCurseEffect(Player player) {
        player.getPersistentData().remove(curseEffect);
    }

    public static MobEffect getCurseEffect(Player player) {
        if (player.getPersistentData().contains(curseEffect)) {
            String effectName = player.getPersistentData().getString(curseEffect);
            return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        }
        return null;
    }

    public static void setCurseEffect(Player player, MobEffect effect) {
        if (effect != null && ForgeRegistries.MOB_EFFECTS.containsValue(effect)) {
            ResourceLocation effectKey = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (effectKey != null) {
                player.getPersistentData().putString(curseEffect, effectKey.toString());
            }
        }
    }

    public static void clearCurseAmplifier(Player player) {
        player.getPersistentData().remove(curseAmplifier);
    }

    public static int getCurseAmplifier(Player player) {
        return player.getPersistentData().getInt(curseAmplifier);
    }

    public static void setCurseAmplifier(Player player, int amplifier) {
        player.getPersistentData().putInt(curseAmplifier, amplifier);
    }
}
