package schauweg.smoothswapping.swaps;

import net.minecraft.client.Mouse;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import schauweg.smoothswapping.SmoothSwapping;
import schauweg.smoothswapping.Vec2;

public class ItemToCursorInventorySwap extends InventorySwap {
    public ItemToCursorInventorySwap(Slot fromSlot, Vec2 relativeCursorPos, ItemStack cursorSlot, boolean checked, int amount) {
        super(new Vec2(fromSlot.x, fromSlot.y), relativeCursorPos, cursorSlot, checked, amount);
    }
}
