package net.turtleboi.ancientcurses.client;

public class PlayerClientData {
    public static boolean isAsleep = false;
    public static boolean isLusted = false;
    public static boolean isVoid = false;
    public static int voidTimer = 0;
    public static int voidTotalTime = 0;
    public static int eliminationKills = 0;
    public static int eliminationKillsRequired = 0;
    private static long trialDurationElapsed = 0;
    private static long trialDurationTotal = 0;

    public static boolean isAsleep() {
        return isAsleep;
    }

    public static void setAsleep(boolean sleep) {
        isAsleep = sleep;
    }

    public static boolean isLusted() {
        return isLusted;
    }

    public static void setLusted(boolean lusted) {
        isLusted = lusted;
    }

    public static boolean isVoid() {
        return isVoid;
    }

    public static void setVoid(boolean voided) {
        isVoid = voided;
    }

    public static void setVoidTimer(int time) {
        voidTimer = time;
    }

    public static Integer getVoidTimer(){
        return voidTimer;
    }

    public static void setTotalVoidTime(int totalTime) {
        voidTotalTime = totalTime;
    }

    public static Integer getTotalVoidTime() {
        return voidTotalTime;
    }

    public static Integer getEliminationKills(){
        return eliminationKills;
    }

    public static void setEliminationKills(int kills) {
        eliminationKills = kills;
    }

    public static Integer getEliminationKillsRequired(){
        return eliminationKillsRequired;
    }

    public static void setEliminationKillsRequired(int killsRequired) {
        eliminationKillsRequired = killsRequired;
    }

    public static Long getTrialDurationElapsed(){
        return trialDurationElapsed;
    }

    public static void setTrialDurationElapsed(long durationElapsed) {
        trialDurationElapsed = durationElapsed;
    }

    public static Long getTrialDurationTotal(){
        return trialDurationTotal;
    }

    public static void setTrialDurationTotal(long durationTotal) {
        trialDurationTotal = durationTotal;
    }
}
