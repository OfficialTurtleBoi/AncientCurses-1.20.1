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
import net.turtleboi.ancientcurses.rite.util.RiteRecord;

import java.util.ArrayList;
import java.util.List;

@AutoRegisterCapability
public class PlayerRiteDataCapability {
    private String curseEffect = "";
    private int curseAmplifier = 0;
    private BlockPos currentAltarPos = null;
    private List<RiteRecord> riteRecords = new ArrayList<>();
    private ResourceKey<Level> altarDimension = null;
    private int pendingRiteUpdateTicks = 0;
    private int highestCompletedRiteTier = 0;

    public boolean isPlayerCursed() {
        return !curseEffect.isEmpty() && curseAmplifier != 0;
    }

    public void clearPlayerCurse() {
        curseEffect = "";
        curseAmplifier = 0;
        clearCurrentAltarPos();
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
                existingRecord.setRiteId(riteRecord.getRiteId());
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

    public List<RiteRecord> getActiveRitesByType(ResourceLocation riteId) {
        List<RiteRecord> activeTrials = new ArrayList<>();
        for (RiteRecord record : riteRecords) {
            if (record.isTrialType(riteId) && !record.isCompleted()) {
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

    public void recordCompletedRiteTier(int tier) {
        this.highestCompletedRiteTier = Math.max(this.highestCompletedRiteTier, tier);
    }

    public boolean hasCompletedRiteTier(int tier) {
        return highestCompletedRiteTier >= tier;
    }

    public void copyFrom(PlayerRiteDataCapability source) {
        this.curseEffect = source.curseEffect;
        this.curseAmplifier = source.curseAmplifier;
        this.currentAltarPos = source.currentAltarPos;
        this.riteRecords = source.riteRecords;
        this.highestCompletedRiteTier = source.highestCompletedRiteTier;
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

        nbt.putInt("PendingRiteUpdateTicks", pendingRiteUpdateTicks);
        nbt.putInt("PendingTrialUpdateTicks", pendingRiteUpdateTicks);
        nbt.putInt("HighestCompletedRiteTier", highestCompletedRiteTier);

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
        if (nbt.contains("PendingRiteUpdateTicks")) {
            pendingRiteUpdateTicks = nbt.getInt("PendingRiteUpdateTicks");
        } else {
            pendingRiteUpdateTicks = nbt.getInt("PendingTrialUpdateTicks");
        }
        highestCompletedRiteTier = nbt.getInt("HighestCompletedRiteTier");

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
