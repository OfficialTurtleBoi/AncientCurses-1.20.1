package net.turtleboi.ancientcurses.capabilities.rites;

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
import net.turtleboi.ancientcurses.rites.Rite;
import net.turtleboi.ancientcurses.rites.RiteRecord;

import java.util.ArrayList;
import java.util.List;

@AutoRegisterCapability
public class PlayerRiteDataCapability {
    private String curseEffect = "";
    private int curseAmplifier = 0;
    private BlockPos currentAltarPos = null;
    private List<RiteRecord> riteRecords = new ArrayList<>();
    private ResourceKey<Level> altarDimension = null;
    private Rite activeRite;
    private int pendingRiteUpdateTicks = 0;
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
        resetRiteProgress();
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

    public void addOrUpdateRiteRecord(RiteRecord riteRecord) {
        boolean found = false;
        for (int i = 0; i < riteRecords.size(); i++) {
            RiteRecord existingRecord = riteRecords.get(i);
            if (existingRecord.getAltarPos().equals(riteRecord.getAltarPos())) {
                existingRecord.setRiteType(riteRecord.getRiteType());
                existingRecord.setCompleted(riteRecord.isCompleted());
                existingRecord.setRewardCollected(riteRecord.isRewardCollected());
                found = true;
                break;
            }
        }
        if (!found) {
            riteRecords.add(riteRecord);
        }
    }

    public void setRiteCompleted(BlockPos altarPos) {
        for (RiteRecord record : riteRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                record.setCompleted(true);
                break;
            }
        }
    }

    public boolean hasCompletedRite(BlockPos altarPos) {
        for (RiteRecord record : riteRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                return record.isCompleted();
            }
        }
        return false;
    }

    public void setRewardCollected(BlockPos altarPos) {
        for (RiteRecord record : riteRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                record.setRewardCollected(true);
                break;
            }
        }
    }

    public boolean hasCollectedReward(BlockPos altarPos) {
        for (RiteRecord record : riteRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                return record.isRewardCollected();
            }
        }
        return false;
    }

    public int getPlayerRitesCompleted() {
        int count = 0;
        for (RiteRecord record : riteRecords) {
            if (record.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public void resetAltarAtPos(BlockPos altarPos) {
        for (RiteRecord record : riteRecords) {
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

    public RiteRecord getRiteRecord(BlockPos altarPos) {
        for (RiteRecord record : riteRecords) {
            if (record.getAltarPos().equals(altarPos)) {
                return record;
            }
        }
        return null;
    }

    public Rite getActiveRite() {
        return activeRite;
    }

    public void setActiveRite(Rite rite) {
        this.activeRite = rite;
    }

    public List<RiteRecord> getActiveRitesByType(String trialType) {
        List<RiteRecord> activeTrials = new ArrayList<>();
        for (RiteRecord record : riteRecords) {
            if (record.getRiteType().equals(trialType) && !record.isCompleted()) {
                activeTrials.add(record);
            }
        }
        return activeTrials;
    }

    public void setPendingRiteUpdate(int ticks) {
        this.pendingRiteUpdateTicks = ticks;
    }

    public int getPendingRiteUpdate() {
        return pendingRiteUpdateTicks;
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

    public void resetRiteProgress(){
        this.currentWave = 0;
        this.survivalTicks = 0;
        this.fetchItems = 0;
    }

    public void copyFrom(PlayerRiteDataCapability source) {
        this.curseEffect = source.curseEffect;
        this.curseAmplifier = source.curseAmplifier;
        this.currentAltarPos = source.currentAltarPos;
        this.riteRecords = source.riteRecords;
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

        nbt.putInt("PendingTrialUpdateTicks", pendingRiteUpdateTicks);
        nbt.putInt("EliminationKills", currentWave);
        nbt.putInt("SurvivalTicks", survivalTicks);
        nbt.putInt("FetchItems", fetchItems);

        ListTag riteRecordsTag = new ListTag();
        for (RiteRecord record : riteRecords) {
            riteRecordsTag.add(record.serializeNBT());
        }
        nbt.put("RiteRecords", riteRecordsTag);
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
        pendingRiteUpdateTicks = nbt.getInt("PendingRiteUpdateTicks");
        currentWave = nbt.getInt("EliminationKills");
        survivalTicks = nbt.getInt("SurvivalTicks");
        fetchItems = nbt.getInt("FetchItems");

        riteRecords.clear();
        if (nbt.contains("RiteRecords")) {
            ListTag riteRecordsTag = nbt.getList("RiteRecords", 10);
            for (int i = 0; i < riteRecordsTag.size(); i++) {
                CompoundTag recordTag = riteRecordsTag.getCompound(i);
                RiteRecord record = new RiteRecord();
                record.deserializeNBT(recordTag);
                riteRecords.add(record);
            }
        }
    }
}
