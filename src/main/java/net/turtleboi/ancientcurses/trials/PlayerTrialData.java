package net.turtleboi.ancientcurses.trials;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

public class PlayerTrialData {
    private static final String IS_CURSED_KEY = "isCursed";
    private static final String CURSE_ID_KEY = "CurseId";
    private static final String CURSE_AMP_KEY = "CurseAmplifier";
    private static final String ALTAR_POS_KEY = "AltarPos";
    private static final String TRIAL_COMPLETED_KEY = "CurseTrialCompleted";
    private static final String NBT_TRIALS_COMPLETED = "TrialsCompleted";
    private static final String COMPLETED_ALTARS_KEY = "CompletedAltars";
    private static final String CURRENT_TRIAL_TYPE = "CurrentTrialType";
    private static final String EFFECT_KEY = "Effect";

    public static final String ELIMINATION_TRIAL = "EliminationTrial";
    public static final String SURVIVAL_TRIAL = "SurvivalTrial";

    public static boolean isPlayerCursed(Player player) {
        return player.getPersistentData().getBoolean(IS_CURSED_KEY);
    }

    public static void setPlayerCursed(Player player, boolean cursed) {
        player.getPersistentData().putBoolean(IS_CURSED_KEY, cursed);
    }

    public static BlockPos getAltarPos(Player player) {
        if (player.getPersistentData().contains(ALTAR_POS_KEY)) {
            long posLong = player.getPersistentData().getLong(ALTAR_POS_KEY);
            return BlockPos.of(posLong);
        }
        return null;
    }

    public static void setAltarPos(Player player, BlockPos altarPos) {
        player.getPersistentData().putLong(ALTAR_POS_KEY, altarPos.asLong());
    }

    public static void resetAltarPos(Player player){
        player.getPersistentData().remove(ALTAR_POS_KEY);
    }

    public static void clearPlayerCurse(Player player) {
        player.getPersistentData().remove(IS_CURSED_KEY);
        player.getPersistentData().remove(CURSE_ID_KEY);
        player.getPersistentData().remove(CURSE_AMP_KEY);
        player.getPersistentData().remove(TRIAL_COMPLETED_KEY);
        player.getPersistentData().remove(CURRENT_TRIAL_TYPE);
    }

    public static void setTrialCompleted(Player player, BlockPos altarPos) {
        CompoundTag completedAltars = player.getPersistentData().getCompound(COMPLETED_ALTARS_KEY);
        completedAltars.putBoolean(altarPos.asLong() + "", true);
        player.getPersistentData().put(COMPLETED_ALTARS_KEY, completedAltars);
    }

    public static boolean hasCompletedTrial(Player player, BlockPos altarPos) {
        CompoundTag completedAltars = player.getPersistentData().getCompound(COMPLETED_ALTARS_KEY);
        return completedAltars.getBoolean(altarPos.asLong() + "");
    }

    public static int getPlayerTrialsCompleted(Player player) {
        return player.getPersistentData().getInt(NBT_TRIALS_COMPLETED);
    }

    public static void incrementPlayerTrialsCompleted(Player player) {
        int currentTrials = getPlayerTrialsCompleted(player);
        player.getPersistentData().putInt(NBT_TRIALS_COMPLETED, currentTrials + 1);
    }

    public static void setCurseAmplifier(Player player, int amplifier) {
        player.getPersistentData().putInt(CURSE_AMP_KEY, amplifier);
    }

    public static int getCurseAmplifier(Player player) {
        return player.getPersistentData().getInt(CURSE_AMP_KEY);
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
                player.getPersistentData().putString(EFFECT_KEY, effectKey.toString());
            }
        }
    }

    public static MobEffect getEffect(Player player) {
        String effectName = player.getPersistentData().getString(EFFECT_KEY);
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
            nbt.putString(EFFECT_KEY, effect.getDescriptionId());
        }

        if (getCurrentTrialType(player).equals(ELIMINATION_TRIAL)) {
            nbt.putInt(EliminationTrial.ELIMINATION_COUNT_KEY, EliminationTrial.getEliminationCount(player));
            nbt.putInt(EliminationTrial.ELIMINATION_REQUIREMENT_KEY, EliminationTrial.getRequiredEliminations(player));
        }

        if (getCurrentTrialType(player).equals(SURVIVAL_TRIAL)) {
            nbt.putLong(SurvivalTrial.TRIAL_DURATION_KEY, SurvivalTrial.getTrialDuration(player));
            nbt.putLong(SurvivalTrial.TRIAL_ELAPSED_TIME_KEY, SurvivalTrial.getTrialElapsedTime(player));
        }

        BlockPos altarPos = getAltarPos(player);
        if (altarPos != null) {
            nbt.putLong(ALTAR_POS_KEY, altarPos.asLong());
        }
        // Other trial data
    }

    public static void loadTrialData(Player player) {
        CompoundTag nbt = player.getPersistentData();
        setCurrentTrialType(player, nbt.getString(CURRENT_TRIAL_TYPE));


        if (nbt.contains(EFFECT_KEY)) {
            String effectName = nbt.getString(EFFECT_KEY);
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
            setEffect(player, effect);
        }

        if (nbt.getString(CURRENT_TRIAL_TYPE).equals(ELIMINATION_TRIAL)){
            EliminationTrial.saveEliminationCount(player, nbt.getInt(EliminationTrial.ELIMINATION_COUNT_KEY));
            EliminationTrial.saveRequiredEliminations(player, nbt.getInt(EliminationTrial.ELIMINATION_REQUIREMENT_KEY));
        }

        if (nbt.getString(CURRENT_TRIAL_TYPE).equals(SURVIVAL_TRIAL)){
            SurvivalTrial.setTrialDuration(player, nbt.getLong(SurvivalTrial.TRIAL_DURATION_KEY));
            SurvivalTrial.setTrialElapsedTime(player, nbt.getLong(SurvivalTrial.TRIAL_ELAPSED_TIME_KEY));
        }

        if (nbt.contains(ALTAR_POS_KEY)) {
            setAltarPos(player, BlockPos.of(nbt.getLong(ALTAR_POS_KEY)));
        }
        // Other trial data
    }

    public static void reconstructTrial(Player player, String trialType) {
        BlockPos altarPos = PlayerTrialData.getAltarPos(player);
        if (altarPos != null) {
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
