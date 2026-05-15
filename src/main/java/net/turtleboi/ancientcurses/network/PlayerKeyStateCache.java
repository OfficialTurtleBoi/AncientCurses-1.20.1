package net.turtleboi.ancientcurses.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerKeyStateCache {
    private static final long INVENTORY_CLICK_SNAPSHOT_LIFETIME_NANOS = 250_000_000L;
    private static final Map<UUID, Boolean> ctrlDownByPlayer = new ConcurrentHashMap<>();
    private static final Map<UUID, CtrlSnapshot> inventoryClickCtrlByPlayer = new ConcurrentHashMap<>();

    public static void setCtrlDown(UUID playerUUID, boolean ctrlDown) {
        ctrlDownByPlayer.put(playerUUID, ctrlDown);
    }

    public static boolean isCtrlDown(UUID playerUUID) {
        return ctrlDownByPlayer.getOrDefault(playerUUID, false);
    }

    public static void setInventoryClickCtrl(UUID playerUUID, boolean ctrlDown) {
        inventoryClickCtrlByPlayer.put(playerUUID, new CtrlSnapshot(ctrlDown, System.nanoTime()));
    }

    public static boolean getInventoryClickCtrl(UUID playerUUID) {
        CtrlSnapshot snapshot = inventoryClickCtrlByPlayer.get(playerUUID);
        if (snapshot == null || System.nanoTime() - snapshot.createdAtNanos() > INVENTORY_CLICK_SNAPSHOT_LIFETIME_NANOS) {
            return isCtrlDown(playerUUID);
        }
        return snapshot.ctrlDown();
    }

    public static void remove(UUID playerUUID) {
        ctrlDownByPlayer.remove(playerUUID);
        inventoryClickCtrlByPlayer.remove(playerUUID);
    }

    private record CtrlSnapshot(boolean ctrlDown, long createdAtNanos) {
    }
}
