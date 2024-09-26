package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class GoldenAmuletItem extends Item {

    public GoldenAmuletItem(Properties pProperties) {
        super(pProperties);
    }

    private void initializeAmuletNBT(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putBoolean("Socketable", true);
        nbt.putInt("SocketCount", 6);

        ListTag socketsList = new ListTag();
        for (int i = 0; i < 6; i++) {
            CompoundTag socketTag = new CompoundTag();
            socketTag.putInt("SlotIndex", i);
            socketTag.putString("SocketType", i == 0 ? "Main" : "Minor");
            socketsList.add(socketTag);
        }
        nbt.put("Sockets", socketsList);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        initializeAmuletNBT(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide && stack.hasTag()) {
            CompoundTag nbt = stack.getTag();
            if (!nbt.contains("Socketable")) {
                initializeAmuletNBT(stack);
            }
        }
    }


    public static UUID getOrCreateUUID(ItemStack amulet) {
        CompoundTag amuletTag = amulet.getOrCreateTag();
        if (!amuletTag.hasUUID("AmuletUUID")) {
            UUID amuletUUID = UUID.randomUUID();
            amuletTag.putUUID("AmuletUUID", amuletUUID);
        }
        return amuletTag.getUUID("AmuletUUID");
    }

    public static UUID getUUID(ItemStack amulet) {
        CompoundTag amuletTag = amulet.getTag();
        return amuletTag != null && amuletTag.hasUUID("AmuletUUID") ? amuletTag.getUUID("AmuletUUID") : null;
    }

    //@Override
    //public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    //    super.appendHoverText(stack, level, tooltip, flag);
    //    CompoundTag amuletTag = stack.getTag();
    //    if (amuletTag != null) {
    //        if (amuletTag.contains("MainGem")) {
    //            ItemStack mainGemStack = ItemStack.of(amuletTag.getCompound("MainGem"));
    //            tooltip.add(Component.literal("Main Gem: " + mainGemStack.getHoverName().getString()).withStyle(ChatFormatting.GOLD));
    //        } else {
    //            tooltip.add(Component.literal("Main Gem: None").withStyle(ChatFormatting.GRAY));
    //        }
//
    //        if (amuletTag.contains("MinorGems")) {
    //            ListTag minorGems = amuletTag.getList("MinorGems", CompoundTag.TAG_COMPOUND);
    //            if (!minorGems.isEmpty()) {
    //                tooltip.add(Component.literal("Minor Gems:").withStyle(ChatFormatting.GREEN));
    //                for (int i = 0; i < minorGems.size(); i++) {
    //                    ItemStack minorGemStack = ItemStack.of(minorGems.getCompound(i));
    //                    tooltip.add(Component.literal("  - " + minorGemStack.getHoverName().getString()).withStyle(ChatFormatting.YELLOW));
    //                }
    //            } else {
    //                tooltip.add(Component.literal("Minor Gems: None").withStyle(ChatFormatting.GRAY));
    //            }
    //        } else {
    //            tooltip.add(Component.literal("Minor Gems: None").withStyle(ChatFormatting.GRAY));
    //        }
    //    } else {
    //        tooltip.add(Component.literal("Main Gem: None").withStyle(ChatFormatting.GRAY));
    //        tooltip.add(Component.literal("Minor Gems: None").withStyle(ChatFormatting.GRAY));
    //    }
    //}

    public void applyGemBonuses(Player player, ItemStack amulet) {
        if (!amulet.hasTag()) {
            return;
        }

        CompoundTag amuletTag = amulet.getTag();
        if (amuletTag == null) {
            return;
        }

        if (amuletTag.contains("MainGem")) {
            CompoundTag mainGemTag = amuletTag.getCompound("MainGem");
            ItemStack mainGem = ItemStack.of(mainGemTag);
            if (mainGem.getItem() instanceof PreciousGemItem preciousGem) {
                preciousGem.applyMajorBonus(player, 0);
            }
        }

        if (amuletTag.contains("MinorGems")) {
            ListTag minorGemsTag = amuletTag.getList("MinorGems", 10);
            for (int i = 0; i < minorGemsTag.size(); i++) {
                CompoundTag minorGemTag = minorGemsTag.getCompound(i);
                ItemStack minorGem = ItemStack.of(minorGemTag);
                if (minorGem.getItem() instanceof PreciousGemItem preciousGem) {
                    preciousGem.applyMinorBonus(player, i + 1);
                }
            }
        }
    }
}

