package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
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
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            double movementMultiplier = getMovementMultiplier(pAmplifier);
            AttributeModifierUtil.applyTransientModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    "COSMovementSpeed",
                    movementMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE);

            double miningMultiplier = getMiningAttackSpeedMultiplier(pAmplifier);
            AttributeModifierUtil.applyTransientModifier(
                    player,
                    Attributes.ATTACK_SPEED,
                    "COSSwingSpeed",
                    miningMultiplier,
                    AttributeModifier.Operation.MULTIPLY_BASE);

            if (pAmplifier >= 1) {
                if (isSleeping(player)) {
                    makePlayerFallAsleep(player);
                    decrementSleepTimer(player);
                } else if (shouldFallAsleep(pAmplifier)) {
                    setSleepTimer(player, sleepDuration);
                }
            }

            if (pAmplifier == 2) {
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

    private double getMovementMultiplier(int pAmplifier) {
        switch (pAmplifier) {
            case 0: return -0.5;
            case 1: return -0.7;
            case 2: return -0.9;
            default: return 0;
        }
    }

    private double getMiningAttackSpeedMultiplier(int pAmplifier) {
        switch (pAmplifier) {
            case 0: return -0.25;
            case 1: return -0.40;
            case 2: return -0.66;
            default: return 0;
        }
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
        for (int i = 9; i < player.getInventory().items.size(); i++) {
            if (!player.getInventory().items.get(i).isEmpty()) {
                player.drop(player.getInventory().items.get(i), true);
            }
        }
    }
}
