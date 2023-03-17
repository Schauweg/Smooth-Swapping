package schauweg.smoothswapping.swaps;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import schauweg.smoothswapping.SmoothSwapping;
import schauweg.smoothswapping.Vec2;

public class ItemToItemInventorySwap extends InventorySwap {
    public ItemToItemInventorySwap(Slot fromSlot, Slot toSlot, boolean checked, int amount) {
        super(new Vec2(fromSlot.x, fromSlot.y), new Vec2(toSlot.x, toSlot.y), toSlot.getStack(), checked, amount);
    }
}
