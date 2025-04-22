package net.turtleboi.ancientcurses.client;

import net.turtleboi.ancientcurses.rites.Rite;

public class PlayerClientData {
    public static boolean isObsessed = false;
    public static boolean isSingularity = false;
    public static int singularityTimer = 0;
    public static int totalSingularityType = 0;
    public static String riteType = "None";
    public static boolean riteComplete;
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
    private static double altarX = 0;
    private static double altarY = 0;
    private static double altarZ = 0;
    private static int itemMaxDurationTicks = 0;
    private static int itemRemainingUseTime = 0;
    private static double itemHitDistance = 0;
    private static boolean itemUsed = false;

    public static boolean isObsessed() {
        return isObsessed;
    }

    public static void setObsessed(boolean obsessed) {
        isObsessed = obsessed;
    }

    public static boolean isSingularity() {
        return isSingularity;
    }

    public static void setSingularity(boolean singularity) {
        isSingularity = singularity;
    }

    public static void setSingularityTimer(int time) {
        singularityTimer = time;
    }

    public static int getSingularityTimer(){
        return singularityTimer;
    }

    public static void setTotalSingularityTime(int totalTime) {
        totalSingularityType = totalTime;
    }

    public static int getTotalSingularityType() {
        return totalSingularityType;
    }

    public static boolean hasRite() {
        if (riteType == null) {
            return false;
        }
        return !riteType.equals("None");
    }

    public static String getRiteType(){
        return riteType;
    }

    public static void setRiteType(String riteString){
        riteType = riteString;
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

    public static float getRiteProgress() {
        String riteType = getRiteType();
        if (riteType.equalsIgnoreCase(Rite.embersRite)) {
            long elapsedTime = getDurationElapsed();
            long totalDuration = getDurationTotal();
            if (totalDuration == 0) return 0.0F;
            return Math.min(1.0F, (float) elapsedTime / (float) totalDuration);
        } else if (riteType.equalsIgnoreCase(Rite.carnageRite)) {
            long remainingDelay = getDurationElapsed();
            long totalDelay = getDurationTotal();
            if (remainingDelay < totalDelay && remainingDelay != 0) {
                return Math.min(1.0F, 1.0F - ((float) remainingDelay / (float) totalDelay));
            } else {
                int killsRemaining = getKillsRemaining();
                int waveKillTotal = getWaveKillTotal();
                return Math.min(1.0F, (float) killsRemaining / (float) waveKillTotal);
            }
        } else if (riteType.equalsIgnoreCase(Rite.famineRite)) {
            int items = getFetchItems();
            int requiredItems = getFetchItemsRequired();
            if (requiredItems == 0) return 0.0F;
            return Math.min(1.0F, (float) items / (float) requiredItems);
        }
        return 0.0F;
    }

    public static boolean isRiteComplete(){
        return riteComplete;
    }

    public static void setRiteComplete(boolean complete) {
        riteComplete = complete;
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

    public static double getAltarX() {
        return altarX;
    }

    public static void setAltarX(double xCoord) {
        altarX = xCoord;
    }

    public static double getAltarY() {
        return altarY;
    }

    public static void setAltarY(double yCoord) {
        altarY = yCoord;
    }

    public static double getAltarZ() {
        return altarZ;
    }

    public static void setAltarZ(double zCoord) {
        altarZ = zCoord;
    }
}
