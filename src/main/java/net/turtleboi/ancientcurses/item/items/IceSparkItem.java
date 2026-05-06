package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.turtleboi.ancientcurses.entity.entities.items.ThrownIceSpark;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class IceSparkItem extends Item {
    private static final String DEPLOYED_UUID_TAG = "DeployedIceSpark";
    private static final int THROW_COOLDOWN_TICKS = 20;
    private static final int RECALL_COOLDOWN_TICKS = 10;

    public IceSparkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && recallDeployedSpark(stack, player)) {
            player.getCooldowns().addCooldown(this, RECALL_COOLDOWN_TICKS);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(stack);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide()) {
            ThrownIceSpark iceSpark = new ThrownIceSpark(level, player);
            iceSpark.setItem(stack.copy());
            iceSpark.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.35F, 0.75F);
            level.addFreshEntity(iceSpark);
            stack.getOrCreateTag().putUUID(DEPLOYED_UUID_TAG, iceSpark.getUUID());
        }

        player.getCooldowns().addCooldown(this, THROW_COOLDOWN_TICKS);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(DEPLOYED_UUID_TAG)) {
            tooltip.add(Component.translatable("item.ancientcurses.ice_spark.deployed")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("item.ancientcurses.ice_spark.tooltip")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    private static boolean recallDeployedSpark(ItemStack stack, Player player) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID(DEPLOYED_UUID_TAG)) {
            return false;
        }

        UUID deployedUuid = tag.getUUID(DEPLOYED_UUID_TAG);
        boolean recalled = false;
        if (player.getServer() != null) {
            for (ServerLevel level : player.getServer().getAllLevels()) {
                Entity entity = level.getEntity(deployedUuid);
                if (entity instanceof ThrownIceSpark iceSpark) {
                    level.playSound(null, iceSpark.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.4F);
                    iceSpark.discard();
                    recalled = true;
                    break;
                }
            }
        }

        tag.remove(DEPLOYED_UUID_TAG);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }

        return recalled;
    }
}
