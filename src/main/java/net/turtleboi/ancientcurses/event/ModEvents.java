package net.turtleboi.ancientcurses.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.ai.AnimalFollowPlayerGoal;
import net.turtleboi.ancientcurses.ai.FishFollowPlayerGoal;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.trials.PlayerTrialProvider;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.effect.effects.*;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.item.items.GoldenAmuletItem;
import net.turtleboi.ancientcurses.item.items.PreciousGemItem;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.trials.SyncTrialDataS2C;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.trials.*;
import net.turtleboi.ancientcurses.util.ItemValueMap;
import net.turtleboi.turtlecore.effect.CoreEffects;
import net.turtleboi.turtlecore.init.CoreAttributes;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.SendParticlesS2C;
import net.turtleboi.turtlecore.particle.CoreParticles;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID)
public class ModEvents {
    private static final Random random = new Random();
    private static int tickCounter = random.nextInt(11) + 10;

    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
            if (trialData.isPlayerCursed()) {
                Trial activeTrial = trialData.getActiveTrial();
                if (activeTrial != null) {
                    activeTrial.trackProgress(player);
                } else {
                    //System.out.println("Player " + player.getName().getString() + " is cursed, but no active trial instance is found.");
                    trialData.setPendingTrialUpdate(20);
                }
            }
        });
    }


    @SubscribeEvent
    public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!event.getObject().getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).isPresent()) {
                event.addCapability(new ResourceLocation(AncientCurses.MOD_ID, "player_trial_data"), new PlayerTrialProvider(player));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(oldStore ->
                    event.getEntity().getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(newStore ->
                            newStore.copyFrom(oldStore)));
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
            Trial playerTrial = trialData.getActiveTrial();
            if (playerTrial instanceof EliminationTrial) {
                trialData.clearPlayerCurse();

                if (player instanceof ServerPlayer serverPlayer) {
                    ModNetworking.sendToPlayer(
                            new SyncTrialDataS2C(
                                    "None",
                                    false,
                                    "",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    "",
                                    0,
                                    0),
                            serverPlayer);
                }

                for (MobEffectInstance effectInstance : player.getActiveEffects()) {
                    MobEffect effect = effectInstance.getEffect();
                    if (CurseRegistry.getCurses().contains(effect)) {
                        player.removeEffect(effect);
                    }
                }
            }

            CompoundTag compound = new CompoundTag();
            trialData.saveNBTData(compound);
            player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(updatedTrialData -> {
                updatedTrialData.loadNBTData(compound);
            });

            BlockPos altarPos = trialData.getCurrentAltarPos();
            ResourceKey<Level> altarDimension = trialData.getAltarDimension();
            if (altarPos != null && altarDimension != null) {
                MinecraftServer server = player.getServer();
                if (server != null) {
                    ServerLevel altarLevel = server.getLevel(altarDimension);
                    if (altarLevel != null) {
                        BlockEntity blockEntity = altarLevel.getBlockEntity(altarPos);
                        if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                            CompoundTag altarNBT = new CompoundTag();
                            altarEntity.saveAdditional(altarNBT);
                            altarEntity.load(altarNBT);
                            //System.out.println("Altar at " + altarPos + " in dimension "
                            //        + altarDimension.location() + " reloaded trial data for player "
                            //        + player.getName().getString());
                        } else {
                            //System.out.println("No altar found at " + altarPos + " in dimension "
                            //        + altarDimension.location() + " for player " + player.getName().getString());
                        }
                    } else {
                        //System.out.println("Could not get ServerLevel for dimension " + altarDimension.location());
                    }
                } else {
                    //System.out.println("Player server is null.");
                }
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        player.reviveCaps();
        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
            Trial playerTrial = trialData.getActiveTrial();
            if (playerTrial instanceof EliminationTrial) {
                trialData.clearPlayerCurse();

                if (player instanceof ServerPlayer serverPlayer) {
                    ModNetworking.sendToPlayer(
                            new SyncTrialDataS2C(
                                    "None",
                                    false,
                                    "",
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    "",
                                    0,
                                    0),
                            serverPlayer);
                }

                for (MobEffectInstance effectInstance : player.getActiveEffects()) {
                    MobEffect effect = effectInstance.getEffect();
                    if (CurseRegistry.getCurses().contains(effect)) {
                        player.removeEffect(effect);
                    }
                }
            }

            CompoundTag compound = new CompoundTag();
            trialData.saveNBTData(compound);
            player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(updatedTrialData -> {
                updatedTrialData.loadNBTData(compound);
            });
        });
        player.invalidateCaps();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (entity instanceof Mob mob && !level.isClientSide) {
            for (Player player : level.players()) {
                if (mob instanceof Monster monster) {
                    MobEffectInstance lustCurse = monster.getEffect(ModEffects.CURSE_OF_OBESSSION.get());
                    if (lustCurse != null) {

                            if (tickCounter <= 0) {
                                for (int i = 0; i < 3; i++) {
                                    CoreNetworking.sendToNear(new SendParticlesS2C(
                                            ParticleTypes.HEART,
                                            mob.getX(),
                                            mob.getEyeY() + 0.25,
                                            mob.getZ(),
                                            0.1,
                                            0.25,
                                            0.1
                                    ), monster);
                                }
                                tickCounter = random.nextInt(11) + 10;
                            } else {
                                tickCounter--;
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
    public static void onEntityAttack(LivingAttackEvent event) {
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
            AttributeInstance hitChanceAttribute = player.getAttribute(CoreAttributes.HIT_CHANCE.get());
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

        MobEffectInstance fortuneFavor = player.getEffect(ModEffects.FORTUNES_FAVOR.get());
        if (fortuneFavor != null) {
            double chance = 0.33;
            BlockState state = event.getState();
            BlockPos pos = event.getPos();
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }

            LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.BLOCK_STATE, state)
                    .withParameter(LootContextParams.TOOL, player.getMainHandItem())
                    .withLuck(EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE, player.getMainHandItem()));
            List<ItemStack> drops = state.getDrops(lootBuilder);

            boolean isFortuneAffected = drops.stream().anyMatch(stack -> !stack.is(state.getBlock().asItem()));

            if (!isFortuneAffected) {
                return;
            }

            int multiplier = 2;
            //System.out.println(Component.literal("Doubling loot!"));
            if (random.nextDouble() < chance) {
                multiplier = 3;
                //System.out.println(Component.literal("Tripling loot!"));
            }

            for (ItemStack stack : drops) {
                if (stack.isEmpty()) continue;
                ItemStack duplicatedStack = stack.copy();
                duplicatedStack.setCount(stack.getCount() * (multiplier - 1));
                ItemEntity itemEntity = new ItemEntity(serverLevel, pos.getX(), pos.getY(), pos.getZ(), duplicatedStack);
                serverLevel.addFreshEntity(itemEntity);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            if (mob.getPersistentData().getBoolean("noDrops")) {
                event.setCanceled(true);
            }
        }

        if (event.getSource().getEntity() instanceof Player player) {
            MobEffectInstance fortuneFavor = player.getEffect(ModEffects.FORTUNES_FAVOR.get());
            if (fortuneFavor != null) {
                double chance = 0.33;
                for (ItemEntity itemEntity : event.getDrops()) {
                    //System.out.println(Component.literal("Doubling loot!"));
                    ItemStack original = itemEntity.getItem();
                    ItemStack multiplied = original.copy();
                    int multiplier = 2;
                    if (random.nextDouble() < chance) {
                        multiplier = 3;
                        //System.out.println(Component.literal("Tripling loot!"));
                    }
                    multiplied.setCount(original.getCount() * multiplier);
                    itemEntity.setItem(multiplied);
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

    private static ItemStack getActiveAmulet(Player player) {
        AtomicReference<ItemStack> activeAmulet = new AtomicReference<>(ItemStack.EMPTY);

        if (ModList.get().isLoaded("curios")) {
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                curiosInventory.getStacksHandler("necklace").ifPresent(slotInventory -> {
                    ItemStack necklaceItem = slotInventory.getStacks().getStackInSlot(0);
                    if (!necklaceItem.isEmpty() && necklaceItem.getItem() instanceof GoldenAmuletItem) {
                        activeAmulet.set(necklaceItem);
                    }
                });
            });
        } else {
            CompoundTag playerData = player.getPersistentData();
            UUID activeAmuletUUID = null;

            if (playerData.contains("ActiveAmuletUUID", 11)) {
                activeAmuletUUID = playerData.getUUID("ActiveAmuletUUID");
            }

            if (activeAmuletUUID != null) {
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.getItem() instanceof GoldenAmuletItem) {
                        UUID amuletUUID = GoldenAmuletItem.getUUID(stack);
                        if (amuletUUID != null && amuletUUID.equals(activeAmuletUUID)) {
                            activeAmulet.set(stack);
                            break;
                        }
                    }
                }
            }
        }

        return activeAmulet.get();
    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityHurt(LivingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        Entity target = event.getEntity();

        //System.out.println(Component.literal(target.getName() + "hit!")); //debug code
        if (target instanceof Player player) {
            //System.out.println(Component.literal(player.getName() + " got hit!")); //debug code

            AttributeInstance dodgeChanceAttribute = player.getAttribute(CoreAttributes.DODGE_CHANCE.get());
            if (dodgeChanceAttribute != null) {
                double hitChance = dodgeChanceAttribute.getValue();
                double randomValue = player.getRandom().nextDouble();
                if (randomValue < hitChance) {
                    event.setCanceled(true);
                }

                Level level = player.level();
                if (event.isCanceled()) {
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

            ItemStack amulet = getActiveAmulet(player);

            if (!amulet.isEmpty()) {
                CompoundTag amuletTag = amulet.getTag();
                if (amuletTag != null) {
                    if (amuletTag.contains("MainGem")) {
                        ItemStack mainGemStack = ItemStack.of(amuletTag.getCompound("MainGem"));
                        if (mainGemStack.getItem() == ModItems.PERFECT_DIAMOND.get()) {
                            boolean isBlocking = player.isBlocking();
                            float chance = player.getRandom().nextFloat();
                            if (isBlocking) {
                                if (chance <= 0.5f) {
                                event.setAmount(0.0f);
                                    player.level().playSound(
                                            null,
                                            player.blockPosition(),
                                            SoundEvents.ANVIL_PLACE,
                                            SoundSource.BLOCKS,
                                            0.5F,
                                            0.9F
                                    );
                                //System.out.println(Component.literal("Damage shield blocked!")); //debug code
                                }
                            } else {
                                if (chance <= 0.2f) {
                                event.setAmount(0.0f);
                                    player.level().playSound(
                                            null,
                                            player.blockPosition(),
                                            SoundEvents.ANVIL_PLACE,
                                            SoundSource.BLOCKS,
                                            0.5F,
                                            0.9F
                                    );
                                //System.out.println(Component.literal("Damage negated!")); //debug code
                                }
                            }
                        }
                    }
                }
            }

            if (attacker instanceof Mob mob) {
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

                                for (int i = 0; i < 10; i++) {
                                    CoreNetworking.sendToNear(new SendParticlesS2C(
                                            CoreParticles.HEAL_PARTICLE.get(),
                                            mob.getX(),
                                            mob.getEyeY() + 0.5,
                                            mob.getZ(),
                                            0.1,
                                            0.25,
                                            0.1
                                    ), mob);
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
            }
        } else if (attacker instanceof Player player){
            ItemStack amulet = getActiveAmulet(player);

            if (!amulet.isEmpty()) {
                CompoundTag amuletTag = amulet.getTag();
                if (amuletTag != null) {
                    if (amuletTag.contains("MainGem")) {
                        ItemStack mainGemStack = ItemStack.of(amuletTag.getCompound("MainGem"));
                        if (mainGemStack.getItem() == ModItems.PERFECT_RUBY.get()){
                            //System.out.println(Component.literal("You have a ruby in your amulet!")); //debug code
                            ItemStack mainHandItem = player.getMainHandItem();
                            double normalDamage = player.getAttributes().getValue(Attributes.ATTACK_DAMAGE) +
                                    EnchantmentHelper.getDamageBonus(mainHandItem, event.getEntity().getMobType());
                            if (event.getAmount() > normalDamage) {
                                player.addEffect(new MobEffectInstance(ModEffects.CRITICAL_FURY.get(), 200, 0));
                                //System.out.println(Component.literal("Critical hit!")); //debug code
                            } else {
                                //System.out.println(Component.literal("Not a critical hit!")); //debug code

                            }
                            // System.out.println(Component.literal("Expected damage: " + normalDamage)); //debug code
                            // System.out.println(Component.literal("Actual damage: " + event.getAmount())); //debug code
                            // System.out.println(Component.literal("Player damage attribute: " + player.getAttributes().getValue(Attributes.ATTACK_DAMAGE))); //debug code
                            // System.out.println(Component.literal("Main hand damage: " + mainHandItem.getDamageValue())); //debug code
                            // System.out.println(Component.literal("Enchantment damage: " + EnchantmentHelper.getDamageBonus(mainHandItem, event.getEntity().getMobType()))); //debug code
                        } else if (mainGemStack.getItem() == ModItems.PERFECT_TOPAZ.get()){
                            if (!player.hasEffect(ModEffects.FRENZIED_BLOWS.get())){
                                player.addEffect(new MobEffectInstance(ModEffects.FRENZIED_BLOWS.get(), 100, 0));
                            } else {
                                int effectAmplifier = Objects.requireNonNull(player.getEffect(ModEffects.FRENZIED_BLOWS.get())).getAmplifier();
                                if (effectAmplifier < 2) {
                                    player.addEffect(new MobEffectInstance(ModEffects.FRENZIED_BLOWS.get(), 100, effectAmplifier + 1));
                                } else if (effectAmplifier == 2){
                                    player.addEffect(new MobEffectInstance(ModEffects.FRENZIED_BLOWS.get(), 100, 2));
                                }
                            }
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

    @SubscribeEvent
    public static void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (entity instanceof Mob mob && level instanceof ServerLevel) {
            for (Player player : level.players()) {
                MobEffectInstance wrathCurse = player.getEffect(ModEffects.CURSE_OF_WRATH.get());
                if (wrathCurse != null && player.distanceTo(entity) <= 25) {

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


                    for (int i = 0; i < 4; i++) {
                        CoreNetworking.sendToNear(new SendParticlesS2C(
                                ParticleTypes.EXPLOSION,
                                entity.getX(),
                                entity.getY() + 1,
                                entity.getZ(),
                                3,
                                3,
                                3
                        ), player);
                    }

                if (player.distanceTo(entity) <= explosionRadius) {
                    player.hurt(new DamageSource(level.damageSources().generic().typeHolder()), player.distanceTo(entity) * 2);
                }
            }
        }

        if (source instanceof ServerPlayer player) {
            player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
                if (trialData.isPlayerCursed()) {
                    ResourceKey<Level> altarDimension = trialData.getAltarDimension();
                    BlockPos altarPos = trialData.getCurrentAltarPos();
                    //System.out.println("[Tick] Altar Dimension: " + (altarDimension != null ? altarDimension.location() : "null") +
                    //        ", Altar Pos: " + (altarPos != null ? altarPos.toShortString() : "null"));
                    if (altarPos != null) {
                        MinecraftServer server = player.getServer();
                        //System.out.println("[Tick] Player server is " + (server != null ? "available" : "null"));
                        if (altarDimension != null && server != null) {
                            ServerLevel altarLevel = server.getLevel(altarDimension);
                            //System.out.println("[Tick] Retrieved altar level for dimension " + altarDimension.location() + ": " + (altarLevel != null ? "available" : "null"));
                            if (altarLevel != null) {
                                BlockEntity blockEntity = altarLevel.getBlockEntity(altarPos);
                                if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                                    Trial activeTrial = altarEntity.getPlayerTrial(player.getUUID());
                                    if (activeTrial != null) {
                                        activeTrial.onEntityKilled(player , entity);
                                        //System.out.println("[Tick] Tracked trial progress for player " + player.getName().getString());
                                    } else {
                                        //System.out.println("[Tick] No active trial instance found for player " + player.getName().getString());
                                    }
                                }
                            } else {
                                //System.out.println("[Tick] Altar level not loaded. Retrying...");
                                trialData.setPendingTrialUpdate(20);
                            }
                        } else if (altarDimension == null) {
                            //System.out.println("[Tick] Altar dimension is null. Retrying...");
                            trialData.setPendingTrialUpdate(20);
                        }
                    }
                }
            });
        }

        if (entity instanceof Player player) {
            List<Mob> nearbyMobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(64.0D));
            for (Mob mob : nearbyMobs) {
                if (mob.getTarget() == player) {
                    CurseOfObessionEffect.removeTarget(mob);
                }
            }

            player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
                MinecraftServer server = player.getServer();
                if (server == null) return;

                ServerLevel overworld = server.getLevel(Level.OVERWORLD);
                if (overworld == null) return;

                BlockPos altarPos = trialData.getCurrentAltarPos();
                if (altarPos == null) {
                    return;
                }

                BlockEntity blockEntity = overworld.getBlockEntity(altarPos);
                if (!(blockEntity instanceof CursedAltarBlockEntity altar)) {
                    return;
                }

                if (!altar.hasPlayerCompletedTrial(player)) {
                    altar.removePlayerFromTrial(player);
                }

                trialData.clearPlayerCurse();
                if (player instanceof ServerPlayer serverPlayer) {
                    ModNetworking.sendToPlayer(
                            new SyncTrialDataS2C(
                                    "None",
                                    false,
                                    "",
                                    0,
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
            });
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
                double magicAmp = player.getAttributeValue(CoreAttributes.MAGIC_AMP.get());
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

    @SubscribeEvent
    public static void onPotionEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            MobEffectInstance curseEffect = event.getEffectInstance();
            if (curseEffect != null) {
                MobEffect effect = curseEffect.getEffect();
                if (CurseRegistry.getCurses().contains(effect)) {
                    player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
                        if (trialData.isPlayerCursed()) {
                            event.setCanceled(true);
                            //System.out.println("Prevented removal of curse from player");
                        } else {
                            //System.out.println("Allowed removal of curse from player");
                        }
                    });
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
        int itemCount = itemEntity.getItem().getCount();
        BlockPos itemPos = itemEntity.blockPosition();
        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
            if (!trialData.isPlayerCursed()) {
                return;
            }

            ServerLevel serverLevel = (ServerLevel) player.level();

            for (TrialRecord trialRecord : trialData.getActiveTrialsByType(Trial.fetchTrial)) {
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
                            fetchTrial.incrementFetchCount(player, itemCount);
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
        });
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getContainer() instanceof EnchantmentMenu enchantMenu) {
            handleEnchantmentTableOpen(player, enchantMenu);
        }
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getContainer() instanceof EnchantmentMenu enchantMenu) {
            handleEnchantmentTableClose(player, enchantMenu);
        }
    }

    private static final String notYetEnchanted = "NotYetEnchanted";

    private static void handleEnchantmentTableOpen(ServerPlayer player, EnchantmentMenu menu) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEnchanted()) {
                continue;
            }

            if (isItemEnchantable(stack)) {
                stack.getOrCreateTag().putBoolean(notYetEnchanted, true);
            }
        }
    }

    private static void handleEnchantmentTableClose(ServerPlayer player, EnchantmentMenu menu) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.getBoolean(notYetEnchanted)) {
                if (stack.isEnchanted()) {
                    ItemStack amulet = getActiveAmulet(player);
                    if (!amulet.isEmpty()) {
                        CompoundTag amuletTag = amulet.getTag();
                        if (amuletTag != null && amuletTag.contains("MainGem")) {
                            ItemStack mainGemStack = ItemStack.of(amuletTag.getCompound("MainGem"));
                            if (mainGemStack.getItem() == ModItems.PERFECT_SAPPHIRE.get()) {

                                ListTag enchantList = stack.getEnchantmentTags();
                                Random random = new Random();

                                for (Tag baseTag : enchantList) {
                                    if (baseTag instanceof CompoundTag enchantCompound) {
                                        int lvl = enchantCompound.getInt("lvl");
                                        String id = enchantCompound.getString("id");
                                        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
                                        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(resourceLocation);
                                        if (enchantment == null) {
                                            continue;
                                        }
                                        Component enchantDisplayName = enchantment.getFullname(lvl);

                                        double roll = random.nextDouble();
                                        if (roll > 0.8) {
                                            enchantCompound.putInt("lvl", lvl + 2);
                                            player.sendSystemMessage(
                                                    Component.literal("The amulet empowers ")
                                                            .append(enchantDisplayName)
                                                            .append(" by +2")
                                            );
                                        } else if (roll > 0.5) {
                                            enchantCompound.putInt("lvl", lvl + 1);
                                            player.sendSystemMessage(
                                                    Component.literal("The amulet empowers ")
                                                            .append(enchantDisplayName)
                                                            .append(" by +1")
                                            );
                                        } else {

                                        }
                                    }
                                }

                                stack.getOrCreateTag().put("Enchantments", enchantList);
                            }
                        }
                    }
                }

                tag.remove(notYetEnchanted);
                if (tag.isEmpty()) {
                    stack.setTag(null);
                }
            }
        }
    }

    private static boolean isItemEnchantable(ItemStack stack) {
        return stack.getItem().isEnchantable(stack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level level = player.level();
        CompoundTag playerData = player.getPersistentData();
        UUID activeAmuletUUID;

        if (playerData.contains("ActiveAmuletUUID", 11)) {
            activeAmuletUUID = playerData.getUUID("ActiveAmuletUUID");
        } else {
            activeAmuletUUID = null;
        }
        AtomicReference<ItemStack> activeAmulet = new AtomicReference<>(ItemStack.EMPTY);

        if (!level.isClientSide && event.phase == TickEvent.Phase.END) {
            if (ModList.get().isLoaded("curios")) {
                CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                    curiosInventory.getStacksHandler("necklace").ifPresent(slotInventory -> {
                        ItemStack necklaceItem = slotInventory.getStacks().getStackInSlot(0);
                        if (!necklaceItem.isEmpty() && necklaceItem.getItem() instanceof GoldenAmuletItem goldenAmuletItem) {
                            goldenAmuletItem.applyGemBonuses(player, necklaceItem);
                        } else if (necklaceItem.isEmpty() || !(necklaceItem.getItem() instanceof GoldenAmuletItem)) {
                            PreciousGemItem.removeBonus(player);
                        }
                    });
                });
            } else {
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.getItem() instanceof GoldenAmuletItem) {
                        UUID amuletUUID = GoldenAmuletItem.getUUID(stack);
                        if (amuletUUID != null && amuletUUID.equals(activeAmuletUUID)) {
                            activeAmulet.set(stack);
                            break;
                        }
                    }
                }

                if (!activeAmulet.get().isEmpty()) {
                    GoldenAmuletItem amuletItem = (GoldenAmuletItem) activeAmulet.get().getItem();
                    amuletItem.applyGemBonuses(player, activeAmulet.get());
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
                player.addEffect(new MobEffectInstance(
                        CoreEffects.STUNNED.get(),
                        20
                ));
                if (prideCurse.getAmplifier() >= 1) {
                    player.hurt(player.level().damageSources().generic(), 1.0F);
                    player.displayClientMessage(Component.literal("Running is for the pathetic!").withStyle(ChatFormatting.RED), true);
                }
                player.hurtMarked = true;
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
            player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
                if (trialData.isPlayerCursed()) {
                    //System.out.println("[Tick] Player " + player.getName().getString() + " is cursed.");
                    int pending = trialData.getPendingTrialUpdate();
                    //System.out.println("[Tick] Pending trial update ticks: " + pending);
                    if (pending > 0) {
                        trialData.setPendingTrialUpdate(pending - 1);
                        //System.out.println("[Tick] Decremented pending trial update ticks to: " + trialData.getPendingTrialUpdate());
                        if (trialData.getPendingTrialUpdate() <= 0) {
                            ResourceKey<Level> altarDimension = trialData.getAltarDimension();
                            BlockPos altarPos = trialData.getCurrentAltarPos();
                            //System.out.println("[Tick] Altar Dimension: " + (altarDimension != null ? altarDimension.location() : "null") +
                            //        ", Altar Pos: " + (altarPos != null ? altarPos.toShortString() : "null"));
                            if (altarPos != null) {
                                MinecraftServer server = player.getServer();
                                //System.out.println("[Tick] Player server is " + (server != null ? "available" : "null"));
                                if (altarDimension != null && server != null) {
                                    ServerLevel altarLevel = server.getLevel(altarDimension);
                                    //System.out.println("[Tick] Retrieved altar level for dimension " + altarDimension.location() + ": " + (altarLevel != null ? "available" : "null"));
                                    if (altarLevel != null) {
                                        BlockEntity blockEntity = altarLevel.getBlockEntity(altarPos);
                                        if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                                            Trial activeTrial = altarEntity.getPlayerTrial(player.getUUID());
                                            if (activeTrial != null) {
                                                activeTrial.trackProgress(player);
                                                //System.out.println("[Tick] Tracked trial progress for player " + player.getName().getString());
                                            } else {
                                                //System.out.println("[Tick] No active trial instance found for player " + player.getName().getString());
                                            }
                                        }
                                    } else {
                                        //System.out.println("[Tick] Altar level not loaded. Retrying...");
                                        trialData.setPendingTrialUpdate(20);
                                    }
                                } else if (altarDimension == null) {
                                    //System.out.println("[Tick] Altar dimension is null. Retrying...");
                                    trialData.setPendingTrialUpdate(20);
                                }
                            }
                        }
                    }

                    BlockPos currentAltarPos = trialData.getCurrentAltarPos();
                    if (currentAltarPos == null) return;
                    BlockEntity blockEntity = player.level().getBlockEntity(currentAltarPos);
                    if (blockEntity instanceof CursedAltarBlockEntity altar) {
                        Trial trial = altar.getPlayerTrial(player.getUUID());
                        if (trial != null) {
                            trial.onPlayerTick(player);
                        }
                    }
                }
            });
        }
    }
}
