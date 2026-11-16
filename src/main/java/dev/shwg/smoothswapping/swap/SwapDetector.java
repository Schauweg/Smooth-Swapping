package dev.shwg.smoothswapping.swap;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.mixin.AbstractContainerScreenAccessor;
import dev.shwg.smoothswapping.util.ContainerContents;
import dev.shwg.smoothswapping.util.ItemStackUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Function;

public final class SwapDetector {

    //ItemStacks from the last rendered frame, used for comparing against current rendered stacks
    private static final NonNullList<ItemStack> lastStacks = NonNullList.create();
    private static ItemStack lastCarriedStack;
    private static Screen lastScreen;
    private static AbstractContainerMenu lastMenu;
    private static boolean wasQuickCrafting;
    private static final Set<Integer> lastQuickCraftSlots = new HashSet<>();

    private SwapDetector() {
    }

    /**
     * Detects which items moved since the last frame and creates the swaps that animate them.
     * Called once per frame, before anything is drawn.
     *
     * <ol>
     *   <li>Check whether last frame's snapshot still belongs to this screen via
     *       {@link #isSnapshotValid(AbstractContainerScreen)}. It's invalid if the screen, the menu or the
     *       number of slots changed. If it's invalid, take a fresh snapshot, clear all swaps and the drag
     *       state, and animate nothing this frame.</li>
     *   <li>Compare the current menu with the snapshot via {@link #diff(AbstractContainerMenu, int, int)}.
     *       Every slot and the cursor that lost items becomes a source, every one that gained items
     *       becomes a destination. For bundles and shulker boxes, the items inside are compared instead
     *       of the container itself.</li>
     *   <li>If anything changed:
     *     <ol>
     *       <li>For every source that still has swaps flying into it: if a different item is there now,
     *           those swaps finish instantly. Otherwise, they keep flying, trimmed to what's still there
     *           (e.g. a hopper took a few items).</li>
     *       <li>Pair sources with destinations of the same item, closest first, and create a swap for
     *           every pair via {@link #createSwaps}. Skipped are: items a finished drag-split already
     *           showed in their slots, and simple cursor clicks if they're disabled in the config
     *           (a double-click collect is always animated).</li>
     *       <li>Take a new snapshot, so the next frame compares against the current state.</li>
     *     </ol>
     *   </li>
     *   <li>Remember the drag-split state (whether one is running, and over which slots), so the next
     *       frame can tell when a drag-split ends.</li>
     * </ol>
     *
     * Items that only appear or only disappear (nothing to pair with) don't animate: they simply
     * show up or vanish, like in vanilla.
     */
    public static void detect(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        if (!isSnapshotValid(screen)) {
            takeSnapshot(screen);
            SwapManager.reset();
            wasQuickCrafting = false;
            lastQuickCraftSlots.clear();
            return;
        }

        AbstractContainerMenu menu = screen.getMenu();
        Diff diff = diff(menu, mouseX, mouseY);

        var accessor = (AbstractContainerScreenAccessor) screen;
        boolean isQuickCrafting = accessor.smoothswapping$isQuickCrafting();
        boolean dragJustEnded = wasQuickCrafting && !isQuickCrafting;

        if (!diff.isEmpty()) {
            for (SlotChange source : diff.sources) {
                ItemStack current = source.slot != null ? source.slot.getItem() : menu.getCarried();
                List<Swap> incoming = SwapManager.getSwapsTo(source.swapPoint);
                if (incoming.isEmpty()) continue;

                if (!allSameType(incoming, current)) {
                    SwapManager.finishSwapsTo(source.swapPoint); //a different item is there now
                } else {
                    SwapManager.trimSwapsTo(source.swapPoint, current.getCount()); //keep flying, with what's left
                }
            }
            ItemStack carried = menu.getCarried();
            boolean cursorCollected = !lastCarriedStack.isEmpty()
                    && ItemStackUtil.isSameType(lastCarriedStack, carried)
                    && carried.getCount() > lastCarriedStack.getCount();
            createSwaps(candidatePairs(diff), dragJustEnded, lastQuickCraftSlots, cursorCollected);
            takeSnapshot(screen);
        }

        wasQuickCrafting = isQuickCrafting;
        lastQuickCraftSlots.clear();
        if (isQuickCrafting) {
            for (Slot s : accessor.smoothswapping$getQuickCraftSlots()) lastQuickCraftSlots.add(s.index);
        }
    }

    public static boolean isSnapshotValid(AbstractContainerScreen<?> screen) {
        AbstractContainerMenu menu = screen.getMenu();
        return screen == lastScreen
                && menu == lastMenu
                && lastStacks.size() == allSlots(menu).size();
    }

    public static void takeSnapshot(AbstractContainerScreen<?> screen) {
        AbstractContainerMenu menu = screen.getMenu();
        lastScreen = screen;
        lastMenu = menu;
        lastCarriedStack = menu.getCarried().copy();
        lastStacks.clear();
        for (Slot slot : allSlots(menu)) {
            lastStacks.add(slot.getItem().copy());
        }
    }


    private static Diff diff(AbstractContainerMenu menu, int mouseX, int mouseY) {
        Diff diff = new Diff();
        List<Slot> slots = allSlots(menu);
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            addChanges(diff, lastStacks.get(i), slot.getItem(), stack -> SlotChange.slot(slot, stack));
        }

        addChanges(diff, lastCarriedStack, menu.getCarried(), stack -> SlotChange.cursor(mouseX, mouseY, stack));

        return diff;
    }

    private static void addChanges(Diff diff, ItemStack oldStack, ItemStack newStack, Function<ItemStack, SlotChange> toChange) {
        if (ItemStack.matches(oldStack, newStack)) return;

        if (ContainerContents.isSameContainer(oldStack, newStack)) {
            addContentChanges(diff, ContainerContents.getContents(oldStack), ContainerContents.getContents(newStack), toChange);
        }else if (ItemStackUtil.isSameType(oldStack, newStack)) {
            int countDelta = newStack.getCount() - oldStack.getCount();
            if (countDelta > 0) {
                diff.destinations.add(toChange.apply(ItemStackUtil.copyWithCount(newStack, countDelta)));
            } else if (countDelta < 0) {
                diff.sources.add(toChange.apply(ItemStackUtil.copyWithCount(oldStack, -countDelta)));
            }
        } else {
            if (!newStack.isEmpty()) {
                diff.destinations.add(toChange.apply(newStack.copy()));
            }
            if (!oldStack.isEmpty()) {
                diff.sources.add(toChange.apply(oldStack));
            }
        }
    }

    private static void addContentChanges(Diff diff, List<ItemStack> oldContents, List<ItemStack> newContents, Function<ItemStack, SlotChange> toChange) {
        for (ItemStack type : distinctTypes(oldContents, newContents)) {
            int countDelta = countOf(newContents, type) - countOf(oldContents, type);
            if (countDelta == 0) continue;

            if (countDelta > 0) {
                diff.destinations.add(toChange.apply(ItemStackUtil.copyWithCount(type, countDelta)));
            } else {
                diff.sources.add(toChange.apply(ItemStackUtil.copyWithCount(type, -countDelta)));
            }
        }
    }

    //one stack per item type that appears in either list
    private static List<ItemStack> distinctTypes(List<ItemStack> a, List<ItemStack> b) {
        List<ItemStack> types = new ArrayList<>();
        for (ItemStack stack : a) addIfNewType(types, stack);
        for (ItemStack stack : b) addIfNewType(types, stack);
        return types;
    }

    private static void addIfNewType(List<ItemStack> types, ItemStack stack) {
        for (ItemStack type : types) {
            if (ItemStackUtil.isSameType(type, stack)) return;
        }
        types.add(stack);
    }

    // How many items of this type the list holds in total, across all its entries
    private static int countOf(List<ItemStack> stacks, ItemStack type) {
        int count = 0;
        for (ItemStack stack : stacks){
            if (ItemStackUtil.isSameType(stack, type)){
                count += stack.getCount();
            }
        }
        return count;
    }

    private static List<Pair> candidatePairs(Diff diff) {
        List<Pair> pairs = new ArrayList<>();
        if (diff.sources.isEmpty() || diff.destinations.isEmpty()) return pairs;

        for (SlotChange source : diff.sources) {
            for (SlotChange destination : diff.destinations) {
                if (source.slot == destination.slot) continue;
                if (!ItemStackUtil.isSameType(source.stack, destination.stack)) continue;
                pairs.add(new Pair(source, destination));
            }
        }

        pairs.sort(Comparator.comparingInt(pair -> pair.distanceSq));
        return pairs;
    }

    private static void createSwaps(List<Pair> pairs, boolean dragJustEnded, Set<Integer> previewSlots, boolean cursorCollected) {
        for (Pair pair : pairs) {
            SlotChange source = pair.source;
            SlotChange target = pair.destination;
            int amount = Math.min(source.remaining, target.remaining);
            if (amount == 0) continue;

            // use up the amounts first, so skipped pairs can't be paired again
            source.remaining -= amount;
            target.remaining -= amount;

            boolean shownByPreview = dragJustEnded
                    && source.swapPoint.type() == SwapPoint.Type.CURSOR
                    && target.swapPoint.type() == SwapPoint.Type.SLOT
                    && previewSlots.contains(target.slot.index);
            if (shownByPreview) continue;

            //check whether pickup of items with mouse should be animated
            boolean involvesCursor = source.swapPoint.type() == SwapPoint.Type.CURSOR
                    || target.swapPoint.type() == SwapPoint.Type.CURSOR;
            boolean isCollect = cursorCollected && target.swapPoint.type() == SwapPoint.Type.CURSOR;
            if (involvesCursor && !isCollect && !SmoothSwapping.CONFIG.animateCursorClicks.getValue()) continue;

            ItemStack stack = ItemStackUtil.copyWithCount(source.stack, amount);
            Swap swap = switch (source.swapPoint.type()) {
                case SLOT -> switch (target.swapPoint.type()) {
                    case SLOT -> Swap.slotToSlot(source.slot, target.slot, stack);
                    case CURSOR -> Swap.slotToCursor(source.slot, target.x, target.y, stack);
                };
                case CURSOR -> switch (target.swapPoint.type()) {
                    case SLOT -> Swap.cursorToSlot(source.x, source.y, target.slot, stack);
                    case CURSOR -> null;   // can't happen: candidatePairs never pairs the cursor with itself
                };
            };

            if (swap == null) {
                SmoothSwapping.LOGGER.warn("Skipped impossible swap {} -> {}", source.swapPoint, target.swapPoint);
                continue;
            }
            SwapManager.addSwap(swap);
        }
    }

    public static void invalidate() {
        lastScreen = null;
        lastMenu = null;
        lastStacks.clear();
        lastCarriedStack = ItemStack.EMPTY;
        wasQuickCrafting = false;
        lastQuickCraftSlots.clear();
    }

    private static List<Slot> allSlots(AbstractContainerMenu menu) {
        if (menu instanceof ExtraSlotsMenu extra) {
            List<Slot> extraSlots = extra.smoothswapping$getExtraSlots();
            if (!extraSlots.isEmpty()) {
                List<Slot> all = new ArrayList<>(menu.slots.size() + extraSlots.size());
                all.addAll(menu.slots);
                all.addAll(extraSlots);
                return all;
            }
        }
        return menu.slots;
    }

    private static boolean allSameType(List<Swap> swaps, ItemStack current) {
        for (Swap swap : swaps) {
            if (!ItemStackUtil.isSameType(swap.stack(), current)) return false;
        }
        return true;
    }

    private static final class Diff {
        final List<SlotChange> sources = new ArrayList<>();
        final List<SlotChange> destinations = new ArrayList<>();

        boolean isEmpty() {
            return sources.isEmpty() && destinations.isEmpty();
        }
    }

    private static final class SlotChange {
        final Slot slot;
        final int x, y;
        final SwapPoint swapPoint;
        final ItemStack stack;
        int remaining;

        SlotChange(Slot slot, SwapPoint swapPoint, int x, int y, ItemStack stack) {
            this.slot = slot;
            this.swapPoint = swapPoint;
            this.x = x;
            this.y = y;
            this.stack = stack;
            this.remaining = stack.getCount();
        }

        static SlotChange slot(Slot slot, ItemStack stack) {
            return new SlotChange(slot, SwapPoint.slot(slot.index), slot.x, slot.y, stack);
        }

        static SlotChange cursor(int mouseX, int mouseY, ItemStack stack) {
            return new SlotChange(null, SwapPoint.CURSOR, mouseX, mouseY, stack);
        }
    }

    private static final class Pair {
        final SlotChange source;
        final SlotChange destination;
        final int distanceSq;

        Pair(SlotChange source, SlotChange destination) {
            this.source = source;
            this.destination = destination;
            int dx = source.x - destination.x;
            int dy = source.y - destination.y;
            this.distanceSq = dx * dx + dy * dy;
        }
    }
}
