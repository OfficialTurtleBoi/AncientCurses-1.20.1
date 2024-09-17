package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.SleepPacketS2C;
import net.turtleboi.ancientcurses.particle.ModParticles;
import net.turtleboi.ancientcurses.util.AttributeModifierUtil;
import java.util.Random;


public class CurseOfSlothEffect extends MobEffect {
    private static final Random random = new Random();
    private static final ResourceLocation sleepTicksTag = new ResourceLocation("ancientcurses", "sleep_ticks");
    private static final int sleepDuration = 60;
    public CurseOfSlothEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) {
            if (PlayerClientData.isAsleep()) {
                if (pLivingEntity.tickCount % 20 == 0 || pLivingEntity.tickCount % 20 == 3 || pLivingEntity.tickCount % 20 == 6) {
                    Vec3 lookVector = pLivingEntity.getLookAngle();
                    double velocityX = lookVector.x * 0.05 + 0.1;
                    double velocityY = 0.1;
                    double velocityZ = lookVector.z * 0.05;
                    double particleX = pLivingEntity.getX();
                    double particleY = pLivingEntity.getY() + pLivingEntity.getBbHeight();
                    double particleZ = pLivingEntity.getZ();

                    if (pLivingEntity.tickCount % 20 == 0) {
                        pLivingEntity.level().addParticle(
                                ModParticles.SLEEP_PARTICLES.get(),
                                particleX, particleY, particleZ,
                                velocityX * 1.5, velocityY * 1.5, velocityZ * 1.5);
                    } else if (pLivingEntity.tickCount % 20 == 3) {
                        pLivingEntity.level().addParticle(
                                ModParticles.SLEEP_PARTICLES.get(),
                                particleX, particleY, particleZ,
                                velocityX, velocityY, velocityZ);
                    } else if (pLivingEntity.tickCount % 20 == 6) {
                        pLivingEntity.level().addParticle(
                                ModParticles.SLEEP_PARTICLES.get(),
                                particleX, particleY, particleZ,
                                velocityX * 0.66, velocityY * 0.66, velocityZ * 0.66);
                    }
                }
            }
        }

        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            double movementMultiplier = getMovementMultiplier(pAmplifier);
            AttributeModifierUtil.applyTransientModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    "COSMovementSpeed",
                    movementMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE);

            double swingSpeedMultiplier = getSwingSpeedMultiplier(pAmplifier);
            AttributeModifierUtil.applyTransientModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    "COSSwingSpeed",
                    swingSpeedMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE);

            if (pAmplifier >= 1) {
                if (isSleeping(player)) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        ModNetworking.sendToPlayer(new SleepPacketS2C(true), serverPlayer);
                    }
                    makePlayerFallAsleep(player);
                    decrementSleepTimer(player);
                } else if (shouldFallAsleep(pAmplifier)) {
                    setSleepTimer(player, sleepDuration);
                }
            }

            if (pAmplifier >= 2) {
                limitInventoryToToolbar(player);
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            AttributeModifierUtil.removeModifier(player, Attributes.MOVEMENT_SPEED, "COSMovementSpeed");
            AttributeModifierUtil.removeModifier(player, Attributes.ATTACK_SPEED, "COSSwingSpeed");
            wakePlayerUp(player);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    private static double getMovementMultiplier(int pAmplifier){
        double[] movementSpeedValues = {-0.5, -0.7, -0.9};
        int index = Math.min(pAmplifier, movementSpeedValues.length - 1);
        return movementSpeedValues[index];
    }

    private static double getSwingSpeedMultiplier(int pAmplifier){
        double[] swingSpeedValues = {-0.25, -0.40, -0.66};
        int index = Math.min(pAmplifier, swingSpeedValues.length - 1);
        return swingSpeedValues[index];
    }

    private boolean shouldFallAsleep(int pAmplifier) {
        int chance = pAmplifier == 1 ? 100 : 66;
        return random.nextInt(chance) == 0;
    }

    private void makePlayerFallAsleep(Player player) {
        player.setSprinting(false);
        AttributeModifierUtil.applyTransientModifier(
                player,
                Attributes.MOVEMENT_SPEED,
                "COSSleepSpeed",
                -10,
                AttributeModifier.Operation.ADDITION);
    }

    private boolean isSleeping(Player player) {
        return getSleepTimer(player) > 0;
    }

    private void decrementSleepTimer(Player player) {
        int sleepTicks = getSleepTimer(player);
        if (sleepTicks > 0) {
            setSleepTimer(player, sleepTicks - 1);
            if (sleepTicks == 1) {
                wakePlayerUp(player);
            }
        }
    }

    private void wakePlayerUp(Player player) {
        AttributeModifierUtil.removeModifier(player, Attributes.MOVEMENT_SPEED, "COSSleepSpeed");
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(new SleepPacketS2C(false), serverPlayer);
        }
    }

    private int getSleepTimer(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getInt(sleepTicksTag.toString());
    }

    private void setSleepTimer(Player player, int ticks) {
        CompoundTag data = player.getPersistentData();
        data.putInt(sleepTicksTag.toString(), ticks);
    }

    private void limitInventoryToToolbar(Player player) {
        for (int i = player.getInventory().items.size() - 1; i >= 9; i--) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty()) {
                player.drop(stack, true);
                player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }
    }
}
