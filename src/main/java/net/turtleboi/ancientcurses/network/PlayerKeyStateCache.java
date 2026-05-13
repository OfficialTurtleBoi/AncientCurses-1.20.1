package net.turtleboi.ancientcurses.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerKeyStateCache {
    private static final Map<UUID, Boolean> ctrlDownByPlayer = new ConcurrentHashMap<>();

    public static void setCtrlDown(UUID playerUUID, boolean ctrlDown) {
        ctrlDownByPlayer.put(playerUUID, ctrlDown);
    }

    public static boolean isCtrlDown(UUID playerUUID) {
        return ctrlDownByPlayer.getOrDefault(playerUUID, false);
    }

    public static void remove(UUID playerUUID) {
        ctrlDownByPlayer.remove(playerUUID);
    }
}
