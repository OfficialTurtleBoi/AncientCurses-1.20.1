package net.turtleboi.ancientcurses.capabilities.trials;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.trials.Trial;
import net.turtleboi.ancientcurses.trials.TrialRecord;

import java.util.ArrayList;
import java.util.List;

@AutoRegisterCapability
public class PlayerTrialDataCapability {
    private String curseEffect = "";
    private int curseAmplifier = 0;
    private BlockPos currentAltarPos = null;
    private List<TrialRecord> trialRecords = new ArrayList<>();
    private ResourceKey<Level> altarDimension = null;
    private Trial activeTrial;
    private int pendingTrialUpdateTicks = 0;
    private int currentWave = 0;
    private int survivalTicks = 0;
    private int fetchItems = 0;

    public boolean isPlayerCursed() {
        return !curseEffect.isEmpty() && curseAmplifier != 0;
    }

    public void clearPlayerCurse() {
        curseEffect = "";
        curseAmplifier = 0;
        clearCurrentAltarPos();
        resetTrialProgress();
    }

    public void clearCurseEffect() {
        curseEffect = "";
    }

    public void clearCurseAmplifier() {
        curseAmplifier = 0;
    }

    public MobEffect getCurseEffect() {
        if (!curseEffect.isEmpty()) {
            ResourceLocation effectKey = new ResourceLocation(curseEffect);
            return ForgeRegistries.MOB_EFFECTS.getValue(effectKey);
        }
        return null;
    }

    public void setCurseEffect(MobEffect effect) {
        if (effect != null) {
            ResourceLocation effectKey = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (effectKey != null) {
                curseEffect = effectKey.toString();
            }
        }
    }

    public int getCurseAmplifier() {
        return curseAmplifier;
    }

    public void setCurseAmplifier(int amplifier) {
        this.curseAmplifier = amplifier + 1;
    }

    public void setCurrentAltarPos(BlockPos altarPos) {
        this.currentAltarPos = altarPos;
    }

    public BlockPos getCurrentAltarPos() {
        return currentAltarPos;
    }

    public void clearCurrentAltarPos() {
        this.currentAltarPos = null;
    }

    public void addOrUpdateTrialRecord(TrialRecord trialRecord) {
        boolean found = false;
        for (int i = 0; i < trialRecords.size(); i++) {
            TrialRecord existingRecord = trialRecords.get(i);
            if (existingRecord.getAltarPos().equals(trialRecord.getAltarPos())) {
                // Update record
                existingRecord.setTrialType(trialRecord.getTrialType());
                existingRecord.setCompleted(trialRecord.isCompleted());
                existingRecord.setRewardCollected(trialRecord.isRewardCollected());
                found = true;
                break;
            }
        }
        if (!found) {
            trialRecords.add(trialRecord);
        }
    }

    public void setTrialCompleted(BlockPos altarPos) {
        for (TrialRecord record : trialRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                record.setCompleted(true);
                break;
            }
        }
    }

    public boolean hasCompletedTrial(BlockPos altarPos) {
        for (TrialRecord record : trialRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                return record.isCompleted();
            }
        }
        return false;
    }

    public void setRewardCollected(BlockPos altarPos) {
        for (TrialRecord record : trialRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                record.setRewardCollected(true);
                break;
            }
        }
    }

    public boolean hasCollectedReward(BlockPos altarPos) {
        for (TrialRecord record : trialRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                return record.isRewardCollected();
            }
        }
        return false;
    }

    public int getPlayerTrialsCompleted() {
        int count = 0;
        for (TrialRecord record : trialRecords) {
            if (record.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public void resetAltarAtPos(BlockPos altarPos) {
        for (TrialRecord record : trialRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                record.setCompleted(false);
                record.setRewardCollected(false);
            }
        }
    }

    public ResourceKey<Level> getAltarDimension() {
        return altarDimension;
    }

    public void setAltarDimension(ResourceKey<Level> dimension) {
        this.altarDimension = dimension;
    }

    public TrialRecord getTrialRecord(BlockPos altarPos) {
        for (TrialRecord record : trialRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                return record;
            }
        }
        return null;
    }

    public Trial getActiveTrial() {
        return activeTrial;
    }

    public void setActiveTrial(Trial trial) {
        this.activeTrial = trial;
    }

    public List<TrialRecord> getActiveTrialsByType(String trialType) {
        List<TrialRecord> activeTrials = new ArrayList<>();
        for (TrialRecord record : trialRecords) {
            if (record.getTrialType().equals(trialType) && !record.isCompleted()) {
                activeTrials.add(record);
            }
        }
        return activeTrials;
    }

    public void setPendingTrialUpdate(int ticks) {
        this.pendingTrialUpdateTicks = ticks;
    }

    public int getPendingTrialUpdate() {
        return pendingTrialUpdateTicks;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(int currentWave) {
        this.currentWave = currentWave;
    }

    public int getSurvivalTicks() {
        return survivalTicks;
    }

    public void setSurvivalTicks(int survivalTicks) {
        this.survivalTicks = survivalTicks;
    }

    public int getFetchItems() {
        return fetchItems;
    }

    public void setFetchItems(int itemCount) {
        this.fetchItems = itemCount;
    }

    public void resetTrialProgress(){
        this.currentWave = 0;
        this.survivalTicks = 0;
        this.fetchItems = 0;
    }

    public void copyFrom(PlayerTrialDataCapability source) {
        this.curseEffect = source.curseEffect;
        this.curseAmplifier = source.curseAmplifier;
        this.currentAltarPos = source.currentAltarPos;
        this.trialRecords = source.trialRecords;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putString("CurseEffect", curseEffect);
        nbt.putInt("CurseAmplifier", curseAmplifier);
        if (currentAltarPos != null) {
            nbt.putLong("CurrentAltarPos", currentAltarPos.asLong());
        }

        if (altarDimension != null) {
            nbt.putString("AltarDimension", altarDimension.location().toString());
        }

        nbt.putInt("PendingTrialUpdateTicks", pendingTrialUpdateTicks);
        nbt.putInt("EliminationKills", currentWave);
        nbt.putInt("SurvivalTicks", survivalTicks);
        nbt.putInt("FetchItems", fetchItems);

        ListTag trialRecordsTag = new ListTag();
        for (TrialRecord record : trialRecords) {
            trialRecordsTag.add(record.serializeNBT());
        }
        nbt.put("TrialRecords", trialRecordsTag);
    }

    public void loadNBTData(CompoundTag nbt) {
        curseEffect = nbt.getString("CurseEffect");
        curseAmplifier = nbt.getInt("CurseAmplifier");
        if (nbt.contains("CurrentAltarPos")) {
            long posLong = nbt.getLong("CurrentAltarPos");
            currentAltarPos = BlockPos.of(posLong);
        } else {
            currentAltarPos = null;
        }

        if (nbt.contains("AltarDimension")) {
            String dimensionString = nbt.getString("AltarDimension");
            altarDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimensionString));
        } else {
            altarDimension = null;
        }
        pendingTrialUpdateTicks = nbt.getInt("PendingTrialUpdateTicks");
        currentWave = nbt.getInt("EliminationKills");
        survivalTicks = nbt.getInt("SurvivalTicks");
        fetchItems = nbt.getInt("FetchItems");

        trialRecords.clear();
        if (nbt.contains("TrialRecords")) {
            ListTag trialRecordsTag = nbt.getList("TrialRecords", 10);
            for (int i = 0; i < trialRecordsTag.size(); i++) {
                CompoundTag recordTag = trialRecordsTag.getCompound(i);
                TrialRecord record = new TrialRecord();
                record.deserializeNBT(recordTag);
                trialRecords.add(record);
            }
        }
    }
}
