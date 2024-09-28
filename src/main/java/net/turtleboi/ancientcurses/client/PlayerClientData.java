package net.turtleboi.ancientcurses.client;

import net.turtleboi.ancientcurses.trials.PlayerTrialData;

public class PlayerClientData {
    public static boolean isAsleep = false;
    public static boolean isLusted = false;
    public static boolean isVoid = false;
    public static int voidTimer = 0;
    public static int voidTotalTime = 0;
    public static String trialType = "None";
    public static int eliminationKills = 0;
    public static int eliminationKillsRequired = 0;
    private static long trialDurationElapsed = 0;
    private static long trialDurationTotal = 0;
    public static String fetchItem = "";
    public static int fetchItems = 0;
    public static int fetchItemsRequired = 0;
    private static float portalOverlayAlpha = 0;

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

    public static boolean hasTrial() {
        if (trialType == null) {
            return false;
        }
        return !trialType.equals("None");
    }

    public static String getTrialType(){
        return trialType;
    }

    public static void setTrialType(String trialString){
        trialType = trialString;
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

    public static String getFetchItem(){
        return fetchItem;
    }

    public static void setFetchItem(String item) {
        fetchItem = item;
    }

    public static Integer getFetchItems(){
        return fetchItems;
    }

    public static void setFetchItems(int items) {
        fetchItems = items;
    }

    public static Integer getFetchItemsRequired(){
        return fetchItemsRequired;
    }

    public static void setFetchItemsRequired(int itemsRequired) {
        fetchItemsRequired = itemsRequired;
    }

    public static float getTrialProgress() {
        String trialType = getTrialType();
        if (trialType.equalsIgnoreCase(PlayerTrialData.survivalTrial)) {
            long elapsedTime = getTrialDurationElapsed();
            long totalDuration = getTrialDurationTotal();
            if (totalDuration == 0) return 0.0F;
            return Math.min(1.0F, (float) elapsedTime / (float) totalDuration);
        } else if (trialType.equalsIgnoreCase(PlayerTrialData.eliminationTrial)) {
            int kills = getEliminationKills();
            int requiredKills = getEliminationKillsRequired();
            if (requiredKills == 0) return 0.0F;
            return Math.min(1.0F, (float) kills / (float) requiredKills);
        } else if (trialType.equalsIgnoreCase(PlayerTrialData.fetchTrial)) {
            int items = getFetchItems();
            int requiredItems = getFetchItemsRequired();
            if (requiredItems == 0) return 0.0F;
            return Math.min(1.0F, (float) items / (float) requiredItems);
        }
        return 0.0F;
    }

    public static Float getPortalOverlayAlpha(){
        return portalOverlayAlpha;
    }

    public static void setPortalOverlayAlpha(float overlayAlpha) {
        portalOverlayAlpha = overlayAlpha;
    }

}
