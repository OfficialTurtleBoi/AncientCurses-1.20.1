package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;

public class CurseOfDiscordEffect extends MobEffect {
    private static int teleportCooldown = 0;
    public CurseOfDiscordEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            if (!player.level().isClientSide) {
                if (pAmplifier >= 1 && teleportCooldown <= 0) {
                    randomTeleport(player);
                    spawnEndermite(player);

                    int minTeleportTime = getMinTeleportTime(pAmplifier);
                    int maxTeleportTime = getMaxTeleportTime(pAmplifier);

                    if (pAmplifier >= 2){
                        scrambleControls(player, 100);
                    }
                    teleportCooldown = minTeleportTime + player.getRandom().nextInt(maxTeleportTime - minTeleportTime + 1);
                } else {
                    teleportCooldown--;
                }
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {

        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
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

    public static void randomTeleport(Player player) {
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
                break;
            }
        }
    }

    public static void spawnEndermite(Player player) {
        ServerLevel serverLevel = (ServerLevel)player.level();
        Endermite endermite = EntityType.ENDERMITE.create(serverLevel);
        if (endermite != null) {
            endermite.moveTo(player.xo, player.yo, player.zo, player.getYRot(), player.getXRot());
            serverLevel.addFreshEntity(endermite);
        }
    }

    public static void scrambleControls(Player player, int durationTicks) {
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, durationTicks, 1, false, false, true));
    }
}