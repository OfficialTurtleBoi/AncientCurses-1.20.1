package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.turtleboi.ancientcurses.particle.ModParticles;

import java.util.function.Consumer;

public class CurseOfWrathEffect extends MobEffect {
    public CurseOfWrathEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity.level().isClientSide) {
            int effectColor = this.getColor();
            float red = ((effectColor >> 16) & 0xFF) / 255.0F;
            float green = ((effectColor >> 8) & 0xFF) / 255.0F;
            float blue = (effectColor & 0xFF) / 255.0F;
            for (int i = 0; i < 5; i++) {
                pLivingEntity.level().addParticle(
                        ModParticles.CURSED_PARTICLES.get(), // Replace this with your custom particle
                        pLivingEntity.getX() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                        pLivingEntity.getY() + pLivingEntity.getRandom().nextDouble() * pLivingEntity.getBbHeight(),
                        pLivingEntity.getZ() + (pLivingEntity.getRandom().nextDouble() - 0.5) * pLivingEntity.getBbWidth(),
                        red, green, blue);
            }
        }

        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {

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
        return duration % 20 == 0;
    }
}