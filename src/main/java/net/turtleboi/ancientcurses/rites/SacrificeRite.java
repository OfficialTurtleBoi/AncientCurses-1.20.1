package net.turtleboi.ancientcurses.rites;

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
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;

public class SacrificeRite implements Rite {
    private UUID playerUUID;
    private MobEffect effect;
    private int amplifier;
    private CursedAltarBlockEntity altar;
    private boolean completed;

    private int totalHealthOffered;

    public static final int baseFirstDegreeThresholdHealth = 20;
    public static final int baseSecondDegreeThresholdHealth = 60;
    public static final int baseThirdDegreeThresholdHealth = 120;

    private boolean completedFirstDegree;
    private boolean completedSecondDegree;
    private boolean completedThirdDegree;

    private static final double offeringMaxDistance = 6.0D;

    public SacrificeRite(Player player, MobEffect effect, int amplifier, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.effect = effect;
        this.amplifier = amplifier + 1;
        this.altar = altar;
        this.completed = false;

        this.totalHealthOffered = 0;
        this.completedFirstDegree = false;
        this.completedSecondDegree = false;
        this.completedThirdDegree = false;

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setCurseEffect(effect);
            riteData.setActiveRite(this);
        });
    }

    public SacrificeRite(CursedAltarBlockEntity altar) {
        this.altar = altar;
        this.completed = false;
        this.totalHealthOffered = 0;
    }

    @Override
    public String getType() {
        return Rite.sacrificeRite;
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
        return completedThirdDegree;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putUUID("PlayerUUID", playerUUID);
        tag.putString("Effect", Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getKey(effect)).toString());
        tag.putInt("TotalHealthOffered", totalHealthOffered);
        tag.putBoolean("Completed", completed);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        String effectName = tag.getString("Effect");
        this.effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        this.totalHealthOffered = tag.getInt("TotalHealthOffered");
        this.completed = tag.getBoolean("Completed");

        this.completedFirstDegree = totalHealthOffered >= getFirstDegreeThresholdHalfHearts();
        this.completedSecondDegree = totalHealthOffered >= getSecondDegreeThresholdHalfHearts();
        this.completedThirdDegree = totalHealthOffered >= getThirdDegreeThresholdHalfHearts();
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

        ModNetworking.sendToPlayer(
                new SyncRiteDataS2C(
                        Rite.sacrificeRite,
                        isRiteCompleted(player),
                        "Health Offered",
                        totalHealthOffered,
                        0,
                        nextThreshold,
                        0, 0,
                        "",
                        completedDegreeIndex(),
                        0
                ),
                (ServerPlayer) player
        );
    }

    @Override
    public void concludeRite(Player player) {
        ModNetworking.sendToPlayer(
                new SyncRiteDataS2C(
                        Rite.sacrificeRite,
                        true,
                        "Complete",
                        0, 0, 0, 0, 0,
                        "",
                        3,
                        0
                ),
                (ServerPlayer) player
        );

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(PlayerRiteDataCapability::clearCurseEffect);

        List<MobEffect> cursesToRemove = new ArrayList<>();
        for (var effectInstance : player.getActiveEffects()) {
            MobEffect mobEffect = effectInstance.getEffect();
            if (CurseRegistry.getCurses().contains(mobEffect)) {
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
        completedFirstDegree  = totalHealthOffered >= getFirstDegreeThresholdHalfHearts();
        completedSecondDegree = totalHealthOffered >= getSecondDegreeThresholdHalfHearts();
        completedThirdDegree  = totalHealthOffered >= getThirdDegreeThresholdHalfHearts();

        if (isRiteCompleted(player)) {
            concludeRite(player);
        }
    }

    private int completedDegreeIndex() {
        if (completedThirdDegree) return 3;
        if (completedSecondDegree) return 2;
        if (completedFirstDegree) return 1;
        return 0;
    }

    private int getFirstDegreeThresholdHalfHearts() {
        return scaleThreshold(baseFirstDegreeThresholdHealth);
    }

    private int getSecondDegreeThresholdHalfHearts() {
        return scaleThreshold(baseSecondDegreeThresholdHealth);
    }

    private int getThirdDegreeThresholdHalfHearts() {
        return scaleThreshold(baseThirdDegreeThresholdHealth);
    }

    private int getNextThresholdHalfHearts() {
        if (!completedFirstDegree) return getFirstDegreeThresholdHalfHearts();
        if (!completedSecondDegree) return getSecondDegreeThresholdHalfHearts();
        return getThirdDegreeThresholdHalfHearts();
    }

    private int scaleThreshold(int baseHalfHearts) {
        int scale = (1 << Math.max(0, amplifier - 1));
        return baseHalfHearts * scale;
    }
}
