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
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.block.entity.LapidaristTableBlockEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.RefreshLapidaryScreenS2C;
import net.turtleboi.turtlecore.client.util.ParticleSpawnQueue;
import org.jetbrains.annotations.NotNull;

public class LapidaristTableContainerMenu extends AbstractContainerMenu {
    public final LapidaristTableBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private static final int MAX_SOCKETS = 6;
    private static final int TE_SLOT_COUNT = 7;
    private ServerPlayer serverPlayer = null;

    public LapidaristTableContainerMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf pData) {
        this(pContainerId, pPlayerInventory, pPlayerInventory.player.level().getBlockEntity(pData.readBlockPos()), new SimpleContainerData(TE_SLOT_COUNT));
    }

    public LapidaristTableContainerMenu(int pContainerId, Inventory pPlayerInventory, BlockEntity pEntity, ContainerData pData){
        super(ModMenuTypes.LAPIDARIST_MENU.get(), pContainerId);
        checkContainerSize(pPlayerInventory, TE_SLOT_COUNT);
        blockEntity = ((LapidaristTableBlockEntity) pEntity);
        this.level = pPlayerInventory.player.level();
        this.data = pData;

        addPlayerInventory(pPlayerInventory);
        addPlayerHotbar(pPlayerInventory);

        if (pPlayerInventory.player instanceof ServerPlayer){
            serverPlayer = (ServerPlayer) pPlayerInventory.player;
        }

        ItemStack socketableItem = blockEntity.getInventory().getStackInSlot(LapidaristTableBlockEntity.socketableItemSlot);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 14, 32) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
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
                    if (serverPlayer != null) {
                        ModNetworking.sendToPlayer(new RefreshLapidaryScreenS2C(blockEntity.getBlockPos()), serverPlayer);
                    }

                }
            });

            int socketCount = getSocketSlotCount(socketableItem);

            if (socketCount == 6) {
                this.addSlot(new PreciousGemSlot(iItemHandler, 2, 80, 9, blockEntity, false));
                this.addSlot(new PreciousGemSlot(iItemHandler, 3, 53, 29, blockEntity, false));
                this.addSlot(new PreciousGemSlot(iItemHandler, 4, 107, 29, blockEntity, false));
                this.addSlot(new PreciousGemSlot(iItemHandler, 5, 62, 59, blockEntity, false));
                this.addSlot(new PreciousGemSlot(iItemHandler, 6, 98, 59, blockEntity, false));

                this.addSlot(new PreciousGemSlot(iItemHandler, 1, 80, 34, blockEntity, true));
            } else if (socketCount == 1) {
                this.addSlot(new PreciousGemSlot(iItemHandler, 2, 80, 9, blockEntity, false));
            } else if (socketCount == 2) {
                this.addSlot(new PreciousGemSlot(iItemHandler, 2, 80, 9, blockEntity, false));
                this.addSlot(new PreciousGemSlot(iItemHandler, 3, 53, 29, blockEntity, false));
            } else if (socketCount == 3) {
                this.addSlot(new PreciousGemSlot(iItemHandler, 2, 80, 9, blockEntity, false));
                this.addSlot(new PreciousGemSlot(iItemHandler, 3, 53, 29, blockEntity, false));
                this.addSlot(new PreciousGemSlot(iItemHandler, 4, 107, 29, blockEntity, false));
            } else if (socketCount == 4) {
                this.addSlot(new PreciousGemSlot(iItemHandler, 2, 80, 9, blockEntity, false));
                this.addSlot(new PreciousGemSlot(iItemHandler, 3, 53, 29, blockEntity, false));
                this.addSlot(new PreciousGemSlot(iItemHandler, 4, 107, 29, blockEntity, false));
                this.addSlot(new PreciousGemSlot(iItemHandler, 5, 62, 59, blockEntity, false));
            }
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

    public void refreshContainer() {
        for (int i = 0; i < slots.size(); i++) {
            //lastSlots.set(i, ItemStack.EMPTY);
        }

        this.broadcastFullState();
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
    private static final int TE_INVENTORY_SLOT_COUNT = TE_SLOT_COUNT;  // must be the number of slots you have!
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


