package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.turtleboi.ancientcurses.ai.AnimalFollowPlayerGoal;
import net.turtleboi.ancientcurses.ai.FishFollowPlayerGoal;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.turtlecore.init.CoreAttributeModifiers;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.SendParticlesS2C;

import java.util.List;
import java.util.Random;

public class CurseOfWrathEffect extends MobEffect {
    private static final Random random = new Random();
    private String healthModifierName = "CoW_MaxHealth";
    private String damageModifierName = "CoW_AttackDamage";
    public CurseOfWrathEffect(MobEffectCategory pCategory, int pColor) {
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

            if (pLivingEntity instanceof Player player) {
                double range = 32.0D;
                AABB area = new AABB(player.blockPosition()).inflate(range);
                List<Mob> mobsInRange = player.level().getEntitiesOfClass(Mob.class, area, mob ->
                        !mob.hasEffect(ModEffects.CURSE_OF_WRATH.get()));
                for (Mob mob : mobsInRange) {
                    mob.addEffect(new MobEffectInstance(ModEffects.CURSE_OF_WRATH.get(), 6000));
                }
            }

            if (pLivingEntity instanceof Mob mob) {
                int randomCounter = random.nextInt(11) + 10;
                if (pLivingEntity.tickCount % randomCounter == 0) {
                    for (int i = 0; i < 3; i++) {
                        CoreNetworking.sendToNear(new SendParticlesS2C(
                                ParticleTypes.ANGRY_VILLAGER,
                                pLivingEntity.getX(),
                                pLivingEntity.getEyeY() + 0.25,
                                pLivingEntity.getZ(),
                                0.1,
                                0.25,
                                0.1), pLivingEntity);
                    }
                }


                double healthBoost = 0.5;
                double damageBoost = 0.5;
                if (pAmplifier == 1) {
                    healthBoost = 1.0;
                    damageBoost = 1.0;
                } else if (pAmplifier >= 2) {
                    healthBoost = 2.0;
                    damageBoost = 2.0;
                }

                CoreAttributeModifiers.applyPermanentModifier(
                        mob,
                        Attributes.MAX_HEALTH,
                        healthModifierName,
                        healthBoost,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                );

                CoreAttributeModifiers.applyPermanentModifier(
                        mob,
                        Attributes.ATTACK_DAMAGE,
                        damageModifierName,
                        damageBoost,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                );

                Player player = mob.level().getNearestPlayer(mob, 32.0D);
                if (pAmplifier >= 1 && player != null && player.hasEffect(ModEffects.CURSE_OF_WRATH.get())) {
                    if (mob instanceof Animal animal) {
                        animal.getLookControl().setLookAt(player, 30.0F, 30.0F);
                        if (animal.goalSelector.getRunningGoals().noneMatch(goal -> goal.getGoal() instanceof FollowMobGoal)) {
                            animal.goalSelector.addGoal(1, new AnimalFollowPlayerGoal(animal, player, 1.25D, 25.0D));
                        }
                        if (animal.distanceTo(player) < 1.75) {
                            if (pLivingEntity.tickCount % 20 == 0) {
                                player.hurt(animal.level().damageSources().mobAttack(animal), 1.0F);
                            }
                        }
                    }
                    if (mob instanceof AbstractFish fish) {
                        fish.getLookControl().setLookAt(player, 30.0F, 30.0F);
                        if (fish.goalSelector.getRunningGoals().noneMatch(goal -> goal.getGoal() instanceof FollowMobGoal)) {
                            fish.goalSelector.addGoal(1, new FishFollowPlayerGoal(fish, player, 1.25D, 25.0D));
                        }
                        if (fish.distanceTo(player) < 1.75) {
                            if (pLivingEntity.tickCount % 20 == 0) {
                                player.hurt(fish.level().damageSources().mobAttack(fish), 1.0F);
                            }
                        }
                    }
                    if (mob instanceof NeutralMob neutralMob) {
                        if (neutralMob instanceof Monster && !(neutralMob instanceof Piglin) || neutralMob instanceof EnderMan) {
                            return;
                        } else if (neutralMob.getTarget() != player) {
                            neutralMob.setTarget(player);
                        }
                    }
                    if (mob instanceof TamableAnimal tameableAnimal) {
                        if (!tameableAnimal.isTame()) {
                            if (tameableAnimal.getTarget() != player) {
                                tameableAnimal.setTarget(player);
                            }
                        } else if (tameableAnimal.isTame()) {
                            if (tameableAnimal.getTarget() == player) {
                                tameableAnimal.setTarget(null);
                            }
                        }
                    }
                    if (mob instanceof Piglin piglin && !piglin.isAggressive()) {
                        if (piglin.getTarget() != player) {
                            piglin.setTarget(player);
                        }
                    }
                    if (mob instanceof IronGolem golem) {
                        if (!golem.isPlayerCreated()) {
                            if (golem.getTarget() != player) {
                                golem.setTarget(player);
                            }
                        } else if (golem.isPlayerCreated()) {
                            if (golem.getTarget() == player) {
                                golem.setTarget(null);
                            }
                        }
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

        }

        if (pLivingEntity instanceof Mob mob) {
            CoreAttributeModifiers.removeModifier(
                    mob,
                    Attributes.MAX_HEALTH,
                    healthModifierName
            );

            CoreAttributeModifiers.removeModifier(
                    mob,
                    Attributes.ATTACK_DAMAGE,
                    damageModifierName
            );
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        return true;
    }
}