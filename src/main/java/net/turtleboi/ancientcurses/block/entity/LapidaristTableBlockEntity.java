package net.turtleboi.ancientcurses.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.turtleboi.ancientcurses.item.items.GoldenAmuletItem;
import net.turtleboi.ancientcurses.screen.LapidaristTableContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LapidaristTableBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(7){
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    public static final int amuletSlot = 0;
    public static final int mainGem = 1;
    public static final int minorGem1 = 2;
    public static final int minorGem2 = 3;
    public static final int minorGem3 = 4;
    public static final int minorGem4 = 5;
    public static final int minorGem5 = 6;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData pData;

    public LapidaristTableBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.LAPIDARIST_TABLE_BE.get(), pPos, pBlockState);
        this.pData = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return 0;
            }

            @Override
            public void set(int pIndex, int pValue) {

            }

            @Override
            public int getCount() {
                return 7;
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER){
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemStackHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops(){
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for(int i = 0; i < itemStackHandler.getSlots(); i++){
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Lapidarist's Workbench");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new LapidaristTableContainerMenu(pContainerId, pPlayerInventory, this, this.pData);
    }

    public ItemStackHandler getInventory() {
        return itemStackHandler;
    }

    private boolean hasAmulet = false;

    public void onSlotChanged() {
        ItemStack amulet = getInventory().getStackInSlot(LapidaristTableBlockEntity.amuletSlot);
        if (!amulet.isEmpty() && amulet.getItem() instanceof GoldenAmuletItem) {
            if (!hasAmulet) {
                loadGemsFromAmulet();
                hasAmulet = true;
            }
            saveGemsToAmulet();
        } else {
            if (hasAmulet) {
                clearGemSlots();
                hasAmulet = false;
            }
        }
        markForUpdate();
    }

    public void markForUpdate() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void clearGemSlots() {
        for (int i = mainGem; i <= minorGem5; i++) {
            getInventory().setStackInSlot(i, ItemStack.EMPTY);
        }
    }


    public void saveGemsToAmulet() {
        ItemStack amulet = getInventory().getStackInSlot(amuletSlot);
        if (!amulet.isEmpty() && amulet.getItem() instanceof GoldenAmuletItem) {
            CompoundTag amuletTag = amulet.getOrCreateTag();
            ItemStack mainGemStack = itemStackHandler.getStackInSlot(mainGem);
            if (!mainGemStack.isEmpty()) {
                amuletTag.put("MainGem", mainGemStack.serializeNBT());
            } else {
                amuletTag.remove("MainGem");
            }
            ListTag minorGems = new ListTag();
            for (int i = minorGem1; i <= minorGem5; i++) {
                ItemStack minorGemStack = getInventory().getStackInSlot(i);
                if (!minorGemStack.isEmpty()) {
                    minorGems.add(minorGemStack.serializeNBT());
                }
            }
            if (!minorGems.isEmpty()) {
                amuletTag.put("MinorGems", minorGems);
            } else {
                amuletTag.remove("MinorGems");
            }
            amulet.setTag(amuletTag);
            this.markForUpdate();
        }
    }

    public void loadGemsFromAmulet() {
        ItemStack amulet = getInventory().getStackInSlot(amuletSlot);
        if (!amulet.isEmpty() && amulet.getItem() instanceof GoldenAmuletItem) {
            CompoundTag amuletTag = amulet.getOrCreateTag();
            if (amuletTag.contains("MainGem")) {
                CompoundTag mainGemTag = amuletTag.getCompound("MainGem");
                ItemStack mainGemStack = ItemStack.of(mainGemTag);
                getInventory().setStackInSlot(mainGem, mainGemStack);
            } else {
                getInventory().setStackInSlot(mainGem, ItemStack.EMPTY);
            }
            if (amuletTag.contains("MinorGems")) {
                ListTag minorGems = amuletTag.getList("MinorGems", CompoundTag.TAG_COMPOUND);
                for (int i = 0; i < minorGems.size(); i++) {
                    CompoundTag minorGemTag = minorGems.getCompound(i);
                    ItemStack minorGemStack = ItemStack.of(minorGemTag);
                    getInventory().setStackInSlot(minorGem1 + i, minorGemStack);
                }
                for (int i = minorGems.size(); i < 5; i++) {
                    getInventory().setStackInSlot(minorGem1 + i, ItemStack.EMPTY);
                }
            } else {
                for (int i = minorGem1; i <= minorGem5; i++) {
                    getInventory().setStackInSlot(i, ItemStack.EMPTY);
                }
            }
            this.markForUpdate();
        }
    }


    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", getInventory().serializeNBT());
        saveGemsToAmulet();
        super.saveAdditional(pTag);
    }


    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        getInventory().deserializeNBT(pTag.getCompound("inventory"));
        loadGemsFromAmulet();
    }
}
