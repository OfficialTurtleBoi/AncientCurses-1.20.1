package net.turtleboi.ancientcurses.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

public interface Rite {
    void trackProgress(Player player);
    boolean isRiteCompleted(Player player);
    void concludeRite(Player player);

    void onEntityKilled(Player player, Entity entity);
    void onPlayerTick(Player player);

    void saveToNBT(CompoundTag tag);
    void loadFromNBT(CompoundTag tag);
    ResourceLocation getId();

    MobEffect getEffect();
    void setAltar(CursedAltarBlockEntity altar);
    CursedAltarBlockEntity getAltar();

    boolean isCompleted();
    void setCompleted(boolean completed);

    default String getType() {
        return getId().toString();
    }

    default int getCompletionDegree() {
        return isCompleted() ? getMaxDegrees() : 0;
    }

    default int getMaxDegrees() {
        return 3;
    }

    default void setMaxDegrees(int maxDegrees) {
    }

    default boolean canConcludeAtAltar() {
        return false;
    }

    default boolean shouldClearOnPlayerExit() {
        return false;
    }

    default void syncToClient(Player player) {
    }

    default int getMinRewardRolls() {
        return 1;
    }

    default int getMaxRewardRolls() {
        return 2;
    }

    default float getSoulShardDropChance(int amplifier) {
        return 0.0F;
    }

    default boolean onItemToss(Player player, ItemEntity itemEntity) {
        return false;
    }

    default BlockPos getGuidanceTarget(ServerPlayer player) {
        return getAltar() != null ? getAltar().getBlockPos() : null;
    }

    default int getCurseEffectDurationTicks(int curseDurationTicks) {
        return MobEffectInstance.INFINITE_DURATION;
    }
}


