package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoulShardItem extends Item {
    public static final String SOUL_ENERGY_TAG = "SoulEnergy";
    public static final int MAX_SOUL_ENERGY = 200;

    public SoulShardItem(Properties properties) {
        super(properties);
    }

    public static int getSoulEnergy(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.min(MAX_SOUL_ENERGY, tag.getInt(SOUL_ENERGY_TAG));
    }

    public static boolean isCharged(ItemStack stack) {
        return getSoulEnergy(stack) >= MAX_SOUL_ENERGY;
    }

    public static void addSoulEnergy(ItemStack stack, int amount) {
        if (amount <= 0) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int nextValue = Math.min(MAX_SOUL_ENERGY, tag.getInt(SOUL_ENERGY_TAG) + amount);
        tag.putInt(SOUL_ENERGY_TAG, nextValue);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getSoulEnergy(stack) > 0 && !isCharged(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getSoulEnergy(stack) / (float) MAX_SOUL_ENERGY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x7A35D4;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isCharged(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level level, List<Component> tooltip, TooltipFlag flag) {
        int energy = getSoulEnergy(stack);
        tooltip.add(Component.translatable("item.ancientcurses.soul_shard.energy", energy, MAX_SOUL_ENERGY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(isCharged(stack)
                ? "item.ancientcurses.soul_shard.charged"
                : "item.ancientcurses.soul_shard.uncharged").withStyle(ChatFormatting.DARK_AQUA));
    }
}
