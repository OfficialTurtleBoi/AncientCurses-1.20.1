package net.turtleboi.ancientcurses.rites;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class RiteRecord {
    private BlockPos altarPos;
    private String riteType;
    private boolean completed;
    private boolean rewardCollected;

    public RiteRecord(BlockPos altarPos, String riteType, boolean completed, boolean rewardCollected) {
        this.altarPos = altarPos;
        this.riteType = riteType;
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

    public String getRiteType() {
        return riteType;
    }

    public void setRiteType(String riteType) {
        this.riteType = riteType;
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
        tag.putString("RiteType", riteType);
        tag.putBoolean("Completed", completed);
        tag.putBoolean("RewardCollected", rewardCollected);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.altarPos = BlockPos.of(tag.getLong("AltarPos"));
        this.riteType = tag.getString("RiteType");
        this.completed = tag.getBoolean("Completed");
        this.rewardCollected = tag.getBoolean("RewardCollected");
    }

    public boolean isTrialType(String type) {
        return Objects.equals(this.riteType, type);
    }

    public MobEffect getRiteEffect() {
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(this.riteType));
    }
}
