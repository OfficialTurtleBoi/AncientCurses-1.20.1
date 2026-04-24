package net.turtleboi.ancientcurses.rite.rites;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.client.rites.SacrificeClientRiteState;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.ancientcurses.rite.ModRites;
import net.turtleboi.ancientcurses.rite.Rite;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;

public class SacrificeRite implements Rite {
    private static final String MAX_DEGREES_KEY = "MaxDegrees";
    private UUID playerUUID;
    private MobEffect effect;
    private int amplifier;
    private CursedAltarBlockEntity altar;
    private boolean completed;
    private int maxDegrees = 3;

    private int totalHealthOffered;

    public static final int baseDegreeThresholdHealth = 20;

    private static final double offeringMaxDistance = 6.0D;

    public SacrificeRite(Player player, MobEffect effect, int amplifier, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.effect = effect;
        this.amplifier = amplifier + 1;
        this.altar = altar;
        this.completed = false;

        this.totalHealthOffered = 0;

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setCurseEffect(effect);
            //riteData.setActiveRite(this);
        });
    }

    public SacrificeRite(CursedAltarBlockEntity altar) {
        this.altar = altar;
        this.completed = false;
        this.totalHealthOffered = 0;
    }

    @Override
    public String getType() {
        return getId().toString();
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
    public MobEffect getEffect() {
        return this.effect;
    }

    public Player getPlayer() {
        if (altar.getLevel() instanceof ServerLevel serverLevel) {
            return serverLevel.getPlayerByUUID(playerUUID);
        }
        return null;
    }

    private boolean isRiteActive() {
        return altar.getPlayerRite(playerUUID) != null;
    }

    @Override
    public boolean isRiteCompleted(Player player) {
        return completedDegreeIndex() >= getMaxDegrees();
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putUUID("PlayerUUID", playerUUID);
        tag.putString("Effect", Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getKey(effect)).toString());
        tag.putInt("TotalHealthOffered", totalHealthOffered);
        tag.putBoolean("Completed", completed);
        tag.putInt(MAX_DEGREES_KEY, maxDegrees);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        String effectName = tag.getString("Effect");
        this.effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        this.totalHealthOffered = tag.getInt("TotalHealthOffered");
        this.completed = tag.getBoolean("Completed");
        this.maxDegrees = Math.max(1, tag.contains(MAX_DEGREES_KEY) ? tag.getInt(MAX_DEGREES_KEY) : 3);
    }

    @Override
    public ResourceLocation getId() {
        return ModRites.SACRIFICE;
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
        int completionDegree = completedDegreeIndex();
        return completionDegree >= 1 && completionDegree < getMaxDegrees();
    }

    @Override
    public void onEntityKilled(Player player, Entity entity) {
        if (!isRiteActive()) return;
        if (!(entity instanceof LivingEntity livingEntity)) return;
        if (!isValidOfferingEntity(livingEntity)) return;
        if (!isWithinOfferingRadius(entity.blockPosition())) return;

        Player ritePlayer = getPlayer();
        if (ritePlayer == null || !ritePlayer.getUUID().equals(player.getUUID())) {
            return;
        }

        int offeredHealth = computeHealthOffer(livingEntity);
        if (offeredHealth <= 0) return;

        totalHealthOffered += offeredHealth;

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, altar.getBlockPos(), SoundEvents.SOUL_ESCAPE, SoundSource.AMBIENT, 0.8f, 0.65f);
            serverLevel.playSound(null, altar.getBlockPos(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.6f, 0.5f);
        }

        checkDegreesAndMaybeConclude(ritePlayer);
        trackProgress(ritePlayer);
    }

    @Override
    public void onPlayerTick(Player player) {
        if (!isRiteActive()) return;
        checkDegreesAndMaybeConclude(player);
        trackProgress(player);
    }

    @Override
    public void trackProgress(Player player) {
        if (player == null) return;

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            BlockPos altarPos = riteData.getCurrentAltarPos();
            if (altarPos != null && riteData.getAltarDimension() != null) {
                MinecraftServer server = player.getServer();
                if (server != null) {
                    ServerLevel altarLevel = server.getLevel(riteData.getAltarDimension());
                    if (altarLevel != null) {
                        BlockEntity blockEntity = altarLevel.getBlockEntity(altarPos);
                        if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                            CompoundTag altarNBT = new CompoundTag();
                            altarEntity.saveAdditional(altarNBT);
                        }
                    }
                }
            }
        });

        int nextThreshold = getNextThresholdHalfHearts();
        int completedDegrees = completedDegreeIndex();
        int totalDegrees = getMaxDegrees();
        int activeDegreeIndex = isRiteCompleted(player) ? -1 : Math.min(completedDegrees, totalDegrees - 1);

        ModNetworking.sendToPlayer(
                SyncRiteDataS2C.fromState(new SacrificeClientRiteState(
                        isRiteCompleted(player),
                        totalHealthOffered,
                        nextThreshold,
                        totalDegrees,
                        completedDegrees,
                        activeDegreeIndex
                )),
                (ServerPlayer) player
        );
    }

    @Override
    public void concludeRite(Player player) {
        ModNetworking.sendToPlayer(
                SyncRiteDataS2C.fromState(new SacrificeClientRiteState(
                        true,
                        totalHealthOffered,
                        getThresholdHalfHearts(getMaxDegrees()),
                        getMaxDegrees(),
                        getMaxDegrees(),
                        -1
                )),
                (ServerPlayer) player
        );

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(PlayerRiteDataCapability::clearCurseEffect);

        List<MobEffect> cursesToRemove = new ArrayList<>();
        for (var effectInstance : player.getActiveEffects()) {
            MobEffect mobEffect = effectInstance.getEffect();
            if (ModEffects.isCurseEffect(mobEffect)) {
                cursesToRemove.add(mobEffect);
            }
        }
        for (MobEffect mobEffect : cursesToRemove) {
            player.removeEffect(mobEffect);
        }

        CoreNetworking.sendToNear((new CameraShakeS2C(0.04F, 900)), player);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY() + 1, player.getZ(),
                    SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.get(), SoundSource.AMBIENT, 1.0f, 0.3f);
        }

        altar.setPlayerRiteCompleted(player);
        setCompleted(true);
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    private boolean isWithinOfferingRadius(BlockPos entityPos) {
        BlockPos altarPos = altar.getBlockPos();
        return altarPos.closerThan(entityPos, offeringMaxDistance);
    }

    private boolean isValidOfferingEntity(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Animal)) return false;
        if (livingEntity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame()) return false;
        return !livingEntity.hasCustomName();
    }

    private int computeHealthOffer(LivingEntity livingEntity) {
        float maxHealth = livingEntity.getMaxHealth();
        if (maxHealth <= 0.0f) return 0;
        return Math.max(1, Math.round(maxHealth * 2.0f));
    }

    private void checkDegreesAndMaybeConclude(Player player) {
        if (isRiteCompleted(player)) {
            concludeRite(player);
        }
    }

    private int completedDegreeIndex() {
        int completedDegrees = 0;
        for (int degree = 1; degree <= getMaxDegrees(); degree++) {
            if (totalHealthOffered >= getThresholdHalfHearts(degree)) {
                completedDegrees = degree;
            } else {
                break;
            }
        }
        return completedDegrees;
    }

    private int getThresholdHalfHearts(int degreeNumber) {
        int triangularScale = degreeNumber * (degreeNumber + 1) / 2;
        return scaleThreshold(baseDegreeThresholdHealth * triangularScale);
    }

    private int getNextThresholdHalfHearts() {
        int nextDegree = Math.min(getMaxDegrees(), completedDegreeIndex() + 1);
        return getThresholdHalfHearts(nextDegree);
    }

    private int scaleThreshold(int baseHalfHearts) {
        int scale = (1 << Math.max(0, amplifier - 1));
        return baseHalfHearts * scale;
    }
}
