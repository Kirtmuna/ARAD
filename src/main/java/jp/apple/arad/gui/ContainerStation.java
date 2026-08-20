package jp.apple.arad.gui;

import jp.apple.arad.station.TileEntityStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerStation extends Container {

    public final TileEntityStation station;

    public ContainerStation(InventoryPlayer playerInv, TileEntityStation station) {
        this.station = station;

        addSlotToContainer(new Slot(station, 0, 100, 93) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return station.isItemValidForSlot(0, stack);
            }
        });
        addSlotToContainer(new Slot(station, 1, 122, 93) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return station.isItemValidForSlot(1, stack);
            }
        });
        addSlotToContainer(new Slot(station, 2, 144, 93) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return station.isItemValidForSlot(2, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv,
                        col + row * 9 + 9,
                        8 + col * 18,
                        118 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 176));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = null;
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack())
            return result;

        ItemStack stack = slot.getStack();
        result = stack.copy();

        if (index < 3) {
            if (!mergeItemStack(stack, 3, inventorySlots.size(), true))
                return null;
        } else {
            if (station.isItemValidForSlot(0, stack)) {
                boolean moved = false;
                for (int i = 0; i < 3 && !moved; i++) {
                    if (inventorySlots.get(i) != null
                            && ((net.minecraft.inventory.Slot) inventorySlots.get(i)).getHasStack()) {
                        moved = mergeItemStack(stack, i, i + 1, false);
                    }
                }
                if (!moved)
                    return null;
            } else {
                return null;
            }
        }

        if (stack.stackSize == 0)
            slot.putStack(null);
        else
            slot.onSlotChanged();

        if (stack.stackSize == result.stackSize)
            return null;

        slot.onPickupFromSlot(player, stack);

        return result;
    }
}