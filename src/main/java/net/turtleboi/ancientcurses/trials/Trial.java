package net.turtleboi.ancientcurses.trials;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

public interface Trial {
    boolean isTrialCompleted(Player player);
    void trackProgress(Player player);
    void rewardPlayer(Player player);
    void setAltar(CursedAltarBlockEntity altar);
    MobEffect getEffect();
}


