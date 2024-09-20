package net.turtleboi.ancientcurses.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.block.entity.LapidaristTableBlockEntity;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.item.items.GoldenAmuletItem;
import net.turtleboi.ancientcurses.util.ModTags;
import org.jetbrains.annotations.NotNull;

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
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 18, 29) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.getItem() instanceof GoldenAmuletItem;
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

            this.addSlot(new PreciousGemSlot(iItemHandler, 2, 80, 4, blockEntity));
            this.addSlot(new PreciousGemSlot(iItemHandler, 3, 53, 24, blockEntity));
            this.addSlot(new PreciousGemSlot(iItemHandler, 4, 107, 24, blockEntity));
            this.addSlot(new PreciousGemSlot(iItemHandler, 1, 80, 29, blockEntity) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return !stack.is(ModTags.Items.MINOR_GEMS);
                }
            });
            this.addSlot(new PreciousGemSlot(iItemHandler, 5, 62, 54, blockEntity));
            this.addSlot(new PreciousGemSlot(iItemHandler, 6, 98, 54, blockEntity));
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


