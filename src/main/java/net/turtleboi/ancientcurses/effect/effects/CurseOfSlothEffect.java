package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.turtlecore.effect.CoreEffects;
import net.turtleboi.turtlecore.init.CoreAttributeModifiers;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.SendParticlesS2C;

import java.util.Random;


public class CurseOfSlothEffect extends MobEffect {
    private static final Random random = new Random();
    private static final int sleepDuration = 60;
    public CurseOfSlothEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide) {
            if (pLivingEntity.tickCount % 20 == 0) {
                int effectColor = this.getColor();
                float red = ((effectColor >> 16) & 0xFF) / 255.0F;
                float green = ((effectColor >> 8) & 0xFF) / 255.0F;
                float blue = (effectColor & 0xFF) / 255.0F;
                for (int i = 0; i < 5; i++) {
                    CoreNetworking.sendToNear(new SendParticlesS2C(
                            ModParticleTypes.CURSED_PARTICLE.get(),
                            pLivingEntity.getX() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            pLivingEntity.getY() + pLivingEntity.getRandom().nextDouble() * pLivingEntity.getBbHeight(),
                            pLivingEntity.getZ() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            red, green, blue), pLivingEntity);
                }
            }

            double movementMultiplier = getMovementMultiplier(pAmplifier);
            CoreAttributeModifiers.applyTransientModifier(
                    pLivingEntity,
                    Attributes.MOVEMENT_SPEED,
                    "COSMovementSpeed",
                    movementMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE);

            double swingSpeedMultiplier = getSwingSpeedMultiplier(pAmplifier);
            CoreAttributeModifiers.applyTransientModifier(
                    pLivingEntity,
                    Attributes.ATTACK_SPEED,
                    "COSSwingSpeed",
                    swingSpeedMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE);

            if (pAmplifier >= 1) {
                if (shouldFallAsleep(pLivingEntity ,pAmplifier)) {
                    makeEntitySleep(pLivingEntity);
                }
            }

            if (pAmplifier >= 2) {
                if (pLivingEntity instanceof Player player) {
                    limitInventoryToToolbar(player);
                }
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            CoreAttributeModifiers.removeModifier(player, Attributes.MOVEMENT_SPEED, "COSMovementSpeed");
            CoreAttributeModifiers.removeModifier(player, Attributes.ATTACK_SPEED, "COSSwingSpeed");
            wakeEntityUp(player);
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

    private boolean shouldFallAsleep(LivingEntity livingEntity, int pAmplifier) {
        if (isSleeping(livingEntity)) {
            return false;
        }
        int chance = pAmplifier == 1 ? 100 : 66;
        return random.nextInt(chance) == 0;
    }


    private void makeEntitySleep(LivingEntity livingEntity) {
        livingEntity.addEffect(new MobEffectInstance(CoreEffects.SLEEP.get(), sleepDuration));
    }

    private boolean isSleeping(LivingEntity livingEntity) {
        return livingEntity.hasEffect(CoreEffects.SLEEP.get());
    }


    private void wakeEntityUp(LivingEntity livingEntity) {
        livingEntity.removeEffect(CoreEffects.SLEEP.get());
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
