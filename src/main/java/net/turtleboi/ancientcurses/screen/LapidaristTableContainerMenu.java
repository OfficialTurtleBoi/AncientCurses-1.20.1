package net.turtleboi.ancientcurses.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.block.entity.LapidaristTableBlockEntity;

public class LapidaristTableContainerMenu extends AbstractContainerMenu {
    public final LapidaristTableBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public LapidaristTableContainerMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf pData) {
        this(pContainerId, pPlayerInventory, pPlayerInventory.player.level().getBlockEntity(pData.readBlockPos()), new SimpleContainerData(7));
    }

    public LapidaristTableContainerMenu(int pContainerId, Inventory pPlayerInventory, BlockEntity pEntity, ContainerData pData){
        super(ModMenuTypes.LAPIDARIST_MENU.get(), pContainerId);
        checkContainerSize(pPlayerInventory, 7);
        blockEntity = ((LapidaristTableBlockEntity) pEntity);
        this.level = pPlayerInventory.player.level();
        this.data = pData;

        addPlayerInventory(pPlayerInventory);
        addPlayerHotbar(pPlayerInventory);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 18, 29));

            this.addSlot(new SlotItemHandler(iItemHandler, 2, 80, 4));
            this.addSlot(new SlotItemHandler(iItemHandler, 3, 53, 24));
            this.addSlot(new SlotItemHandler(iItemHandler, 4, 107, 24));
            this.addSlot(new SlotItemHandler(iItemHandler, 1, 80, 29));
            this.addSlot(new SlotItemHandler(iItemHandler, 5, 62, 54));
            this.addSlot(new SlotItemHandler(iItemHandler, 6, 98, 54));

        });

        addDataSlots(pData);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 79 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 137));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.LAPIDARIST_TABLE.get());
    }
}


