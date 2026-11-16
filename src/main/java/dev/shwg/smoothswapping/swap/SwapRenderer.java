package dev.shwg.smoothswapping.swap;

import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.smoothswapping.util.ItemStackUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/*? if >=1.21.6 {*/
import org.joml.Matrix3x2f;
/*?}*/

/*? if <1.21.6 {*/
//import java.util.ArrayList;
/*?}*/

/*? if >=1.19.4 && <1.21.6 {*/
//import org.joml.Matrix4f;
/*?}*/

/*? if <1.19.4 {*/
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
*//*?}*/

public class SwapRenderer {

    private static boolean debugTracePrinted;

    public interface ItemDraw { void draw(ItemStack stack); }
    public interface DecorationDraw { void draw(ItemStack stack, @Nullable String countText); }

    /*? if <1.21.6 {*/
    /*private static final float Z_OFFSET = 90;

    private static final List<Runnable> LEGACY_DEFERRED = new ArrayList<>();

    public static void flushLegacyDeferred() {
        if (LEGACY_DEFERRED.isEmpty()) return;

        float previousBlitOffset = raiseBlitOffset();
        try {
            for (Runnable r : LEGACY_DEFERRED) r.run();
        } finally {
            restoreBlitOffset(previousBlitOffset);
            LEGACY_DEFERRED.clear();
        }
    }
    *//*?}*/

    /*? if <1.19.4 {*/
    /*private static float raiseBlitOffset() {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        float previous = itemRenderer.blitOffset;
        itemRenderer.blitOffset = 100.0F; // same as vanilla's renderSlot
        return previous;
    }

    private static void restoreBlitOffset(float previous) {
        Minecraft.getInstance().getItemRenderer().blitOffset = previous;
    }
    *//*?} else if <1.21.6 {*/
    /*private static float raiseBlitOffset() { return 0; }
    private static void restoreBlitOffset(float previous) { }
    *//*?}*/

    // SwapRenderer
    public static void beginFrame() {
        /*? if <1.21.6 {*/
        //LEGACY_DEFERRED.clear();
        /*?}*/
    }

    public static void swapSlotRenderItem(GenericGraphics graphics, ItemStack stack, int x, int y, Slot slot, ItemDraw original) {
        List<Swap> swaps = SwapManager.getSwapsTo(slot);
        if (swaps.isEmpty()) {
            original.draw(stack);
            return;
        }

        drawSwapsDeferred(graphics, swaps, x, y, swap -> original.draw(swap.stack()));

        if (stack.getCount() - inFlightOf(swaps, stack) > 0) {
            original.draw(stack);
        }
    }

    public static void swapSlotRenderItemDecorations(GenericGraphics graphics, Font font, ItemStack stack, int x, int y, String countText, Slot slot, DecorationDraw original) {
        List<Swap> swaps = SwapManager.getSwapsTo(slot);
        if (swaps.isEmpty()) {
            original.draw(stack, countText);
            return;
        }

        drawSwapsDeferred(graphics, swaps, x, y, swap -> original.draw(swap.stack(), null));

        int ownCount = stack.getCount() - inFlightOf(swaps, stack);
        if (ownCount > 0) {
            original.draw(ItemStackUtil.copyWithCount(stack, ownCount), countText);
        }
    }

    public static void swapMouseRenderItemAndDecorations(GenericGraphics graphics, ItemStack stack, int x, int y, String countText, DecorationDraw original) {
        List<Swap> swaps = SwapManager.getCursorSwaps();
        if (swaps.isEmpty()) {
            original.draw(stack, countText);
            return;
        }

        drawSwapsNow(graphics, swaps, x, y, swap -> original.draw(swap.stack(), null));

        int ownCount = stack.getCount() - inFlightOf(swaps, stack);
        if (ownCount > 0) {
            original.draw(ItemStackUtil.copyWithCount(stack, ownCount), countText);
        }
    }

    private static void drawSwapsNow(GenericGraphics graphics, List<Swap> swaps, int x, int y, Consumer<Swap> draw) {
        drawSwaps(graphics, swaps, x, y, draw, false);
    }

    private static void drawSwapsDeferred(GenericGraphics graphics, List<Swap> swaps, int x, int y, Consumer<Swap> draw) {
        drawSwaps(graphics, swaps, x, y, draw, true);
    }

    // Queues every swap at its current offset from the destination (x, y), returns the amount in flight
    private static void drawSwaps(GenericGraphics graphics, List<Swap> swaps, int x, int y, Consumer<Swap> draw, boolean deferred) {
        long now = SwapManager.getFrameTime();
        for (Swap swap : swaps) {
            float t = swap.easedProgress(now);
            float dx = swap.offsetX(x, t);
            float dy = swap.offsetY(y, t);

            if (deferred) {
                /*? if >=1.21.6 {*/
                // No z anymore, draw order decides: defer via ShwgConfig, restore the slot pass pose
                Matrix3x2f savedPose = new Matrix3x2f(graphics.getPose());
                graphics.addDeferred((g, mouseX, mouseY, partialTicks) -> {
                    g.pushPose();
                    g.getPose().set(savedPose);
                    g.translate(dx, dy);
                    draw.accept(swap);
                    g.popPose();
                });
                /*?} else if >=1.19.4 {*/
                /*// Shared pose stack: restore the slot pass pose when flushing
                Matrix4f savedPose = new Matrix4f(graphics.getPose().last().pose());
                LEGACY_DEFERRED.add(() -> {
                    graphics.pushPose();
                    graphics.getPose().last().pose().set(savedPose);
                    graphics.translate(dx, dy, Z_OFFSET);
                    draw.accept(swap);
                    graphics.popPose();
                });
                *//*?} else {*/
                /*// Own offset-only PoseStack, screen position lives on the model-view stack
                LEGACY_DEFERRED.add(() -> {
                    graphics.pushPose();
                    graphics.translate(dx, dy, Z_OFFSET);
                    draw.accept(swap);
                    graphics.popPose();
                });
                *//*?}*/
            } else {
                graphics.pushPose();
                /*~ if >= 1.21.6 'Z_OFFSET' -> '0'*/
                graphics.translate(dx, dy, 0);
                draw.accept(swap);
                graphics.popPose();
            }
        }
    }



    // Fallback entry points: same rendering as the vanilla path, but only for the slot's own draw call
    public static void fallbackSlotRenderItem(GenericGraphics graphics, ItemStack stack, int x, int y, ItemDraw original) {
        Slot slot = fallbackSlot(stack, x, y);
        if (slot == null) {
            original.draw(stack);
            return;
        }
        SlotRenderContext.runFallbackDraw(() -> swapSlotRenderItem(graphics, stack, x, y, slot, original));
    }

    public static void fallbackSlotRenderItemDecorations(GenericGraphics graphics, Font font, ItemStack stack, int x, int y, @Nullable String countText, DecorationDraw original) {
        Slot slot = fallbackSlot(stack, x, y);
        if (slot == null) {
            original.draw(stack, countText);
            return;
        }
        SlotRenderContext.runFallbackDraw(() -> swapSlotRenderItemDecorations(graphics, font, stack, x, y, countText, slot, original));
    }

    // The slot this draw call belongs to, or null if the fallback should leave it alone
    private static @Nullable Slot fallbackSlot(ItemStack stack, int x, int y) {
        Slot slot = SlotRenderContext.current();
        if (slot == null || !SwapManager.hasSwapsTo(slot)) return null;
        if (x != slot.x || y != slot.y) return null;
        if (!ItemStackUtil.isSameType(stack, slot.getItem())) return null;
        return slot;
    }


    // What a slot shows while swaps fly into it: its count minus everything still in flight
    public static int ownCount(Slot slot, int count) {
        return count - inFlightOf(SwapManager.getSwapsTo(slot), slot.getItem());
    }

    //only what will become part of this stack: items flying into a bundle don't increase the bundle count (always 1)
    public static int inFlightOf(List<Swap> swaps, ItemStack slotStack) {
        int inFlight = 0;
        for (Swap swap : swaps) {
            if (ItemStackUtil.isSameType(swap.stack(), slotStack))
                inFlight += swap.amount();
        }
        return inFlight;
    }
}
