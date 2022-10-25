package schauweg.smoothswapping;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SwapStacks {

    private final ItemStack oldStack, newStack;
    public int itemCountToChange;

    public SwapStacks(ItemStack oldStack, ItemStack newStack, int itemCountToChange) {
        this.oldStack = oldStack;
        this.newStack = newStack;
        this.itemCountToChange = itemCountToChange;
    }

    public ItemStack getOldStack() {
        return oldStack;
    }

    public ItemStack getNewStack() {
        return newStack;
    }

    public int getOldCount(){
//        return ItemStack.areItemsEqual(oldStack, Items.AIR.getDefaultStack()) ? 0 : oldStack.getCount();
        return oldStack.getCount();
    }

    public int getNewCount(){
//        return ItemStack.areItemsEqual(newStack, Items.AIR.getDefaultStack()) ? 0 : newStack.getCount();
        return newStack.getCount();
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
