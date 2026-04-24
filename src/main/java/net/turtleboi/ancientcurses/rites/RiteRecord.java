package net.turtleboi.ancientcurses.rites;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import java.util.Objects;

public class RiteRecord {
    private BlockPos altarPos;
    private String riteId;
    private boolean completed;
    private boolean rewardCollected;

    public RiteRecord(BlockPos altarPos, ResourceLocation riteId, boolean completed, boolean rewardCollected) {
        this.altarPos = altarPos;
        setRiteId(riteId);
        this.completed = completed;
        this.rewardCollected = rewardCollected;
    }

    public RiteRecord() {

    }

    public BlockPos getAltarPos() {
        return altarPos;
    }

    public void setAltarPos(BlockPos altarPos) {
        this.altarPos = altarPos;
    }

    public String getRiteIdString() {
        return riteId;
    }

    public void setRiteId(String riteId) {
        setRiteId(ModRites.parse(riteId));
    }

    public void setRiteId(ResourceLocation riteId) {
        this.riteId = riteId != null ? riteId.toString() : "";
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
        tag.putString("RiteId", riteId);
        tag.putBoolean("Completed", completed);
        tag.putBoolean("RewardCollected", rewardCollected);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.altarPos = BlockPos.of(tag.getLong("AltarPos"));
        setRiteId(tag.getString("RiteId"));
        this.completed = tag.getBoolean("Completed");
        this.rewardCollected = tag.getBoolean("RewardCollected");
    }

    public boolean isTrialType(ResourceLocation riteId) {
        return Objects.equals(getRiteId(), riteId);
    }

    public ResourceLocation getRiteId() {
        return ModRites.parse(this.riteId);
    }
}
