package net.turtleboi.ancientcurses.screen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import net.turtleboi.ancientcurses.item.items.GoldenAmuletItem;
import net.turtleboi.ancientcurses.item.items.PreciousGemItem;
import net.turtleboi.ancientcurses.util.ModTags;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LapidaristTableContainerMenu extends AbstractContainerMenu {
    public final LapidaristTableBlockEntity blockEntity;
    private final Level level;
    public ContainerData data;
    private final List<PreciousGemSlot> gemSlots = new ArrayList<>();
    private int lastActiveGemCount = -1;

    public LapidaristTableContainerMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf pData) {
        this(pContainerId, pPlayerInventory, pPlayerInventory.player.level().getBlockEntity(pData.readBlockPos()), new SimpleContainerData(13));
    }

    public LapidaristTableContainerMenu(int pContainerId, Inventory pPlayerInventory, BlockEntity pEntity, ContainerData pData){
        super(ModMenuTypes.LAPIDARIST_MENU.get(), pContainerId);
        checkContainerSize(pPlayerInventory, 13);
        DataSlot activeGemSlots = DataSlot.standalone();
        addDataSlot(activeGemSlots);
        blockEntity = ((LapidaristTableBlockEntity) pEntity);
        this.level = pPlayerInventory.player.level();
        data = pData;

        addPlayerInventory(pPlayerInventory);
        addPlayerHotbar(pPlayerInventory);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 14, 32) {
                @Override
                public boolean mayPlace(@NotNull ItemStack itemStack) {
                    CompoundTag pCompoundTag = itemStack.getTag();
                    if (pCompoundTag != null && pCompoundTag.contains("Socketable")) {
                        if (itemStack.getTag().getBoolean("Socketable")) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void set(@NotNull ItemStack stack) {
                    super.set(stack);
                    blockEntity.onSlotChanged();
                    updateActiveGemSlots(this.getItem());
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    blockEntity.onSlotChanged();
                    updateActiveGemSlots(this.getItem());
                }
            });

            PreciousGemSlot gemSlot1 = new PreciousGemSlot(iItemHandler, 2, 80, 9, blockEntity);
            PreciousGemSlot gemSlot2 = new PreciousGemSlot(iItemHandler, 3, 53, 29, blockEntity);
            PreciousGemSlot gemSlot3 = new PreciousGemSlot(iItemHandler, 4, 107, 29, blockEntity);
            PreciousGemSlot gemSlot4 = new PreciousGemSlot(iItemHandler, 5, 62, 59, blockEntity);
            PreciousGemSlot gemSlot5 = new PreciousGemSlot(iItemHandler, 6, 98, 59, blockEntity);
            PreciousGemSlot mainGemSlot = new PreciousGemSlot(iItemHandler, 1, 80, 34, blockEntity) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    ItemStack socketableItem = ((LapidaristTableBlockEntity) pEntity).getInventory().getStackInSlot(LapidaristTableBlockEntity.socketableItemSlot);
                    return !socketableItem.isEmpty() && super.mayPlace(stack) && stack.getItem() instanceof PreciousGemItem && !stack.is(ModTags.Items.MINOR_GEMS);
                }
            };
            this.addSlot(mainGemSlot);
            this.addSlot(gemSlot1);
            this.addSlot(gemSlot2);
            this.addSlot(gemSlot3);
            this.addSlot(gemSlot4);
            this.addSlot(gemSlot5);
            gemSlots.add(mainGemSlot);
            gemSlots.add(gemSlot1);
            gemSlots.add(gemSlot2);
            gemSlots.add(gemSlot3);
            gemSlots.add(gemSlot4);
            gemSlots.add(gemSlot5);

            PreciousGemSlot gemSlot6 = new PreciousGemSlot(iItemHandler, 7, 80, 32, blockEntity);
            this.addSlot(gemSlot6);
            gemSlots.add(gemSlot6);

            PreciousGemSlot gemSlot7 = new PreciousGemSlot(iItemHandler, 8, 98, 32, blockEntity);
            PreciousGemSlot gemSlot8 = new PreciousGemSlot(iItemHandler, 9, 62, 32, blockEntity);
            this.addSlot(gemSlot7);
            this.addSlot(gemSlot8);
            gemSlots.add(gemSlot7);
            gemSlots.add(gemSlot8);

            PreciousGemSlot gemSlot9 = new PreciousGemSlot(iItemHandler, 10, 92, 23, blockEntity);
            PreciousGemSlot gemSlot10 = new PreciousGemSlot(iItemHandler, 11, 68, 23, blockEntity);
            PreciousGemSlot gemSlot11 =new PreciousGemSlot(iItemHandler, 12, 80, 45, blockEntity);
            this.addSlot(gemSlot9);
            this.addSlot(gemSlot10);
            this.addSlot(gemSlot11);
            gemSlots.add(gemSlot9);
            gemSlots.add(gemSlot10);
            gemSlots.add(gemSlot11);
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

    private void updateActiveGemSlots(ItemStack socketableItem) {
        for (PreciousGemSlot slot : gemSlots) {
            slot.setActive(false);
        }

        if (socketableItem.isEmpty()) {
            data.set(0, 0);
            return;
        }

        if (socketableItem.getItem() instanceof GoldenAmuletItem) {
            activateSlots(0, 6);
            data.set(0, 6);
        } else {
            int socketCount = getGemSlots(socketableItem);
            switch (socketCount) {
                case 1:
                    activateSlots(6, 7);
                    data.set(0, 1);
                    break;
                case 2:
                    activateSlots(7, 9);
                    data.set(0, 2);
                    break;
                case 3:
                    activateSlots(9, 12);
                    data.set(0, 3);
                    break;
                default:
                    break;
            }
        }
    }

    private void activateSlots(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex && i < gemSlots.size(); i++) {
            PreciousGemSlot slot = gemSlots.get(i);
            slot.setActive(true);
        }
    }

    private int getGemSlots(ItemStack itemStack){
        CompoundTag pCompoundTag = itemStack.getTag();
        if (pCompoundTag != null && pCompoundTag.contains("SocketCount")) {
            return pCompoundTag.getInt("SocketCount");
        }
        return 0;
    }

    public List<PreciousGemSlot> getAllGemSlots() {
        return gemSlots;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        int activeGemCount = data.get(0);
        if (activeGemCount != lastActiveGemCount) {
            lastActiveGemCount = activeGemCount;
            for (int i = 0; i < gemSlots.size(); i++) {
                PreciousGemSlot slot = gemSlots.get(i);
                slot.setActive(i < activeGemCount);
            }
        }
    }


    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 13;  // must be the number of slots you have!
    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.LAPIDARIST_TABLE.get());
    }
}


