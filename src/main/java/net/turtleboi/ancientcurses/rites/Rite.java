package net.turtleboi.ancientcurses.rites;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;

public interface Rite {
    String curseEffect = "CurseEffect";
    String curseAmplifier = "CurseAmplifier";

    String carnageRite = "CarnageRite";
    String embersRite = "EmbersRite";
    String famineRite = "FamineRite";
    String odysseyRite = "OdysseyRite";
    String sacrificeRite = "sacrificeRite";
    String alacrityRite = "AlacrityRite";

    void trackProgress(Player player);
    boolean isRiteCompleted(Player player);
    void concludeRite(Player player);

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


