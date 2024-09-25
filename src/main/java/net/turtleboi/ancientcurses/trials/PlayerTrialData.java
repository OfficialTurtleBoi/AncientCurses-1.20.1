package net.turtleboi.ancientcurses.trials;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;

public class PlayerTrialData {
    private static final String curseEffect = "CurseEffect";
    private static final String curseAmplifier = "CurseAmplifier";

    private static final String currentAltarPos = "CurrentAltarPos";
    private static final String completedAltars = "CompletedAltars";

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

    public static void addAltarToTrialList(Player player, BlockPos altarPos, boolean completed) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        CompoundTag altarData = new CompoundTag();
        altarData.putLong("AltarPos", altarPos.asLong());
        altarData.putBoolean("Completed", completed);
        altarData.putBoolean("RewardCollected", false);
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

    public static void setRewardCollected(Player player, BlockPos altarPos) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag existingAltar = completedAltarsList.getCompound(i);
            if (existingAltar.getLong("AltarPos") == altarPos.asLong()) {
                existingAltar.putBoolean("RewardCollected", true);
                break;
            }
        }
        player.getPersistentData().put(completedAltars, completedAltarsList);
    }

    public static boolean hasCollectedReward(Player player, BlockPos altarPos) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        for (int i = 0; i < completedAltarsList.size(); i++) {
            CompoundTag existingAltar = completedAltarsList.getCompound(i);
            if (existingAltar.getLong("AltarPos") == altarPos.asLong()) {
                return existingAltar.getBoolean("RewardCollected");
            }
        }
        return false;
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

    public static void clearCurseEffect(Player player) {
        player.getPersistentData().remove(curseEffect);
    }

    public static int getCurseAmplifier(Player player) {
        return player.getPersistentData().getInt(curseAmplifier);
    }

    public static void setCurseAmplifier(Player player, int amplifier) {
        player.getPersistentData().putInt(curseAmplifier, amplifier);
    }

    public static void clearCurseAmplifier(Player player) {
        player.getPersistentData().remove(curseAmplifier);
    }

    public static int getPlayerTrialsCompleted(Player player) {
        ListTag completedAltarsList = player.getPersistentData().getList(completedAltars, 10);
        return completedAltarsList.size();
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
}
