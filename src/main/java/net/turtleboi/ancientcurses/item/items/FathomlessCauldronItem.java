package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.entity.entities.items.ThrownCauldronPotion;
import net.turtleboi.ancientcurses.item.tooltip.CauldronTooltip;
import net.turtleboi.ancientcurses.network.PlayerKeyStateCache;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FathomlessCauldronItem extends ArtifactItem {
    public static final int MAX_POTION_SLOTS = 4;
    public static final int USES_PER_POTION = 16;
    public static final int MAX_MODIFIER_COUNT = 64;

    private static final String POTIONS_TAG = "CauldronPotions";
    private static final String POTION_ID_TAG = "PotionTag";
    private static final String CUSTOM_EFFECTS_TAG = "CustomEffects";
    private static final String USES_TAG = "Uses";
    private static final String GUNPOWDER_TAG = "Gunpowder";
    private static final String DRAGONS_BREATH_TAG = "DragonsBreath";

    public FathomlessCauldronItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack cauldron, ItemStack other, Slot slot,
                                            ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || other.isEmpty() || !slot.allowModification(player)) {
            return false;
        }

        Item item = other.getItem();
        if (item == Items.GUNPOWDER) {
            return addModifier(cauldron, other, true, player);
        }
        if (item == Items.DRAGON_BREATH) {
            return addModifier(cauldron, other, false, player);
        }
        if (item instanceof PotionItem && insertPotion(cauldron, other)) {
            other.shrink(1);
            playInsertSound(player);
            return true;
        }

        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack cauldron, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        ItemStack slotStack = slot.getItem();
        boolean bulk = isCtrlDown(player);

        if (!slotStack.isEmpty()) {
            return insertFromSlot(cauldron, slotStack, player, bulk);
        }

        return bulk ? removeModifierStack(cauldron, slot, player) : removePotion(cauldron, slot, player);
    }

    private static boolean insertFromSlot(ItemStack cauldron, ItemStack slotStack, Player player, boolean bulk) {
        Item item = slotStack.getItem();
        int moved = 0;

        if (item instanceof PotionItem) {
            moved = insertPotion(cauldron, slotStack.copyWithCount(1)) ? 1 : 0;
        } else if (item == Items.GUNPOWDER && getDragonsBreath(cauldron) == 0) {
            moved = bulk
                    ? Math.min(slotStack.getCount(), MAX_MODIFIER_COUNT - getGunpowder(cauldron))
                    : Math.min(1, MAX_MODIFIER_COUNT - getGunpowder(cauldron));
            if (moved > 0) {
                setGunpowder(cauldron, getGunpowder(cauldron) + moved);
            }
        } else if (item == Items.DRAGON_BREATH && getGunpowder(cauldron) == 0) {
            moved = bulk
                    ? Math.min(slotStack.getCount(), MAX_MODIFIER_COUNT - getDragonsBreath(cauldron))
                    : Math.min(1, MAX_MODIFIER_COUNT - getDragonsBreath(cauldron));
            if (moved > 0) {
                setDragonsBreath(cauldron, getDragonsBreath(cauldron) + moved);
            }
        }

        if (moved <= 0) {
            return false;
        }

        slotStack.shrink(moved);
        playInsertSound(player);
        return true;
    }

    private static boolean addModifier(ItemStack cauldron, ItemStack stack, boolean gunpowder, Player player) {
        if (gunpowder ? getDragonsBreath(cauldron) > 0 : getGunpowder(cauldron) > 0) {
            return false;
        }

        int current = gunpowder ? getGunpowder(cauldron) : getDragonsBreath(cauldron);
        if (current >= MAX_MODIFIER_COUNT) {
            return false;
        }

        int added = Math.min(stack.getCount(), MAX_MODIFIER_COUNT - current);
        if (gunpowder) {
            setGunpowder(cauldron, current + added);
        } else {
            setDragonsBreath(cauldron, current + added);
        }
        stack.shrink(added);
        playInsertSound(player);
        return true;
    }

    private static boolean removePotion(ItemStack cauldron, Slot slot, Player player) {
        ListTag slots = potionSlots(cauldron);
        if (slots == null || slots.isEmpty()) {
            return false;
        }

        int last = slots.size() - 1;
        ItemStack potion = potionStack(slots.getCompound(last));
        slots.remove(last);
        if (slots.isEmpty()) {
            cauldron.getOrCreateTag().remove(POTIONS_TAG);
        }

        ItemStack remainder = slot.safeInsert(potion);
        if (!remainder.isEmpty()) {
            insertPotion(cauldron, remainder);
        } else {
            playRemoveSound(player);
        }
        return true;
    }

    private static boolean removeModifierStack(ItemStack cauldron, Slot slot, Player player) {
        int gunpowder = getGunpowder(cauldron);
        if (gunpowder > 0) {
            ItemStack remainder = slot.safeInsert(new ItemStack(Items.GUNPOWDER, gunpowder));
            int placed = gunpowder - remainder.getCount();
            if (placed > 0) {
                setGunpowder(cauldron, remainder.getCount());
                playRemoveSound(player);
                return true;
            }
            return false;
        }

        int dragonsBreath = getDragonsBreath(cauldron);
        if (dragonsBreath > 0) {
            ItemStack remainder = slot.safeInsert(new ItemStack(Items.DRAGON_BREATH, dragonsBreath));
            int placed = dragonsBreath - remainder.getCount();
            if (placed > 0) {
                setDragonsBreath(cauldron, remainder.getCount());
                playRemoveSound(player);
                return true;
            }
        }
        return false;
    }

    private static boolean isCtrlDown(Player player) {
        return player.level().isClientSide()
                ? PlayerKeyStateCache.getInventoryClickCtrl(player.getUUID())
                : PlayerKeyStateCache.isCtrlDown(player.getUUID());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return getGunpowder(stack) > 0 || getDragonsBreath(stack) > 0 ? UseAnim.BOW : UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return getGunpowder(stack) > 0 || getDragonsBreath(stack) > 0 ? 72 : 48;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getPotionCount(stack) == 0) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide()
                && getGunpowder(stack) == 0 && getDragonsBreath(stack) == 0) {
            drink(level, player, stack);
            consumeUse(stack);
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || level.isClientSide()) {
            return;
        }

        int charged = getUseDuration(stack) - timeLeft;
        if (charged < 3) {
            return;
        }

        float power = BowItem.getPowerForTime(charged);
        float speed = 0.3F + power * 1.1F;
        int gunpowder = getGunpowder(stack);
        int dragonsBreath = getDragonsBreath(stack);

        if (gunpowder > 0) {
            throwPotion(level, player, stack, false, speed);
            setGunpowder(stack, gunpowder - 1);
            consumeUse(stack);
        } else if (dragonsBreath > 0) {
            throwPotion(level, player, stack, true, speed);
            setDragonsBreath(stack, dragonsBreath - 1);
            consumeUse(stack);
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        List<ItemStack> potions = new ArrayList<>();
        List<Integer> uses = new ArrayList<>();
        ListTag slots = potionSlots(stack);

        for (int i = 0; i < MAX_POTION_SLOTS; i++) {
            if (slots != null && i < slots.size()) {
                CompoundTag slot = slots.getCompound(i);
                potions.add(potionStack(slot));
                uses.add(slot.getInt(USES_TAG));
            } else {
                potions.add(ItemStack.EMPTY);
                uses.add(0);
            }
        }

        return Optional.of(new CauldronTooltip(potions, uses, modifierStack(stack)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ancientcurses.fathomless_cauldron.controls")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static float hasContentsProperty(ItemStack stack) {
        ListTag potions = potionSlots(stack);
        return potions != null && !potions.isEmpty() ? 1.0F : 0.0F;
    }

    public static int getFirstPotionColor(ItemStack stack) {
        ListTag slots = potionSlots(stack);
        if (slots == null || slots.isEmpty()) {
            return 0xFFFFFF;
        }
        List<MobEffectInstance> effects = PotionUtils.getMobEffects(potionStack(slots.getCompound(0)));
        return effects.isEmpty() ? 0x385DC6 : PotionUtils.getColor(effects);
    }

    private static boolean insertPotion(ItemStack cauldron, ItemStack potion) {
        String potionId = getPotionId(potion);
        ListTag customEffects = getCustomEffects(potion);

        if (potionId.equals("minecraft:water") && (customEffects == null || customEffects.isEmpty())) {
            return false;
        }

        ListTag slots = potionSlotsOrCreate(cauldron);
        for (int i = 0; i < slots.size(); i++) {
            CompoundTag slot = slots.getCompound(i);
            if (samePotion(slot, potionId, customEffects)) {
                if (slot.getInt(USES_TAG) >= USES_PER_POTION) {
                    return false;
                }
                slot.putInt(USES_TAG, USES_PER_POTION);
                return true;
            }
        }

        if (slots.size() >= MAX_POTION_SLOTS) {
            return false;
        }

        CompoundTag slot = new CompoundTag();
        slot.putString(POTION_ID_TAG, potionId);
        if (customEffects != null && !customEffects.isEmpty()) {
            slot.put(CUSTOM_EFFECTS_TAG, customEffects);
        }
        slot.putInt(USES_TAG, USES_PER_POTION);
        slots.add(slot);
        cauldron.getOrCreateTag().put(POTIONS_TAG, slots);
        return true;
    }

    private static void drink(Level level, Player player, ItemStack stack) {
        ListTag slots = potionSlots(stack);
        if (slots == null) {
            return;
        }

        for (int i = 0; i < slots.size(); i++) {
            for (MobEffectInstance effect : PotionUtils.getMobEffects(potionStack(slots.getCompound(i)))) {
                if (effect.getEffect().isInstantenous()) {
                    effect.getEffect().applyInstantenousEffect(player, player, player, effect.getAmplifier(), 1.0);
                } else {
                    player.addEffect(new MobEffectInstance(
                            effect.getEffect(), effect.getDuration(), effect.getAmplifier(),
                            effect.isAmbient(), effect.isVisible(), effect.showIcon()));
                }
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F,
                level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    private static void throwPotion(Level level, Player player, ItemStack cauldron, boolean lingering, float speed) {
        ThrownCauldronPotion thrown = ThrownCauldronPotion.create(level, player, cauldron, lingering);
        float pitch = player.getXRot() * ((float) Math.PI / 180F);
        float yaw = player.getYRot() * ((float) Math.PI / 180F);
        thrown.setDeltaMovement(
                -Math.sin(yaw) * Math.cos(pitch) * speed,
                -Math.sin(pitch) * speed,
                Math.cos(yaw) * Math.cos(pitch) * speed);
        level.addFreshEntity(thrown);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    public static List<MobEffectInstance> getAllEffects(ItemStack cauldron) {
        ListTag slots = potionSlots(cauldron);
        if (slots == null) {
            return List.of();
        }

        List<MobEffectInstance> effects = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            effects.addAll(PotionUtils.getMobEffects(potionStack(slots.getCompound(i))));
        }
        return effects;
    }

    private static void consumeUse(ItemStack stack) {
        ListTag slots = potionSlots(stack);
        if (slots == null) {
            return;
        }

        ListTag remaining = new ListTag();
        for (int i = 0; i < slots.size(); i++) {
            CompoundTag slot = slots.getCompound(i).copy();
            int uses = slot.getInt(USES_TAG) - 1;
            if (uses > 0) {
                slot.putInt(USES_TAG, uses);
                remaining.add(slot);
            }
        }

        if (remaining.isEmpty()) {
            stack.getOrCreateTag().remove(POTIONS_TAG);
        } else {
            stack.getOrCreateTag().put(POTIONS_TAG, remaining);
        }
    }

    @Nullable
    public static ListTag potionSlots(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(POTIONS_TAG)) {
            return null;
        }
        return tag.getList(POTIONS_TAG, CompoundTag.TAG_COMPOUND);
    }

    private static ListTag potionSlotsOrCreate(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(POTIONS_TAG)) {
            tag.put(POTIONS_TAG, new ListTag());
        }
        return tag.getList(POTIONS_TAG, CompoundTag.TAG_COMPOUND);
    }

    public static int getPotionCount(ItemStack stack) {
        ListTag slots = potionSlots(stack);
        return slots == null ? 0 : slots.size();
    }

    public static int getGunpowder(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(GUNPOWDER_TAG);
    }

    private static void setGunpowder(ItemStack stack, int amount) {
        if (amount <= 0) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove(GUNPOWDER_TAG);
            }
        } else {
            stack.getOrCreateTag().putInt(GUNPOWDER_TAG, amount);
        }
    }

    public static int getDragonsBreath(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(DRAGONS_BREATH_TAG);
    }

    private static void setDragonsBreath(ItemStack stack, int amount) {
        if (amount <= 0) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove(DRAGONS_BREATH_TAG);
            }
        } else {
            stack.getOrCreateTag().putInt(DRAGONS_BREATH_TAG, amount);
        }
    }

    private static String getPotionId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("Potion") ? tag.getString("Potion") : "minecraft:empty";
    }

    @Nullable
    private static ListTag getCustomEffects(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("CustomPotionEffects")) {
            return null;
        }
        return tag.getList("CustomPotionEffects", CompoundTag.TAG_COMPOUND);
    }

    private static boolean samePotion(CompoundTag slot, String potionId, @Nullable ListTag customEffects) {
        if (!slot.getString(POTION_ID_TAG).equals(potionId)) {
            return false;
        }

        boolean slotHasCustom = slot.contains(CUSTOM_EFFECTS_TAG);
        boolean newHasCustom = customEffects != null && !customEffects.isEmpty();
        if (slotHasCustom != newHasCustom) {
            return false;
        }

        return !slotHasCustom
                || slot.getList(CUSTOM_EFFECTS_TAG, CompoundTag.TAG_COMPOUND).equals(customEffects);
    }

    private static ItemStack potionStack(CompoundTag slot) {
        ItemStack stack = new ItemStack(Items.POTION);
        CompoundTag tag = new CompoundTag();
        tag.putString("Potion", slot.getString(POTION_ID_TAG));
        if (slot.contains(CUSTOM_EFFECTS_TAG)) {
            tag.put("CustomPotionEffects", slot.getList(CUSTOM_EFFECTS_TAG, CompoundTag.TAG_COMPOUND));
        }
        stack.setTag(tag);
        return stack;
    }

    private static ItemStack modifierStack(ItemStack stack) {
        int gunpowder = getGunpowder(stack);
        if (gunpowder > 0) {
            return new ItemStack(Items.GUNPOWDER, gunpowder);
        }

        int dragonsBreath = getDragonsBreath(stack);
        return dragonsBreath > 0 ? new ItemStack(Items.DRAGON_BREATH, dragonsBreath) : ItemStack.EMPTY;
    }

    private static void playInsertSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F,
                0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playRemoveSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F,
                0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }
}
