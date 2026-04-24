package net.turtleboi.ancientcurses.rites;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;

public final class RiteLocator {
    private RiteLocator() {
    }

    public static Rite findActiveRite(Player player) {
        if (player == null) {
            return null;
        }

        PlayerRiteDataCapability riteData = player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).orElse(null);
        return riteData != null ? resolveActiveRite(player, riteData) : null;
    }

    public static CursedAltarBlockEntity findActiveAltar(Player player) {
        if (player == null) {
            return null;
        }

        PlayerRiteDataCapability riteData = player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).orElse(null);
        return riteData != null ? resolveActiveAltar(player, riteData) : null;
    }

    public static Rite resolveActiveRite(Player player, PlayerRiteDataCapability riteData) {
        if (player == null || riteData == null) {
            return null;
        }

        CursedAltarBlockEntity altar = resolveActiveAltar(player, riteData);
        return altar != null ? altar.getPlayerRite(player.getUUID()) : null;
    }

    public static CursedAltarBlockEntity resolveActiveAltar(Player player, PlayerRiteDataCapability riteData) {
        if (player == null || riteData == null) {
            return null;
        }

        BlockPos altarPos = riteData.getCurrentAltarPos();
        ResourceKey<Level> altarDimension = riteData.getAltarDimension();
        if (altarPos == null || altarDimension == null) {
            return null;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return null;
        }

        ServerLevel altarLevel = server.getLevel(altarDimension);
        if (altarLevel == null) {
            return null;
        }

        BlockEntity blockEntity = altarLevel.getBlockEntity(altarPos);
        if (blockEntity instanceof CursedAltarBlockEntity altar) {
            return altar;
        }

        return null;
    }
}
