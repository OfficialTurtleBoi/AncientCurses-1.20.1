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
        if (!nbt.contains("Socketable")) {
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

        getOrCreateUUID(stack);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        initializeAmuletNBT(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide) {
            CompoundTag nbt = stack.getOrCreateTag();
            if (!nbt.contains("Socketable") || !nbt.contains("SocketCount")) {
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

