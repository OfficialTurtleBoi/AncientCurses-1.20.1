package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ThornCrownItem extends ArtifactItem {
    public static final int THORN_SHOT_COOLDOWN_TICKS = 100;
    public static final float THORN_REFLECT_RATIO = 0.35F;
    public static final float THORN_REFLECT_MIN = 1.0F;
    public static final float THORN_REFLECT_MAX = 4.0F;
    private static final String THORN_CHARGES_TAG = "ThornCharges";
    private static final String STORED_REFLECT_DAMAGE_TAG = "StoredReflectDamage";
    private static final String BURST_REMAINING_TAG = "ThornBurstRemaining";
    private static final String BURST_DELAY_TAG = "ThornBurstDelay";
    private static final String BURST_CIRCLE_TAG = "ThornBurstCircle";
    private static final String BURST_INDEX_TAG = "ThornBurstIndex";
    private static final int MAX_CHARGES = 7;
    private static final int THORNS_PER_BURST = 7;
    private static final float REFLECT_DAMAGE_PER_CHARGE = 7.0F;
    private static final int BURST_INTERVAL_TICKS = 2;
    private static final double THORN_ARROW_DAMAGE = 3.5D;
    private static final float THORN_ARROW_SPEED = 2.4F;
    private static final float THORN_ARROW_INACCURACY = 0.45F;

    public ThornCrownItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this) || getCharges(stack) <= 0 || isBurstActive(player)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            activateBurst(player, stack, player.isCrouching());
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SWEET_BERRY_BUSH_BREAK, SoundSource.PLAYERS, 0.85F,
                0.85F + level.getRandom().nextFloat() * 0.25F);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int charges = getCharges(stack);
        float progress = getStoredReflectDamage(stack);
        if (charges > 0 || progress > 0.0F) {
            tooltip.add(Component.literal(charges + "/" + MAX_CHARGES + " Thorn Shots")
                    .withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCharges(stack) > 0 || getStoredReflectDamage(stack) > 0.0F;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        float totalProgress = getCharges(stack) + (getStoredReflectDamage(stack) / REFLECT_DAMAGE_PER_CHARGE);
        return Math.round(Mth.clamp(totalProgress / MAX_CHARGES, 0.0F, 1.0F) * 13.0F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x4E8A39;
    }

    public static boolean isWearing(Player player) {
        return !getEquippedStack(player).isEmpty();
    }

    public static float getReflectDamage(float incomingDamage) {
        return Math.max(THORN_REFLECT_MIN, Math.min(THORN_REFLECT_MAX, incomingDamage * THORN_REFLECT_RATIO));
    }

    public static void addReflectedDamage(Player player, float reflectedDamage) {
        ItemStack crown = getEquippedStack(player);
        if (crown.isEmpty() || reflectedDamage <= 0.0F) {
            return;
        }

        CompoundTag tag = crown.getOrCreateTag();
        int charges = tag.getInt(THORN_CHARGES_TAG);
        float storedDamage = tag.getFloat(STORED_REFLECT_DAMAGE_TAG) + reflectedDamage;

        while (charges < MAX_CHARGES && storedDamage >= REFLECT_DAMAGE_PER_CHARGE) {
            charges++;
            storedDamage -= REFLECT_DAMAGE_PER_CHARGE;
        }

        if (charges >= MAX_CHARGES) {
            charges = MAX_CHARGES;
            storedDamage = Math.min(storedDamage, REFLECT_DAMAGE_PER_CHARGE - 0.001F);
        }

        tag.putInt(THORN_CHARGES_TAG, charges);
        if (storedDamage > 0.0F) {
            tag.putFloat(STORED_REFLECT_DAMAGE_TAG, storedDamage);
        } else {
            tag.remove(STORED_REFLECT_DAMAGE_TAG);
        }
    }

    @Override
    public boolean tryActivateArtifactAbility(ServerPlayer player, ItemStack stack, boolean crouching) {
        if (getEquippedStack(player) != stack
                || player.getCooldowns().isOnCooldown(stack.getItem())
                || isBurstActive(player)
                || getCharges(stack) <= 0) {
            return false;
        }

        activateBurst(player, stack, crouching);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SWEET_BERRY_BUSH_BREAK, SoundSource.PLAYERS, 0.85F,
                0.85F + player.level().getRandom().nextFloat() * 0.25F);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        return true;
    }

    public static void tickBurst(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(BURST_REMAINING_TAG) || player.level().isClientSide() || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        int delay = data.getInt(BURST_DELAY_TAG);
        if (delay > 0) {
            data.putInt(BURST_DELAY_TAG, delay - 1);
            return;
        }

        int remaining = data.getInt(BURST_REMAINING_TAG);
        int index = data.getInt(BURST_INDEX_TAG);
        boolean circle = data.getBoolean(BURST_CIRCLE_TAG);

        fireThorn(level, player, circle, index);
        remaining--;

        if (remaining <= 0) {
            clearBurst(data);
            return;
        }

        data.putInt(BURST_REMAINING_TAG, remaining);
        data.putInt(BURST_INDEX_TAG, index + 1);
        data.putInt(BURST_DELAY_TAG, BURST_INTERVAL_TICKS);
    }

    public static ItemStack getEquippedStack(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof ThornCrownItem) {
            return helmet;
        }

        if (!ModList.get().isLoaded("curios")) {
            return ItemStack.EMPTY;
        }

        AtomicReference<ItemStack> equipped = new AtomicReference<>(ItemStack.EMPTY);
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory ->
                curiosInventory.getStacksHandler("head").ifPresent(slotInventory -> {
                    for (int i = 0; i < slotInventory.getStacks().getSlots(); i++) {
                        ItemStack stack = slotInventory.getStacks().getStackInSlot(i);
                        if (stack.getItem() instanceof ThornCrownItem) {
                            equipped.set(stack);
                            return;
                        }
                    }
                }));
        return equipped.get();
    }

    private static boolean isBurstActive(Player player) {
        return player.getPersistentData().getInt(BURST_REMAINING_TAG) > 0;
    }

    private static void activateBurst(Player player, ItemStack stack, boolean circle) {
        consumeCharge(stack);
        beginBurst(player, circle);
        player.getCooldowns().addCooldown(stack.getItem(), THORN_SHOT_COOLDOWN_TICKS);
    }

    private static void beginBurst(Player player, boolean circle) {
        CompoundTag data = player.getPersistentData();
        data.putInt(BURST_REMAINING_TAG, THORNS_PER_BURST);
        data.putInt(BURST_DELAY_TAG, 0);
        data.putInt(BURST_INDEX_TAG, 0);
        data.putBoolean(BURST_CIRCLE_TAG, circle);
    }

    private static void clearBurst(CompoundTag data) {
        data.remove(BURST_REMAINING_TAG);
        data.remove(BURST_DELAY_TAG);
        data.remove(BURST_INDEX_TAG);
        data.remove(BURST_CIRCLE_TAG);
    }

    private static void fireThorn(ServerLevel level, Player player, boolean circle, int burstIndex) {
        Arrow arrow = new Arrow(level, player);
        arrow.setBaseDamage(THORN_ARROW_DAMAGE);
        arrow.setCritArrow(false);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

        if (circle) {
            float yaw = player.getYRot() + (360.0F / THORNS_PER_BURST) * burstIndex;
            arrow.shootFromRotation(player, 0.0F, yaw, 0.0F, THORN_ARROW_SPEED, 0.0F);
        } else {
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    THORN_ARROW_SPEED, THORN_ARROW_INACCURACY);
        }

        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5F,
                1.1F + level.getRandom().nextFloat() * 0.2F);
    }

    private static void consumeCharge(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int charges = Math.max(0, tag.getInt(THORN_CHARGES_TAG) - 1);
        if (charges > 0) {
            tag.putInt(THORN_CHARGES_TAG, charges);
        } else {
            tag.remove(THORN_CHARGES_TAG);
        }
    }

    private static int getCharges(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(THORN_CHARGES_TAG);
    }

    private static float getStoredReflectDamage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0.0F : tag.getFloat(STORED_REFLECT_DAMAGE_TAG);
    }
}
