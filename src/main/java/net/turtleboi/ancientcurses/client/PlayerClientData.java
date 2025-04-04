package net.turtleboi.ancientcurses.client;

import net.turtleboi.ancientcurses.trials.Trial;

public class PlayerClientData {
    public static boolean isLusted = false;
    public static boolean isVoid = false;
    public static int voidTimer = 0;
    public static int voidTotalTime = 0;
    public static String trialType = "None";
    public static boolean trialComplete;
    public static String eliminationTarget = "None";
    public static int waveCount = 0;
    public static int killsRemaining = 0;
    public static int waveKillTotal = 0;
    private static long durationElapsed = 0;
    private static long durationTotal = 0;
    public static String fetchItem = "";
    public static int fetchItems = 0;
    public static int fetchItemsRequired = 0;
    private static float portalOverlayAlpha = 0;

    //Item utils
    private static int itemMaxDurationTicks = 0;
    private static int itemRemainingUseTime = 0;
    private static double itemHitDistance = 0;
    private static boolean itemUsed = false;

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

    public static int getVoidTimer(){
        return voidTimer;
    }

    public static void setTotalVoidTime(int totalTime) {
        voidTotalTime = totalTime;
    }

    public static int getTotalVoidTime() {
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

    public static String getEliminationTarget(){
        return eliminationTarget;
    }

    public static void setEliminationTarget(String targetString){
        eliminationTarget = targetString;
    }

    public static int getWaveCount(){
        return waveCount;
    }

    public static void setWaveCount(int kills) {
        waveCount = kills;
    }

    public static int getKillsRemaining(){
        return killsRemaining;
    }

    public static void setKillsRemaining(int killsRemaining) {
        PlayerClientData.killsRemaining = killsRemaining;
    }

    public static int getWaveKillTotal(){
        return waveKillTotal;
    }

    public static void setWaveKillTotal(int waveKillTotal) {
        PlayerClientData.waveKillTotal = waveKillTotal;
    }

    public static long getDurationElapsed(){
        return durationElapsed;
    }

    public static void setDurationElapsed(long durationElapsed) {
        PlayerClientData.durationElapsed = durationElapsed;
    }

    public static long getDurationTotal(){
        return durationTotal;
    }

    public static void setDurationTotal(long durationTotal) {
        PlayerClientData.durationTotal = durationTotal;
    }

    public static String getFetchItem(){
        return fetchItem;
    }

    public static void setFetchItem(String item) {
        fetchItem = item;
    }

    public static int getFetchItems(){
        return fetchItems;
    }

    public static void setFetchItems(int items) {
        fetchItems = items;
    }

    public static int getFetchItemsRequired(){
        return fetchItemsRequired;
    }

    public static void setFetchItemsRequired(int itemsRequired) {
        fetchItemsRequired = itemsRequired;
    }

    public static float getTrialProgress() {
        String trialType = getTrialType();
        if (trialType.equalsIgnoreCase(Trial.survivalTrial)) {
            long elapsedTime = getDurationElapsed();
            long totalDuration = getDurationTotal();
            if (totalDuration == 0) return 0.0F;
            return Math.min(1.0F, (float) elapsedTime / (float) totalDuration);
        } else if (trialType.equalsIgnoreCase(Trial.eliminationTrial)) {
            long remainingDelay = getDurationElapsed();
            long totalDelay = getDurationTotal();
            if (remainingDelay < totalDelay && remainingDelay != 0) {
                return Math.min(1.0F, 1.0F - ((float) remainingDelay / (float) totalDelay));
            } else {
                int killsRemaining = getKillsRemaining();
                int waveKillTotal = getWaveKillTotal();
                return Math.min(1.0F, (float) killsRemaining / (float) waveKillTotal);
            }
        } else if (trialType.equalsIgnoreCase(Trial.fetchTrial)) {
            int items = getFetchItems();
            int requiredItems = getFetchItemsRequired();
            if (requiredItems == 0) return 0.0F;
            return Math.min(1.0F, (float) items / (float) requiredItems);
        }
        return 0.0F;
    }

    public static boolean isTrialComplete(){
        return trialComplete;
    }

    public static void setTrialComplete(boolean complete) {
        trialComplete = complete;
    }

    public static float getPortalOverlayAlpha(){
        return portalOverlayAlpha;
    }

    public static void setPortalOverlayAlpha(float overlayAlpha) {
        portalOverlayAlpha = overlayAlpha;
    }

    public static int getItemMaxDurationTicks(){
        return itemMaxDurationTicks;
    }

    public static void setItemMaxDurationTicks(int maxChargeTicks) {
        itemMaxDurationTicks = maxChargeTicks;
    }

    public static int getItemRemainingUseTime(){
        return itemRemainingUseTime;
    }

    public static void setItemRemainingUseTime(int remainingUseTime) {
        itemRemainingUseTime = remainingUseTime;
    }

    public static double getItemHitDistance(){
        return itemHitDistance;
    }

    public static void setItemHitDistance(double hitDistance) {
        itemHitDistance = hitDistance;
    }

    public static boolean getItemUsed(){
        return itemUsed;
    }

    public static void setItemUsed(boolean beingUsed) {
        itemUsed = beingUsed;
    }
}
