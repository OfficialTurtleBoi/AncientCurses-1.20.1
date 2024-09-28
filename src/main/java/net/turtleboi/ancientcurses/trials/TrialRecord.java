package net.turtleboi.ancientcurses.trials;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class TrialRecord {
    private BlockPos altarPos;
    private String trialType;
    private boolean completed;
    private boolean rewardCollected;

    public TrialRecord(BlockPos altarPos, String trialType, boolean completed, boolean rewardCollected) {
        this.altarPos = altarPos;
        this.trialType = trialType;
        this.completed = completed;
        this.rewardCollected = rewardCollected;
    }

    public TrialRecord() {

    }

    public BlockPos getAltarPos() {
        return altarPos;
    }

    public void setAltarPos(BlockPos altarPos) {
        this.altarPos = altarPos;
    }

    public String getTrialType() {
        return trialType;
    }

    public void setTrialType(String trialType) {
        this.trialType = trialType;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isRewardCollected() {
        return rewardCollected;
    }

    public void setRewardCollected(boolean rewardCollected) {
        this.rewardCollected = rewardCollected;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("AltarPos", altarPos.asLong());
        tag.putString("TrialType", trialType);
        tag.putBoolean("Completed", completed);
        tag.putBoolean("RewardCollected", rewardCollected);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.altarPos = BlockPos.of(tag.getLong("AltarPos"));
        this.trialType = tag.getString("TrialType");
        this.completed = tag.getBoolean("Completed");
        this.rewardCollected = tag.getBoolean("RewardCollected");
    }

    public boolean isTrialType(String type) {
        return Objects.equals(this.trialType, type);
    }

    public MobEffect getTrialEffect() {
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(this.trialType));
    }
}
