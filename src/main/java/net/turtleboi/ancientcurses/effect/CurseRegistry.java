package net.turtleboi.ancientcurses.effect;

import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

public class CurseRegistry {
    private static final List<MobEffect> CURSES = new ArrayList<>();

    public static void initialize() {
        CURSES.add(ModEffects.CURSE_OF_AVARICE.get());
        CURSES.add(ModEffects.CURSE_OF_ENDING.get());
        CURSES.add(ModEffects.CURSE_OF_ENVY.get());
        CURSES.add(ModEffects.CURSE_OF_FRAILTY.get());
        CURSES.add(ModEffects.CURSE_OF_GLUTTONY.get());
        CURSES.add(ModEffects.CURSE_OF_NATURE.get());
        CURSES.add(ModEffects.CURSE_OF_OBESSSION.get());
        CURSES.add(ModEffects.CURSE_OF_PESTILENCE.get());
        CURSES.add(ModEffects.CURSE_OF_PRIDE.get());
        CURSES.add(ModEffects.CURSE_OF_SHADOWS.get());
        CURSES.add(ModEffects.CURSE_OF_SLOTH.get());
        CURSES.add(ModEffects.CURSE_OF_WRATH.get());
    }

    public static List<MobEffect> getCurses() {
        return CURSES;
    }
}
