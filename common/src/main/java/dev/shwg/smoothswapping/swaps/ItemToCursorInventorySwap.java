package dev.shwg.smoothswapping.swaps;

import dev.shwg.smoothswapping.Vec2;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ItemToCursorInventorySwap extends InventorySwap {
    private boolean startedRender;
    private int copiedStackHash;
    private int targetStackHash;
    private boolean arrived;

    public ItemToCursorInventorySwap(Slot fromSlot, Vec2 relativeCursorPos, ItemStack fromStack, boolean checked, int amount) {
        // slot has fixed size: 16
        // defined in net.minecraft.client.gui.screen.ingame.HandledScreen#drawSlot(
        //      net.minecraft.client.util.math.MatrixStack matrices,
        //      net.minecraft.screen.slot.Slot slot
        // )
        super(new Vec2(fromSlot.x + 8, fromSlot.y + 8), relativeCursorPos, fromStack, checked, amount);
        startedRender = false;
        arrived = false;
        targetStackHash = -1;
    }

    public boolean isStartedRender() {
        return startedRender;
    }

    public void setStartedRender(boolean startedRender) {
        this.startedRender = startedRender;
    }

    public int getCopiedStackHash() {
        return copiedStackHash;
    }

    public void setCopiedStackHash(int copiedStackHash) {
        this.copiedStackHash = copiedStackHash;
    }

    public int getTargetStackHash() {
        return targetStackHash;
    }

    public void setTargetStackHash(int targetStackHash) {
        this.targetStackHash = targetStackHash;
    }

    public boolean isArrived() {
        return arrived;
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }
}
