package net.turtleboi.ancientcurses.block.blockentity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
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
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;
import net.turtleboi.ancientcurses.trials.EliminationTrial;
import net.turtleboi.ancientcurses.trials.PlayerTrialData;
import net.turtleboi.ancientcurses.trials.SurvivalTrial;
import net.turtleboi.ancientcurses.trials.Trial;
import net.turtleboi.ancientcurses.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CursedAltarBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = BaseEntityBlock.box(0, 0, 0, 16, 12, 16);
    public static final List<BlockPos> SOUL_TORCH_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2).filter((p_207914_) ->
            Math.abs(p_207914_.getX()) == 2 || Math.abs(p_207914_.getZ()) == 2).map(BlockPos::immutable).toList();

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
            if (random.nextInt(16) == 0 && isSoulTorch(level, torchPos)) {
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

    private boolean isSoulTorch(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.SCONCED_SOUL_TORCH.get()) || level.getBlockState(pos).is(ModBlocks.SCONCED_WALL_SOUL_TORCH.get());
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

                for (Player player : level.players()) {
                    BlockPos playerAltarPos = PlayerTrialData.getCurrentAltarPos(player);
                    if (playerAltarPos != null && playerAltarPos.equals(pos)) {
                        PlayerTrialData.resetAltarAtPos(player, playerAltarPos);
                    }
                }
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

                if (PlayerTrialData.isPlayerCursed(player)) {
                    //player.sendSystemMessage(Component.literal("You are already cursed! Complete your trial before interacting again.").withStyle(ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }

                if (altarEntity.hasPlayerCompletedTrial(player)){
                    rewardPlayer(player, altarEntity, level, pos);
                    //player.sendSystemMessage(Component.literal("You've completed this trial!").withStyle(ChatFormatting.GREEN));
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
                                    altarEntity.setGemInSlot(i, heldItem.split(1));
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
                    if (!altarEntity.hasPlayerCompletedTrial(player) && altarEntity.canPlayerUse(player)) {
                        startTrial(player, altarEntity);
                        return InteractionResult.SUCCESS;
                    } else if (!altarEntity.hasPlayerCompletedTrial(player) && !altarEntity.canPlayerUse(player)){
                        //player.sendSystemMessage(Component.literal("The altar is recharging.").withStyle(ChatFormatting.RED));
                        return InteractionResult.FAIL;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    public void startTrial(Player player, CursedAltarBlockEntity altarEntity) {
        if (altarEntity.hasPlayerCompletedTrial(player)) {
            //player.sendSystemMessage(Component.literal("You have already completed the trial for this altar.").withStyle(ChatFormatting.GREEN));
            return;
        }

        MobEffect randomCurse = CursedAltarBlockEntity.getRandomCurse();
        int randomAmplifier = CursedAltarBlockEntity.getRandomAmplifier(player);

        BlockPos playerPos = player.blockPosition();
        Level level = player.level();

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
        int amplifier = PlayerTrialData.getCurseAmplifier(player);
        if (altarEntity.hasCollectedReward(player)) {
            //player.sendSystemMessage(Component.literal("You have already received your reward for this trial.").withStyle(ChatFormatting.RED));
            return;
        }

        //System.out.println(Component.literal("Giving player loot of amplifier: " + amplifier));

        if (player instanceof ServerPlayer) {
            rewardPlayerWithLootTable(player, amplifier, level, pos);
            //Trial trial = altarEntity.getPlayerTrial(player.getUUID());
            //if (trial != null) {
            //    System.out.println("Trial is not null. Proceeding to remove event bar.");
            //    //trial.removeEventBar(player);
            //} else {
            //    System.out.println("Trial is null for player: " + player.getName().getString());
            //}
            ModNetworking.sendToPlayer(
                    new SyncTrialDataS2C(
                            "None",
                            0,
                            0,
                            0,
                            0,
                            "",
                            0,
                            0),
                    (ServerPlayer) player);
            PlayerTrialData.clearCurseAmplifier(player);
            altarEntity.markRewardCollected(player);
        }
    }

    private void rewardPlayerWithLootTable(Player player, int pAmplifier, Level level, BlockPos pos) {
        ResourceLocation[] lootTableLocations = {
                new ResourceLocation("ancientcurses", "amplifier_0"),
                new ResourceLocation("ancientcurses", "amplifier_1"),
                new ResourceLocation("ancientcurses", "amplifier_2")};
        int lootIndex = Math.min(pAmplifier, lootTableLocations.length - 1);

        ServerLevel serverLevel = (ServerLevel) player.level();
        LootTable lootTable = serverLevel.getServer().getLootData().getElement(LootDataType.TABLE, lootTableLocations[lootIndex]);

        if (lootTable == null) {
            //player.sendSystemMessage(Component.literal("No loot table found for this trial reward.").withStyle(ChatFormatting.RED));
            return;
        }

        Random random = new Random();
        int minRolls, maxRolls;
        if (pAmplifier == 0) {
            minRolls = 3;
            maxRolls = 5;
        } else if (pAmplifier == 1) {
            minRolls = 2;
            maxRolls = 3;
        } else {
            minRolls = 1;
            maxRolls = 1;
        }

        int rollCount = random.nextInt(maxRolls - minRolls + 1) + minRolls;

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
                new ResourceLocation("ancientcurses", "ancient_gems"),
                new ResourceLocation("ancientcurses", "perfect_gems"),
                new ResourceLocation("ancientcurses", "polished_gems"),
                new ResourceLocation("ancientcurses", "broken_gems")
        };

        int[][] lootChances = {
                {0, 0, 10, 100},
                {0, 10, 75, 100},
                {10, 66, 100, 100}
        };

        int ampIndex = Math.min(pAmplifier, lootChances.length - 1);
        for (int tier = 0; tier < gemLootLocations.length; tier++) {
            ResourceLocation gemLootLocation = gemLootLocations[tier];
            LootTable gemLootTable = serverLevel.getServer().getLootData().getElement(LootDataType.TABLE, gemLootLocation);

            if (gemLootTable != null) {
                int gemLootChance = lootChances[ampIndex][tier];
                if (random.nextInt(100) < gemLootChance) {
                    LootParams.Builder gemLootParamsBuilder = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .withParameter(LootContextParams.ORIGIN, player.position());

                    LootParams gemLootParams = gemLootParamsBuilder.create(LootContextParamSets.CHEST);
                    ObjectArrayList<ItemStack> gemLoot = gemLootTable.getRandomItems(gemLootParams);

                    if (!gemLoot.isEmpty()) {
                        totalGeneratedLoot.add(gemLoot.get(0));
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
