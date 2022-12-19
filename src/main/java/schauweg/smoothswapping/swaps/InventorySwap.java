package schauweg.smoothswapping.swaps;

import net.minecraft.item.ItemStack;
import schauweg.smoothswapping.Vec2;

public class InventorySwap {

    private double x, y;
    private final double distance, startX, startY, angle;
    private boolean renderDestinationSlot;
    private final boolean checked;
    private final int amount;
    
    private final ItemStack swapItem;

    public InventorySwap(Vec2 fromVec, Vec2 toVec, ItemStack swapItem, boolean checked, int amount) {
        this.x = toVec.v[0] - fromVec.v[0];
        this.y = toVec.v[1] - fromVec.v[1];
        this.startX = toVec.v[0] - fromVec.v[0];
        this.startY = toVec.v[1] - fromVec.v[1];
        this.angle = (float) (Math.atan2(y, x) + Math.PI);
        this.distance = Math.hypot(x, y);
        this.renderDestinationSlot = false;
        this.checked = checked;
        this.amount = amount;
        this.swapItem = swapItem;
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
