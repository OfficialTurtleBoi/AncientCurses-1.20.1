package net.turtleboi.ancientcurses.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.turtleboi.ancientcurses.api.ISlotAccessor;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.block.entity.LapidaristTableBlockEntity;
import net.turtleboi.ancientcurses.mixin.SlotAccessorMixin;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.RefreshLapidaryScreenS2C;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class LapidaristTableContainerMenu extends AbstractContainerMenu {
    public final LapidaristTableBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private static final int maxSockets = 6;
    public static int teSlotCount = 1 + maxSockets;

    public LapidaristTableContainerMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf pData) {
        this(pContainerId, pPlayerInventory, pPlayerInventory.player.level().getBlockEntity(pData.readBlockPos()), new SimpleContainerData(teSlotCount));
    }

    public LapidaristTableContainerMenu(int pContainerId, Inventory pPlayerInventory, BlockEntity pEntity, ContainerData pData){
        super(ModMenuTypes.LAPIDARIST_MENU.get(), pContainerId);
        blockEntity = ((LapidaristTableBlockEntity) pEntity);
        checkContainerSize(pPlayerInventory, teSlotCount);
        this.level = pPlayerInventory.player.level();
        this.data = pData;

        addPlayerInventory(pPlayerInventory);
        addPlayerHotbar(pPlayerInventory);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 14, 32) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    if(this.hasItem()){
                        return false;
                    }

                    if (stack.hasTag()) {
                        assert stack.getTag() != null;
                        if (stack.getTag().getBoolean("Socketable")) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void set(@NotNull ItemStack stack) {
                    super.set(stack);
                    blockEntity.onSlotChanged();
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    blockEntity.onSlotChanged();
                }
            });

            this.addSlot(new PreciousGemSlot(iItemHandler, 1, 0, 0, blockEntity, true));
            this.addSlot(new PreciousGemSlot(iItemHandler, 2, 0, 0, blockEntity, false));
            this.addSlot(new PreciousGemSlot(iItemHandler, 3, 0, 0, blockEntity, false));
            this.addSlot(new PreciousGemSlot(iItemHandler, 4, 0, 0, blockEntity, false));
            this.addSlot(new PreciousGemSlot(iItemHandler, 5, 0, 0, blockEntity, false));
            this.addSlot(new PreciousGemSlot(iItemHandler, 6, 0, 0, blockEntity, false));
        });
        addDataSlots(pData);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public static int getSocketSlotCount(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return 0;
        }

        if (stack.getTag().getBoolean("Socketable")) {
            return stack.getTag().getInt("SocketCount");
        }
        return 0;
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = teSlotCount;
    @Override
    public ItemStack quickMoveStack(@NotNull Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.LAPIDARIST_TABLE.get());
    }
}


