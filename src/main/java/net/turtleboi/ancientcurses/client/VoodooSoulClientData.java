package net.turtleboi.ancientcurses.client;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VoodooSoulClientData {
    private static final Set<UUID> SOUL_CLONES = new HashSet<>();

    public static void setSoulClone(UUID uuid, boolean active) {
        if (active) {
            SOUL_CLONES.add(uuid);
        } else {
            SOUL_CLONES.remove(uuid);
        }
    }

    public static boolean isSoulClone(UUID uuid) {
        return SOUL_CLONES.contains(uuid);
    }
}
