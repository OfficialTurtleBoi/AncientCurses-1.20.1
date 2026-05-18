package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.turtleboi.ancientcurses.effect.ModEffects;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class BloodpriceSigilItem extends ArtifactItem {
    public static final int INTERCEPT_WINDOW_TICKS = 100;
    public static final int PAYOUT_DELAY_TICKS = 40;
    public static final int BLOODPRICE_COOLDOWN_TICKS = 600;
    private static final float MAX_BAR_DEBT = 20.0F;
    private static final String ACTIVE_TAG = "BloodpriceActive";
    private static final String DEBT_TAG = "BloodpriceDebt";
    private static final String PAYOUT_DELAY_TAG = "BloodpricePayoutDelay";
    private static final String PAYOUT_FLAG = "BloodpriceResolving";

    public BloodpriceSigilItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && activateSigil(player, stack)) {
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public boolean tryActivateArtifactAbility(ServerPlayer player, ItemStack stack, boolean crouching) {
        if (!isEquippedCurioStack(player, stack)) {
            return false;
        }
        return activateSigil(player, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        float debt = getDebt(stack);
        if (debt > 0.0F) {
            tooltip.add(Component.translatable("item.ancientcurses.bloodprice_sigil.debt", formatValue(debt))
                    .withStyle(ChatFormatting.DARK_RED));
        }
        if (isInterceptActive(stack)) {
            tooltip.add(Component.translatable("item.ancientcurses.bloodprice_sigil.active")
                    .withStyle(ChatFormatting.RED));
        } else if (isPayoutPending(stack)) {
            tooltip.add(Component.translatable("item.ancientcurses.bloodprice_sigil.pending")
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.ancientcurses.bloodprice_sigil.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return isInterceptActive(stack) || isPayoutPending(stack) || getDebt(stack) > 0.0F;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (isInterceptActive(stack)) {
            return 13;
        }
        return Math.round(13.0F * Mth.clamp(getDebt(stack) / MAX_BAR_DEBT, 0.0F, 1.0F));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return isInterceptActive(stack) ? 0xC61D24 : 0x7A0E16;
    }

    public static boolean absorbDamage(Player player, float amount) {
        if (amount <= 0.0F || player.level().isClientSide() || player.getPersistentData().getBoolean(PAYOUT_FLAG)) {
            return false;
        }

        ItemStack stack = findTrackedSigil(player);
        if (stack.isEmpty() || !isInterceptActive(stack)) {
            return false;
        }

        addDebt(stack, amount);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.65F, 0.55F);
        return true;
    }

    public static void reduceDebtFromKill(Player player, LivingEntity victim) {
        ItemStack stack = findTrackedSigil(player);
        if (stack.isEmpty()) {
            return;
        }

        float debt = getDebt(stack);
        if (debt <= 0.0F) {
            return;
        }

        float reduction = Mth.clamp((float) victim.getMaxHealth() * 0.25F, 2.0F, 12.0F);
        setDebt(stack, Math.max(0.0F, debt - reduction));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4F, 0.65F);
    }

    public static void tick(Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        ItemStack stack = findTrackedSigil(player);
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (isInterceptActive(stack)) {
            if (player.hasEffect(ModEffects.BLOOD_DEBT.get())) {
                return;
            }
            tag.remove(ACTIVE_TAG);
            if (getDebt(stack) > 0.0F) {
                tag.putInt(PAYOUT_DELAY_TAG, PAYOUT_DELAY_TICKS);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.65F, 0.6F);
            } else {
                tag.remove(PAYOUT_DELAY_TAG);
            }
        }

        int payoutDelay = tag.getInt(PAYOUT_DELAY_TAG);
        if (payoutDelay > 0) {
            if (payoutDelay == 1) {
                resolveDebt(player, stack);
            } else {
                tag.putInt(PAYOUT_DELAY_TAG, payoutDelay - 1);
            }
        }
    }

    private static void resolveDebt(Player player, ItemStack stack) {
        float debt = getDebt(stack);
        clearState(stack);
        player.getCooldowns().addCooldown(stack.getItem(), BLOODPRICE_COOLDOWN_TICKS);

        if (debt <= 0.0F) {
            return;
        }

        float appliedDamage = Math.max(0.0F, Math.min(debt, player.getHealth() - 1.0F));
        player.getPersistentData().putBoolean(PAYOUT_FLAG, true);
        try {
            if (appliedDamage > 0.0F) {
                player.hurt(player.damageSources().magic(), appliedDamage);
            }
        } finally {
            player.getPersistentData().remove(PAYOUT_FLAG);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.85F, 0.6F);
    }

    private static ItemStack findTrackedSigil(Player player) {
        ItemStack fallback = ItemStack.EMPTY;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BloodpriceSigilItem) {
                if (hasTrackedState(stack)) {
                    return stack;
                }
                if (fallback.isEmpty()) {
                    fallback = stack;
                }
            }
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof BloodpriceSigilItem) {
                if (hasTrackedState(stack)) {
                    return stack;
                }
                if (fallback.isEmpty()) {
                    fallback = stack;
                }
            }
        }

        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof BloodpriceSigilItem) {
                if (hasTrackedState(stack)) {
                    return stack;
                }
                if (fallback.isEmpty()) {
                    fallback = stack;
                }
            }
        }

        if (ModList.get().isLoaded("curios")) {
            AtomicReference<ItemStack> curioTracked = new AtomicReference<>(ItemStack.EMPTY);
            AtomicReference<ItemStack> curioFallback = new AtomicReference<>(fallback);
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                String[] handlers = new String[] {"belt", "charm"};
                for (String handlerName : handlers) {
                    curiosInventory.getStacksHandler(handlerName).ifPresent(handler -> {
                        for (int i = 0; i < handler.getStacks().getSlots(); i++) {
                            ItemStack stack = handler.getStacks().getStackInSlot(i);
                            if (stack.getItem() instanceof BloodpriceSigilItem) {
                                if (hasTrackedState(stack)) {
                                    curioTracked.set(stack);
                                    return;
                                }
                                if (curioFallback.get().isEmpty()) {
                                    curioFallback.set(stack);
                                }
                            }
                        }
                    });
                    if (!curioTracked.get().isEmpty()) {
                        break;
                    }
                }
            });
            if (!curioTracked.get().isEmpty()) {
                return curioTracked.get();
            }
            fallback = curioFallback.get();
        }

        return fallback;
    }

    private static boolean activateSigil(Player player, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(stack.getItem()) || isInterceptActive(stack) || isPayoutPending(stack)) {
            return false;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(ACTIVE_TAG, true);
        tag.remove(PAYOUT_DELAY_TAG);
        player.addEffect(new MobEffectInstance(ModEffects.BLOOD_DEBT.get(), INTERCEPT_WINDOW_TICKS, 0, false, true, true));

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.75F, 0.7F);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        return true;
    }

    private static boolean isEquippedCurioStack(Player player, ItemStack target) {
        if (!ModList.get().isLoaded("curios")) {
            return false;
        }

        AtomicReference<Boolean> found = new AtomicReference<>(false);
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            String[] handlers = new String[] {"belt", "charm"};
            for (String handlerName : handlers) {
                curiosInventory.getStacksHandler(handlerName).ifPresent(handler -> {
                    for (int i = 0; i < handler.getStacks().getSlots(); i++) {
                        if (handler.getStacks().getStackInSlot(i) == target) {
                            found.set(true);
                            return;
                        }
                    }
                });
                if (found.get()) {
                    return;
                }
            }
        });
        return found.get();
    }

    private static boolean hasTrackedState(ItemStack stack) {
        return isInterceptActive(stack) || isPayoutPending(stack) || getDebt(stack) > 0.0F;
    }

    private static void addDebt(ItemStack stack, float amount) {
        setDebt(stack, getDebt(stack) + amount);
    }

    private static void setDebt(ItemStack stack, float amount) {
        CompoundTag tag = stack.getOrCreateTag();
        if (amount > 0.0F) {
            tag.putFloat(DEBT_TAG, amount);
        } else {
            tag.remove(DEBT_TAG);
        }
    }

    private static void clearState(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(ACTIVE_TAG);
        tag.remove(DEBT_TAG);
        tag.remove(PAYOUT_DELAY_TAG);
    }

    private static boolean isInterceptActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(ACTIVE_TAG);
    }

    private static boolean isPayoutPending(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getInt(PAYOUT_DELAY_TAG) > 0;
    }

    private static float getDebt(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0.0F : Math.max(0.0F, tag.getFloat(DEBT_TAG));
    }

    private static String formatValue(float value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
