package net.turtleboi.ancientcurses.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void EntityItemPickupEvent(EntityItemPickupEvent event){
        Player player = event.getEntity();
        ItemEntity itemEntity = event.getItem();
        AttributeInstance ItemDestroyChanceAttribute = player.getAttribute(ModAttributes.ITEM_DESTROY_CHANCE.get());

        if (ItemDestroyChanceAttribute!=null) {

            double randomValue = player.getRandom().nextDouble();
            double itemdestroychance = ItemDestroyChanceAttribute.getValue();
            if (itemdestroychance != 0) {

                if (randomValue > itemdestroychance) {
                    event.setCanceled(true);
                    itemEntity.remove(Entity.RemovalReason.DISCARDED);
                    player.sendSystemMessage(Component.literal("DESTROYED!")); //debug code
                }
            }
        }
    }
}
