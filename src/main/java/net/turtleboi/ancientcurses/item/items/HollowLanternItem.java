package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HollowLanternItem extends ArtifactItem {
    private static final double REVEAL_RADIUS = 20.0D;
    private static final int GLOWING_DURATION_TICKS = 100;
    private static final int COOLDOWN_TICKS = 600;
    private static final int MAX_SOLID_BLOCKS_PENETRATED = 4;
    private static final int PASSIVE_PULSE_INTERVAL_TICKS = 40;

    public HollowLanternItem(Properties properties) {
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

        int revealedCount = revealNearbyEntities((ServerLevel) level, player);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.7F, 0.45F);

        if (revealedCount == 0) {
            player.displayClientMessage(Component.translatable("item.ancientcurses.hollow_lantern.no_entities")
                    .withStyle(ChatFormatting.DARK_GRAY), true);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) {
            return;
        }

        boolean held = isSelected || player.getOffhandItem() == stack;
        if (!held || player.tickCount % PASSIVE_PULSE_INTERVAL_TICKS != 0) {
            return;
        }

        if (hasHiddenEntityNearby(level, player)) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    6, 0.35D, 0.45D, 0.35D, 0.01D);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.25F, 0.55F);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ancientcurses.hollow_lantern.tooltip")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static int revealNearbyEntities(ServerLevel level, Player player) {
        int revealedCount = 0;
        AABB area = player.getBoundingBox().inflate(REVEAL_RADIUS);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive());

        for (LivingEntity entity : nearbyEntities) {
            if (!canRevealThroughBlocks(level, player.getEyePosition(), entity.getEyePosition())) {
                continue;
            }

            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOWING_DURATION_TICKS, 0, false, false));
            level.sendParticles(ParticleTypes.END_ROD,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    8, entity.getBbWidth() * 0.35D, entity.getBbHeight() * 0.35D, entity.getBbWidth() * 0.35D, 0.02D);
            revealedCount++;
        }

        return revealedCount;
    }

    private static boolean hasHiddenEntityNearby(Level level, Player player) {
        AABB area = player.getBoundingBox().inflate(REVEAL_RADIUS);
        return !level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive() && (entity.isInvisible() || entity.isShiftKeyDown())).isEmpty();
    }

    private static boolean canRevealThroughBlocks(Level level, Vec3 start, Vec3 end) {
        Vec3 path = end.subtract(start);
        int steps = Math.max(1, (int) Math.ceil(path.length() * 2.0D));
        BlockPos lastPos = null;
        int solidBlocks = 0;

        for (int i = 1; i < steps; i++) {
            Vec3 sample = start.add(path.scale(i / (double) steps));
            BlockPos pos = BlockPos.containing(sample);
            if (pos.equals(lastPos)) {
                continue;
            }

            lastPos = pos;
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !state.isSolidRender(level, pos)) {
                continue;
            }

            solidBlocks++;
            if (solidBlocks > MAX_SOLID_BLOCKS_PENETRATED) {
                return false;
            }
        }

        return true;
    }
}
