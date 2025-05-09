package net.turtleboi.ancientcurses.rites;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

public class AlacrityRite implements Rite {
    @Override
    public void trackProgress(Player player) {

    }

    @Override
    public boolean isRiteCompleted(Player player) {
        return false;
    }

    @Override
    public void concludeRite(Player player) {

    }

    @Override
    public void onEntityKilled(Player player, Entity entity) {

    }

    @Override
    public void onPlayerTick(Player player) {

    }

    @Override
    public void saveToNBT(CompoundTag tag) {

    }

    @Override
    public void loadFromNBT(CompoundTag tag) {

    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public MobEffect getEffect() {
        return null;
    }

    @Override
    public void setAltar(CursedAltarBlockEntity altar) {

    }

    @Override
    public CursedAltarBlockEntity getAltar() {
        return null;
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public void setCompleted(boolean completed) {

    }
}
