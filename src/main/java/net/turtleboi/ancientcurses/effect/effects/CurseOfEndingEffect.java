package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.VoidPacketS2C;
import net.turtleboi.ancientcurses.particle.ModParticles;

import java.util.List;

public class CurseOfEndingEffect extends MobEffect {
    public CurseOfEndingEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) {
            if (pLivingEntity.tickCount % 20 == 0) {
                int effectColor = this.getColor();
                float red = ((effectColor >> 16) & 0xFF) / 255.0F;
                float green = ((effectColor >> 8) & 0xFF) / 255.0F;
                float blue = (effectColor & 0xFF) / 255.0F;
                for (int i = 0; i < 5; i++) {
                    pLivingEntity.level().addParticle(
                            ModParticles.CURSED_PARTICLES.get(),
                            pLivingEntity.getX() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            pLivingEntity.getY() + pLivingEntity.getRandom().nextDouble() * pLivingEntity.getBbHeight(),
                            pLivingEntity.getZ() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            red, green, blue);
                }
            }
        }

        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            if (!player.level().isClientSide) {
                int teleportCooldown = getTeleportCooldown(player);
                if (pAmplifier >= 1 && teleportCooldown <= 0) {
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

                int voidTimer = getVoidTimer(player);
                int voidCooldown = getVoidCooldown(player);
                if (pAmplifier >= 2 && voidCooldown <= 0) {
                    if (voidTimer > 0) {
                        MobEffectInstance levitationEffect = player.getEffect(MobEffects.LEVITATION);
                        if (levitationEffect == null){
                            giveLevitation(player, voidTimer);
                        }
                        attractEntities(player);
                        setVoidTimer(player,voidTimer - 1);
                        //player.sendSystemMessage(Component.literal("Void timer: " + getVoidTimer(player))); //debug code
                    } else if (voidTimer == 0) {
                        resetVoid(player);
                    } else {
                        startVoidEffect(player);
                    }
                } else {
                    setVoidCooldown(player,voidCooldown - 1);
                    //player.displayClientMessage(Component.literal("Void cooldown: " + getVoidCooldown(player)), true); //debug code
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    if (isVoid(player)) {
                        ModNetworking.sendToPlayer(
                                new VoidPacketS2C(
                                        true,
                                        getVoidTimer(player),
                                        getTotalVoidLifetime(player)
                                ),
                                serverPlayer
                        );
                    } else {
                        ModNetworking.sendToPlayer(
                                new VoidPacketS2C(
                                        false,
                                        0,
                                        0
                                ),
                                serverPlayer
                        );
                    }
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
        double[] teleportChanceValues = {0.25, 0.33, 0.5};
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
        setVoidCooldown(player, 300 + player.getRandom().nextInt(600 - 300 + 1));
    }

    public static int getTotalVoidLifetime(Player player) {
        return player.getPersistentData().getInt("totalVoidLifetime");
    }

    private void setVoidTimer(Player player, int timer) {
        player.getPersistentData().putInt("voidTimer", timer);
        if (!player.getPersistentData().contains("totalVoidLifetime") || player.getPersistentData().getInt("totalVoidLifetime") == 0) {
            player.getPersistentData().putInt("totalVoidLifetime", timer);
        }
    }


    private static int getVoidTimer(Player player) {
        return player.getPersistentData().getInt("voidTimer");
    }

    public static boolean isVoid(Player player){
        return getVoidTimer(player) > 0 && getVoidCooldown(player) <= 0;
    }

    private void resetVoidTimer(Player player){
        player.getPersistentData().remove("voidTimer");
        setVoidTimer(player, 100 + player.getRandom().nextInt(200 - 100 + 1));
    }

    private void startVoidEffect(Player player) {
        int totalVoidDuration = 100 + player.getRandom().nextInt(200 - 100 + 1);
        player.getPersistentData().putInt("totalVoidLifetime", totalVoidDuration);
        setVoidTimer(player, totalVoidDuration);
    }

    private void resetVoid(Player player) {
        resetVoidCooldown(player);
        resetVoidTimer(player);
        player.getPersistentData().remove("totalVoidLifetime");
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
        int[] maxEndermiteValues = {2, 4, 8};
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
        int endermiteCounter = 0;
        spawnSingleEndermite(player, serverLevel);
        endermiteCounter++;

        if (pAmplifier >= 2){
            spawnSingleEndermite(player, serverLevel);
            endermiteCounter++;
        }

        int maxExtraMites = getMaxEndermite(pAmplifier);
        double spawnChance = getEndermiteChance(pAmplifier);
        for (int i = 1; i < maxExtraMites; i++) {
            if (player.getRandom().nextDouble() < spawnChance) {
                spawnSingleEndermite(player, serverLevel);
                endermiteCounter++;
                spawnChance *= 0.66;
            } else {
                break;
            }
            if (endermiteCounter >= 4 && canSpawnEnderman(player, serverLevel)) {
                if (canSpawnEnderman(player, serverLevel)) {
                    spawnEnderman(player, serverLevel);
                }
                endermiteCounter = 0;  // Reset counter after spawning Enderman
            }
        }
        if (endermiteCounter >= 4 && canSpawnEnderman(player, serverLevel)) {
            spawnEnderman(player, serverLevel);
        }
    }

    private static void spawnSingleEndermite(Player player, ServerLevel serverLevel) {
        Endermite endermite = EntityType.ENDERMITE.create(serverLevel);
        if (endermite != null) {
            endermite.moveTo(player.xo, player.yo, player.zo, player.getYRot(), player.getXRot());
            serverLevel.addFreshEntity(endermite);
        }
    }

    private static boolean canSpawnEnderman(Player player, ServerLevel serverLevel) {
        int nearbyEndermenCount = serverLevel.getEntitiesOfClass(EnderMan.class, player.getBoundingBox().inflate(18.0D)).size();
        return nearbyEndermenCount < 3;
    }

    private static void spawnEnderman(Player player, ServerLevel serverLevel) {
        EnderMan enderman = EntityType.ENDERMAN.create(serverLevel);
        if (enderman != null) {
            enderman.moveTo(player.xo, player.yo, player.zo, player.getYRot(), player.getXRot());
            enderman.setTarget(player);
            enderman.setAggressive(true);
            serverLevel.addFreshEntity(enderman);
            serverLevel.playSound(
                    null,
                    enderman.getX(),
                    enderman.getY(),
                    enderman.getZ(),
                    SoundEvents.ENDERMAN_STARE,
                    SoundSource.HOSTILE,
                    1.0F,
                    1.0F);
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