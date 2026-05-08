package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.entity.entities.VoodooSoulEntity;
import net.turtleboi.turtlecore.util.TargetingUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoodooDollItem extends Item {
    private static final int COOLDOWN_TICKS = 1 * 60;
    private static final float SOUL_HEALTH_SCALE = 0.4F;

    public VoodooDollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        LivingEntity target = findTarget(player);
        if (target == null) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (!extractSoul((ServerLevel) level, serverPlayer, target)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.getCooldowns().isOnCooldown(this) || !(target instanceof Mob)) {
            return InteractionResult.FAIL;
        }

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (!extractSoul((ServerLevel) player.level(), serverPlayer, target)) {
                return InteractionResult.FAIL;
            }
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ancientcurses.voodoo_doll.tooltip")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    private static boolean extractSoul(ServerLevel level, ServerPlayer player, LivingEntity target) {
        if (!(target instanceof Mob) || target.isDeadOrDying()) {
            return false;
        }

        if (target.getPersistentData().hasUUID(VoodooSoulEntity.ACTIVE_SOUL_TAG)) {
            return false;
        }

        Entity soulEntity = target.getType().create(level);
        if (!(soulEntity instanceof Mob soul)) {
            return false;
        }

        copyBodyData(target, soul);
        float soulHealth = Math.max(1.0F, target.getMaxHealth() * SOUL_HEALTH_SCALE);
        VoodooSoulEntity.markSoulClone(soul, player, target);
        VoodooSoulEntity.setSoulHealth(soul, soulHealth);
        soul.setPos(target.getX(), target.getY(), target.getZ());
        soul.xo = soul.getX();
        soul.yo = soul.getY();
        soul.zo = soul.getZ();
        soul.setYRot(target.getYRot());
        soul.setXRot(target.getXRot());
        soul.yRotO = target.yRotO;
        soul.xRotO = target.xRotO;
        soul.setDeltaMovement(Vec3.ZERO);
        soul.setNoGravity(true);
        soul.setNoAi(true);
        soul.setTarget(player);
        clearArmorEquipment(soul);
        level.addFreshEntity(soul);
        VoodooSoulEntity.beginSoulTravel(soul, player);
        target.getPersistentData().putUUID(VoodooSoulEntity.ACTIVE_SOUL_TAG, soul.getUUID());
        VoodooSoulEntity.syncSoulCloneToClients(soul, true);

        level.playSound(null, target.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.0F, 0.7F);
        level.playSound(null, player.blockPosition(), SoundEvents.WOOL_HIT, SoundSource.PLAYERS, 0.8F, 0.6F);
        return true;
    }

    @Nullable
    private static LivingEntity findTarget(Player player) {
        LivingEntity target = TargetingUtils.getTarget(player);
        return isValidTarget(target) ? target : null;
    }

    private static boolean isValidTarget(@Nullable LivingEntity entity) {
        return entity instanceof Mob mob && mob.isAlive() && !mob.isSpectator();
    }

    private static void copyBodyData(LivingEntity body, Mob soul) {
        CompoundTag bodyTag = new CompoundTag();
        body.saveWithoutId(bodyTag);
        scrubCopiedSoulData(bodyTag);
        soul.load(bodyTag);
    }

    private static void scrubCopiedSoulData(CompoundTag bodyTag) {
        bodyTag.remove("UUID");
        bodyTag.remove("Pos");
        bodyTag.remove("Motion");
        bodyTag.remove("Rotation");
        bodyTag.remove("Health");
        bodyTag.remove("AbsorptionAmount");
        bodyTag.remove("active_effects");
        bodyTag.remove("ActiveEffects");
        bodyTag.remove("Attributes");
        bodyTag.remove("DeathTime");
        bodyTag.remove("HurtTime");
        bodyTag.remove("HurtByTimestamp");
        bodyTag.remove("FallDistance");
        bodyTag.remove("Fire");
        bodyTag.remove("ForgeCaps");
        bodyTag.remove("ForgeData");
        bodyTag.remove("ForcedAge");
        bodyTag.remove("HurtBy");
        bodyTag.remove("Leash");
        bodyTag.remove("NoAI");
        bodyTag.remove("NoGravity");
        bodyTag.remove("OnGround");
        bodyTag.remove("Passengers");
    }

    private static void clearArmorEquipment(Mob soul) {
        soul.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        soul.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        soul.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        soul.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        soul.setDropChance(EquipmentSlot.HEAD, 0.0F);
        soul.setDropChance(EquipmentSlot.CHEST, 0.0F);
        soul.setDropChance(EquipmentSlot.LEGS, 0.0F);
        soul.setDropChance(EquipmentSlot.FEET, 0.0F);
    }
}
