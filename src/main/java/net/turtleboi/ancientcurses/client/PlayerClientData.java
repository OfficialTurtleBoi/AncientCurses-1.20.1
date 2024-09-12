package net.turtleboi.ancientcurses.client;

public class PlayerClientData {
    public static boolean isLusted = false;
    public static boolean isVoid = false;
    public static long voidStartTime = 0;

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

    public static void setVoidStartTime(long startTime) {
        voidStartTime = startTime;
    }

    public static Long getVoidStartTime(){
        return voidStartTime;
    }
}
