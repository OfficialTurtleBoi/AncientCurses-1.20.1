package net.turtleboi.ancientcurses.capabilities.rites;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerRiteProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<PlayerRiteDataCapability> PLAYER_RITE_DATA = CapabilityManager.get(new CapabilityToken<>() {});

    private PlayerRiteDataCapability riteData = null;
    private final LazyOptional<PlayerRiteDataCapability> optional = LazyOptional.of(this::createPlayerRiteData);

    private final Player player;

    public PlayerRiteProvider(Player player) {
        this.player = player;
    }

    private PlayerRiteDataCapability createPlayerRiteData() {
        if (this.riteData == null) {
            this.riteData = new PlayerRiteDataCapability();
        }
        return this.riteData;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_RITE_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerRiteData().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerRiteData().loadNBTData(nbt);
    }
}
