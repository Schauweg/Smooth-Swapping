package dev.shwg.smoothswapping.swap;

import com.mojang.blaze3d.platform.Window;
import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.mixin.AbstractContainerScreenAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*? if >=1.21.11 {*/
import net.minecraft.util.Util;
/*?} else {*/
//import net.minecraft.Util;
/*?}*/

public final class SwapManager {

    private static long frameTime;
    private static final Int2ObjectOpenHashMap<List<Swap>> INVENTORY_SWAPS = new Int2ObjectOpenHashMap<>();
    private static final List<Swap> CURSOR_SWAPS = new ArrayList<>();

    private static boolean errorReported;

    private SwapManager() {
    }

    public static void onFrame(AbstractContainerScreen<?> screen) {
        SwapRenderer.beginFrame();

        try {
            if (!SmoothSwapping.CONFIG.modEnabled() || screen instanceof CreativeModeInventoryScreen) {
                reset();
                SwapDetector.invalidate();
                return;
            }

            frameTime = Util.getMillis();

            //removing swaps that arrived this frame
            CURSOR_SWAPS.removeIf(swap -> swap.isFinished(frameTime));
            for (List<Swap> swaps : INVENTORY_SWAPS.values()) {
                swaps.removeIf(swap -> swap.isFinished(frameTime));
            }

            Minecraft mc = Minecraft.getInstance();
            Window window = mc.getWindow();
            int mouseX = (int) (mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth());
            int mouseY = (int) (mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());

            var accessor = (AbstractContainerScreenAccessor) screen;
            int localX = mouseX - accessor.smoothswapping$getLeftPos() - 8;
            int localY = mouseY - accessor.smoothswapping$getTopPos() - 8;

            //run actual detection for this frame
            SwapDetector.detect(screen, localX, localY);
        } catch (Throwable throwable) {
            reportError(throwable);
            reset();
            SwapDetector.invalidate();
        }

    }

    public static long getFrameTime() {
        return frameTime;
    }

    public static List<Swap> getSwapsTo(int slotIndex) {
        List<Swap> swaps = INVENTORY_SWAPS.get(slotIndex);
        return swaps == null ? Collections.emptyList() : Collections.unmodifiableList(swaps);
    }

    public static List<Swap> getSwapsTo(Slot slot) {
        return getSwapsTo(slot.index);
    }

    public static List<Swap> getSwapsTo(SwapPoint point) {
        List<Swap> swaps = switch (point.type()) {
            case SLOT -> INVENTORY_SWAPS.get(point.slotIndex());
            case CURSOR -> CURSOR_SWAPS;
        };
        return swaps == null ? Collections.emptyList() : Collections.unmodifiableList(swaps);
    }

    public static boolean hasSwapsTo(int slotIndex) {
        List<Swap> swaps = INVENTORY_SWAPS.get(slotIndex);
        return swaps != null && !swaps.isEmpty();
    }

    public static boolean hasSwapsTo(Slot slot) {
        return hasSwapsTo(slot.index);
    }

    public static List<Swap> getCursorSwaps() {
        return Collections.unmodifiableList(CURSOR_SWAPS);
    }

    public static void finishSwapsTo(SwapPoint point) {
        List<Swap> swaps = switch (point.type()) {
            case SLOT -> INVENTORY_SWAPS.get(point.slotIndex());
            case CURSOR -> CURSOR_SWAPS;
        };
        if (swaps != null) {
            swaps.clear();
        }
    }

    public static void trimSwapsTo(SwapPoint point, int available) {
        List<Swap> swaps = switch (point.type()) {
            case SLOT -> INVENTORY_SWAPS.get(point.slotIndex());
            case CURSOR -> CURSOR_SWAPS;
        };
        if (swaps == null) return;

        int excess = -available;
        for (Swap swap : swaps) excess += swap.amount();

        for (int i = swaps.size() - 1; i >= 0 && excess > 0; i--) {
            Swap swap = swaps.get(i);
            int cut = Math.min(excess, swap.amount());
            excess -= cut;
            if (cut == swap.amount()) {
                swaps.remove(i);
            } else {
                swaps.set(i, swap.withAmount(swap.amount() - cut));
            }
        }
    }

    public static void addSwap(Swap swap) {
        List<Swap> list = switch (swap.target().type()) {
            case SLOT -> slotList(swap.target().slotIndex());
            case CURSOR -> CURSOR_SWAPS;
        };
        list.add(swap);
    }

    private static List<Swap> slotList(int slotIndex) {
        List<Swap> list = INVENTORY_SWAPS.get(slotIndex);
        if (list == null) {
            list = new ArrayList<>();
            INVENTORY_SWAPS.put(slotIndex, list);
        }
        return list;
    }

    public static void reset() {
        INVENTORY_SWAPS.clear();
        CURSOR_SWAPS.clear();
    }

    private static void reportError(Throwable t) {
        if (errorReported) return;
        errorReported = true;
        SmoothSwapping.LOGGER.error("Smooth Swapping hit an error and reset its animations, please report this", t);
    }
}
