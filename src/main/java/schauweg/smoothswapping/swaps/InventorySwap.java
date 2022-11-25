package schauweg.smoothswapping.swaps;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class InventorySwap {

    private double x, y;
    private final double distance, startX, startY, angle;
    private boolean renderDestinationSlot, checked;
    private final int amount;
    
    private final ItemStack swapItem;

    public InventorySwap(Slot fromSlot, Slot toSlot, boolean checked, int amount) {
        this.x = toSlot.x - fromSlot.x;
        this.y = toSlot.y - fromSlot.y;
        this.startX = toSlot.x - fromSlot.x;
        this.startY = toSlot.y - fromSlot.y;
        this.angle = (float) (Math.atan2(y, x) + Math.PI);
        this.distance = Math.hypot(x, y);
        this.renderDestinationSlot = false;
        this.checked = checked;
        this.amount = amount;
        this.swapItem = toSlot.getStack();
    }

    public double getAngle() {
        return angle;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getDistance() {
        return distance;
    }

    public boolean renderDestinationSlot() {
        return renderDestinationSlot;
    }

    public void setRenderDestinationSlot(boolean renderDestinationSlot) {
        this.renderDestinationSlot = renderDestinationSlot;
    }

    public boolean isChecked() {
        return checked;
    }

    public int getAmount() {
        return amount;
    }

    public ItemStack getSwapItem() {
        return swapItem;
    }
}
