package net.turtleboi.ancientcurses.block.altar;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.rite.Rite;
import net.turtleboi.ancientcurses.rite.util.RiteLocator;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class RewardUtil {
    private RewardUtil() {
    }

    public static void rewardPlayer(Player player, CursedAltarBlockEntity altarEntity, Level level, BlockPos pos) {
        int amplifier = player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                .map(PlayerRiteDataCapability::getCurseAmplifier)
                .orElse(0);

        Rite rite = altarEntity.getPlayerRite(player.getUUID());
        if (rite == null) {
            rite = RiteLocator.findActiveRite(player);
        }

        if (altarEntity.hasCollectedReward(player)) {
            return;
        }

        if (rite == null) {
            altarEntity.markRewardCollected(player);
            return;
        }

        if (player instanceof ServerPlayer) {
            CoreNetworking.sendToNear(new CameraShakeS2C(0.125F, 1000), player);
            if (altarEntity.hasPendingGemFusion()) {
                rewardPendingGemFusion(level, player, altarEntity, pos);
            } else {
                rewardPlayerWithLootTable(level, player, amplifier, rite, pos);
            }
            player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
                if (amplifier > 0) {
                    riteData.recordCompletedRiteTier(amplifier);
                }
                riteData.clearCurseAmplifier();
            });
            altarEntity.markRewardCollected(player);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, altarEntity.getBlockPos().getX() + 0.5, altarEntity.getBlockPos().getY() + 1,
                    altarEntity.getBlockPos().getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(), SoundSource.AMBIENT, 1.0F, 0.5F);
            serverLevel.playSound(null, altarEntity.getBlockPos().getX() + 0.5, altarEntity.getBlockPos().getY() + 1,
                    altarEntity.getBlockPos().getZ() + 0.5, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0f, 1.0f);
        }
    }

    private static void rewardPendingGemFusion(Level level, Player player, CursedAltarBlockEntity altarEntity, BlockPos pos) {
        altarEntity.startPendingGemFusionResolution();
    }

    private static void rewardPlayerWithLootTable(Level level, Player player, int amplifier, Rite rite, BlockPos pos) {
        int adjustedAmplifier = amplifier - 1;
        ResourceLocation[] lootTableLocations = {
                new ResourceLocation(AncientCurses.MOD_ID, "amplifier_0"),
                new ResourceLocation(AncientCurses.MOD_ID, "amplifier_1"),
                new ResourceLocation(AncientCurses.MOD_ID, "amplifier_2")
        };
        int lootIndex = Math.max(0, Math.min(adjustedAmplifier, lootTableLocations.length - 1));

        ServerLevel serverLevel = (ServerLevel) player.level();
        LootTable lootTable = serverLevel.getServer().getLootData().getElement(LootDataType.TABLE, lootTableLocations[lootIndex]);
        if (lootTable == null) {
            return;
        }

        Random random = new Random();
        int minRolls = 1;
        int maxRolls = 2;

        minRolls = rite.getMinRewardRolls();
        maxRolls = rite.getMaxRewardRolls();

        int rollCount = random.nextInt(maxRolls - minRolls + 1) + minRolls;
        List<ItemStack> totalGeneratedLoot = new ArrayList<>();

        for (int i = 0; i < rollCount; i++) {
            LootParams lootParams = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .create(LootContextParamSets.CHEST);

            ObjectArrayList<ItemStack> generatedLoot = lootTable.getRandomItems(lootParams);
            totalGeneratedLoot.addAll(generatedLoot);
        }

        addGemLoot(serverLevel, player, adjustedAmplifier, random, totalGeneratedLoot);
        addCursedSoulShardLoot(amplifier, rite, random, totalGeneratedLoot);

        for (ItemStack item : totalGeneratedLoot) {
            if (!item.isEmpty()) {
                ejectItemsTowardPlayer(level, pos, player, Collections.singletonList(item));
            }
        }
    }

    private static void addGemLoot(ServerLevel serverLevel, Player player, int adjustedAmplifier, Random random, List<ItemStack> totalGeneratedLoot) {
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
            LootTable gemLootTable = serverLevel.getServer().getLootData().getElement(LootDataType.TABLE, gemLootLocations[tier]);
            if (gemLootTable == null) {
                continue;
            }

            int amplifierIndex = Math.max(0, Math.min(adjustedAmplifier, lootChances.length - 1));
            int gemLootChance = lootChances[amplifierIndex][tier];
            if (random.nextInt(100) >= gemLootChance) {
                continue;
            }

            LootParams gemLootParams = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .create(LootContextParamSets.CHEST);

            ObjectArrayList<ItemStack> gemLoot = gemLootTable.getRandomItems(gemLootParams);
            if (!gemLoot.isEmpty()) {
                totalGeneratedLoot.add(gemLoot.get(0));
                break;
            }
        }
    }

    private static void addCursedSoulShardLoot(int amplifier, Rite rite, Random random, List<ItemStack> totalGeneratedLoot) {
        if (amplifier < 2) {
            return;
        }

        float chance = rite.getSoulShardDropChance(amplifier);
        int guaranteedShards = (int) Math.floor(chance);
        float bonusShardChance = chance - guaranteedShards;

        for (int i = 0; i < guaranteedShards; i++) {
            totalGeneratedLoot.add(new ItemStack(ModItems.CURSED_SOUL_SHARD.get()));
        }

        if (random.nextFloat() < bonusShardChance) {
            totalGeneratedLoot.add(new ItemStack(ModItems.CURSED_SOUL_SHARD.get()));
        }
    }
    public static void ejectItemsTowardPlayer(Level level, BlockPos pos, Player player, List<ItemStack> items) {
        for (ItemStack item : items) {
            if (item.isEmpty()) {
                continue;
            }

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
