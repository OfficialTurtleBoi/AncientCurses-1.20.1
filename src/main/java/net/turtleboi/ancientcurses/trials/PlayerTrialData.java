package net.turtleboi.ancientcurses.trials;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

public class PlayerTrialData {
    private static final String curseEffect = "CurseEffect";
    private static final String curseAmplifier = "CurseAmplifier";

    private static final String completedAltars = "CompletedAltars";

    private static final String currentTrialType = "CurrentTrialType";
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
        clearCurrentTrialType(player);
    }

    public static void addAltarToTrialList(Player player, BlockPos altarPos, boolean completed) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        CompoundTag altarData = new CompoundTag();
        altarData.putLong("AltarPos", altarPos.asLong());
        altarData.putBoolean("Completed", completed);
        boolean alreadyInList = false;
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag existingAltar = completedAltarsList.getCompound(i);
            if (existingAltar.getLong("AltarPos") == altarPos.asLong()) {
                alreadyInList = true;
                break;
            }
        }

        if (!alreadyInList) {
            completedAltarsList.add(altarData);
        }

        player.getPersistentData().put(completedAltars, completedAltarsList);
    }


    public static void setTrialCompleted(Player player, BlockPos altarPos) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag existingAltar = completedAltarsList.getCompound(i);
            if (existingAltar.getLong("AltarPos") == altarPos.asLong()) {
                existingAltar.putBoolean("Completed", true);
                break;
            }
        }
        player.getPersistentData().put(completedAltars, completedAltarsList);
    }

    public static boolean hasCompletedTrial(Player player, BlockPos altarPos) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag existingAltar = completedAltarsList.getCompound(i);
            if (existingAltar.getLong("AltarPos") == altarPos.asLong()) {
                return existingAltar.getBoolean("Completed");
            }
        }
        return false;
    }

    public static int getPlayerTrialsCompleted(Player player) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        return completedAltarsList.size();
    }

    public static BlockPos getAltarPos(Player player) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        if (!completedAltarsList.isEmpty()) {
            CompoundTag firstAltar = completedAltarsList.getCompound(0);
            return BlockPos.of(firstAltar.getLong("AltarPos"));
        }
        return null;
    }

    public static void resetAltarAtPos(Player player, BlockPos altarPos) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        ListTag updatedAltarsList = new ListTag();
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag existingAltar = completedAltarsList.getCompound(i);
            if (existingAltar.getLong("AltarPos") != altarPos.asLong()) {
                updatedAltarsList.add(existingAltar);
            }
        }

        player.getPersistentData().put(completedAltars, updatedAltarsList);
    }

    public static void setCurseEffect(Player player, MobEffect effect) {
        if (effect != null && ForgeRegistries.MOB_EFFECTS.containsValue(effect)) {
            ResourceLocation effectKey = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (effectKey != null) {
                player.getPersistentData().putString(curseEffect, effectKey.toString());
            }
        }
    }

    public static MobEffect getCurseEffect(Player player) {
        String effectName = player.getPersistentData().getString(curseEffect);
        if (effectName != null && !effectName.isEmpty()) {
            return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        }
        return null;
    }

    public static void clearCurseEffect(Player player) {
        player.getPersistentData().remove(curseEffect);
    }

    public static void setCurseAmplifier(Player player, int amplifier) {
        player.getPersistentData().putInt(curseAmplifier, amplifier);
    }

    public static int getCurseAmplifier(Player player) {
        return player.getPersistentData().getInt(curseAmplifier);
    }

    public static void clearCurseAmplifier(Player player) {
        player.getPersistentData().remove(curseAmplifier);
    }

    public static void setCurrentTrialType(Player player, String trialType) {
        player.getPersistentData().putString(currentTrialType, trialType);
    }

    public static String getCurrentTrialType(Player player) {
        return player.getPersistentData().getString(currentTrialType);
    }

    public static void clearCurrentTrialType(Player player) {
        player.getPersistentData().remove(currentTrialType);
    }



    public static void saveTrialData(Player player) {
        CompoundTag nbt = player.getPersistentData();
        nbt.putString(currentTrialType, getCurrentTrialType(player));

        MobEffect effect = getCurseEffect(player);
        if (effect != null) {
            nbt.putString(curseEffect, effect.getDescriptionId());
        }

        if (getCurrentTrialType(player).equals(eliminationTrial)) {
            nbt.putInt(EliminationTrial.eliminationCount, EliminationTrial.getEliminationCount(player));
            nbt.putInt(EliminationTrial.eliminationRequirement, EliminationTrial.getRequiredEliminations(player));
        }

        if (getCurrentTrialType(player).equals(survivalTrial)) {
            nbt.putLong(SurvivalTrial.trialDurationTotal, SurvivalTrial.getTrialDuration(player));
            nbt.putLong(SurvivalTrial.trialDurationElapsed, SurvivalTrial.getTrialElapsedTime(player));
        }

        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        nbt.put(completedAltars, completedAltarsList);
    }


    public static void loadTrialData(Player player) {
        CompoundTag nbt = player.getPersistentData();
        setCurrentTrialType(player, nbt.getString(currentTrialType));

        if (nbt.contains(curseEffect)) {
            String effectName = nbt.getString(curseEffect);
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
            setCurseEffect(player, effect);
        }

        if (nbt.getString(currentTrialType).equals(eliminationTrial)) {
            EliminationTrial.saveEliminationCount(player, nbt.getInt(EliminationTrial.eliminationCount));
            EliminationTrial.saveRequiredEliminations(player, nbt.getInt(EliminationTrial.eliminationRequirement));
        }

        if (nbt.getString(currentTrialType).equals(survivalTrial)) {
            SurvivalTrial.setTrialDuration(player, nbt.getLong(SurvivalTrial.trialDurationTotal));
            SurvivalTrial.setTrialElapsedTime(player, nbt.getLong(SurvivalTrial.trialDurationElapsed));
        }

        ListTag completedAltarsList = nbt.getList(completedAltars, 10);
        player.getPersistentData().put(completedAltars, completedAltarsList);
    }


    public static void reconstructTrial(Player player, String trialType) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag altarData = completedAltarsList.getCompound(i);
            BlockPos altarPos = BlockPos.of(altarData.getLong("AltarPos"));
            boolean completed = altarData.getBoolean("Completed");
            if (!completed) {
                BlockEntity blockEntity = player.level().getBlockEntity(altarPos);
                if (blockEntity instanceof CursedAltarBlockEntity altar) {
                    Trial trial = null;
                    MobEffect effect = PlayerTrialData.getCurseEffect(player);
                    if (trialType.equals(eliminationTrial)) {
                        int requiredEliminations = EliminationTrial.getRequiredEliminations(player);
                        trial = new EliminationTrial(player, effect, requiredEliminations, altar);
                    } else if (trialType.equals(survivalTrial)) {
                        long trialDuration = SurvivalTrial.getTrialDuration(player);
                        long elapsedTime = SurvivalTrial.getTrialElapsedTime(player);
                        long remainingTime = trialDuration - elapsedTime;
                        trial = new SurvivalTrial(player, effect, remainingTime, altar);
                    }
                    if (trial != null) {
                        altar.addPlayerTrial(player.getUUID(), trial);
                    }
                }
            }
        }
    }

}
