package schauweg.smoothswapping.swaps;

import net.minecraft.screen.slot.Slot;

public class InventorySwap {

    private final float angle;
    private double x;
    private double y;
    private final double distance;
    private boolean renderDestinationSlot;
    private boolean checked;
    private final int amount;

    public InventorySwap(Slot fromSlot, Slot toSlot, boolean checked, int amount) {
        this.x = toSlot.x - fromSlot.x;
        this.y = toSlot.y - fromSlot.y;
        this.angle = (float) (Math.atan2(y, x) + Math.PI);
        this.distance = Math.hypot(x, y);
        this.renderDestinationSlot = false;
        this.checked = checked;
        this.amount = amount;
    }

    public float getAngle() {
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

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getAmount() {
        return amount;
    }
}
