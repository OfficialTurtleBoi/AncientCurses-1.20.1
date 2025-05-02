package net.turtleboi.ancientcurses.block.blockentity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.block.entity.ModBlockEntities;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.rites.CarnageRite;
import net.turtleboi.ancientcurses.rites.Rite;
import net.turtleboi.ancientcurses.sound.ModSounds;
import net.turtleboi.ancientcurses.rites.FamineRite;
import net.turtleboi.ancientcurses.util.ModTags;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CursedAltarBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = BaseEntityBlock.box(0, 0, 0, 16, 12, 16);
    public static final List<BlockPos> SOUL_TORCH_OFFSETS = BlockPos.betweenClosedStream(-2, -2, -2, 2, 2, 2)
            .filter((p_207914_) -> Math.abs(p_207914_.getX()) == 2 || Math.abs(p_207914_.getZ()) == 2)
            .map(BlockPos::immutable)
            .toList();

    public CursedAltarBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (!level.isClientSide()) {
            return;
        }

        for (BlockPos offset : SOUL_TORCH_OFFSETS) {
            BlockPos torchPos = pos.offset(offset);
            BlockState torchState = level.getBlockState(torchPos);
            if (random.nextInt(16) == 0 && isSoulLight(level, torchPos)) {
                double particleX, particleY, particleZ;

                if (torchState.is(Blocks.SOUL_WALL_TORCH)) {
                    Direction direction = torchState.getValue(WallTorchBlock.FACING);
                    Direction oppositeDirection = direction.getOpposite();
                    particleX = torchPos.getX() + 0.5D + 0.27D * oppositeDirection.getStepX();
                    particleY = torchPos.getY() + 0.7D;
                    particleZ = torchPos.getZ() + 0.5D + 0.27D * oppositeDirection.getStepZ();
                } else {
                    particleX = torchPos.getX() + 0.5D;
                    particleY = torchPos.getY() + 0.7D;
                    particleZ = torchPos.getZ() + 0.5D;
                }

                double altarX = pos.getX() + 0.5D;
                double altarY = pos.getY() + 1.0D;
                double altarZ = pos.getZ() + 0.5D;

                double velocityX = (altarX - particleX) * 0.05;
                double velocityY = (altarY - particleY) * 0.05;
                double velocityZ = (altarZ - particleZ) * 0.05;

                level.addParticle(ParticleTypes.SCULK_SOUL, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
            }
        }
    }

    private boolean isSoulLight(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.SCONCED_SOUL_TORCH.get()) ||
                level.getBlockState(pos).is(ModBlocks.SCONCED_WALL_SOUL_TORCH.get()) ||
                level.getBlockState(pos).is(Blocks.SOUL_LANTERN);
    }

    private void convertSoulLights(CursedAltarBlockEntity altarEntity, Level level) {
        BlockPos altarPos = altarEntity.getBlockPos();

        for (BlockPos offset : SOUL_TORCH_OFFSETS) {
            BlockPos lightPos = altarPos.offset(offset);
            if (isSoulLight(level, lightPos)) {
                BlockState lightState = level.getBlockState(lightPos);
                BlockState finalLightState;

                if (lightState.getBlock() instanceof WallTorchBlock) {
                    Direction direction = lightState.getValue(WallTorchBlock.FACING);
                    finalLightState = ModBlocks.SCONCED_WALL_CURSED_TORCH.get().defaultBlockState()
                            .setValue(WallTorchBlock.FACING, direction);
                    level.setBlock(lightPos, finalLightState, 3);
                } else if (lightState.getBlock() instanceof TorchBlock) {
                    finalLightState = ModBlocks.SCONCED_CURSED_TORCH.get().defaultBlockState();
                    level.setBlock(lightPos, finalLightState, 3);
                } else if (lightState.getBlock() instanceof LanternBlock) {
                    finalLightState = ModBlocks.CURSED_LANTERN.get().defaultBlockState();
                    level.setBlock(lightPos, finalLightState, 3);
                }

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                            lightPos.getX() + 0.5,
                            lightPos.getY() + 0.7,
                            lightPos.getZ() + 0.5,
                            20,
                            0.05,
                            0.05,
                            0.05,
                            0.01
                    );

                    serverLevel.playSound(
                            null,
                            lightPos.getX() + 0.5,
                            lightPos.getY() + 0.7,
                            lightPos.getZ() + 0.5,
                            SoundEvents.GHAST_SHOOT,
                            SoundSource.HOSTILE,
                            0.25f,
                            0.5f
                    );
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                for (int i = 0; i < altarEntity.itemStackHandler.getSlots(); i++) {
                    ItemStack stack = altarEntity.itemStackHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                        altarEntity.itemStackHandler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }

                altarEntity.releaseDimensionActive();

                //for (Player player : level.players()) {
                //    player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
                //        BlockPos playerAltarPos = riteData.getCurrentAltarPos();
                //        if (playerAltarPos != null && playerAltarPos.equals(pos)) {
                //            riteData.resetAltarAtPos(playerAltarPos);
                //        }
                //    });
                //}
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof CursedAltarBlockEntity altarEntity) {
                if (altarEntity.isAnimating()) {
                    return InteractionResult.FAIL;
                }

                InteractionResult playerRiteDataResult = player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                        .map(riteData -> {
                            if (riteData.isPlayerCursed()) {
                                Rite playerRite = riteData.getActiveRite();
                                if (playerRite instanceof CarnageRite carnageRite){
                                    if (carnageRite.completedFirstDegree || carnageRite.completedSecondDegree) {
                                        carnageRite.concludeRite(player);
                                        return InteractionResult.SUCCESS;
                                    } else {
                                        return InteractionResult.FAIL;
                                    }
                                } else if (playerRite instanceof FamineRite famineRite){
                                    if (famineRite.completedFirstDegree || famineRite.completedSecondDegree) {
                                        famineRite.concludeRite(player);
                                        return InteractionResult.SUCCESS;
                                    } else {
                                        return InteractionResult.FAIL;
                                    }
                                } else if (playerRite == null){
                                    return InteractionResult.FAIL;
                                }
                                //player.sendSystemMessage(Component.literal("You are already cursed! Complete your rite before interacting again.").withStyle(ChatFormatting.RED));
                            }

                            if (riteData.getCurrentAltarPos() != null &&
                                    !Objects.equals(riteData.getCurrentAltarPos(), altarEntity.getBlockPos())) {
                                // player.sendSystemMessage(Component.literal("You have an altar! Complete your rite before interacting again.").withStyle(ChatFormatting.RED));
                                return InteractionResult.FAIL;
                            }

                            return InteractionResult.PASS;
                        }).orElse(InteractionResult.PASS);

                if (playerRiteDataResult == InteractionResult.FAIL) {
                    return InteractionResult.FAIL;
                }

                if (altarEntity.hasPlayerCompletedRite(player)){
                    rewardPlayer(player, altarEntity, level, pos);
                    //player.sendSystemMessage(Component.literal("You've completed this rite!").withStyle(ChatFormatting.GREEN));
                    if (player.isShiftKeyDown() && altarEntity.canPlayerUse(player)) {
                        //player.sendSystemMessage(Component.literal("Getting gem!"));
                        for (int i = 2; i >= 0; i--) {
                            ItemStack gem = altarEntity.getGemInSlot(i);
                            if (!gem.isEmpty()) {
                                if (player.getInventory().add(gem)) {
                                    altarEntity.setGemInSlot(i, ItemStack.EMPTY);
                                    altarEntity.setChanged();
                                } else {
                                    ejectItemsTowardPlayer(level, pos, player, Collections.singletonList(gem));
                                }
                                player.level().playSound(
                                        null,
                                        player.blockPosition(),
                                        ModSounds.GEM_PLACE.get(),
                                        SoundSource.BLOCKS,
                                        1.0F + 0.07f * i,
                                        0.9F + 0.1f * i + (float) player.getRandom().
                                                nextIntBetweenInclusive(0, 3) / 100
                                );
                                return InteractionResult.SUCCESS;
                            }
                        }
                    } else if (altarEntity.canPlayerUse(player)){
                        //player.sendSystemMessage(Component.literal("Right click!"));
                        ItemStack heldItem = player.getItemInHand(hand);
                        if (isPreciousGem(heldItem)) {
                            //player.sendSystemMessage(Component.literal("Adding gem!"));
                            for (int i = 0; i < 3; i++) {
                                if (altarEntity.getGemInSlot(i).isEmpty()) {
                                    player.level().playSound(
                                            null,
                                            player.blockPosition(),
                                            ModSounds.GEM_PLACE.get(),
                                            SoundSource.BLOCKS,
                                            1.0F + 0.07f * i,
                                            0.9F + 0.1f * i + (float) player.getRandom().
                                                    nextIntBetweenInclusive(0, 3) / 100
                                    );
                                    ItemStack gemToPlace = player.getAbilities().instabuild ? heldItem.copyWithCount(1) : heldItem.split(1);
                                    altarEntity.setGemInSlot(i, gemToPlace);
                                    altarEntity.setChanged();
                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }

                        ItemStack gemSlot0 = altarEntity.getGemInSlot(0);
                        ItemStack gemSlot1 = altarEntity.getGemInSlot(1);
                        ItemStack gemSlot2 = altarEntity.getGemInSlot(2);

                        if (!gemSlot0.isEmpty() && gemSlot0.is(gemSlot1.getItem()) && gemSlot0.is(gemSlot2.getItem()) && !gemSlot0.is(ModTags.Items.MAJOR_GEMS)) {
                            //player.sendSystemMessage(Component.literal("3 Matching gems! Upgrading..."));
                            altarEntity.startAnimation();
                            level.sendBlockUpdated(pos, state, state, 3);
                            return InteractionResult.SUCCESS;
                        }

                    } else {
                        //.sendSystemMessage(Component.literal("Too bad so sad!"));
                    }
                } else {
                    if (!altarEntity.hasPlayerCompletedRite(player) && altarEntity.canPlayerUse(player)) {
                        convertSoulLights(altarEntity, level);
                        CoreNetworking.sendToNear(new CameraShakeS2C(0.125F, 1000), player);
                        startRite(player, altarEntity);
                        return InteractionResult.SUCCESS;
                    } else if (!altarEntity.hasPlayerCompletedRite(player) && !altarEntity.canPlayerUse(player)){
                        //player.sendSystemMessage(Component.literal("The altar is recharging.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    public static int SoulTorchAround(BlockGetter world, BlockPos centerPos ) {
        int soultorchamount = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos offsetPos = centerPos.offset(x, y, z);
                    if (world.getBlockState(offsetPos).is(ModBlocks.SCONCED_SOUL_TORCH.get())) {
                        soultorchamount+=1;
                    }
                }
            }
        }

        return soultorchamount;
    }

    public void startRite(Player player, CursedAltarBlockEntity altarEntity) {
        if (altarEntity.hasPlayerCompletedRite(player)) {
            //player.sendSystemMessage(Component.literal("You have already completed the rite for this altar.").withStyle(ChatFormatting.GREEN));
            return;
        }

        MobEffect randomCurse = CursedAltarBlockEntity.getRandomCurse();

        BlockPos playerPos = player.blockPosition();
        Level level = player.level();
        int randomAmplifier = CursedAltarBlockEntity.getRandomAmplifier(player);

        level.playSound(
                null,
                playerPos,
                SoundEvents.ELDER_GUARDIAN_CURSE,
                SoundSource.AMBIENT,
                1.0F,
                0.25F
        );

        level.playSound(
                null,
                playerPos,
                SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.get(),
                SoundSource.AMBIENT,
                1.0F,
                0.5F
        );

        altarEntity.cursePlayer(player, randomCurse, randomAmplifier);
        //player.displayClientMessage(Component.literal(
        //        "You have been cursed with " + randomCurse.getDisplayName().getString() + "!").withStyle(ChatFormatting.DARK_PURPLE), true); //debug code
    }

    private boolean isPreciousGem(ItemStack itemStack){
        return itemStack.is(ModTags.Items.PRECIOUS_GEMS);
    }

    private void rewardPlayer(Player player, CursedAltarBlockEntity altarEntity, Level level, BlockPos pos) {
        int amplifier = player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                .map(PlayerRiteDataCapability::getCurseAmplifier)
                .orElse(0);

        Rite rite = player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                .resolve()
                .map(PlayerRiteDataCapability::getActiveRite)
                .orElse(null);

        if (rite == null) {
            return;
        }

        if (altarEntity.hasCollectedReward(player)) {
            //player.sendSystemMessage(Component.literal("You have already received your reward for this rite.").withStyle(ChatFormatting.RED));
            return;
        }

        //System.out.println(Component.literal("Giving player loot of amplifier: " + amplifier - 1));

        if (player instanceof ServerPlayer) {
            CoreNetworking.sendToNear((new CameraShakeS2C(0.125F, 1000)), player);
            rewardPlayerWithLootTable(level,player, amplifier, rite, pos);
            ModNetworking.sendToPlayer(
                    new SyncRiteDataS2C(
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
                    (ServerPlayer) player);
            player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
                riteData.clearCurseAmplifier();
                riteData.resetRiteProgress();
                riteData.setActiveRite(null);
            });
            altarEntity.markRewardCollected(player);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    altarEntity.getBlockPos().getX() + 0.5,
                    altarEntity.getBlockPos().getY() + 1,
                    altarEntity.getBlockPos().getZ() + 0.5,
                    SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(),
                    SoundSource.AMBIENT,
                    1.0F,
                    0.5F
            );

            serverLevel.playSound(
                    null,
                    altarEntity.getBlockPos().getX() + 0.5,
                    altarEntity.getBlockPos().getY() + 1,
                    altarEntity.getBlockPos().getZ() + 0.5,
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundSource.AMBIENT,
                    1.0f,
                    1.0f
            );
        }
    }

    private void rewardPlayerWithLootTable(Level level, Player player, int pAmplifier, Rite rite, BlockPos pos) {
        int adjustedAmplifier = pAmplifier - 1;
        //System.out.println("pAmplifier: " + adjustedAmplifier);
        ResourceLocation[] lootTableLocations = {
                new ResourceLocation(AncientCurses.MOD_ID, "amplifier_0"),
                new ResourceLocation(AncientCurses.MOD_ID, "amplifier_1"),
                new ResourceLocation(AncientCurses.MOD_ID, "amplifier_2")};
        int lootIndex = Math.max(0, Math.min(adjustedAmplifier, lootTableLocations.length - 1));
        //System.out.println("Calculated lootIndex: " + lootIndex + " using lootTableLocations: " + lootTableLocations[lootIndex]);


        ServerLevel serverLevel = (ServerLevel) player.level();
        LootTable lootTable = serverLevel.getServer().getLootData().getElement(LootDataType.TABLE, lootTableLocations[lootIndex]);

        if (lootTable == null) {
            //player.sendSystemMessage(Component.literal("No loot table found for this rite reward.").withStyle(ChatFormatting.RED));
            return;
        }

        Random random = new Random();

        int minRolls = 1, maxRolls = 2;
        if (rite instanceof CarnageRite carnageRite) {
            if (carnageRite.completedThirdDegree) {
                minRolls = 3;
                maxRolls = 5;
            } else if (carnageRite.completedSecondDegree) {
                minRolls = 2;
                maxRolls = 3;
            }
        }

        if (rite instanceof FamineRite famineRite) {
            if (famineRite.completedThirdDegree) {
                minRolls = 3;
                maxRolls = 5;
            } else if (famineRite.completedSecondDegree) {
                minRolls = 2;
                maxRolls = 3;
            }
        }

        //System.out.println("Rolls parameters: minRolls = " + minRolls + ", maxRolls = " + maxRolls);

        int rollCount = random.nextInt(maxRolls - minRolls + 1) + minRolls;
        //System.out.println("Calculated rollCount: " + rollCount);

        List<ItemStack> totalGeneratedLoot = new ArrayList<>();

        for (int i = 0; i < rollCount; i++) {
            LootParams.Builder lootParamsBuilder = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position());

            LootParams lootParams = lootParamsBuilder.create(LootContextParamSets.CHEST);
            ObjectArrayList<ItemStack> generatedLoot = lootTable.getRandomItems(lootParams);

            totalGeneratedLoot.addAll(generatedLoot);
        }

        ResourceLocation[] gemLootLocations = {
                new ResourceLocation(AncientCurses.MOD_ID, "ancient_gems"),
                new ResourceLocation(AncientCurses.MOD_ID, "perfect_gems"),
                new ResourceLocation(AncientCurses.MOD_ID, "polished_gems"),
                new ResourceLocation(AncientCurses.MOD_ID, "broken_gems")
        };

        int[][] lootChances = {
                {0, 0, 10, 100},
                {0, 10, 75, 100},
                {5, 66, 100, 100}
        };

        for (int tier = 0; tier < gemLootLocations.length; tier++) {
            ResourceLocation gemLootLocation = gemLootLocations[tier];
            LootTable gemLootTable = serverLevel.getServer().getLootData().getElement(LootDataType.TABLE, gemLootLocation);

            //System.out.println("Processing gem loot for tier " + tier + " at location " + gemLootLocation);
            if (gemLootTable != null) {
                int amplifierIndex = Math.max(0, Math.min(adjustedAmplifier, lootChances.length - 1));
                int gemLootChance = lootChances[amplifierIndex][tier];
                //System.out.println("For amplifierIndex " + amplifierIndex + ", gemLootChance for tier " + tier + ": " + gemLootChance);
                if (random.nextInt(100) < gemLootChance) {
                    LootParams.Builder gemLootParamsBuilder = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .withParameter(LootContextParams.ORIGIN, player.position());

                    LootParams gemLootParams = gemLootParamsBuilder.create(LootContextParamSets.CHEST);
                    ObjectArrayList<ItemStack> gemLoot = gemLootTable.getRandomItems(gemLootParams);

                    if (!gemLoot.isEmpty()) {
                        totalGeneratedLoot.add(gemLoot.get(0));
                        //System.out.println("Added gem loot item: " + gemLoot.get(0));
                        break;
                    }
                }
            }
        }

        for (ItemStack item : totalGeneratedLoot) {
            if (!item.isEmpty()) {
                ejectItemsTowardPlayer(level, pos, player, Collections.singletonList(item));
            }
        }
    }

    private void ejectItemsTowardPlayer(Level level, BlockPos pos, Player player, List<ItemStack> items) {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, item.copy());
                double dX = player.getX() - pos.getX();
                double dY = (player.getY() + player.getEyeHeight()) - (pos.getY() + 1.5);
                double dZ = player.getZ() - pos.getZ();
                double velocityFactor = 0.1;
                itemEntity.setDeltaMovement(dX * velocityFactor, dY * velocityFactor, dZ * velocityFactor);

                level.addFreshEntity(itemEntity);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CursedAltarBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide) {
            return createTickerHelper(pBlockEntityType, ModBlockEntities.CURSED_ALTAR_BE.get(), CursedAltarBlockEntity::bookAnimationTick);
        } else {
            return createTickerHelper(pBlockEntityType, ModBlockEntities.CURSED_ALTAR_BE.get(), CursedAltarBlockEntity::serverTick);
        }
    }
}
