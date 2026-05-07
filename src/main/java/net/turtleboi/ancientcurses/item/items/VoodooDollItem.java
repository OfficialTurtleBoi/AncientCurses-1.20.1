package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.entity.entities.VoodooSoulEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoodooDollItem extends Item {
    private static final double TARGET_RANGE = 25.0D;
    private static final int COOLDOWN_TICKS = 20 * 60;
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

        LivingEntity target = findTarget(level, player);
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

        VoodooSoulEntity soul = ModEntities.VOODOO_SOUL.get().create(level);
        if (soul == null) {
            return false;
        }

        float soulHealth = Math.max(1.0F, target.getMaxHealth() * SOUL_HEALTH_SCALE);
        soul.setPos(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
        soul.setOwner(player);
        soul.setOriginalBody(target);
        soul.setSoulHealth(soulHealth);
        level.addFreshEntity(soul);
        target.getPersistentData().putUUID(VoodooSoulEntity.ACTIVE_SOUL_TAG, soul.getUUID());

        level.playSound(null, target.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.0F, 0.7F);
        level.playSound(null, player.blockPosition(), SoundEvents.WOOL_HIT, SoundSource.PLAYERS, 0.8F, 0.6F);
        return true;
    }

    @Nullable
    private static LivingEntity findTarget(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(TARGET_RANGE));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(TARGET_RANGE)).inflate(1.0D);
        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                level,
                player,
                eye,
                end,
                searchBox,
                entity -> isValidTarget(player, entity));
        if (hitResult != null && hitResult.getEntity() instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }

    private static boolean isValidTarget(Player player, Entity entity) {
        return entity instanceof Mob mob
                && mob.isAlive()
                //&& mob != player
                && !mob.isSpectator()
                && player.hasLineOfSight(mob);
    }
}
