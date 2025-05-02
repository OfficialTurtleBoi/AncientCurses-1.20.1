package net.turtleboi.ancientcurses.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AltarSavedData extends SavedData {
    private static final String DATA_NAME = "cursed_altars";
    private final Set<Long> altarPosLongs = new HashSet<>();

    public static AltarSavedData get(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(
                        AltarSavedData::load,
                        AltarSavedData::new,
                        DATA_NAME
                );
    }

    private static AltarSavedData load(CompoundTag nbt) {
        AltarSavedData data = new AltarSavedData();
        ListTag list = nbt.getList("Altars", LongTag.TAG_LONG);
        for (Tag tag : list) {
            if (tag instanceof LongTag) {
                data.altarPosLongs.add(((LongTag) tag).getAsLong());
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (Long posLong : altarPosLongs) {
            list.add(LongTag.valueOf(posLong));
        }
        nbt.put("Altars", list);
        return nbt;
    }

    public void addAltar(BlockPos pos) {
        if (altarPosLongs.add(pos.asLong())) {
            setDirty();
        }
    }

    public void removeAltar(BlockPos pos) {
        if (altarPosLongs.remove(pos.asLong())) {
            setDirty();
        }
    }

    public Set<BlockPos> getAltars() {
        return altarPosLongs.stream()
                .map(BlockPos::of)
                .collect(Collectors.toSet());
    }
}
