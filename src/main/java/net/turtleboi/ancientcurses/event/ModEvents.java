package net.turtleboi.ancientcurses.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.effect.effects.CurseOfEnvyEffect;
import net.turtleboi.ancientcurses.effect.effects.CurseOfGluttonyEffect;
import net.turtleboi.ancientcurses.effect.effects.CurseOfGreedEffect;
import net.turtleboi.ancientcurses.effect.effects.CurseOfNatureEffect;
import net.turtleboi.ancientcurses.init.ModAttributes;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.util.ItemValueMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

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
            //ENVY CURSE EFFECT
            MobEffectInstance envycurse = player.getEffect(ModEffects.CURSE_OF_ENVY.get());
            if (envycurse!=null) {
                double itemDropOnUseChance = CurseOfEnvyEffect.getItemDropOnUseChance(envycurse.getAmplifier());
                double randomValue2 = player.getRandom().nextDouble();
                if (randomValue2 < itemDropOnUseChance) {
                    EquipmentSlot slot = EquipmentSlot.MAINHAND;
                    ItemStack DiscardItem = player.getItemBySlot(slot);

                    if (DiscardItem != ItemStack.EMPTY && !player.level().isClientSide) {
                        player.drop(DiscardItem, false);
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
            //FRAILTY CURSE EFFECT
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
        if (event.getEntity() instanceof Player) {
            ItemEntity itemEntity = event.getItem();
            ItemStack itemStack = event.getItem().getItem();
            Player player = event.getEntity();
            Level level = player.level();
            MobEffectInstance greedCurse = player.getEffect(ModEffects.CURSE_OF_GREED.get());
            if (greedCurse != null) {
                int amplifier = greedCurse.getAmplifier();
                if (amplifier >= 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true));
                }

                if (amplifier >= 1) {
                    double randomValue = player.getRandom().nextDouble();
                    double itemDestroyChance = CurseOfGreedEffect.getItemDestroyChance(amplifier);
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

            int itemValue = ItemValueMap.getItemValue(itemStack, level);
            player.displayClientMessage(Component.literal(
                    "Picked up " + itemStack.getHoverName().getString() + " with a value of: " + itemValue).withStyle(ChatFormatting.GOLD), true //debug code
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void BreakEvent(BlockEvent.BreakEvent event) {




        Player player = event.getPlayer();
        Level level = player.level();
        BlockPos blockPos = event.getPos();

        //ENVY CURSE EFFECT
        MobEffectInstance envycurse = player.getEffect(ModEffects.CURSE_OF_ENVY.get());
        if (envycurse!=null) {
            double itemDropOnUseChance = CurseOfEnvyEffect.getItemDropOnUseChance(envycurse.getAmplifier());
            double randomValue2 = player.getRandom().nextDouble();
            if (randomValue2 < itemDropOnUseChance) {
                EquipmentSlot slot = EquipmentSlot.MAINHAND;
                ItemStack DiscardItem = player.getItemBySlot(slot);

                if (DiscardItem != ItemStack.EMPTY && !player.level().isClientSide) {
                    player.drop(DiscardItem, false);
                    player.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }

        //NATURE CURSE EFFECT
        MobEffectInstance natureCurse = player.getEffect(ModEffects.CURSE_OF_NATURE.get());
        if (natureCurse != null) {
            int amplifier = natureCurse.getAmplifier();
            Block brokenBlock = level.getBlockState(blockPos).getBlock();
            if (amplifier >= 0 && isStoneTypeBlock(brokenBlock)) {
                double randomValue = player.getRandom().nextDouble();
                double silverFishSpawnChance = CurseOfNatureEffect.getSilverFishSpawnChance(amplifier);
                if (randomValue < silverFishSpawnChance) {
                    Silverfish silverfish = new Silverfish(EntityType.SILVERFISH, level);
                    silverfish.setPos(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
                    level.addFreshEntity(silverfish);
                    silverfish.spawnAnim();
                }
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void RightClickItemuseOnBlock(PlayerInteractEvent.RightClickBlock event){

            Player player = event.getEntity();
            //ENVY CURSE EFFECT
            MobEffectInstance envycurse = player.getEffect(ModEffects.CURSE_OF_ENVY.get());
            if (envycurse!=null) {
                double itemDropOnUseChance = CurseOfEnvyEffect.getItemDropOnUseChance(envycurse.getAmplifier());
                double randomValue2 = player.getRandom().nextDouble();
                if (randomValue2 < itemDropOnUseChance) {
                    InteractionHand hand = event.getHand();
                    EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;

                    ItemStack DiscardItem = player.getItemBySlot(slot);

                    if (DiscardItem != ItemStack.EMPTY && !player.level().isClientSide) {
                        player.drop(DiscardItem, false);
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
    }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void RightClickItemuse(LivingEntityUseItemEvent event){
        if (event.getEntity() instanceof Player player&&event instanceof LivingEntityUseItemEvent.Start) {            //ENVY CURSE EFFECT
            MobEffectInstance envycurse = player.getEffect(ModEffects.CURSE_OF_ENVY.get());
            if (envycurse != null) {
                double itemDropOnUseChance = CurseOfEnvyEffect.getItemDropOnUseChance(envycurse.getAmplifier());
                double randomValue2 = player.getRandom().nextDouble();

                if (randomValue2 < itemDropOnUseChance) {
                    InteractionHand hand = event.getEntity().getUsedItemHand();
                    EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;                    ItemStack DiscardItem = player.getItemBySlot(slot);

                    if (DiscardItem != ItemStack.EMPTY && !player.level().isClientSide) {
                        player.drop(DiscardItem, false);
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void PlayerDamageEvent(LivingDamageEvent event) {
        Entity entityCause = event.getSource().getEntity();

        if (event.getEntity() instanceof Player player && entityCause instanceof Mob mobCause && !player.level().isClientSide) {
            MobEffectInstance envyCurse = player.getEffect(ModEffects.CURSE_OF_ENVY.get());
            if (envyCurse != null) {
                double itemDropOnUseChance = CurseOfEnvyEffect.getItemDropOnUseChance(envyCurse.getAmplifier());
                double randomValue = player.getRandom().nextDouble();
                if (randomValue < itemDropOnUseChance) {
                    List<EquipmentSlot> equippedItems = new ArrayList<>();

                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        equippedItems.add(slot);


                    }
                    EquipmentSlot slot = equippedItems.get(player.getRandom().nextInt(equippedItems.size()));
                    ItemStack playerItem = player.getItemBySlot(slot);

                    if (!playerItem.isEmpty()) {
                        ItemStack mobItem = mobCause.getItemBySlot(slot);
                        if (mobItem.isEmpty()) {
                            player.setItemSlot(slot, ItemStack.EMPTY);
                            mobCause.setItemSlot(slot, playerItem.copy());
                        }
                    }
                }
            }
    }



}

    public static final TagKey<Block> StoneForge = BlockTags.create(new ResourceLocation("forge", "stone"));
    public static final TagKey<Block> CobblestoneForge = BlockTags.create(new ResourceLocation("forge", "cobblestone"));
    public static final TagKey<Block> OreForge = BlockTags.create(new ResourceLocation("forge", "ores"));

    private static boolean isStoneTypeBlock(Block block) {
        return block.defaultBlockState().is(OreForge)||
                block.defaultBlockState().is(CobblestoneForge)||
                block.defaultBlockState().is(StoneForge);
    }





    public static Pair<ItemStack, EquipmentSlot> getRandomEquippedItem(Player player) {
        List<Pair<ItemStack, EquipmentSlot>> equippedItems = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = player.getItemBySlot(slot);
            if (!item.isEmpty()) {
                equippedItems.add(Pair.of(item, slot));
            }
        }

        if (equippedItems.isEmpty()) {
            return Pair.of(ItemStack.EMPTY, null);
        }

        RandomSource random = player.getRandom();
        return equippedItems.get(random.nextInt(equippedItems.size()));
    }

}
