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

    private static final String COMPLETED_ALTARS_KEY = "CompletedAltars";

    private static final String CURRENT_TRIAL_TYPE = "CurrentTrialType";
    public static final String ELIMINATION_TRIAL = "EliminationTrial";
    public static final String SURVIVAL_TRIAL = "SurvivalTrial";

    public static boolean isPlayerCursed(Player player) {
        return player.getPersistentData().contains(curseEffect) && player.getPersistentData().contains(curseAmplifier);
    }

    public static void clearPlayerCurse(Player player) {
        player.getPersistentData().remove(curseEffect);
        player.getPersistentData().remove(curseAmplifier);
        player.getPersistentData().remove(CURRENT_TRIAL_TYPE);
    }

    public static void addAltarToTrialList(Player player, BlockPos altarPos, boolean completed) {
        ListTag completedAltarsList = player.getPersistentData().getList(COMPLETED_ALTARS_KEY, 10);
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

        player.getPersistentData().put(COMPLETED_ALTARS_KEY, completedAltarsList);
    }


    public static void setTrialCompleted(Player player, BlockPos altarPos) {
        ListTag completedAltarsList = player.getPersistentData().getList(COMPLETED_ALTARS_KEY, 10);
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag existingAltar = completedAltarsList.getCompound(i);
            if (existingAltar.getLong("AltarPos") == altarPos.asLong()) {
                existingAltar.putBoolean("Completed", true);
                break;
            }
        }
        player.getPersistentData().put(COMPLETED_ALTARS_KEY, completedAltarsList);
    }

    public static boolean hasCompletedTrial(Player player, BlockPos altarPos) {
        ListTag completedAltarsList = player.getPersistentData().getList(COMPLETED_ALTARS_KEY, 10);
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag existingAltar = completedAltarsList.getCompound(i);
            if (existingAltar.getLong("AltarPos") == altarPos.asLong()) {
                return existingAltar.getBoolean("Completed");
            }
        }
        return false;
    }

    public static int getPlayerTrialsCompleted(Player player) {
        ListTag completedAltarsList = player.getPersistentData().getList(COMPLETED_ALTARS_KEY, 10);
        return completedAltarsList.size();
    }

    public static BlockPos getAltarPos(Player player) {
        ListTag completedAltarsList = player.getPersistentData().getList(COMPLETED_ALTARS_KEY, 10);
        if (!completedAltarsList.isEmpty()) {
            CompoundTag firstAltar = completedAltarsList.getCompound(0);
            return BlockPos.of(firstAltar.getLong("AltarPos"));
        }
        return null;
    }

    // Method to reset a specific altar's position
    public static void resetAltarAtPos(Player player, BlockPos altarPos) {
        ListTag completedAltarsList = player.getPersistentData().getList(COMPLETED_ALTARS_KEY, 10);
        ListTag updatedAltarsList = new ListTag();
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag existingAltar = completedAltarsList.getCompound(i);
            if (existingAltar.getLong("AltarPos") != altarPos.asLong()) {
                updatedAltarsList.add(existingAltar);
            }
        }

        player.getPersistentData().put(COMPLETED_ALTARS_KEY, updatedAltarsList);
    }



    public static void setCurseAmplifier(Player player, int amplifier) {
        player.getPersistentData().putInt(curseAmplifier, amplifier);
    }

    public static int getCurseAmplifier(Player player) {
        return player.getPersistentData().getInt(curseAmplifier);
    }

    public static void setCurrentTrialType(Player player, String trialType) {
        player.getPersistentData().putString(CURRENT_TRIAL_TYPE, trialType);
    }

    public static String getCurrentTrialType(Player player) {
        return player.getPersistentData().getString(CURRENT_TRIAL_TYPE);
    }

    public static void clearCurrentTrialType(Player player) {
        player.getPersistentData().remove(CURRENT_TRIAL_TYPE);
    }

    public static void setEffect(Player player, MobEffect effect) {
        if (effect != null && ForgeRegistries.MOB_EFFECTS.containsValue(effect)) {
            ResourceLocation effectKey = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (effectKey != null) {
                player.getPersistentData().putString(curseEffect, effectKey.toString());
            }
        }
    }

    public static MobEffect getEffect(Player player) {
        String effectName = player.getPersistentData().getString(curseEffect);
        if (effectName != null && !effectName.isEmpty()) {
            return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        }
        return null;
    }

    public static void saveTrialData(Player player) {
        CompoundTag nbt = player.getPersistentData();
        nbt.putString(CURRENT_TRIAL_TYPE, getCurrentTrialType(player));

        MobEffect effect = getEffect(player);
        if (effect != null) {
            nbt.putString(curseEffect, effect.getDescriptionId());
        }

        if (getCurrentTrialType(player).equals(ELIMINATION_TRIAL)) {
            nbt.putInt(EliminationTrial.ELIMINATION_COUNT_KEY, EliminationTrial.getEliminationCount(player));
            nbt.putInt(EliminationTrial.ELIMINATION_REQUIREMENT_KEY, EliminationTrial.getRequiredEliminations(player));
        }

        if (getCurrentTrialType(player).equals(SURVIVAL_TRIAL)) {
            nbt.putLong(SurvivalTrial.TRIAL_DURATION_KEY, SurvivalTrial.getTrialDuration(player));
            nbt.putLong(SurvivalTrial.TRIAL_ELAPSED_TIME_KEY, SurvivalTrial.getTrialElapsedTime(player));
        }

        ListTag completedAltarsList = player.getPersistentData().getList(COMPLETED_ALTARS_KEY, 10);
        nbt.put(COMPLETED_ALTARS_KEY, completedAltarsList);
    }


    public static void loadTrialData(Player player) {
        CompoundTag nbt = player.getPersistentData();
        setCurrentTrialType(player, nbt.getString(CURRENT_TRIAL_TYPE));

        if (nbt.contains(curseEffect)) {
            String effectName = nbt.getString(curseEffect);
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
            setEffect(player, effect);
        }

        if (nbt.getString(CURRENT_TRIAL_TYPE).equals(ELIMINATION_TRIAL)) {
            EliminationTrial.saveEliminationCount(player, nbt.getInt(EliminationTrial.ELIMINATION_COUNT_KEY));
            EliminationTrial.saveRequiredEliminations(player, nbt.getInt(EliminationTrial.ELIMINATION_REQUIREMENT_KEY));
        }

        if (nbt.getString(CURRENT_TRIAL_TYPE).equals(SURVIVAL_TRIAL)) {
            SurvivalTrial.setTrialDuration(player, nbt.getLong(SurvivalTrial.TRIAL_DURATION_KEY));
            SurvivalTrial.setTrialElapsedTime(player, nbt.getLong(SurvivalTrial.TRIAL_ELAPSED_TIME_KEY));
        }

        ListTag completedAltarsList = nbt.getList(COMPLETED_ALTARS_KEY, 10);
        player.getPersistentData().put(COMPLETED_ALTARS_KEY, completedAltarsList);
    }


    public static void reconstructTrial(Player player, String trialType) {
        ListTag completedAltarsList = player.getPersistentData().getList(COMPLETED_ALTARS_KEY, 10);
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag altarData = completedAltarsList.getCompound(i);
            BlockPos altarPos = BlockPos.of(altarData.getLong("AltarPos"));
            boolean completed = altarData.getBoolean("Completed");
            if (!completed) {
                BlockEntity blockEntity = player.level().getBlockEntity(altarPos);
                if (blockEntity instanceof CursedAltarBlockEntity altar) {
                    Trial trial = null;
                    MobEffect effect = PlayerTrialData.getEffect(player);
                    if (trialType.equals(ELIMINATION_TRIAL)) {
                        int requiredEliminations = EliminationTrial.getRequiredEliminations(player);
                        trial = new EliminationTrial(player, effect, requiredEliminations, altar);
                    } else if (trialType.equals(SURVIVAL_TRIAL)) {
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
