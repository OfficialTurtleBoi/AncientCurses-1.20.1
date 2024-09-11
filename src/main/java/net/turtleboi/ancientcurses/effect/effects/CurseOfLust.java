package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.util.AttributeModifierUtil;

import java.util.List;
import java.util.UUID;

public class CurseOfLust extends MobEffect {
    private static final String curseGiverKey = "curseoflustgiveruuid";
    public CurseOfLust(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide) {
            if (pLivingEntity instanceof Player player) {
                if (pAmplifier >= 1 && getLustCooldown(player) <= 0) {
                    if (getLustTimer(player) > 0) {
                        forcePOVControl(player, pAmplifier);
                        if (pAmplifier >= 2) {
                            applyBerserkEffect(player, pAmplifier);
                        }
                        setLustTimer(player,getLustTimer(player) - 1);
                        player.sendSystemMessage(Component.literal("Lust timer: " + getLustTimer(player)));
                    } else if (getLustTimer(player) == 0) {
                        resetLustCooldown(player, pAmplifier);
                    } else {
                        triggerLustEffect(player, pAmplifier);
                    }
                } else {
                    setLustCooldown(player,getLustCooldown(player) - 1);
                    player.displayClientMessage(Component.literal("Lust cooldown: " + getLustCooldown(player)), true);
                }
            }

            if (pLivingEntity instanceof Monster monster) {
                applyBerserkAttributes(monster);
                targetCurseGiver(monster);
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            AttributeModifierUtil.removeModifier(player, Attributes.MOVEMENT_SPEED, "COLStun");
        }

        if (pLivingEntity instanceof Monster monster) {
            String movementName = "COLMovementSpeed";
            UUID movementUUID = AttributeModifierUtil.generateUUIDFromName(movementName);
            AttributeInstance movementInstance = monster.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementInstance != null) {
                movementInstance.removeModifier(movementUUID);
            }

            String damageName = "COLAttackDamage";
            UUID damageUUID = AttributeModifierUtil.generateUUIDFromName(damageName);
            AttributeInstance damageInstance = monster.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageInstance != null) {
                damageInstance.removeModifier(damageUUID);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }

    private static int getMinLustTime(int pAmplifier){
        int[] minLustTimeValues = {1800, 1200, 600};
        int index = Math.min(pAmplifier, minLustTimeValues.length - 1);
        return minLustTimeValues[index];
    }

    private static int getMaxLustTime(int pAmplifier){
        int[] maxLustTimeValues = {2400, 1800, 1200};
        int index = Math.min(pAmplifier, maxLustTimeValues.length - 1);
        return maxLustTimeValues[index];
    }

    private static int getLustTime(int pAmplifier){
        int[] lustTimeValues = {20, 100, 200};
        int index = Math.min(pAmplifier, lustTimeValues.length - 1);
        return lustTimeValues[index];
    }

    private void setLustCooldown(Player player, int cooldown) {
        player.getPersistentData().putInt("lustCooldown", cooldown);
    }

    private int getLustCooldown(Player player) {
        return player.getPersistentData().getInt("lustCooldown");
    }

    private void setLustTimer(Player player, int timer) {
        player.getPersistentData().putInt("lustTimer", timer);
    }

    private int getLustTimer(Player player) {
        return player.getPersistentData().getInt("lustTimer");
    }

    private void triggerLustEffect(Player player, int pAmplifier) {
        Mob targetMob = getNearbyMob(player, pAmplifier);
        if (targetMob != null) {
            forcePOVControl(player, pAmplifier);
            AttributeModifierUtil.applyTransientModifier(
                    player,
                    Attributes.MOVEMENT_SPEED,
                    "COLStun",
                    -10,
                    AttributeModifier.Operation.ADDITION);
        }
    }

    private void resetLustCooldown(Player player, int pAmplifier) {
        int minLustTime = getMinLustTime(pAmplifier);
        int maxLustTime = getMaxLustTime(pAmplifier);
        setLustTimer(player, getLustTime(pAmplifier));
        AttributeModifierUtil.removeModifier(player, Attributes.MOVEMENT_SPEED, "COLStun");
        setLustCooldown(player, minLustTime + player.getRandom().nextInt(maxLustTime - minLustTime + 1));
    }

    private Mob getNearbyMob(Player player, int pAmplifier) {
        List<Mob> nearbyMobs = player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(25.0D));
        if (!nearbyMobs.isEmpty()) {
            if (pAmplifier >= 2) {
                List<Monster> nearbyMonsters = player.level().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(25.0D));
                if (!nearbyMonsters.isEmpty()) {
                    return nearbyMonsters.get(player.level().random.nextInt(nearbyMonsters.size()));
                }
            }
            return nearbyMobs.get(player.level().random.nextInt(nearbyMobs.size()));
        }
        return null;
    }


    private void forcePOVControl(Player player, int pAmplifier) {
        Mob randomMob = getNearbyMob(player, pAmplifier);
        if (randomMob != null) {
            player.lookAt(EntityAnchorArgument.Anchor.EYES, randomMob.position());
        }
    }

    private void applyBerserkEffect(Player player, int pAmplifier) {
        if (getNearbyMob(player, pAmplifier) instanceof Monster berserkMob) {
            berserkMob.addEffect(new MobEffectInstance(ModEffects.CURSE_OF_LUST.get(), 600, 0));
            CompoundTag data = berserkMob.getPersistentData();
            data.putUUID(curseGiverKey, player.getUUID());
        }
    }

    private static void applyBerserkAttributes(Mob monster) {
        String movementName = "COLMovementSpeed";
        UUID movementUUID = AttributeModifierUtil.generateUUIDFromName(movementName);
        AttributeInstance movementInstance = monster.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementInstance != null) {
            if (movementInstance.getModifier(movementUUID) != null) {
                movementInstance.removeModifier(movementUUID);
            }
            movementInstance.addTransientModifier(new AttributeModifier(movementUUID, movementName, 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        String damageName = "COLAttackDamage";
        UUID damageUUID = AttributeModifierUtil.generateUUIDFromName(damageName);
        AttributeInstance damageInstance = monster.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageInstance != null) {
            if (damageInstance.getModifier(damageUUID) != null) {
                damageInstance.removeModifier(damageUUID);
            }
            damageInstance.addTransientModifier(new AttributeModifier(damageUUID, damageName, 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private static void targetCurseGiver(Mob monster) {
        CompoundTag data = monster.getPersistentData();
        if (data.contains(curseGiverKey)) {
            UUID curseGiverUUID = data.getUUID(curseGiverKey);
            Player targetPlayer = monster.level().getPlayerByUUID(curseGiverUUID);
            if (targetPlayer != null) {
                monster.setTarget(targetPlayer);
            }
        }
    }
}