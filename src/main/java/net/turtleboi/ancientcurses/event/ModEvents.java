package net.turtleboi.ancientcurses.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.effect.effects.CurseOfGluttonyEffect;
import net.turtleboi.ancientcurses.init.ModAttributes;
import net.turtleboi.ancientcurses.item.ModItems;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID)
public class ModEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingUpdate(PlayerEvent event) {
        Player player = (Player) event.getEntity();


    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerAttack(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player) {
            AttributeInstance hitChanceAttribute = player.getAttribute(ModAttributes.HIT_CHANCE.get());
            if (hitChanceAttribute != null) {
                double hitChance = hitChanceAttribute.getValue();
                double randomValue = player.getRandom().nextDouble();
                if (randomValue > hitChance) {
                    event.setCanceled(true);
                }

                LivingEntity target = event.getEntity();
                Level level = target.level();
                if (event.isCanceled() && target != null) {
                    double x = target.getX();
                    double y = target.getY() + target.getBbHeight() / 2.0;
                    double z = target.getZ();
                    for (int i = 0; i < 10; i++) {
                        level.addParticle(ParticleTypes.CLOUD,
                                x + (player.getRandom().nextDouble() - 0.5),
                                y + (player.getRandom().nextDouble() - 0.5),
                                z + (player.getRandom().nextDouble() - 0.5),
                                0.0, 0.0, 0.0);
                    }
                    //player.sendSystemMessage(Component.literal("Missed!")); //debug code
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack itemStack = event.getItem();
            if (itemStack.isEdible()) {
                FoodProperties foodProperties = itemStack.getItem().getFoodProperties(itemStack, player);
                MobEffectInstance curseEffect = player.getEffect(ModEffects.CURSE_OF_GLUTTONY.get());
                if (curseEffect != null && foodProperties != null) {
                    int baseHunger = foodProperties.getNutrition();
                    float baseSaturation = foodProperties.getSaturationModifier();
                    int amplifier = curseEffect.getAmplifier();

                    player.getFoodData().eat(-baseHunger, -baseSaturation);
                    CurseOfGluttonyEffect.modifyFoodRestoration(player, itemStack, amplifier);
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
            double itemDestroyChance = ItemDestroyChanceAttribute.getValue();
            if (itemDestroyChance != 0) {

                if (randomValue > itemDestroyChance) {
                    event.setCanceled(true);
                    itemEntity.remove(Entity.RemovalReason.DISCARDED);
                    player.getInventory().add(new ItemStack(ModItems.ROT_CLUMP.get()));
                    player.displayClientMessage(Component.literal("How unlucky...").withStyle(ChatFormatting.RED), true);
                }
            }
        }
    }
}
