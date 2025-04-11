package net.turtleboi.ancientcurses.rites;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.entity.CursedPortalEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EmbersRite implements Rite {
    private UUID playerUUID;
    private long elapsedTime;
    private long riteDuration;
    private CursedAltarBlockEntity altar;
    private MobEffect effect;
    private int pAmplifier;
    private boolean completed;
    public static final String riteDurationTotal = "RiteDuration";
    public static final String riteDurationElapsed = "RiteElapsedTime";
    private int portalCooldown = 0;
    public EmbersRite(Player player, MobEffect effect, int pAmplifier, long riteDuration, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.altar = altar;
        this.effect = effect;
        this.riteDuration = riteDuration;
        //System.out.println(Component.literal("Setting rite duration to: " + this.riteDuration));
        //System.out.println(Component.literal("Setting elapsed time to: " + this.elapsedTime));
        this.pAmplifier = pAmplifier + 1;
        this.completed = false;
        this.portalCooldown = 0;
        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setCurseEffect(effect);
            riteData.setActiveRite(this);
            this.elapsedTime = riteData.getSurvivalTicks();
        });
    }

    public EmbersRite(CursedAltarBlockEntity altar) {
        this.altar = altar;
        this.completed = false;
    }

    public boolean isRiteActive() {
        return altar.getPlayerRite(playerUUID) != null;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putUUID("PlayerUUID", playerUUID);
        tag.putString("Effect", Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getKey(effect)).toString());
        tag.putLong(riteDurationElapsed, elapsedTime);
        tag.putLong(riteDurationTotal, riteDuration);
        tag.putBoolean("Completed", completed);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        String effectName = tag.getString("Effect");
        this.effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
        this.elapsedTime = tag.getLong(riteDurationElapsed);
        this.riteDuration = tag.getLong(riteDurationTotal);
        this.completed = tag.getBoolean("Completed");
    }

    @Override
    public String getType() {
        return Rite.embersRite;
    }

    @Override
    public void setAltar(CursedAltarBlockEntity altar) {
        this.altar = altar;
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

    @Override
    public boolean isRiteCompleted(Player player) {
        //System.out.println(Component.literal("Completed rite!"));
        return elapsedTime >= riteDuration;
    }

    @Override
    public void onPlayerTick(Player player) {
        if (!isRiteActive()) {
            return;
        }

        if (effect != ModEffects.CURSE_OF_PESTILENCE.get()) {
            portalCooldown++;
            if (pAmplifier > 0) {
                if (portalCooldown >= 600 / pAmplifier) {
                    portalCooldown = 0;
                    //System.out.println(Component.literal("Spawning new summoning portal"));
                    MinecraftServer server = player.getServer();
                    if (server != null) {
                        ServerLevel level = (ServerLevel) player.level();
                        List<Entity> mobsToSpawn = buildPortalSpawnList(level, 3 * this.pAmplifier);
                        CursedPortalEntity.spawnSummoningPortalAtPos(level, altar, player.getOnPos(), mobsToSpawn);
                    }
                }
            }
        }

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setSurvivalTicks((int) elapsedTime);
        });

        trackProgress(player);
        if (isRiteCompleted(player)) {
            concludeRite(player);
        }
    }

    @Override
    public void trackProgress(Player player) {
        if (!isRiteActive()) {
            return;
        }

        if (player != null) {
            elapsedTime++;
            //System.out.println("Setting elapsed time to: " + elapsedTime);
            float progressPercentage = Math.min((float) elapsedTime / riteDuration, 1.0f);
            ModNetworking.sendToPlayer(
                    new SyncRiteDataS2C(
                            Rite.embersRite,
                            isRiteCompleted(player),
                            "",
                            0,
                            0,
                            0,
                            elapsedTime,
                            riteDuration,
                            "",
                            0,
                            0),
                    (ServerPlayer) player);
            //player.displayClientMessage(Component.literal(String.format("Rite progress: %.2f%% complete", progressPercentage * 100))
            //        .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    @Override
    public void concludeRite(Player player) {
        //player.displayClientMessage(Component.literal("You have survived the rite! Collect your reward").withStyle(ChatFormatting.GREEN), true);
        ModNetworking.sendToPlayer(
                new SyncRiteDataS2C(
                        Rite.embersRite,
                        isRiteCompleted(player),
                        "",
                        0,
                        0,
                        0,
                        riteDuration,
                        riteDuration,
                        "",
                        0,
                        0),
                (ServerPlayer) player);
        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(PlayerRiteDataCapability::clearCurseEffect);

        List<MobEffect> cursesToRemove = new ArrayList<>();
        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            MobEffect effect = effectInstance.getEffect();
            if (CurseRegistry.getCurses().contains(effect)) {
                cursesToRemove.add(effect);
            }
        }

        for (MobEffect effect : cursesToRemove) {
            player.removeEffect(effect);
        }

        CoreNetworking.sendToNear((new CameraShakeS2C(0.125F, 1000)), player);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.get(),
                    SoundSource.AMBIENT,
                    1.00f,
                    0.25f
            );
        }

        CursedPortalEntity.spawnPortalNearPlayer(player, altar.getBlockPos(),  altar.getLevel(), altar);
        //System.out.println(Component.literal("Spawning new portal to altar"));
        altar.setPlayerRiteCompleted(player);
        this.completed = true;
    }

    @Override
    public void onEntityKilled(Player player, Entity entity) {

    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    private List<Entity> buildPortalSpawnList(ServerLevel level, int mobCount) {
        List<Entity> mobs = new ArrayList<>();
        for (int i = 0; i < mobCount; i++) {
            Entity mob = selectRandomTargetMob().create(level);
            if (mob != null) {
                mobs.add(mob);
            }
        }
        return mobs;
    }

    private EntityType<?> selectRandomTargetMob() {
        List<CarnageRite.WeightedMob> mobList = CarnageRite.MobList.ELIMINATION_TRIAL_MOBS;
        double totalWeight = mobList.stream().mapToDouble(CarnageRite.WeightedMob::weight).sum();
        double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cumulativeWeight = 0.0;
        for (CarnageRite.WeightedMob weightedMob : mobList) {
            cumulativeWeight += weightedMob.weight();
            if (randomValue <= cumulativeWeight) {
                return weightedMob.mobType();
            }
        }
        return mobList.get(mobList.size() - 1).mobType();
    }
}
