package net.turtleboi.ancientcurses.block.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.item.items.SoulShardItem;
import net.turtleboi.ancientcurses.rite.ModRites;
import net.turtleboi.ancientcurses.sound.ModSounds;
import net.turtleboi.ancientcurses.util.ModTags;

import java.util.HashMap;
import java.util.Map;

public final class GemUtil {
    private static final Map<Item, Item> GEM_UPGRADE_MAP = new HashMap<>();

    static {
        GEM_UPGRADE_MAP.put(ModItems.BROKEN_AMETHYST.get(), ModItems.POLISHED_AMETHYST.get());
        GEM_UPGRADE_MAP.put(ModItems.POLISHED_AMETHYST.get(), ModItems.PERFECT_AMETHYST.get());
        GEM_UPGRADE_MAP.put(ModItems.BROKEN_DIAMOND.get(), ModItems.POLISHED_DIAMOND.get());
        GEM_UPGRADE_MAP.put(ModItems.POLISHED_DIAMOND.get(), ModItems.PERFECT_DIAMOND.get());
        GEM_UPGRADE_MAP.put(ModItems.BROKEN_EMERALD.get(), ModItems.POLISHED_EMERALD.get());
        GEM_UPGRADE_MAP.put(ModItems.POLISHED_EMERALD.get(), ModItems.PERFECT_EMERALD.get());
        GEM_UPGRADE_MAP.put(ModItems.BROKEN_RUBY.get(), ModItems.POLISHED_RUBY.get());
        GEM_UPGRADE_MAP.put(ModItems.POLISHED_RUBY.get(), ModItems.PERFECT_RUBY.get());
        GEM_UPGRADE_MAP.put(ModItems.BROKEN_SAPPHIRE.get(), ModItems.POLISHED_SAPPHIRE.get());
        GEM_UPGRADE_MAP.put(ModItems.POLISHED_SAPPHIRE.get(), ModItems.PERFECT_SAPPHIRE.get());
        GEM_UPGRADE_MAP.put(ModItems.BROKEN_TOPAZ.get(), ModItems.POLISHED_TOPAZ.get());
        GEM_UPGRADE_MAP.put(ModItems.POLISHED_TOPAZ.get(), ModItems.PERFECT_TOPAZ.get());
    }

    private GemUtil() {
    }

    public static boolean isPreciousGem(ItemStack itemStack) {
        return itemStack.is(ModTags.Items.PRECIOUS_GEMS);
    }

    public static boolean canUpgrade(ItemStack gem1, ItemStack gem2, ItemStack gem3) {
        return !gem1.isEmpty()
                && gem1.is(gem2.getItem())
                && gem1.is(gem3.getItem())
                && !gem1.is(ModTags.Items.MAJOR_GEMS)
                && GEM_UPGRADE_MAP.containsKey(gem1.getItem());
    }

    public static ItemStack getUpgradedGem(ItemStack gem) {
        Item upgradedItem = GEM_UPGRADE_MAP.get(gem.getItem());
        return upgradedItem != null ? new ItemStack(upgradedItem) : ItemStack.EMPTY;
    }

    public static int getRequiredRiteTier(ItemStack gem) {
        if (!GEM_UPGRADE_MAP.containsKey(gem.getItem())) {
            return Integer.MAX_VALUE;
        }

        Item upgradedItem = GEM_UPGRADE_MAP.get(gem.getItem());
        return upgradedItem != null && GEM_UPGRADE_MAP.containsKey(upgradedItem) ? 1 : 2;
    }

    public static boolean isGemFusionIngredient(CursedAltarBlockEntity altarEntity, ItemStack stack) {
        return getNextGemFusionSlot(altarEntity, stack) != -1;
    }

    public static int getNextGemFusionSlot(CursedAltarBlockEntity altarEntity, ItemStack stack) {
        if (!isPreciousGem(stack) || altarEntity.hasPendingGemFusion()) {
            return -1;
        }

        if (!altarEntity.getRitualItemInSlot(0).is(ModItems.SOUL_SHARD.get())) {
            return -1;
        }

        ItemStack catalyst = altarEntity.getRitualItemInSlot(0);
        if (catalyst.isEmpty() || !stackHasFilledSoulShard(catalyst)) {
            return -1;
        }

        int requiredTier = getRequiredRiteTier(stack);
        if (requiredTier == Integer.MAX_VALUE) {
            return -1;
        }

        ItemStack firstGem = ItemStack.EMPTY;
        int gemCount = 0;
        for (int slot = 1; slot <= 8; slot++) {
            ItemStack ritualStack = altarEntity.getRitualItemInSlot(slot);
            if (!ritualStack.isEmpty()) {
                if (!isPreciousGem(ritualStack)) {
                    return -1;
                }
                if (firstGem.isEmpty()) {
                    firstGem = ritualStack;
                } else if (!ItemStack.isSameItemSameTags(firstGem, ritualStack)) {
                    return -1;
                }
                gemCount++;
            }
        }

        if (!firstGem.isEmpty() && !ItemStack.isSameItemSameTags(firstGem, stack)) {
            return -1;
        }

        if (gemCount >= 3) {
            return -1;
        }

        for (int slot = 1; slot <= 3; slot++) {
            if (altarEntity.getRitualItemInSlot(slot).isEmpty()) {
                return slot;
            }
        }

        return -1;
    }

    public static boolean hasCompleteGemFusionRecipe(CursedAltarBlockEntity altarEntity) {
        ItemStack catalyst = altarEntity.getRitualItemInSlot(0);
        if (!stackHasFilledSoulShard(catalyst) || altarEntity.hasPendingGemFusion()) {
            return false;
        }

        ItemStack gem1 = altarEntity.getRitualItemInSlot(1);
        ItemStack gem2 = altarEntity.getRitualItemInSlot(2);
        ItemStack gem3 = altarEntity.getRitualItemInSlot(3);
        for (int slot = 4; slot <= 8; slot++) {
            if (!altarEntity.getRitualItemInSlot(slot).isEmpty()) {
                return false;
            }
        }

        if (!canUpgrade(gem1, gem2, gem3)) {
            return false;
        }

        return getRequiredRiteTier(gem1) != Integer.MAX_VALUE;
    }

    public static boolean tryStartGemFusion(CursedAltarBlockEntity altarEntity, Player player) {
        if (!hasCompleteGemFusionRecipe(altarEntity)) {
            return false;
        }

        ItemStack firstGem = altarEntity.getRitualItemInSlot(1);
        int requiredTier = getRequiredRiteTier(firstGem);
        if (requiredTier == Integer.MAX_VALUE) {
            return false;
        }

        altarEntity.setGemInSlot(0, altarEntity.getRitualItemInSlot(1).copy());
        altarEntity.setGemInSlot(1, altarEntity.getRitualItemInSlot(2).copy());
        altarEntity.setGemInSlot(2, altarEntity.getRitualItemInSlot(3).copy());
        altarEntity.setPendingGemFusionResult(getUpgradedGem(firstGem));
        for (int slot = 0; slot <= 8; slot++) {
            altarEntity.setRitualItemInSlot(slot, ItemStack.EMPTY);
        }
        altarEntity.startAnimation();
        RiteUtil.startRite(player, altarEntity, getGemFusionRiteType(player.level().getRandom()), requiredTier - 1);
        return true;
    }

    private static ResourceLocation getGemFusionRiteType(RandomSource random) {
        return random.nextBoolean() ? ModRites.CARNAGE : ModRites.FAMINE;
    }

    private static boolean stackHasFilledSoulShard(ItemStack stack) {
        return stack.is(ModItems.SOUL_SHARD.get()) && !stack.isEmpty() && stack.getTag() != null && stack.getTag().getInt(SoulShardItem.SOUL_ENERGY_TAG) >= SoulShardItem.MAX_SOUL_ENERGY;
    }

    public static void playGemPlaceSound(Level level, BlockPos pos, int slotIndex) {
        level.playSound(
                null,
                pos,
                ModSounds.GEM_PLACE.get(),
                SoundSource.BLOCKS,
                1.0F + 0.07f * slotIndex,
                0.9F + 0.1f * slotIndex + (float) level.getRandom().nextIntBetweenInclusive(0, 3) / 100
        );
    }
}
