package net.turtleboi.ancientcurses.client;

public class PlayerClientData {
    public static boolean isLusted = false;
    public static boolean isVoid = false;
    public static int voidTimer = 0;
    public static int voidTotalTime = 0;

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

    public static Integer getTotalVoidTime(){
        return voidTotalTime;
    }
}
