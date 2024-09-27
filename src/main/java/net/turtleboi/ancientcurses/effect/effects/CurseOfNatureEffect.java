package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;

import java.lang.reflect.Method;
import java.util.List;

public class CurseOfNatureEffect extends MobEffect {
    public CurseOfNatureEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    // Keep the lightning strike timer
    public int tickcounterlightning = 0;

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

        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
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


            if (pAmplifier >= 2) {
                if (player.level().isDay() && !player.level().isClientSide) {
                    float lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
                    BlockPos blockpos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
                    boolean isWet = player.isInWaterRainOrBubble() || player.isInPowderSnow || player.wasInPowderSnow;
                    if (lightLevel > 0.5F && player.getRandom().nextFloat() * 30.0F < (lightLevel - 0.4F) * 2.0F && !isWet && player.level().canSeeSky(blockpos)) {
                        player.setSecondsOnFire(6);
                    }
                }
            }

            // Lightning strike logic
            if (pAmplifier >= 2) {
                // Increment tick counter every tick
                tickcounterlightning++;

                // Define a constant cooldown in ticks (e.g., 3 seconds)
                int lightningboltcooldown = 20 * 3;

                // Check if the counter has reached the cooldown time
                if (tickcounterlightning >= lightningboltcooldown) {
                    // Strike player and nearby mobs with lightning
                    strikewithlightningbolt(player);
                    strikemobswithlightning(player, 25.0D);

                    // Reset the lightning tick counter
                    tickcounterlightning = 0;
                }
            }
        }

        // Call the superclass method
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        if (pLivingEntity instanceof Player player) {
            // Additional effect cleanup logic (if necessary)
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int pAmplifier) {
        // Always return true to make the effect tick each time
        return true;
    }

    // Helper to calculate silverfish spawn chance
    public static double getSilverFishSpawnChance(int pAmplifier) {
        double[] silverfishSpawnChanceValues = {0.25, 0.33, 0.5};
        int index = Math.min(pAmplifier, silverfishSpawnChanceValues.length - 1);
        return silverfishSpawnChanceValues[index];
    }

    // Method to strike mobs with lightning within a radius
    private void strikemobswithlightning(Player player, double radius) {
        Level level = player.level();
        List<Mob> nearbyMobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius));
        int counter = 3; // Limit to striking 3 mobs

        for (Mob mob : nearbyMobs) {

            // Only strike if the mob can be turned and is not targeting the player
            if (isLightningStruckMob(mob)&&mob.getTarget() != player) {
                if (counter == 0) {
                    break;
                }
                counter--;
                strikewithlightningbolt(mob);
            }
        }
    }

    // Method to strike an entity with a lightning bolt
    private void strikewithlightningbolt(LivingEntity livingEntity) {
        Level level = livingEntity.level();
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        lightningBolt.setPos(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());

        level.addFreshEntity(lightningBolt);
    }

    public static final TagKey<EntityType<?>> LIGHTNING_STRUCK_MOBS = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), new ResourceLocation("ancientcurses", "lightning_struck_change_mobs"));
    public static boolean isLightningStruckMob(LivingEntity entity) {
        return entity.getType().is(LIGHTNING_STRUCK_MOBS);
    }

}
