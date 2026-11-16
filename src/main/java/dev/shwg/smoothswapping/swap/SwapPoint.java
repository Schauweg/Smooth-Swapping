package dev.shwg.smoothswapping.swap;

public final class SwapPoint {

    private static final int NO_INDEX = -1;

    public enum Type { SLOT, CURSOR }

    public static final SwapPoint CURSOR = new SwapPoint(Type.CURSOR, NO_INDEX);

    private final Type type;
    private final int slotIndex;

    private SwapPoint(Type type, int slotIndex) {
        this.type = type;
        this.slotIndex = slotIndex;
    }

    public static SwapPoint slot(int slotIndex) {
        return new SwapPoint(Type.SLOT, slotIndex);
    }

    public Type type() {
        return type;
    }

    /** Only valid for SLOT points. */
    public int slotIndex() {
        if (type != Type.SLOT) throw new IllegalStateException("Not a slot point: " + this);
        return slotIndex;
    }

    @Override
    public String toString() {
        return type == Type.SLOT ? String.valueOf(slotIndex) : "cursor";
    }
}
