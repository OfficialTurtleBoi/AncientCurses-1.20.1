package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
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
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.LustedPacketS2C;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.util.AttributeModifierUtil;

import java.util.List;
import java.util.UUID;

public class CurseOfObessionEffect extends MobEffect {
    public CurseOfObessionEffect(MobEffectCategory pCategory, int pColor) {
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
                            ModParticleTypes.CURSED_PARTICLE.get(),
                            pLivingEntity.getX() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            pLivingEntity.getY() + pLivingEntity.getRandom().nextDouble() * pLivingEntity.getBbHeight(),
                            pLivingEntity.getZ() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            red, green, blue);
                }
            }
        }

        if (!pLivingEntity.level().isClientSide) {
            if (pLivingEntity.tickCount % 20 == 0) {
                int effectColor = this.getColor();
                float red = ((effectColor >> 16) & 0xFF) / 255.0F;
                float green = ((effectColor >> 8) & 0xFF) / 255.0F;
                float blue = (effectColor & 0xFF) / 255.0F;
                for (int i = 0; i < 5; i++) {
                    pLivingEntity.level().addParticle(
                            ModParticleTypes.CURSED_PARTICLE.get(),
                            pLivingEntity.getX() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            pLivingEntity.getY() + pLivingEntity.getRandom().nextDouble() * pLivingEntity.getBbHeight(),
                            pLivingEntity.getZ() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                            red, green, blue);
                }
            }

            if (pLivingEntity instanceof Player player) {
                if (pAmplifier >= 1 && getLustCooldown(player) <= 0) {
                    if (getLustTimer(player) > 0) {
                        getNearbyMob(player, pAmplifier);
                        forcePOVControl(player);
                        AttributeModifierUtil.applyTransientModifier(
                                player,
                                Attributes.MOVEMENT_SPEED,
                                "COLStun",
                                -10,
                                AttributeModifier.Operation.ADDITION);
                        if (pAmplifier >= 2) {
                            applyBerserkEffect(player);
                        }
                        setLustTimer(player,getLustTimer(player) - 1);
                        //player.sendSystemMessage(Component.literal("Lust timer: " + getLustTimer(player)));
                    } else if (getLustTimer(player) == 0) {
                        resetLustCooldown(player, pAmplifier);
                    } else {
                        triggerLustEffect(player);
                    }
                } else {
                    setLustCooldown(player,getLustCooldown(player) - 1);
                    //player.displayClientMessage(Component.literal("Lust cooldown: " + getLustCooldown(player)), true);
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    ModNetworking.sendToPlayer(new LustedPacketS2C(CurseOfObessionEffect.hasLustTarget(player)), serverPlayer);
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

    private static void setLustCooldown(Player player, int cooldown) {
        player.getPersistentData().putInt("lustCooldown", cooldown);
    }

    private int getLustCooldown(Player player) {
        return player.getPersistentData().getInt("lustCooldown");
    }

    public static void setLustTimer(Player player, int timer) {
        player.getPersistentData().putInt("lustTimer", timer);
    }

    private int getLustTimer(Player player) {
        return player.getPersistentData().getInt("lustTimer");
    }

    public static boolean hasLustTarget(Player player){
        return player.getPersistentData().getBoolean("hasLustTarget");
    }

    private void triggerLustEffect(Player player) {
        Mob targetMob = getLustTarget(player);
        if (targetMob != null) {
            forcePOVControl(player);
            player.getPersistentData().putBoolean("isLusted", true);
        }
    }

    public static void resetLustCooldown(Player player, int pAmplifier) {
        int minLustTime = getMinLustTime(pAmplifier);
        int maxLustTime = getMaxLustTime(pAmplifier);
        setLustTimer(player, 0);
        setLustTimer(player, getLustTime(pAmplifier));
        clearLustTarget(player);
        AttributeModifierUtil.removeModifier(player, Attributes.MOVEMENT_SPEED, "COLStun");
        setLustCooldown(player, minLustTime + player.getRandom().nextInt(maxLustTime - minLustTime + 1));
    }

    private Mob getNearbyMob(Player player, int pAmplifier) {
        if (hasLustTarget(player)) {
            return getLustTarget(player);
        }
        List<Mob> nearbyMobs = player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(25.0D));
        if (!nearbyMobs.isEmpty()) {
            Mob nearestMob = null;
            double nearestDistance = Double.MAX_VALUE;
            if (pAmplifier >= 2) {
                List<Monster> nearbyMonsters = player.level().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(25.0D));
                for (Monster monster : nearbyMonsters) {
                    double distanceToMonster = player.distanceToSqr(monster);
                    if (distanceToMonster < nearestDistance) {
                        nearestDistance = distanceToMonster;
                        nearestMob = monster;
                    }
                }
            }
            if (nearestMob == null) {
                for (Mob mob : nearbyMobs) {
                    double distanceToMob = player.distanceToSqr(mob);
                    if (distanceToMob < nearestDistance) {
                        nearestDistance = distanceToMob;
                        nearestMob = mob;
                    }
                }
            }
            if (nearestMob != null) {
                player.getPersistentData().putBoolean("hasLustTarget", true);
                player.getPersistentData().putUUID("lustTarget", nearestMob.getUUID());
                return nearestMob;
            }
        }
        return null;
    }


    private Mob getLustTarget(Player player) {
        if (player.getPersistentData().hasUUID("lustTarget")) {
            UUID targetUUID = player.getPersistentData().getUUID("lustTarget");
            Level level = player.level();
            Entity entity = ((ServerLevel) level).getEntity(targetUUID);
            if (entity instanceof Mob mob) {
                return mob;
            } else {
                clearLustTarget(player);
            }
        }
        return null;
    }

    public static void clearLustTarget(Player player) {
        player.getPersistentData().remove("lustTarget");
        player.getPersistentData().putBoolean("isLusted", false);
        player.getPersistentData().putBoolean("hasLustTarget", false);
    }

    private void forcePOVControl(Player player) {
        Mob randomMob = getLustTarget(player);
        if (randomMob != null) {
            player.lookAt(EntityAnchorArgument.Anchor.EYES, randomMob.getEyePosition());
        }
    }

    private void applyBerserkEffect(Player player) {
        if (getLustTarget(player) instanceof Monster berserkMob) {
            berserkMob.addEffect(new MobEffectInstance(ModEffects.CURSE_OF_OBESSSION.get(), 600, 0));
            CompoundTag data = berserkMob.getPersistentData();
            data.putUUID("curseoflustgiveruuid", player.getUUID());
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
            movementInstance.addTransientModifier(new AttributeModifier(movementUUID, movementName, 0.5, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        String damageName = "COLAttackDamage";
        UUID damageUUID = AttributeModifierUtil.generateUUIDFromName(damageName);
        AttributeInstance damageInstance = monster.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageInstance != null) {
            if (damageInstance.getModifier(damageUUID) != null) {
                damageInstance.removeModifier(damageUUID);
            }
            damageInstance.addTransientModifier(new AttributeModifier(damageUUID, damageName, 0.5, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    private static void targetCurseGiver(Mob monster) {
        if (monster.getPersistentData().contains("curseoflustgiveruuid")) {
            UUID curseGiverUUID = monster.getPersistentData().getUUID("curseoflustgiveruuid");
            Player targetPlayer = monster.level().getPlayerByUUID(curseGiverUUID);
            if (targetPlayer != null) {
                monster.setTarget(targetPlayer);
            }
        }
    }

    public static void removeTarget(Mob monster) {
        monster.getPersistentData().remove("curseoflustgiveruuid");
        monster.setTarget(null);
    }
}