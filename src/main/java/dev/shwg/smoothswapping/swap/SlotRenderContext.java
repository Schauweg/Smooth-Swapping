package dev.shwg.smoothswapping.swap;

import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public final class SlotRenderContext {

    private static @Nullable Slot current;
    private static boolean handledByVanilla;
    private static boolean fallbackDrawing;

    private SlotRenderContext() {}

    // Runs one slot render with this slot as context, restores the previous one afterwards
    public static void run(Slot slot, Runnable render) {
        Slot previousSlot = current;
        boolean previousHandled = handledByVanilla;
        current = slot;
        handledByVanilla = false;
        try {
            render.run();
        } finally {
            current = previousSlot;
            handledByVanilla = previousHandled;
        }
    }

    public static void runFallbackDraw(Runnable draw) {
        boolean previous = fallbackDrawing;
        fallbackDrawing = true;
        try {
            draw.run();
        } finally {
            fallbackDrawing = previous;
        }
    }

    public static @Nullable Slot current() {
        return current;
    }

    // Called by the vanilla renderSlot wraps, so the fallback leaves this slot alone
    public static void markHandled() {
        handledByVanilla = true;
    }

    // Fallback only acts inside a slot render that didn't go through vanilla's renderSlot
    public static boolean needsFallback() {
        return current != null && !handledByVanilla && !fallbackDrawing;
    }
}
