package net.turtleboi.ancientcurses.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.rites.ModRites;
import net.turtleboi.ancientcurses.rites.Rite;

import java.util.UUID;

public class ActiveRiteSession {
    private final UUID playerUuid;
    private final Rite rite;

    public ActiveRiteSession(UUID playerUuid, Rite rite) {
        this.playerUuid = playerUuid;
        this.rite = rite;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Rite getRite() {
        return rite;
    }

    public boolean matches(Player player) {
        return player != null && playerUuid.equals(player.getUUID());
    }

    public CompoundTag save() {
        CompoundTag sessionTag = new CompoundTag();
        sessionTag.putUUID("PlayerUUID", playerUuid);
        sessionTag.putString("RiteId", rite.getId().toString());

        CompoundTag riteData = new CompoundTag();
        rite.saveToNBT(riteData);
        sessionTag.put("RiteData", riteData);
        return sessionTag;
    }

    public static ActiveRiteSession load(CompoundTag tag, CursedAltarBlockEntity altar) {
        UUID playerUUID = tag.getUUID("PlayerUUID");
        ResourceLocation riteId = ModRites.parse(tag.getString("RiteId"));
        CompoundTag riteData = tag.getCompound("RiteData");
        Rite rite = ModRites.loadRite(riteId, altar, riteData);
        if (rite == null) {
            return null;
        }
        rite.setAltar(altar);
        return new ActiveRiteSession(playerUUID, rite);
    }
}
