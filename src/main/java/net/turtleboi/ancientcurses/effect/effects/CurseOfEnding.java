package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.VoidPacketS2C;

import java.util.List;

public class CurseOfEnding extends MobEffect {
    public CurseOfEnding(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            if (!player.level().isClientSide) {
                int teleportCooldown = getTeleportCooldown(player);
                double teleportChance = getTeleportChance(pAmplifier);
                if (pAmplifier >= 1 && teleportCooldown <= 0 && player.getRandom().nextDouble() < teleportChance) {
                    randomTeleport(player, pAmplifier);
                    giveConfusion(player, 100);

                    int minTeleportTime = getMinTeleportTime(pAmplifier);
                    int maxTeleportTime = getMaxTeleportTime(pAmplifier);
                    teleportCooldown = minTeleportTime + player.getRandom().nextInt(maxTeleportTime - minTeleportTime + 1);
                    setTeleportCooldown(player, teleportCooldown);
                } else {
                    teleportCooldown--;
                    setTeleportCooldown(player, teleportCooldown);
                }

                if (pAmplifier >= 2 && getVoidCooldown(player) <= 0) {
                    if (getVoidTimer(player) > 0) {
                        MobEffectInstance levitationEffect = player.getEffect(MobEffects.LEVITATION);
                        if (levitationEffect == null){
                            giveLevitation(player, getVoidTimer(player));
                        }
                        attractEntities(player);
                        setVoidTimer(player,getVoidTimer(player) - 1);
                        player.sendSystemMessage(Component.literal("Void timer: " + getVoidTimer(player)));
                    } else if (getVoidTimer(player) == 0) {
                        resetVoid(player);
                    } else {
                        startVoidEffect(player);
                    }
                } else {
                    setVoidCooldown(player,getVoidCooldown(player) - 1);
                    player.displayClientMessage(Component.literal("Void cooldown: " + getVoidCooldown(player)), true);
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    ModNetworking.sendToPlayer(
                            new VoidPacketS2C(
                                    CurseOfEnding.isVoid(player),
                                    player.getPersistentData().getLong("voidStartTime")
                            ), serverPlayer);
                }
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            resetTeleportCooldown(player);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    public static double getTeleportChance(int pAmplifier){
        double[] teleportChanceValues = {0.25, 0.40, 0.66};
        int index = Math.min(pAmplifier, teleportChanceValues.length - 1);
        return teleportChanceValues[index];
    }

    private static int getMinTeleportTime(int pAmplifier){
        int[] minTeleportTimeValues = {600, 300, 200};
        int index = Math.min(pAmplifier, minTeleportTimeValues.length - 1);
        return minTeleportTimeValues[index];
    }

    private static int getMaxTeleportTime(int pAmplifier){
        int[] maxTeleportTimeValues = {2400, 1200, 600};
        int index = Math.min(pAmplifier, maxTeleportTimeValues.length - 1);
        return maxTeleportTimeValues[index];
    }

    private void setTeleportCooldown(Player player, int cooldown) {
        player.getPersistentData().putInt("teleportCooldown", cooldown);
    }

    private int getTeleportCooldown(Player player) {
        return player.getPersistentData().getInt("teleportCooldown");
    }

    private void resetTeleportCooldown(Player player){
        player.getPersistentData().remove("teleportCooldown");
    }

    private void setVoidCooldown(Player player, int cooldown) {
        player.getPersistentData().putInt("voidCooldown", cooldown);
    }

    private static int getVoidCooldown(Player player) {
        return player.getPersistentData().getInt("voidCooldown");
    }

    private void resetVoidCooldown(Player player){
        player.getPersistentData().remove("voidCooldown");
        setVoidCooldown(player, 600);
    }

    private void setVoidTimer(Player player, int timer) {
        player.getPersistentData().putInt("voidTimer", timer);
    }

    private static int getVoidTimer(Player player) {
        return player.getPersistentData().getInt("voidTimer");
    }

    public static boolean isVoid(Player player){
        return getVoidTimer(player) > 0 && getVoidCooldown(player) <= 0;
    }

    private void resetVoidTimer(Player player){
        player.getPersistentData().remove("voidTimer");
        setVoidTimer(player, 100);
    }

    private void startVoidEffect(Player player) {
        player.getPersistentData().putLong("voidStartTime", player.level().getGameTime());
        setVoidTimer(player, 100);
    }

    private void resetVoid(Player player) {
        resetVoidCooldown(player);
        resetVoidTimer(player);
        player.getPersistentData().remove("voidStartTime");
        player.displayClientMessage(Component.literal("The void effect has ended."), true);
    }

    public static void randomTeleport(Player player, int pAmplifier) {
        for (int i = 0; i < 16; ++i) {
            double x = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;
            double y = Mth.clamp(player.getY() + (double)(player.getRandom().nextInt(16) - 8), 0.0D, player.level().getMaxBuildHeight() - 1);
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 16.0D;
            if (player.isPassenger()) {
                player.stopRiding();
            }

            if (player.randomTeleport(x, y, z, true)) {
                player.level().playSound(null, player.xo, player.yo, player.zo, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                spawnEndermiteSwarm(player, pAmplifier);
                break;
            }
        }
    }

    private static int getMaxEndermite(int pAmplifier){
        int[] maxEndermiteValues = {2, 4, 7};
        int index = Math.min(pAmplifier, maxEndermiteValues.length - 1);
        return maxEndermiteValues[index];
    }

    private static double getEndermiteChance(int pAmplifier){
        double[] endermiteChanceValues = {0.25, 0.5, 0.8};
        int index = Math.min(pAmplifier, endermiteChanceValues.length - 1);
        return endermiteChanceValues[index];
    }

    public static void spawnEndermiteSwarm(Player player, int pAmplifier) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        spawnSingleEndermite(player, serverLevel);
        if (pAmplifier >= 2){
            spawnSingleEndermite(player, serverLevel);
        }
        int maxExtraMites = getMaxEndermite(pAmplifier);
        double spawnChance = getEndermiteChance(pAmplifier);
        for (int i = 1; i < maxExtraMites; i++) {
            if (player.getRandom().nextDouble() < spawnChance) {
                spawnSingleEndermite(player, serverLevel);
                spawnChance *= 0.66;
            } else {
                break;
            }
        }
    }

    private static void spawnSingleEndermite(Player player, ServerLevel serverLevel) {
        Endermite endermite = EntityType.ENDERMITE.create(serverLevel);
        if (endermite != null) {
            endermite.moveTo(player.xo, player.yo, player.zo, player.getYRot(), player.getXRot());
            serverLevel.addFreshEntity(endermite);
        }
    }

    public static void giveConfusion(Player player, int durationTicks) {
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, durationTicks, 2, false, false, false));
    }

    public static void giveLevitation(Player player, int durationTicks) {
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, durationTicks, 2, false, false, false));
    }

    private void attractEntities(Player player) {
        Level level = player.level();
        List<Entity> nearbyEntities = level.getEntities(player, player.getBoundingBox().inflate(10.0D));
        for (Entity entity : nearbyEntities) {
            if (entity != player) {
                Vec3 direction = new Vec3(
                        player.getX() - entity.getX(),
                        player.getY() - entity.getY(),
                        player.getZ() - entity.getZ()
                ).normalize();
                entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(0.25D)));
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY(), entity.getZ(), 5, 0, 0, 0, 0.1D);
                }
            }
        }
        }
}