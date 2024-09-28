package net.turtleboi.ancientcurses.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.ai.AnimalFollowPlayerGoal;
import net.turtleboi.ancientcurses.ai.FishFollowPlayerGoal;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.effect.effects.*;
import net.turtleboi.ancientcurses.init.ModAttributes;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.item.items.GoldenAmuletItem;
import net.turtleboi.ancientcurses.item.items.PreciousGemItem;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.SendParticlesS2C;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.trials.*;
import net.turtleboi.ancientcurses.util.ItemValueMap;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID)
public class ModEvents {
    private static final Random random = new Random();
    private static int tickCounter = random.nextInt(11) + 10;

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        BlockPos altarPos = PlayerTrialData.getCurrentAltarPos(player);
        if (altarPos == null) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(altarPos);
        if (!(blockEntity instanceof CursedAltarBlockEntity altar)) {
            return;
        }

        altar.removePlayerFromTrial(player);
        MobEffect curseEffect = PlayerTrialData.getCurseEffect(player);
        if (curseEffect != null) {
            player.removeEffect(curseEffect);
            player.addEffect(new MobEffectInstance(curseEffect, 2400, PlayerTrialData.getCurseAmplifier(player), false, false, true));
        }
        PlayerTrialData.clearCurrentAltarPos(player);
        PlayerTrialData.clearCurseEffect(player);
        PlayerTrialData.clearCurseAmplifier(player);

        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(
                    new SyncTrialDataS2C(
                            "None",
                            "",
                            0,
                            0,
                            0,
                            0,
                            "",
                            0,
                            0),
                    serverPlayer);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (entity instanceof Mob mob && !level.isClientSide) {
            for (Player player : level.players()) {
                MobEffectInstance wrathCurse = player.getEffect(ModEffects.CURSE_OF_WRATH.get());
                if (wrathCurse != null && player.distanceTo(entity) <= 25) {
                    int amplifier = wrathCurse.getAmplifier();

                    if (amplifier >= 1 ) {
                        if (mob instanceof Animal animal) {
                            animal.getLookControl().setLookAt(player, 30.0F, 30.0F);
                            if (animal.goalSelector.getRunningGoals().noneMatch(goal -> goal.getGoal() instanceof FollowMobGoal)) {
                                animal.goalSelector.addGoal(1, new AnimalFollowPlayerGoal(animal, player, 1.25D, 25.0D));
                            }
                            if (animal.distanceTo(player) < 1.75) {
                                if (tickCounter <= 0) {
                                    player.hurt(level.damageSources().mobAttack(animal), 1.0F);
                                }
                            }
                        }
                        if (mob instanceof AbstractFish fish) {
                            fish.getLookControl().setLookAt(player, 30.0F, 30.0F);
                            if (fish.goalSelector.getRunningGoals().noneMatch(goal -> goal.getGoal() instanceof FollowMobGoal)) {
                                fish.goalSelector.addGoal(1, new FishFollowPlayerGoal(fish, player, 1.25D, 25.0D));
                            }
                            if (fish.distanceTo(player) < 1.75) {
                                if (tickCounter <= 0) {
                                    player.hurt(level.damageSources().mobAttack(fish), 1.0F);
                                }
                            }
                        }
                        if (mob instanceof NeutralMob neutralMob) {
                            if (neutralMob instanceof Monster && !(neutralMob instanceof Piglin) || neutralMob instanceof EnderMan) {
                                return;
                            } else if (neutralMob.getTarget() != player){
                                 neutralMob.setTarget(player);
                            }
                        }
                        if (mob instanceof TamableAnimal tameableAnimal) {
                            if (!tameableAnimal.isTame()) {
                                if (tameableAnimal.getTarget() != player){
                                    tameableAnimal.setTarget(player);
                                }
                            } else if (tameableAnimal.isTame()){
                                if (tameableAnimal.getTarget() == player){
                                    tameableAnimal.setTarget(null);
                                }
                            }
                        }
                        if (mob instanceof Piglin piglin && !piglin.isAggressive()) {
                            if (piglin.getTarget() != player){
                                piglin.setTarget(player);
                            }
                        }
                        if (mob instanceof IronGolem golem) {
                            if (!golem.isPlayerCreated()) {
                                if (golem.getTarget() != player){
                                    golem.setTarget(player);
                                }
                            } else if (golem.isPlayerCreated()){
                                if (golem.getTarget() == player){
                                    golem.setTarget(null);
                                }
                            }
                        }
                        if (player instanceof ServerPlayer serverPlayer) {
                            if (tickCounter <= 0) {
                                ModNetworking.sendToPlayer(new SendParticlesS2C(
                                        ParticleTypes.ANGRY_VILLAGER,
                                        mob.getX(),
                                        mob.getEyeY() + 0.25,
                                        mob.getZ(),
                                        0.1,
                                        0.25,
                                        0.1,
                                        3,
                                        1
                                ), serverPlayer);
                                tickCounter = random.nextInt(11) + 10;
                            } else {
                                tickCounter--;
                            }
                        }
                    }
                }
                if (mob instanceof Monster monster) {
                    MobEffectInstance lustCurse = monster.getEffect(ModEffects.CURSE_OF_OBESSSION.get());
                    if (lustCurse != null) {
                        if (player instanceof ServerPlayer serverPlayer) {
                            if (tickCounter <= 0) {
                                ModNetworking.sendToPlayer(new SendParticlesS2C(
                                        ParticleTypes.HEART,
                                        mob.getX(),
                                        mob.getEyeY() + 0.25,
                                        mob.getZ(),
                                        0.1,
                                        0.25,
                                        0.1,
                                        3,
                                        1
                                ), serverPlayer);
                                tickCounter = random.nextInt(11) + 10;
                            } else {
                                tickCounter--;
                            }
                        }
                    }
                }
            }

            if (mob.getPersistentData().contains("curseoflustgiveruuid")) {
                UUID curseGiverUUID = mob.getPersistentData().getUUID("curseoflustgiveruuid");
                Player curseGiver = mob.level().getPlayerByUUID(curseGiverUUID);
                if (curseGiver == null || curseGiver.isDeadOrDying()) {
                    CurseOfObessionEffect.removeTarget(mob);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerAttack(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player) {
            //ENVY CURSE EFFECT
            MobEffectInstance envyCurse = player.getEffect(ModEffects.CURSE_OF_ENVY.get());
            if (envyCurse!=null) {
                double itemDropOnUseChance = CurseOfEnvyEffect.getItemDropOnUseChance(envyCurse.getAmplifier());
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
            //MobEffectInstance wrathCurse = player.getEffect(ModEffects.CURSE_OF_WRATH.get());
            //if (wrathCurse!=null) {
            //    player.sendSystemMessage(Component.literal("Target health: " + event.getEntity().getHealth() + "/" + event.getEntity().getMaxHealth())); //debug code
            //}

            MobEffectInstance lustCurse = player.getEffect(ModEffects.CURSE_OF_OBESSSION.get());
            if (lustCurse != null) {
                int amplifier = lustCurse.getAmplifier();
                float damage = event.getAmount();
                float reflectDamage = damage * (0.1F + 0.1F * amplifier);
                reflectDamage = Math.max(1.0F, reflectDamage);

                player.hurt(new DamageSource(player.level().damageSources().generic().typeHolder()), reflectDamage);
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
            ItemStack itemStack = itemEntity.getItem();
            Player player = event.getEntity();
            Level level = player.level();
            MobEffectInstance greedCurse = player.getEffect(ModEffects.CURSE_OF_AVARICE.get());
            if (greedCurse != null) {
                CurseOfAvariceEffect.resetInventoryValue(player);
                int amplifier = greedCurse.getAmplifier();
                if (amplifier >= 0) {
                    int itemValue = ItemValueMap.getItemValue(itemStack, player.level());
                    int stackSize = itemStack.getCount();
                    int valueBasedAmplifier = Math.min(((itemValue * stackSize) / 100) - 1, 4);
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 * (1 + valueBasedAmplifier), valueBasedAmplifier, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60 * (1 + valueBasedAmplifier), valueBasedAmplifier, false, true));
                }

                if (amplifier >= 1) {
                    double itemDestroyChance = CurseOfAvariceEffect.getItemDestroyChance(amplifier);
                    if (itemDestroyChance != 0) {
                        int stackSize = itemStack.getCount();
                        for (int i = 0; i < stackSize; i++) {
                            double randomValue = player.getRandom().nextDouble();
                            if (randomValue <= itemDestroyChance) {
                                itemStack.shrink(1);
                                double x = player.getX();
                                double y = player.getY() + player.getBbHeight() / 2.0;
                                double z = player.getZ();
                                for (int j = 0; j < 10; j++) {
                                    level.addParticle(ParticleTypes.CLOUD,
                                            x + (player.getRandom().nextDouble() - 0.5),
                                            y + (player.getRandom().nextDouble() - 0.5),
                                            z + (player.getRandom().nextDouble() - 0.5),
                                            0.0, 0.1, 0.0);
                                }
                                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 1.0F, 1.0F);
                                player.getInventory().add(new ItemStack(ModItems.ROT_CLUMP.get()));
                                player.displayClientMessage(Component.literal("How unlucky...").withStyle(ChatFormatting.RED), true);
                            }
                        }

                        if (itemStack.isEmpty()) {
                            itemEntity.remove(Entity.RemovalReason.DISCARDED);
                        }
                    }
                }
            }

            //int itemValue = ItemValueMap.getItemValue(itemStack, level);
            //player.displayClientMessage(Component.literal(
            //        "Picked up " + itemStack.getHoverName().getString() + " with a value of: " + itemValue).withStyle(ChatFormatting.GOLD), true //debug code
            //);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        CurseOfAvariceEffect.resetInventoryValue(player);
    }

    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        Player player = event.getEntity();
        CurseOfAvariceEffect.resetInventoryValue(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void BreakEvent(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = player.level();
        BlockPos blockPos = event.getPos();

        //ENVY CURSE EFFECT
        MobEffectInstance envyCurse = player.getEffect(ModEffects.CURSE_OF_ENVY.get());
        if (envyCurse!=null) {
            double itemDropOnUseChance = CurseOfEnvyEffect.getItemDropOnUseChance(envyCurse.getAmplifier());
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
        MobEffectInstance envyCurse = player.getEffect(ModEffects.CURSE_OF_ENVY.get());
        if (envyCurse!=null) {
            double itemDropOnUseChance = CurseOfEnvyEffect.getItemDropOnUseChance(envyCurse.getAmplifier());
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

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (event.getTarget() instanceof Villager) {
            MobEffectInstance prideCurse = player.getEffect(ModEffects.CURSE_OF_PRIDE.get());
            if (prideCurse != null) {
                event.setCanceled(true);
                player.displayClientMessage(Component.literal("You're better than them!").withStyle(ChatFormatting.RED), true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemRightClick(LivingEntityUseItemEvent event){
        if (event.getEntity() instanceof Player player&&event instanceof LivingEntityUseItemEvent.Start) {
            MobEffectInstance envyCurse = player.getEffect(ModEffects.CURSE_OF_ENVY.get());
            if (envyCurse != null) {
                double itemDropOnUseChance = CurseOfEnvyEffect.getItemDropOnUseChance(envyCurse.getAmplifier());
                double randomValue2 = player.getRandom().nextDouble();

                if (randomValue2 < itemDropOnUseChance) {
                    InteractionHand hand = event.getEntity().getUsedItemHand();
                    EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    ItemStack DiscardItem = player.getItemBySlot(slot);

                    if (DiscardItem != ItemStack.EMPTY && !player.level().isClientSide) {
                        player.drop(DiscardItem, false);
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerAttacked(LivingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        Entity target = event.getEntity();
        if (attacker instanceof Mob mob && target instanceof Player player) {
            Level level = mob.level();
            if (!level.isClientSide) {
                MobEffectInstance shadowCurse = player.getEffect(ModEffects.CURSE_OF_SHADOWS.get());
                if (shadowCurse != null) {
                    MobEffectInstance invisibilityEffect = mob.getEffect(MobEffects.INVISIBILITY);
                    if (invisibilityEffect != null) {
                        mob.removeEffect(MobEffects.INVISIBILITY);
                    }
                }

                MobEffectInstance envyCurse = player.getEffect(ModEffects.CURSE_OF_ENVY.get());
                if (envyCurse != null) {
                    int amplifier = envyCurse.getAmplifier();
                    if (amplifier >= 1) {
                        float healingPercentage = CurseOfEnvyEffect.getHealPercentage(amplifier);
                        float healing = event.getAmount() * healingPercentage;
                        mob.heal(healing);
                        if (player instanceof ServerPlayer serverPlayer) {
                            ModNetworking.sendToPlayer(new SendParticlesS2C(
                                    ModParticleTypes.HEAL_PARTICLE.get(),
                                    mob.getX(),
                                    mob.getEyeY() + 0.5,
                                    mob.getZ(),
                                    0.1,
                                    0.25,
                                    0.1,
                                    10,
                                    1
                            ), serverPlayer);
                        }
                    }

                    if (amplifier >= 2) {
                        double itemDropChance = CurseOfEnvyEffect.getItemDropOnUseChance(amplifier);
                        double randomValue = player.getRandom().nextDouble();
                        if (randomValue < itemDropChance) {
                            EquipmentSlot[] slots = EquipmentSlot.values();
                            EquipmentSlot slot = slots[player.getRandom().nextInt(slots.length)];

                            ItemStack playerItem = player.getItemBySlot(slot);
                            if (!playerItem.isEmpty()) {
                                ItemStack mobItem = mob.getItemBySlot(slot);
                                if (mobItem.isEmpty()) {
                                    player.setItemSlot(slot, ItemStack.EMPTY);
                                    mob.setItemSlot(slot, playerItem.copy());
                                    mob.setGuaranteedDrop(slot);

                                    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                                            SoundEvents.ITEM_PICKUP, SoundSource.HOSTILE, 1.0F, 1.0F);
                                }
                            }
                        }
                    }
                }

                MobEffectInstance endCurse = player.getEffect(ModEffects.CURSE_OF_ENDING.get());
                if (endCurse != null) {
                    int pAmplifier = endCurse.getAmplifier();
                    if (!player.level().isClientSide) {
                        double teleportChance = CurseOfEndingEffect.getTeleportChance(pAmplifier);
                        if (player.getRandom().nextDouble() < teleportChance) {
                            CurseOfEndingEffect.randomTeleport(player, endCurse.getAmplifier());
                            if (pAmplifier >= 1) {
                                CurseOfEndingEffect.giveConfusion(player, 100);
                            }
                        }

                        if (CurseOfEndingEffect.isVoid(player)){
                            event.setAmount(1);
                        }
                    }
                }

                MobEffectInstance lustCurse = player.getEffect(ModEffects.CURSE_OF_OBESSSION.get());
                if (lustCurse != null && CurseOfObessionEffect.hasLustTarget(player)) {
                    CurseOfObessionEffect.resetLustCooldown(player, lustCurse.getAmplifier());
                }
            }
        } else if (target instanceof Player player){
            AttributeInstance dodgeChanceAttribute = player.getAttribute(ModAttributes.DODGE_CHANCE.get());
            if (dodgeChanceAttribute != null) {
                double hitChance = dodgeChanceAttribute.getValue();
                double randomValue = player.getRandom().nextDouble();
                if (randomValue > hitChance) {
                    event.setCanceled(true);
                }

                Level level = player.level();
                if (event.isCanceled() && player != null) {
                    double x = player.getX();
                    double y = player.getY() + player.getBbHeight() / 2.0;
                    double z = player.getZ();
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

    public static final TagKey<Block> StoneForge = BlockTags.create(new ResourceLocation("forge", "stone"));
    public static final TagKey<Block> CobblestoneForge = BlockTags.create(new ResourceLocation("forge", "cobblestone"));
    public static final TagKey<Block> OreForge = BlockTags.create(new ResourceLocation("forge", "ores"));

    private static boolean isStoneTypeBlock(Block block) {
        return block.defaultBlockState().is(OreForge)||
                block.defaultBlockState().is(CobblestoneForge)||
                block.defaultBlockState().is(StoneForge);
    }

    @SubscribeEvent
    public static void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (entity instanceof Mob mob && level instanceof ServerLevel) {
            for (Player player : level.players()) {
                MobEffectInstance wrathCurse = player.getEffect(ModEffects.CURSE_OF_WRATH.get());
                if (wrathCurse != null && player.distanceTo(entity) <= 25) {
                    int amplifier = wrathCurse.getAmplifier();

                    double healthBoost = 1.5;
                    double damageBoost = 1.5;
                    if (amplifier == 1) {
                        healthBoost = 2.0;
                        damageBoost = 2.0;
                    } else if (amplifier >= 2) {
                        healthBoost = 3.0;
                        damageBoost = 3.0;
                    }

                    if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
                        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(mob.getMaxHealth() * healthBoost);
                        mob.setHealth(mob.getMaxHealth());
                    }

                    if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                        mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(mob.getAttribute(Attributes.ATTACK_DAMAGE).getValue() * damageBoost);
                    }


                }
            }
        }

        if (entity instanceof Monster && !level.isClientSide) {
            for (Player player : level.players()) {
                MobEffectInstance shadowCurse = player.getEffect(ModEffects.CURSE_OF_SHADOWS.get());
                if (shadowCurse != null) {
                    int amplifier = shadowCurse.getAmplifier();
                    if (player.distanceToSqr(entity) < 2500) {
                        double spawnRateMultiplier = 1.0 + (0.5 * amplifier);
                        if (level.random.nextFloat() < spawnRateMultiplier) {
                            BlockPos spawnPos = new BlockPos((int) (entity.getX() + level.random.nextInt(6) - 3),
                                    (int) entity.getY(), (int) (entity.getZ() + level.random.nextInt(6) - 3));
                            Monster newMob = (Monster) entity.getType().create(level);
                            if (newMob != null) {
                                newMob.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                                level.addFreshEntity(newMob);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        Entity source = event.getSource().getEntity();

        if (source instanceof Player player) {
            MobEffectInstance wrathCurse = player.getEffect(ModEffects.CURSE_OF_WRATH.get());
            if (wrathCurse != null && wrathCurse.getAmplifier() >= 2) {
                double explosionRadius = 4.0;

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F,
                        (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

                if (player instanceof ServerPlayer serverPlayer) {
                    ModNetworking.sendToPlayer(new SendParticlesS2C(
                            ParticleTypes.EXPLOSION,
                            entity.getX(),
                            entity.getY() + 1,
                            entity.getZ(),
                            3,
                            3,
                            3,
                            4,
                            explosionRadius
                    ), serverPlayer);
                }
                if (player.distanceTo(entity) <= explosionRadius) {
                    player.hurt(new DamageSource(level.damageSources().generic().typeHolder()), player.distanceTo(entity) * 2);
                }
            }
        }

        if (source instanceof ServerPlayer player) {
            UUID playerUUID = player.getUUID();
            if (PlayerTrialData.isPlayerCursed(player)) {
                BlockPos altarPos = PlayerTrialData.getCurrentAltarPos(player);
                if (altarPos == null) {
                    return;
                }

                BlockEntity blockEntity = player.level().getBlockEntity(altarPos);
                if (!(blockEntity instanceof CursedAltarBlockEntity altar)) {
                    return;
                }

                Trial trial = altar.getPlayerTrial(playerUUID);
                if (trial != null) {
                    trial.onEntityKilled(player, entity);
                } else {
                    //System.out.println("Trial is null for player: " + player.getName().getString());
                }
            }
        }

        if (entity instanceof Player player) {
            List<Mob> nearbyMobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(64.0D));
            for (Mob mob : nearbyMobs) {
                if (mob.getTarget() == player) {
                    CurseOfObessionEffect.removeTarget(mob);
                }
            }

            BlockPos altarPos = PlayerTrialData.getCurrentAltarPos(player);
            if (altarPos == null) {
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(altarPos);
            if (!(blockEntity instanceof CursedAltarBlockEntity altar)) {
                return;
            }

            if (!altar.hasPlayerCompletedTrial(player)) {
                altar.removePlayerFromTrial(player);
            }

            PlayerTrialData.clearPlayerCurse(player);
            if (player instanceof ServerPlayer serverPlayer) {
                ModNetworking.sendToPlayer(
                        new SyncTrialDataS2C(
                                "None",
                                "",
                                0,
                                0,
                                0,
                                0,
                                "",
                                0,
                                0),
                        serverPlayer);
            }

            player.displayClientMessage(Component.literal("The Altars Feed on your soul...").withStyle(ChatFormatting.DARK_RED), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerSleep(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (player.hasEffect(ModEffects.CURSE_OF_SHADOWS.get())) {
            player.displayClientMessage(Component.literal("It's too dark to sleep...").withStyle(ChatFormatting.RED), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            MobEffectInstance prideCurse = player.getEffect(ModEffects.CURSE_OF_PRIDE.get());
            if (prideCurse != null) {
                int amplifier = prideCurse.getAmplifier();
                if (event.getDistance() > 1.5F) {
                    event.setDamageMultiplier(1.5F + (0.5F * amplifier));
                } else {
                    event.setDamageMultiplier(1.0F);
                }
                if (amplifier == 1) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
                } else if (amplifier > 1){
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, amplifier + 1));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            MobEffectInstance prideCurse = player.getEffect(ModEffects.CURSE_OF_PRIDE.get());
            if (prideCurse != null) {
                event.setAmount(0.0F);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player) {
            MobEffectInstance prideCurse = player.getEffect(ModEffects.CURSE_OF_PRIDE.get());
            if (prideCurse != null && prideCurse.getAmplifier() >= 1) {
                MobEffectInstance addedEffect = event.getEffectInstance();
                if (addedEffect.getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
                    event.setResult(Event.Result.DENY);
                    player.displayClientMessage(Component.literal("Help is for the weak!").withStyle(ChatFormatting.RED), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerEquip(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            MobEffectInstance prideCurse = player.getEffect(ModEffects.CURSE_OF_PRIDE.get());
            if (prideCurse != null && prideCurse.getAmplifier() >= 2) {
                EquipmentSlot slot = event.getSlot();
                ItemStack newStack = event.getTo();
                if (slot.getType() == EquipmentSlot.Type.ARMOR || slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
                    if (!newStack.isEmpty()) {
                        if (!player.getInventory().add(newStack)) {
                            player.drop(newStack, false);
                        }
                    }
                }

            }
        }
    }

    @SubscribeEvent
    public static void onPotionEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            MobEffectInstance newEffect = event.getEffectInstance();
            if (newEffect.getEffect().isBeneficial()) {
                double magicAmp = player.getAttributeValue(ModAttributes.MAGIC_AMP.get());
                //System.out.println("Player Magical Potency: " + magicAmp); //debug code
                if (magicAmp > 1.0) {
                    int originalDuration = newEffect.getDuration();
                    int increasedDuration = (int) (originalDuration * magicAmp);
                    //System.out.println("Original Potion Duration: " + originalDuration); //debug code
                    //System.out.println("Increased Potion Duration: " + increasedDuration); //debug code
                    newEffect.update(new MobEffectInstance(newEffect.getEffect(), increasedDuration, newEffect.getAmplifier(),
                            newEffect.isAmbient(), newEffect.isVisible(), newEffect.showIcon()));
                }
            }
        }
    }

    //@SubscribeEvent
    //public static void onAnvilUpdate(AnvilUpdateEvent event) {
    //    ItemStack leftStack = event.getLeft();
    //    ItemStack rightStack = event.getRight();
    //    if (rightStack.getItem() == Items.DIAMOND) {
    //        ItemStack resultStack = leftStack.copy();
    //        CompoundTag nbt = resultStack.getOrCreateTag();
    //        int currentSockets = nbt.getInt("SocketCount");
    //        boolean isSocketable = nbt.getBoolean("Socketable");
//
    //        if (!isSocketable || (isSocketable && currentSockets < 3)) {
    //            if (!isSocketable) {
    //                nbt.putBoolean("Socketable", true);
    //                currentSockets = 0;
    //            }
    //            if (currentSockets < 3) {
    //                nbt.putInt("SocketCount", currentSockets + 1);
    //                ListTag socketsList = nbt.getList("Sockets", CompoundTag.TAG_COMPOUND);
    //                CompoundTag newSocket = new CompoundTag();
    //                newSocket.putInt("SlotIndex", currentSockets);
    //                newSocket.putString("SocketType", "General");
    //                socketsList.add(newSocket);
    //                nbt.put("Sockets", socketsList);
    //            }
//
    //            resultStack.setTag(nbt);
    //            event.setOutput(resultStack);
    //            event.setCost(5);
    //            event.setMaterialCost(1);
    //        }
    //    }
    //}

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ItemEntity itemEntity)) {
            return;
        }

        Item tossedItem = itemEntity.getItem().getItem();
        BlockPos itemPos = itemEntity.blockPosition();
        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        if (!PlayerTrialData.isPlayerCursed(player)) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) player.level();

        for (TrialRecord trialRecord : PlayerTrialData.getActiveTrialsByType(player, PlayerTrialData.fetchTrial)) {
            BlockPos altarPos = trialRecord.getAltarPos();
            BlockPos lowerBound = altarPos.above(1);
            BlockPos upperBound = altarPos.above(3);
            boolean isWithinHeight = itemPos.getY() >= lowerBound.getY() && itemPos.getY() <= upperBound.getY();
            boolean isWithinRadius = altarPos.getCenter().closerThan(itemEntity.position(), 3.0);

            if (isWithinHeight && isWithinRadius) {
                BlockEntity blockEntity = serverLevel.getBlockEntity(altarPos);
                if (!(blockEntity instanceof CursedAltarBlockEntity altar)) {
                    continue;
                }

                Trial trial = altar.getPlayerTrial(player.getUUID());
                if (trial instanceof FetchTrial fetchTrial) {
                    Item requiredItem = fetchTrial.getRequiredItem();

                    if (tossedItem.equals(requiredItem)) {
                        fetchTrial.incrementFetchCount();
                        itemEntity.discard();
                        serverLevel.sendParticles(
                                ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                                altarPos.getX() + 0.5,
                                altarPos.getY() + 1.0,
                                altarPos.getZ() + 0.5,
                                100,
                                0.2,
                                2.0,
                                0.2,
                                0.01
                        );
                        serverLevel.playSound(
                                null,
                                altarPos.getX() + 0.5,
                                altarPos.getY() + 1.0,
                                altarPos.getZ() + 0.5,
                                SoundEvents.GHAST_SHOOT,
                                SoundSource.HOSTILE,
                                1.0f,
                                0.5f
                        );
                        trial.trackProgress(player);

                        if (fetchTrial.isTrialCompleted(player)) {
                            fetchTrial.concludeTrial(player);
                        }

                        //System.out.println("Player " + player.getName().getString() + " has thrown " + tossedItem.getDescriptionId() + " at altar " + altarPos + ". Collected: " + fetchTrial.getCollectedCount() + "/" + fetchTrial.getRequiredCount());
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level level = player.level();
        CompoundTag playerData = player.getPersistentData();
        UUID activeAmuletUUID = null;

        if (playerData.contains("ActiveAmuletUUID", 11)) {
            activeAmuletUUID = playerData.getUUID("ActiveAmuletUUID");
        }
        ItemStack activeAmulet  = ItemStack.EMPTY;

        if (!level.isClientSide && event.phase == TickEvent.Phase.END) {
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof GoldenAmuletItem) {
                    UUID amuletUUID = GoldenAmuletItem.getUUID(stack);
                    if (amuletUUID != null && amuletUUID.equals(activeAmuletUUID)) {
                        activeAmulet = stack;
                        break;
                    }
                }
            }

            if (!activeAmulet.isEmpty()) {
                GoldenAmuletItem amuletItem = (GoldenAmuletItem) activeAmulet.getItem();
                amuletItem.applyGemBonuses(player, activeAmulet);
            } else {
                PreciousGemItem.removeBonus(player);
                player.getPersistentData().remove("ActiveAmuletUUID");
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.getItem() instanceof GoldenAmuletItem amuletItem) {
                        UUID newUUID = GoldenAmuletItem.getOrCreateUUID(stack);
                        player.getPersistentData().putUUID("ActiveAmuletUUID", newUUID);
                        amuletItem.applyGemBonuses(player, stack);
                        break;
                    }
                }
            }
        }

        MobEffectInstance shadowCurse = player.getEffect(ModEffects.CURSE_OF_SHADOWS.get());
        if (shadowCurse != null && shadowCurse.getAmplifier() >= 1 && !level.isClientSide) {
            int amplifier = shadowCurse.getAmplifier();

            if (player.tickCount % (24000 / (amplifier + 1)) == 0) {
                if (level.random.nextFloat() < 0.25) {
                    BlockPos spawnPos = player.blockPosition().above(15);
                    Phantom phantom = EntityType.PHANTOM.create(level);
                    if (phantom != null) {
                        phantom.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                        level.addFreshEntity(phantom);
                    }
                }
            }
        }

        MobEffectInstance prideCurse = player.getEffect(ModEffects.CURSE_OF_PRIDE.get());
        if (prideCurse != null && !level.isClientSide) {
            if (player.isSprinting()) {
                player.setSprinting(false);
                if (prideCurse.getAmplifier() >= 1) {
                    player.hurt(player.level().damageSources().generic(), 1.0F);
                    player.displayClientMessage(Component.literal("Running is for the pathetic!").withStyle(ChatFormatting.RED), true);
                }
            }

            if (prideCurse.getAmplifier() >= 2) {
                ItemStack mainHandItem = player.getMainHandItem();
                if (!mainHandItem.isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    player.drop(mainHandItem, false);
                }
                ItemStack offHandItem = player.getOffhandItem();
                if (!offHandItem.isEmpty()) {
                    player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                    player.drop(offHandItem, false);
                }
            }
        }

        MobEffectInstance lustCurse = player.getEffect(ModEffects.CURSE_OF_OBESSSION.get());
        if (lustCurse != null) {
            int amplifier = lustCurse.getAmplifier();
            double aggroDistanceMultiplier = 2.0 + amplifier;
            List<Monster> nearbyMonsters = level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(aggroDistanceMultiplier * 16));
            for (Monster monster : nearbyMonsters) {
                if (monster.getTarget() == null) {
                    monster.setTarget(player);
                }
            }
        }

        if (event.phase == TickEvent.Phase.END && !player.level().isClientSide()) {
            if (PlayerTrialData.isPlayerCursed(player)) {
                BlockPos altarPos = PlayerTrialData.getCurrentAltarPos(player);
                if (altarPos == null) return;

                BlockEntity blockEntity = player.level().getBlockEntity(altarPos);
                if (!(blockEntity instanceof CursedAltarBlockEntity altar)) return;

                Trial trial = altar.getPlayerTrial(player.getUUID());
                if (trial != null) {
                    trial.onPlayerTick(player);
                }
            }
        }
    }
}
