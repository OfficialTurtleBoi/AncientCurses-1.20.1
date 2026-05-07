package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.entity.entities.PlagueIdolEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlagueIdolItem extends Item {
    private static final int PLACE_COOLDOWN_TICKS = 20;
    private static final float MAX_DEPLOYED_HEALTH = 40.0F;

    public PlagueIdolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
        if (!level.getBlockState(placePos).getCollisionShape(level, placePos).isEmpty()) {
            return InteractionResult.PASS;
        }

        if (context.getPlayer() != null && context.getPlayer().getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }

        ItemStack usedStack = context.getItemInHand();
        int remainingDurability = usedStack.getMaxDamage() - usedStack.getDamageValue();
        if (remainingDurability <= 0) {
            return InteractionResult.FAIL;
        }

        AABB bounds = new AABB(placePos).deflate(0.1D);
        if (!level.noCollision(bounds)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            PlagueIdolEntity idol = new PlagueIdolEntity(ModEntities.PLAGUE_IDOL.get(), level);
            idol.setPos(placePos.getX() + 0.5D, placePos.getY(), placePos.getZ() + 0.5D);
            idol.setItemDamage(usedStack.getDamageValue());
            idol.setHealth(Math.min(MAX_DEPLOYED_HEALTH, remainingDurability));
            if (context.getPlayer() != null) {
                double xToPlayer = context.getPlayer().getX() - idol.getX();
                double zToPlayer = context.getPlayer().getZ() - idol.getZ();
                float yaw = (float) (Mth.atan2(zToPlayer, xToPlayer) * Mth.RAD_TO_DEG) - 90.0F;
                idol.setYRot(yaw);
                idol.yRotO = yaw;
                idol.setYHeadRot(yaw);
                idol.yHeadRotO = yaw;
                idol.setYBodyRot(yaw);
                idol.yBodyRotO = yaw;
                idol.setOwnerUUID(context.getPlayer().getUUID());
            }
            level.addFreshEntity(idol);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                usedStack.shrink(1);
            }
            level.playSound(null, placePos, SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 0.8F, 0.75F);

            if (context.getPlayer() != null) {
                context.getPlayer().getCooldowns().addCooldown(this, PLACE_COOLDOWN_TICKS);
                context.getPlayer().awardStat(Stats.ITEM_USED.get(this));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ancientcurses.plague_idol.tooltip")
                .withStyle(ChatFormatting.DARK_GREEN));
    }
}
