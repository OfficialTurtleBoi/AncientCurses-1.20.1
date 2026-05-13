package net.turtleboi.ancientcurses.item.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.entity.entities.items.ThrownCauldronPotion;
import net.turtleboi.ancientcurses.item.tooltip.CauldronTooltip;
import net.turtleboi.ancientcurses.network.PlayerKeyStateCache;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FathomlessCauldronItem extends Item {
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

        Item otherItem = other.getItem();

        if (otherItem == Items.GUNPOWDER) {
            if (getDragonsBreath(cauldron) > 0) {
                return false;
            }
            int current = getGunpowder(cauldron);
            if (current >= MAX_MODIFIER_COUNT) {
                return false;
            }
            int amountToAdd = Math.min(other.getCount(), MAX_MODIFIER_COUNT - current);
            setGunpowder(cauldron, current + amountToAdd);
            other.shrink(amountToAdd);
            playInsertSound(player);
            return true;
        }

        if (otherItem == Items.DRAGON_BREATH) {
            if (getGunpowder(cauldron) > 0) {
                return false;
            }
            int current = getDragonsBreath(cauldron);
            if (current >= MAX_MODIFIER_COUNT) {
                return false;
            }
            int amountToAdd = Math.min(other.getCount(), MAX_MODIFIER_COUNT - current);
            setDragonsBreath(cauldron, current + amountToAdd);
            other.shrink(amountToAdd);
            playInsertSound(player);
            return true;
        }

        if (otherItem instanceof PotionItem) {
            if (tryInsertPotion(cauldron, other)) {
                other.shrink(1);
                playInsertSound(player);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack cauldron, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        ItemStack slotStack = slot.getItem();

        if (!slotStack.isEmpty()) {
            Item slotItem = slotStack.getItem();
            boolean inserted = false;
            if (slotItem instanceof PotionItem) {
                inserted = tryInsertPotion(cauldron, slotStack.copyWithCount(1));
            } else if (slotItem == Items.GUNPOWDER && getDragonsBreath(cauldron) == 0) {
                int current = getGunpowder(cauldron);
                if (current < MAX_MODIFIER_COUNT) {
                    setGunpowder(cauldron, current + 1);
                    inserted = true;
                }
            } else if (slotItem == Items.DRAGON_BREATH && getGunpowder(cauldron) == 0) {
                int current = getDragonsBreath(cauldron);
                if (current < MAX_MODIFIER_COUNT) {
                    setDragonsBreath(cauldron, current + 1);
                    inserted = true;
                }
            }
            if (inserted) {
                slotStack.shrink(1);
                playInsertSound(player);
                return true;
            }
            return false;
        }

        boolean ctrlDown = PlayerKeyStateCache.isCtrlDown(player.getUUID());
        if (ctrlDown && player.isShiftKeyDown()) {
            return extractAllModifierIntoSlot(cauldron, slot, player);
        }
        if (ctrlDown) {
            return extractModifierIntoSlot(cauldron, slot, player);
        }

        return extractPotionIntoSlot(cauldron, slot, player);
    }

    private static boolean extractPotionIntoSlot(ItemStack cauldron, Slot slot, Player player) {
        ListTag potionSlots = getPotionsList(cauldron);
        if (potionSlots == null || potionSlots.isEmpty()) {
            return false;
        }
        int lastIndex = potionSlots.size() - 1;
        ItemStack potionStack = buildPotionStack(potionSlots.getCompound(lastIndex));
        potionSlots.remove(lastIndex);
        if (potionSlots.isEmpty()) {
            cauldron.getOrCreateTag().remove(POTIONS_TAG);
        }
        ItemStack remainder = slot.safeInsert(potionStack);
        if (!remainder.isEmpty()) {
            tryInsertPotion(cauldron, remainder);
        } else {
            playRemoveSound(player);
        }
        return true;
    }

    private static boolean extractModifierIntoSlot(ItemStack cauldron, Slot slot, Player player) {
        int gunpowder = getGunpowder(cauldron);
        if (gunpowder > 0) {
            ItemStack remainder = slot.safeInsert(new ItemStack(Items.GUNPOWDER, 1));
            if (remainder.isEmpty()) {
                setGunpowder(cauldron, gunpowder - 1);
                playRemoveSound(player);
                return true;
            }
            return false;
        }
        int dragonsBreath = getDragonsBreath(cauldron);
        if (dragonsBreath > 0) {
            ItemStack remainder = slot.safeInsert(new ItemStack(Items.DRAGON_BREATH, 1));
            if (remainder.isEmpty()) {
                setDragonsBreath(cauldron, dragonsBreath - 1);
                playRemoveSound(player);
                return true;
            }
            return false;
        }
        return false;
    }

    private static boolean extractAllModifierIntoSlot(ItemStack cauldron, Slot slot, Player player) {
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
            return false;
        }
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (getGunpowder(stack) > 0 || getDragonsBreath(stack) > 0) {
            return UseAnim.BOW;
        }
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if (getGunpowder(stack) > 0 || getDragonsBreath(stack) > 0) {
            return 72;
        }
        return 48;
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
        if (entity instanceof Player player && !level.isClientSide()) {
            if (getGunpowder(stack) == 0 && getDragonsBreath(stack) == 0) {
                drinkAll(level, player, stack);
                consumeOneUseFromAll(stack);
            }
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || level.isClientSide()) {
            return;
        }

        int ticksCharged = getUseDuration(stack) - timeLeft;
        if (ticksCharged < 3) {
            return;
        }

        float chargedPower = BowItem.getPowerForTime(ticksCharged);
        float launchSpeed = 0.3F + chargedPower * 1.1F;

        int gunpowder = getGunpowder(stack);
        int dragonsBreath = getDragonsBreath(stack);

        if (gunpowder > 0) {
            throwCauldronPotion(level, player, stack, false, launchSpeed);
            setGunpowder(stack, gunpowder - 1);
            consumeOneUseFromAll(stack);
        } else if (dragonsBreath > 0) {
            throwCauldronPotion(level, player, stack, true, launchSpeed);
            setDragonsBreath(stack, dragonsBreath - 1);
            consumeOneUseFromAll(stack);
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        List<ItemStack> potionStacks = new ArrayList<>();
        List<Integer> potionUses = new ArrayList<>();

        ListTag slots = getPotionsList(stack);
        for (int slotIndex = 0; slotIndex < MAX_POTION_SLOTS; slotIndex++) {
            if (slots != null && slotIndex < slots.size()) {
                CompoundTag slotTag = slots.getCompound(slotIndex);
                potionStacks.add(buildPotionStack(slotTag));
                potionUses.add(slotTag.getInt(USES_TAG));
            } else {
                potionStacks.add(ItemStack.EMPTY);
                potionUses.add(0);
            }
        }

        int gunpowder = getGunpowder(stack);
        int dragonsBreath = getDragonsBreath(stack);
        ItemStack modifierStack;
        if (gunpowder > 0) {
            modifierStack = new ItemStack(Items.GUNPOWDER, gunpowder);
        } else if (dragonsBreath > 0) {
            modifierStack = new ItemStack(Items.DRAGON_BREATH, dragonsBreath);
        } else {
            modifierStack = ItemStack.EMPTY;
        }

        return Optional.of(new CauldronTooltip(potionStacks, potionUses, modifierStack));
    }

    public static float hasContentsProperty(ItemStack stack) {
        ListTag potions = getPotionsList(stack);
        return (potions != null && !potions.isEmpty()) ? 1.0F : 0.0F;
    }

    public static int getFirstPotionColor(ItemStack stack) {
        ListTag potions = getPotionsList(stack);
        if (potions == null || potions.isEmpty()) {
            return 0xFFFFFF;
        }
        List<MobEffectInstance> effects = PotionUtils.getMobEffects(buildPotionStack(potions.getCompound(0)));
        return effects.isEmpty() ? 0x385DC6 : PotionUtils.getColor(effects);
    }

    private static boolean tryInsertPotion(ItemStack cauldron, ItemStack potionStack) {
        String potionId = getPotionIdFromStack(potionStack);
        ListTag customEffects = getCustomEffectsFromStack(potionStack);

        if (potionId.equals("minecraft:water") && (customEffects == null || customEffects.isEmpty())) {
            return false;
        }

        ListTag slots = getOrCreatePotionsList(cauldron);

        for (int i = 0; i < slots.size(); i++) {
            CompoundTag slot = slots.getCompound(i);
            if (slotsMatch(slot, potionId, customEffects)) {
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

        CompoundTag newSlot = new CompoundTag();
        newSlot.putString(POTION_ID_TAG, potionId);
        if (customEffects != null && !customEffects.isEmpty()) {
            newSlot.put(CUSTOM_EFFECTS_TAG, customEffects);
        }
        newSlot.putInt(USES_TAG, USES_PER_POTION);
        slots.add(newSlot);
        cauldron.getOrCreateTag().put(POTIONS_TAG, slots);
        return true;
    }

    private static void drinkAll(Level level, Player player, ItemStack stack) {
        ListTag slots = getPotionsList(stack);
        if (slots == null) {
            return;
        }
        for (int i = 0; i < slots.size(); i++) {
            for (MobEffectInstance effect : PotionUtils.getMobEffects(buildPotionStack(slots.getCompound(i)))) {
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

    private static void throwCauldronPotion(Level level, Player player, ItemStack cauldron,
                                             boolean lingering, float launchSpeed) {
        ThrownCauldronPotion thrown = ThrownCauldronPotion.create(level, player, cauldron, lingering);
        float pitchRadians = (player.getXRot()) * ((float) Math.PI / 180F);
        float yawRadians   = player.getYRot()          * ((float) Math.PI / 180F);
        thrown.setDeltaMovement(
                -Math.sin(yawRadians) * Math.cos(pitchRadians) * launchSpeed,
                -Math.sin(pitchRadians) * launchSpeed,
                 Math.cos(yawRadians)  * Math.cos(pitchRadians) * launchSpeed);
        level.addFreshEntity(thrown);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    public static List<MobEffectInstance> getAllEffects(ItemStack cauldronStack) {
        ListTag slots = getPotionsList(cauldronStack);
        if (slots == null) {
            return List.of();
        }
        List<MobEffectInstance> allEffects = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            allEffects.addAll(PotionUtils.getMobEffects(buildPotionStack(slots.getCompound(i))));
        }
        return allEffects;
    }

    private static void consumeOneUseFromAll(ItemStack stack) {
        ListTag slots = getPotionsList(stack);
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
    public static ListTag getPotionsList(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(POTIONS_TAG)) {
            return null;
        }
        return tag.getList(POTIONS_TAG, CompoundTag.TAG_COMPOUND);
    }

    private static ListTag getOrCreatePotionsList(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(POTIONS_TAG)) {
            tag.put(POTIONS_TAG, new ListTag());
        }
        return tag.getList(POTIONS_TAG, CompoundTag.TAG_COMPOUND);
    }

    public static int getPotionCount(ItemStack stack) {
        ListTag list = getPotionsList(stack);
        return list == null ? 0 : list.size();
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

    private static String getPotionIdFromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains("Potion")) ? tag.getString("Potion") : "minecraft:empty";
    }

    @Nullable
    private static ListTag getCustomEffectsFromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("CustomPotionEffects")) {
            return null;
        }
        return tag.getList("CustomPotionEffects", CompoundTag.TAG_COMPOUND);
    }

    private static boolean slotsMatch(CompoundTag slot, String potionId, @Nullable ListTag customEffects) {
        if (!slot.getString(POTION_ID_TAG).equals(potionId)) {
            return false;
        }
        boolean slotHasCustom = slot.contains(CUSTOM_EFFECTS_TAG);
        boolean newHasCustom = customEffects != null && !customEffects.isEmpty();
        if (slotHasCustom != newHasCustom) {
            return false;
        }
        if (slotHasCustom) {
            return slot.getList(CUSTOM_EFFECTS_TAG, CompoundTag.TAG_COMPOUND).equals(customEffects);
        }
        return true;
    }

    private static ItemStack buildPotionStack(CompoundTag slot) {
        ItemStack fakePotion = new ItemStack(Items.POTION);
        CompoundTag tag = new CompoundTag();
        tag.putString("Potion", slot.getString(POTION_ID_TAG));
        if (slot.contains(CUSTOM_EFFECTS_TAG)) {
            tag.put("CustomPotionEffects", slot.getList(CUSTOM_EFFECTS_TAG, CompoundTag.TAG_COMPOUND));
        }
        fakePotion.setTag(tag);
        return fakePotion;
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
