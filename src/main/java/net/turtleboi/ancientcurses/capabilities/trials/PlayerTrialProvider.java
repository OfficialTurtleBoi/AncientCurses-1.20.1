package net.turtleboi.ancientcurses.capabilities.trials;

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

public class PlayerTrialProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<PlayerTrialDataCapability> PLAYER_TRIAL_DATA = CapabilityManager.get(new CapabilityToken<>() {});

    private PlayerTrialDataCapability trialData = null;
    private final LazyOptional<PlayerTrialDataCapability> optional = LazyOptional.of(this::createPlayerTrialData);

    private final Player player;

    public PlayerTrialProvider(Player player) {
        this.player = player;
    }

    private PlayerTrialDataCapability createPlayerTrialData() {
        if (this.trialData == null) {
            this.trialData = new PlayerTrialDataCapability();
        }
        return this.trialData;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_TRIAL_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerTrialData().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerTrialData().loadNBTData(nbt);
    }
}
