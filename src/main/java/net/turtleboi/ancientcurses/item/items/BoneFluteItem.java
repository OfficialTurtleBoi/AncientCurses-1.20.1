package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.turtleboi.turtlecore.capabilities.party.PlayerPartyProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BoneFluteItem extends Item {
    private static final String CHARM_OWNER_TAG = "AncientCursesBoneFluteOwner";
    private static final String CHARM_END_TICK_TAG = "AncientCursesBoneFluteEndTick";
    private static final double CHARM_RADIUS = 12.0D;
    private static final int CHARM_DURATION_TICKS = 200;
    private static final int COOLDOWN_TICKS = 900;

    public BoneFluteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        int charmedCount = charmNearbyUndead((ServerLevel) level, player);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_FLUTE.get(), SoundSource.PLAYERS, 1.0F, 0.55F);

        if (charmedCount == 0) {
            player.displayClientMessage(Component.translatable("item.ancientcurses.bone_flute.no_charm")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ancientcurses.bone_flute.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    public static void tickCharmedMob(Mob mob) {
        CompoundTag tag = mob.getPersistentData();
        if (!tag.hasUUID(CHARM_OWNER_TAG)) {
            return;
        }

        long endTick = tag.getLong(CHARM_END_TICK_TAG);
        if (mob.level().getGameTime() >= endTick) {
            clearCharm(mob);
            return;
        }

        Player owner = mob.level().getPlayerByUUID(tag.getUUID(CHARM_OWNER_TAG));
        if (owner == null || owner.isDeadOrDying()) {
            clearCharm(mob);
            return;
        }

        if (mob.getTarget() == owner) {
            mob.setTarget(null);
        }

        if (mob.getTarget() == null || mob.getTarget().isDeadOrDying() || isCharmedBy(mob.getTarget(), owner)) {
            findTargetForCharmedMob(mob, owner).ifPresent(mob::setTarget);
        }
    }

    public static boolean isCharmedBy(LivingEntity entity, Player player) {
        CompoundTag tag = entity.getPersistentData();
        return tag.hasUUID(CHARM_OWNER_TAG) && tag.getUUID(CHARM_OWNER_TAG).equals(player.getUUID());
    }

    public static void clearCharm(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        UUID ownerUUID = tag.hasUUID(CHARM_OWNER_TAG) ? tag.getUUID(CHARM_OWNER_TAG) : null;
        if (ownerUUID != null) {
            removeMobFromOwnerParty(entity, ownerUUID);
        }

        tag.remove(CHARM_OWNER_TAG);
        tag.remove(CHARM_END_TICK_TAG);
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

    private static int charmNearbyUndead(ServerLevel level, Player player) {
        AABB area = player.getBoundingBox().inflate(CHARM_RADIUS);
        List<Mob> nearbyUndead = level.getEntitiesOfClass(Mob.class, area, BoneFluteItem::canCharm);
        int charmedCount = 0;

        for (Mob mob : nearbyUndead) {
            float charmChance = mob instanceof AbstractSkeleton ? 0.75F : 0.5F;
            if (level.random.nextFloat() >= charmChance) {
                continue;
            }

            CompoundTag tag = mob.getPersistentData();
            tag.putUUID(CHARM_OWNER_TAG, player.getUUID());
            tag.putLong(CHARM_END_TICK_TAG, level.getGameTime() + CHARM_DURATION_TICKS);
            addMobToPlayerParty(player, mob);
            mob.setTarget(null);
            level.sendParticles(ParticleTypes.NOTE,
                    mob.getX(), mob.getY() + mob.getBbHeight() + 0.25D, mob.getZ(),
                    4, 0.25D, 0.2D, 0.25D, 0.0D);
            charmedCount++;
        }

        return charmedCount;
    }

    private static boolean canCharm(Mob mob) {
        return mob.isAlive()
                && mob.getMobType() == MobType.UNDEAD
                && !(mob instanceof WitherSkeleton)
                && !(mob instanceof WitherBoss);
    }

    private static Optional<LivingEntity> findTargetForCharmedMob(Mob mob, Player owner) {
        AABB area = mob.getBoundingBox().inflate(16.0D);
        return mob.level().getEntitiesOfClass(Monster.class, area,
                        target -> target != mob
                                && target.isAlive()
                                && !isCharmedBy(target, owner)
                                && target.hasLineOfSight(mob))
                .stream()
                .min(Comparator.comparingDouble(mob::distanceToSqr))
                .map(target -> target);
    }

    private static void addMobToPlayerParty(Player player, Mob mob) {
        player.getCapability(PlayerPartyProvider.PLAYER_PARTY)
                .ifPresent(party -> party.addMob(mob.getUUID()));
    }

    private static void removeMobFromOwnerParty(LivingEntity entity, UUID ownerUUID) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner == null) {
            return;
        }

        owner.getCapability(PlayerPartyProvider.PLAYER_PARTY)
                .ifPresent(party -> party.removeMob(entity.getUUID()));
    }
}
