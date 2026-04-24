package net.turtleboi.ancientcurses.client;

import net.turtleboi.ancientcurses.client.rites.ClientRiteState;
import net.turtleboi.ancientcurses.client.rites.NoRiteState;

public class PlayerClientData {
    private static final int FIRST_BEACON_WARMUP_LEAD_TICKS = 10;

    public static boolean isObsessed = false;
    public static boolean isSingularity = false;
    public static int singularityTimer = 0;
    public static int totalSingularityType = 0;
    private static ClientRiteState activeRiteState = NoRiteState.INSTANCE;
    private static float portalOverlayAlpha = 0;

    //Item utils
    private static double altarX = 0;
    private static double altarY = 0;
    private static double altarZ = 0;
    private static int itemMaxDurationTicks = 0;
    private static int itemRemainingUseTime = 0;
    private static double itemHitDistance = 0;
    private static boolean itemUsed = false;
    public static long itemUsedTime = 0;
    private static boolean dowsingRodUsed = false;
    private static long dowsingRodUsedTime = 0;

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
        return !(activeRiteState instanceof NoRiteState);
    }

    public static String getRiteId(){
        return activeRiteState.getRiteId();
    }

    public static ClientRiteState getActiveRiteState() {
        return activeRiteState;
    }

    public static void setActiveRiteState(ClientRiteState riteState){
        activeRiteState = riteState == null ? NoRiteState.INSTANCE : riteState;
    }

    public static float getRiteProgress() {
        return activeRiteState.getProgress();
    }

    public static boolean isRiteComplete(){
        return activeRiteState.isComplete();
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

    public static void startFirstBeaconUse(int maxDurationTicks) {
        itemMaxDurationTicks = maxDurationTicks;
        itemRemainingUseTime = Math.max(0, maxDurationTicks - FIRST_BEACON_WARMUP_LEAD_TICKS);
        itemHitDistance = 0;
        itemUsed = true;
    }

    public static void syncFirstBeaconUse(int maxDurationTicks, int remainingUseTime, double hitDistance, boolean beingUsed) {
        itemMaxDurationTicks = maxDurationTicks;
        if (beingUsed && itemUsed) {
            int currentElapsed = itemMaxDurationTicks - itemRemainingUseTime;
            int serverElapsed = maxDurationTicks - remainingUseTime;
            itemRemainingUseTime = serverElapsed < currentElapsed ? itemRemainingUseTime : remainingUseTime;
        } else {
            itemRemainingUseTime = remainingUseTime;
        }

        if (hitDistance != 0) {
            itemHitDistance = hitDistance;
        }
        itemUsed = beingUsed;
    }

    public static long getItemUsedTime() {
        return itemUsedTime;
    }

    public static void setItemUsedTime(long usedAt) {
        itemUsedTime = usedAt;
    }

    public static boolean getDowsingRodUsed() {
        return dowsingRodUsed;
    }

    public static void setDowsingRodUsed(boolean beingUsed) {
        dowsingRodUsed = beingUsed;
    }

    public static long getDowsingRodUsedTime() {
        return dowsingRodUsedTime;
    }

    public static void setDowsingRodUsedTime(long usedAt) {
        dowsingRodUsedTime = usedAt;
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
