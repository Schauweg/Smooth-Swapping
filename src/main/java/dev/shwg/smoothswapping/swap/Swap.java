package dev.shwg.smoothswapping.swap;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.util.ItemStackUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class Swap {

    private final SwapPoint source;
    private final SwapPoint target;
    private final ItemStack stack;
    private final int sourceX;
    private final int sourceY;
    private final long startTime;
    private final long duration;

    private Swap(SwapPoint source, SwapPoint target, ItemStack stack, int sourceX, int sourceY, long startTime, long duration) {
        this.source = source;
        this.target = target;
        this.stack = stack.copy();
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.startTime = startTime;
        this.duration = Math.max(1, duration);
    }

    private Swap(SwapPoint source, SwapPoint target, ItemStack stack, int sourceX, int sourceY, int targetX, int targetY, long startTime) {
        this(source, target, stack, sourceX, sourceY, startTime, durationFor(sourceX, sourceY, targetX, targetY));
    }

    private static long durationFor(int sourceX, int sourceY, int targetX, int targetY) {
        float distance = (float) Math.hypot(sourceX - targetX, sourceY - targetY);
        return SmoothSwapping.CONFIG.swapDuration(distance);
    }

    public static Swap of(SwapPoint source, SwapPoint target, ItemStack stack, int startX, int startY, int targetX, int targetY, long startTime) {
        return new Swap(source, target, stack, startX, startY, targetX, targetY, startTime);
    }

    public static Swap slotToSlot(Slot from, Slot to, ItemStack stack) {
        return new Swap(SwapPoint.slot(from.index), SwapPoint.slot(to.index), stack, from.x, from.y, to.x, to.y, SwapManager.getFrameTime());
    }

    // mouseX/Y: cursor position now, only used to estimate the distance (the target moves)
    public static Swap slotToCursor(Slot sourceSlot, int mouseX, int mouseY, ItemStack stack) {
        return new Swap(SwapPoint.slot(sourceSlot.index), SwapPoint.CURSOR, stack,
                sourceSlot.x, sourceSlot.y, mouseX, mouseY, SwapManager.getFrameTime());
    }

    // mouseX/Y: where the carried item was, used as the start position
    public static Swap cursorToSlot(int mouseX, int mouseY, Slot targetSlot, ItemStack stack) {
        return new Swap(SwapPoint.CURSOR, SwapPoint.slot(targetSlot.index), stack,
                mouseX, mouseY, targetSlot.x, targetSlot.y, SwapManager.getFrameTime());
    }

    public SwapPoint source() {
        return source;
    }

    public SwapPoint target() {
        return target;
    }

    public float progress(long now) {
        return Mth.clamp((now - startTime) / (float) duration, 0f, 1f);
    }

    public boolean isFinished(long now) {
        return now - startTime >= duration;
    }

    public float offsetX(float destinationX, float easingProgress) {
        return (sourceX - destinationX) * (1f - easingProgress);
    }

    public float offsetY(float destinationY, float easingProgress) {
        return (sourceY - destinationY) * (1f - easingProgress);
    }

    public float easedProgress(long now) {
        return (float) SmoothSwapping.CONFIG.animationCurve.ease(progress(now));
    }

    public ItemStack stack() {
        return stack;
    }

    public int amount() {
        return stack.getCount();
    }

    public Swap withAmount(int amount) {
        return new Swap(source, target, ItemStackUtil.copyWithCount(stack, amount),
                sourceX, sourceY, startTime, duration);
    }

    @Override
    public String toString() {
        return "Swap{" + slotName(source) + " -> " + slotName(target)
                + ", " + stack
                + ", start=(" + sourceX + ", " + sourceY + ")"
                + ", " + duration + "ms}";
    }

    private static String slotName(SwapPoint point) {
        return point.type().name();
    }
}
