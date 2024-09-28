package net.turtleboi.ancientcurses.trials;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

public interface Trial {
    void trackProgress(Player player);
    boolean isTrialCompleted(Player player);
    void concludeTrial(Player player);

    void onEntityKilled(Player player, Entity entity);
    void onPlayerTick(Player player);

    void saveToNBT(CompoundTag tag);
    void loadFromNBT(CompoundTag tag);
    String getType();

    MobEffect getEffect();
    void setAltar(CursedAltarBlockEntity altar);

    boolean isCompleted();
    void setCompleted(boolean completed);
}


