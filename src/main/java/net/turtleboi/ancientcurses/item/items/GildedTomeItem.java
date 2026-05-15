package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.effect.ModEffects;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GildedTomeItem extends ArtifactItem {
    private static final String BANKED_XP_TAG = "BankedXp";
    private static final float BANKED_XP_FRACTION = 0.3F;
    private static final int ENRICHMENT_DURATION_TICKS = 2400;
    private static final int MAX_BAR_XP = 1000;
    private static final Set<UUID> RELEASING_BANKED_XP = new HashSet<>();

    public GildedTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (player.isShiftKeyDown()) {
            activateEnrichment(level, player, stack);
        } else {
            releaseBankedXp(level, player, stack);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getBankedXp(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(13, Math.round(13.0F * Math.min(MAX_BAR_XP, getBankedXp(stack)) / (float) MAX_BAR_XP));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xF4D35E;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getBankedXp(stack) > 0 || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ancientcurses.gilded_tome.banked", getBankedXp(stack))
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.ancientcurses.gilded_tome.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    public static int adjustXpPickup(Player player, int amount) {
        if (amount <= 0) {
            return amount;
        }

        if (RELEASING_BANKED_XP.contains(player.getUUID())) {
            return amount;
        }

        boolean enriched = player.hasEffect(ModEffects.ENRICHMENT.get());
        int adjustedAmount = enriched ? amount * 2 : amount;
        ItemStack bankingTome = findFirstTome(player);
        if (bankingTome.isEmpty()) {
            return adjustedAmount;
        }

        int bankedAmount = (int) Math.floor(adjustedAmount * BANKED_XP_FRACTION);
        if (bankedAmount <= 0) {
            return adjustedAmount;
        }

        addBankedXp(bankingTome, bankedAmount);
        return Math.max(0, adjustedAmount - bankedAmount);
    }

    private static void activateEnrichment(Level level, Player player, ItemStack stack) {
        player.addEffect(new MobEffectInstance(ModEffects.ENRICHMENT.get(), ENRICHMENT_DURATION_TICKS, 0, false, true, true));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.25F);
        player.displayClientMessage(Component.translatable("item.ancientcurses.gilded_tome.enriched")
                .withStyle(ChatFormatting.GOLD), true);
    }

    private static void releaseBankedXp(Level level, Player player, ItemStack stack) {
        int bankedXp = getBankedXp(stack);
        if (bankedXp <= 0) {
            player.displayClientMessage(Component.translatable("item.ancientcurses.gilded_tome.empty")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
            return;
        }

        stack.getOrCreateTag().putInt(BANKED_XP_TAG, 0);
        RELEASING_BANKED_XP.add(player.getUUID());
        try {
            player.giveExperiencePoints(bankedXp);
        } finally {
            RELEASING_BANKED_XP.remove(player.getUUID());
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.75F);
    }

    private static int getBankedXp(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.max(0, tag.getInt(BANKED_XP_TAG));
    }

    private static void addBankedXp(ItemStack stack, int amount) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(BANKED_XP_TAG, getBankedXp(stack) + amount);
    }

    private static ItemStack findFirstTome(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof GildedTomeItem) {
                return stack;
            }
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof GildedTomeItem) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
