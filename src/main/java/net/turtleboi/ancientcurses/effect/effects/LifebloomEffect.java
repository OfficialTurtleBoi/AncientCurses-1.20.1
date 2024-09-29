package net.turtleboi.ancientcurses.effect.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;

public class LifebloomEffect extends MobEffect {
    public LifebloomEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    private boolean healthRegenActive = false;

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide && pLivingEntity instanceof Player player) {
            if (player.getHealth() < (player.getMaxHealth()/2)){
                setHealthRegenState(true);
            }

            if (getHealthRegenState() && player.tickCount % (20 * (pAmplifier + 1)) == 0){
                player.heal(1.0F);
            }

            if (player.getHealth() == player.getMaxHealth()){
                setHealthRegenState(false);
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

    private void setHealthRegenState(boolean triggered){
        this.healthRegenActive = triggered;
    }

    private boolean getHealthRegenState(){
        return healthRegenActive;
    }
}