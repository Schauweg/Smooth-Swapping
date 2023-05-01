package dev.shwg.smoothswapping;

import net.minecraft.item.ItemStack;

public class SwapStacks {

    private final ItemStack oldStack, newStack;
    public int itemCountToChange;
    private final int slotID;

    public SwapStacks(int slotID, ItemStack oldStack, ItemStack newStack, int itemCountToChange) {
        this.oldStack = oldStack;
        this.newStack = newStack;
        this.itemCountToChange = itemCountToChange;
        this.slotID = slotID;
    }

    public ItemStack getOldStack() {
        return oldStack;
    }

    public ItemStack getNewStack() {
        return newStack;
    }

    public int getSlotID() {
        return slotID;
    }

    @Override
    public String toString() {
        return "SwapStacks{" +
                "oldStack=" + oldStack +
                ", newStack=" + newStack +
                ", itemCountToChange=" + itemCountToChange +
                '}';
    }
}
