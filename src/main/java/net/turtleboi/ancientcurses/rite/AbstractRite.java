package net.turtleboi.ancientcurses.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.entity.entities.CursedPortalEntity;
import net.turtleboi.ancientcurses.item.items.DowsingRod;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractRite implements Rite {
    protected static final String PLAYER_UUID_KEY = "PlayerUUID";
    protected static final String EFFECT_KEY = "Effect";
    protected static final String COMPLETED_KEY = "Completed";
    protected static final String MAX_DEGREES_KEY = "MaxDegrees";

    protected UUID playerUUID;
    protected MobEffect effect;
    protected CursedAltarBlockEntity altar;
    protected boolean completed;
    protected int maxDegrees = 3;

    protected AbstractRite(CursedAltarBlockEntity altar) {
        this.altar = altar;
    }

    @Override
    public MobEffect getEffect() {
        return this.effect;
    }

    @Override
    public void setAltar(CursedAltarBlockEntity altar) {
        this.altar = altar;
    }

    @Override
    public CursedAltarBlockEntity getAltar() {
        return this.altar;
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    protected SyncRiteDataS2C buildSyncPacket(Player player) {
        return SyncRiteDataS2C.none();
    }

    protected int getDisplayDegreeCount() {
        return getMaxDegrees();
    }

    @Override
    public int getMaxDegrees() {
        return maxDegrees;
    }

    @Override
    public void setMaxDegrees(int maxDegrees) {
        this.maxDegrees = Math.max(1, maxDegrees);
    }

    @Override
    public boolean canConcludeAtAltar() {
        int completionDegree = getCompletionDegree();
        return completionDegree >= 1 && completionDegree < getMaxDegrees();
    }

    @Override
    public void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(buildSyncPacket(player), serverPlayer);
        }
    }

    @Override
    public int getMinRewardRolls() {
        return Math.max(1, getCompletionDegree());
    }

    @Override
    public int getMaxRewardRolls() {
        int completedDegrees = Math.max(1, getCompletionDegree());
        int bonusRollRange = Math.max(1, (int) Math.ceil(completedDegrees * 0.5D));
        return completedDegrees + bonusRollRange;
    }

    @Override
    public float getSoulShardDropChance(int amplifier) {
        if (amplifier < 2) {
            return 0.0F;
        }

        int completionDegree = Math.max(0, getCompletionDegree());
        if (amplifier >= 3) {
            return 1.0F + Math.max(0, completionDegree - 1) * 0.05F;
        }

        return completionDegree * 0.05F;
    }

    @Override
    public boolean onItemToss(Player player, ItemEntity itemEntity) {
        return false;
    }

    @Override
    public BlockPos getGuidanceTarget(ServerPlayer player) {
        return getAltar() != null ? getAltar().getBlockPos() : null;
    }

    @Override
    public int getCurseEffectDurationTicks(int curseDurationTicks) {
        return MobEffectInstance.INFINITE_DURATION;
    }

    protected void clearCurseEffects(Player player) {
        List<MobEffect> cursesToRemove = new ArrayList<>();
        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            MobEffect activeEffect = effectInstance.getEffect();
            if (ModEffects.isCurseEffect(activeEffect)) {
                cursesToRemove.add(activeEffect);
            }
        }

        for (MobEffect activeEffect : cursesToRemove) {
            player.removeEffect(activeEffect);
        }
    }

    protected void playCompletionEffects(Player player, float shakeAmount) {
        CoreNetworking.sendToNear(new CameraShakeS2C(shakeAmount, 1000), player);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.get(),
                    SoundSource.AMBIENT,
                    1.0f,
                    0.25f
            );
        }
    }

    protected void finishRite(Player player, boolean spawnReturnPortal, float shakeAmount) {
        syncToClient(player);
        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(PlayerRiteDataCapability::clearCurseEffect);
        clearCurseEffects(player);
        playCompletionEffects(player, shakeAmount);
        if (spawnReturnPortal) {
            CursedPortalEntity.spawnPortalNearPlayer(player, altar.getBlockPos(), altar.getLevel(), altar);
        }
        altar.setPlayerRiteCompleted(player);
        setCompleted(true);
    }

    protected void sendGuidance(ServerPlayer player, BlockPos target) {
        DowsingRod.UseState.updateActiveTarget(player, target);
    }

    protected void saveBaseData(CompoundTag tag) {
        if (playerUUID != null) {
            tag.putUUID(PLAYER_UUID_KEY, playerUUID);
        }
        if (effect != null) {
            tag.putString(EFFECT_KEY, net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getKey(effect).toString());
        }
        tag.putBoolean(COMPLETED_KEY, completed);
        tag.putInt(MAX_DEGREES_KEY, maxDegrees);
    }

    protected void loadBaseData(CompoundTag tag) {
        if (tag.hasUUID(PLAYER_UUID_KEY)) {
            this.playerUUID = tag.getUUID(PLAYER_UUID_KEY);
        }
        if (tag.contains(EFFECT_KEY)) {
            this.effect = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(tag.getString(EFFECT_KEY)));
        }
        this.completed = tag.getBoolean(COMPLETED_KEY);
        this.maxDegrees = Math.max(1, tag.contains(MAX_DEGREES_KEY) ? tag.getInt(MAX_DEGREES_KEY) : 3);
    }
}
