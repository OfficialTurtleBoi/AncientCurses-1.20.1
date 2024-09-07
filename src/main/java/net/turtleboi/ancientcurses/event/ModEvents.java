package net.turtleboi.ancientcurses.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.init.ModAttributes;

import java.util.Random;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID)
public class ModEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerAttack(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player) {
            AttributeInstance hitChanceAttribute = player.getAttribute(ModAttributes.HIT_CHANCE.get());

            if (hitChanceAttribute != null) {
                double hitChance = hitChanceAttribute.getValue();
                double randomValue = player.getRandom().nextDouble();
                if (randomValue > hitChance) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("Missed!")); //debug code
                }
            }
        }
    }
}
