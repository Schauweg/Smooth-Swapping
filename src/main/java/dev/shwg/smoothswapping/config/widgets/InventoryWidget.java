package dev.shwg.smoothswapping.config.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.input.events.GenericKeyEvent;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.textures.GenericTexture;
import dev.shwg.shwgconfig.util.DisplayItemStack;
import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.swap.Swap;
import dev.shwg.smoothswapping.swap.SwapPoint;
import dev.shwg.smoothswapping.util.ItemStackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InventoryWidget extends GenericAbstractWidget {

    private static final GenericTexture CONTAINER_TEXTURE = GenericTexture.vanillaTexture(
            "textures/gui/container/generic_54.png", 256, 256);

    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 222;
    private static final int BORDER_WIDTH = 7;
    private static final int BORDER_WIDTH_TOP = 17;
    private static final int SLOT_SIZE = 18;
    private static final int SPLITTER_HEIGHT = 14;
    private static final int SPLITTER_TEXTURE_V = 125;
    private static final int HOVER_COLOR = 0x80FFFFFF;
    // Lifts flying items above the slots (ignored from 1.21.6, where draw order decides)
    private static final float FLYING_Z = 100;

    private final int columns;
    private final int rows;
    private final List<WidgetSlot> slots = new ArrayList<>();
    private final Component title;
    private final Font font = Minecraft.getInstance().font;

    private WidgetSlot focusedSlot;
    private ItemStack mouseStack = ItemStack.EMPTY;

    private final DragState rightDrag = new DragState(true);
    private final DragState leftDrag = new DragState(false);

    // Preview swaps: same Swap class and easing as in-game, but owned by this widget
    private final Map<WidgetSlot, List<Swap>> slotSwaps = new HashMap<>();
    private final List<Swap> cursorSwaps = new ArrayList<>();
    private int lastMouseX;
    private int lastMouseY;
    private long frameTime;

    public InventoryWidget(int x, int y, int rows, int columns, Component title, Map<Integer, ItemStack> items) {
        super(x, y, columns * SLOT_SIZE + 2 * BORDER_WIDTH, rows * SLOT_SIZE + BORDER_WIDTH_TOP + BORDER_WIDTH + SPLITTER_HEIGHT, title);
        this.title = title;
        this.rows = rows;
        this.columns = columns;

        SimpleContainer container = new SimpleContainer(columns * rows);
        for (int index = 0; index < columns * rows; index++) {
            slots.add(new WidgetSlot(container, index, getSlotX(index), getSlotY(index)));
        }

        items.forEach(container::setItem);
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        frameTime = now();
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        removeFinishedSwaps();

        renderFrame(graphics);
        graphics.drawString(font, title, getX() + 8, getY() + 6, 0xFF404040, false);
        renderSlots(graphics, mouseX, mouseY);
        renderSlotSwaps(graphics);
        renderCursor(graphics, mouseX, mouseY);
    }

    private void renderFrame(GenericGraphics graphics) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        int innerW = w - 2 * BORDER_WIDTH;

        graphics.blit(CONTAINER_TEXTURE, x, y, 0, 0, BORDER_WIDTH, h - BORDER_WIDTH, BORDER_WIDTH, h - BORDER_WIDTH); //left border
        graphics.blit(CONTAINER_TEXTURE, x, y + h - BORDER_WIDTH, 0, TEXTURE_HEIGHT - BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH); //bottom-left corner
        graphics.blit(CONTAINER_TEXTURE, x + BORDER_WIDTH, y, BORDER_WIDTH, 0, innerW, BORDER_WIDTH_TOP, innerW, BORDER_WIDTH_TOP); // top border
        graphics.blit(CONTAINER_TEXTURE, x + w - BORDER_WIDTH, y, TEXTURE_WIDTH - BORDER_WIDTH, 0, BORDER_WIDTH, h - BORDER_WIDTH, BORDER_WIDTH, h - BORDER_WIDTH); //right border
        graphics.blit(CONTAINER_TEXTURE, x + w - BORDER_WIDTH, y + h - BORDER_WIDTH, TEXTURE_WIDTH - BORDER_WIDTH, TEXTURE_HEIGHT - BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH); //bottom-right corner
        graphics.blit(CONTAINER_TEXTURE, x + BORDER_WIDTH, y + h - BORDER_WIDTH, BORDER_WIDTH, TEXTURE_HEIGHT - BORDER_WIDTH, innerW, BORDER_WIDTH, innerW, BORDER_WIDTH); //bottom border
        graphics.blit(CONTAINER_TEXTURE, x + BORDER_WIDTH, y + BORDER_WIDTH_TOP + (rows - 1) * SLOT_SIZE, BORDER_WIDTH, SPLITTER_TEXTURE_V, innerW, SPLITTER_HEIGHT, innerW, SPLITTER_HEIGHT); //splitter (above the hotbar row)
    }

    private void renderSlots(GenericGraphics graphics, int mouseX, int mouseY) {
        focusedSlot = null;

        for (int i = 0; i < slots.size(); i++) {
            WidgetSlot slot = slots.get(i);
            graphics.blit(CONTAINER_TEXTURE, getSlotX(i) - 1, getSlotY(i) - 1, BORDER_WIDTH, BORDER_WIDTH_TOP, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);

            // only what was already there before the swaps flying into this slot
            ItemStack stack = slot.getItem();
            int ownCount = stack.getCount() - inFlight(slotSwaps.get(slot));
            if (ownCount > 0) {
                graphics.renderDecoratedItem(font, ItemStackUtil.copyWithCount(stack, ownCount), slot.x, slot.y);
            }

            if (isPointOverSlot(slot, mouseX, mouseY)) {
                focusedSlot = slot;
                graphics.fillWH(slot.x, slot.y, SLOT_SIZE - 2, SLOT_SIZE - 2, HOVER_COLOR);
            }
        }
    }

    // after all slots, so flying items are drawn on top of every slot
    private void renderSlotSwaps(GenericGraphics graphics) {
        slotSwaps.forEach((slot, swaps) -> {
            for (Swap swap : swaps) {
                renderFlying(graphics, swap, slot.x, slot.y);
            }
        });
    }

    private void renderCursor(GenericGraphics graphics, int mouseX, int mouseY) {
        int cursorX = mouseX - 8;
        int cursorY = mouseY - 8;

        int ownCount = mouseStack.getCount() - inFlight(cursorSwaps);
        if (ownCount > 0) {
            graphics.renderDecoratedItem(font, ItemStackUtil.copyWithCount(mouseStack, ownCount), cursorX, cursorY);
        }
        for (Swap swap : cursorSwaps) {
            renderFlying(graphics, swap, cursorX, cursorY);
        }
    }

    private void renderFlying(GenericGraphics graphics, Swap swap, int destinationX, int destinationY) {
        float eased = swap.easedProgress(frameTime);
        graphics.pushPose();
        graphics.translate(swap.offsetX(destinationX, eased), swap.offsetY(destinationY, eased), FLYING_Z);
        graphics.renderDecoratedItem(font, swap.stack(), destinationX, destinationY);
        graphics.popPose();
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        //silence mouse clicks
    }

    @Override
    protected void updateGenericWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        // intentionally silent
    }

    @Override
    protected boolean onKeyPressed(GenericKeyEvent keyEvent) {
        if (focusedSlot != null) {
            // Number keys 1-9: swap the hovered slot with the matching hotbar slot
            int hotbarIndex = keyEvent.getKey().getValue() - InputConstants.KEY_1;
            if (hotbarIndex >= 0 && hotbarIndex < Math.min(9, columns)) {
                WidgetSlot hotbarSlot = slots.get(hotbarStart() + hotbarIndex);
                if (hotbarSlot != focusedSlot) {
                    swapSlots(focusedSlot, hotbarSlot);
                    return true;
                }
            }
        }
        return super.onKeyPressed(keyEvent);
    }

    @Override
    protected boolean onMouseClicked(GenericMouseButtonEvent event) {
        lastMouseX = (int) event.getMouseX();
        lastMouseY = (int) event.getMouseY();
        WidgetSlot target = getSlotAt(event.getMouseX(), event.getMouseY());

        if (target == null) {
            endDrag(rightDrag, false);
            endDrag(leftDrag, true);
            return false;
        }

        // Pressing the other button cancels the drag that is in progress
        if (event.isLeftClick()) {
            endDrag(rightDrag, false);
        }
        if (event.isRightClick()) {
            endDrag(leftDrag, true);
        }

        if (event.isLeftClick() && event.isDoubleClick() && !mouseStack.isEmpty()) {
            collectMatchingItemsToCursor();
            return true;
        }

        if (event.isLeftClick() && event.isShiftDown() && !target.getItem().isEmpty()) {
            quickMove(target);
            return true;
        }

        if (event.isRightClick()) {
            handleRightClick(target);
            return true;
        }

        if (event.isLeftClick()) {
            handleLeftClick(target);
            return true;
        }

        return super.onMouseClicked(event);
    }

    @Override
    protected boolean onMouseDragged(GenericMouseButtonEvent event, double deltaX, double deltaY) {
        lastMouseX = (int) event.getMouseX();
        lastMouseY = (int) event.getMouseY();
        DragState drag = rightDrag.active ? rightDrag : leftDrag.active ? leftDrag : null;
        if (drag != null) {
            dragOver(drag, getSlotAt(event.getMouseX(), event.getMouseY()));
            return true;
        }
        return super.onMouseDragged(event, deltaX, deltaY);
    }

    @Override
    protected boolean onMouseReleased(GenericMouseButtonEvent event) {
        lastMouseX = (int) event.getMouseX();
        lastMouseY = (int) event.getMouseY();

        if (leftDrag.active) {
            releaseDrag(leftDrag, event.getMouseX(), event.getMouseY(), this::clickSlotWithCursor);
            return true;
        }

        if (rightDrag.active) {
            releaseDrag(rightDrag, event.getMouseX(), event.getMouseY(), this::rightClickSlotWithCursor);
            return true;
        }

        return super.onMouseReleased(event);
    }

    private void handleLeftClick(WidgetSlot slot) {
        if (mouseStack.isEmpty()) {
            ItemStack slotStack = slot.getItem();
            if (!slotStack.isEmpty()) {
                finishSwapsTo(slot);
                mouseStack = slotStack.copy();
                slot.set(ItemStack.EMPTY);
                animateSlotToCursor(slot, mouseStack);
            }
            return;
        }

        startDrag(leftDrag);
        tryAddSlotToDrag(leftDrag, slot);
    }

    private void handleRightClick(WidgetSlot slot) {
        if (mouseStack.isEmpty()) {
            ItemStack slotStack = slot.getItem();
            if (slotStack.isEmpty()) return;

            finishSwapsTo(slot);
            ItemStack remaining = slotStack.copy();
            mouseStack = remaining.split((slotStack.getCount() + 1) / 2);
            slot.set(remaining);
            animateSlotToCursor(slot, mouseStack);
            return;
        }

        startDrag(rightDrag);
        tryAddSlotToDrag(rightDrag, slot);
    }

    private void clickSlotWithCursor(WidgetSlot slot) {
        ItemStack slotStack = slot.getItem();

        if (slotStack.isEmpty()) {
            animateCursorToSlot(slot, mouseStack);
            slot.set(mouseStack.copy());
            mouseStack = ItemStack.EMPTY;
            finishCursorSwaps();
            return;
        }

        if (ItemStackUtil.isSameType(mouseStack, slotStack)) {
            int max = maxStackFor(slot, slotStack);
            int total = slotStack.getCount() + mouseStack.getCount();
            int moved = Math.min(total, max) - slotStack.getCount();

            animateCursorToSlot(slot, ItemStackUtil.copyWithCount(mouseStack, moved));

            ItemStack merged = slotStack.copy();
            merged.setCount(Math.min(total, max));
            slot.set(merged);

            if (total <= max) {
                mouseStack = ItemStack.EMPTY;
                finishCursorSwaps();
            } else {
                mouseStack.setCount(total - max);
            }
            return;
        }

        // Different items: cursor and slot trade places
        finishSwapsTo(slot);
        finishCursorSwaps();
        ItemStack previous = slotStack.copy();
        animateCursorToSlot(slot, mouseStack);
        animateSlotToCursor(slot, previous);
        slot.set(mouseStack.copy());
        mouseStack = previous;
    }

    // A normal right click while holding items: one item into the slot, or a trade if it's a different item
    private void rightClickSlotWithCursor(WidgetSlot slot) {
        ItemStack slotStack = slot.getItem();

        // different item: trade places, same as a left click
        if (!slotStack.isEmpty() && !ItemStackUtil.isSameType(mouseStack, slotStack)) {
            clickSlotWithCursor(slot);
            return;
        }

        // same item, but the slot is full
        if (slotStack.getCount() >= maxStackFor(slot, mouseStack)) return;

        // empty or same item with room: one item moves from the cursor into the slot
        animateCursorToSlot(slot, ItemStackUtil.copyWithCount(mouseStack, 1));
        slot.set(ItemStackUtil.copyWithCount(mouseStack, slotStack.getCount() + 1));
        mouseStack.shrink(1);

        if (mouseStack.isEmpty()) {
            finishCursorSwaps();
        }
    }

    private void collectMatchingItemsToCursor() {
        if (mouseStack.isEmpty()) return;

        int maxCount = mouseStack.getMaxStackSize();
        if (mouseStack.getCount() >= maxCount) return;

        List<WidgetSlot> matching = new ArrayList<>();
        for (WidgetSlot slot : slots) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && ItemStackUtil.isSameType(mouseStack, stack)) {
                matching.add(slot);
            }
        }

        // Smallest stacks first
        matching.sort(Comparator.comparingInt(slot -> slot.getItem().getCount()));

        for (WidgetSlot slot : matching) {
            if (mouseStack.getCount() >= maxCount) break;

            ItemStack remaining = slot.getItem().copy();
            int amount = Math.min(maxCount - mouseStack.getCount(), remaining.getCount());

            finishSwapsTo(slot);
            addSlotToCursorSwap(slot, ItemStackUtil.copyWithCount(remaining, amount));
            mouseStack.grow(amount);
            remaining.shrink(amount);
            slot.set(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
        }
    }

    private void swapSlots(WidgetSlot first, WidgetSlot second) {
        ItemStack firstStack = first.getItem().copy();
        ItemStack secondStack = second.getItem().copy();

        finishSwapsTo(first);
        finishSwapsTo(second);
        animateSlotToSlot(first, second, firstStack);
        animateSlotToSlot(second, first, secondStack);

        first.set(secondStack);
        second.set(firstStack);
    }

    // A drag over 2+ slots keeps its distribution, anything less was just a click on the slot
    private void releaseDrag(DragState drag, double mouseX, double mouseY, Consumer<WidgetSlot> click) {
        if (drag.slots.size() > 1) {
            endDrag(drag, false);
            return;
        }

        WidgetSlot clicked = drag.slots.isEmpty()
                ? getSlotAt(mouseX, mouseY)
                : drag.slots.get(0);
        endDrag(drag, false);
        if (clicked != null) {
            click.accept(clicked);
        }
    }

    private void startDrag(DragState drag) {
        drag.reset();
        drag.active = true;
        drag.startStack = mouseStack.copy();
    }

    private void endDrag(DragState drag, boolean revert) {
        if (drag.active && revert) {
            for (WidgetSlot slot : drag.slots) {
                slot.set(drag.originalStacks.get(slot).copy());
            }
            mouseStack = drag.startStack.copy();
        }
        drag.reset();
    }

    private void dragOver(DragState drag, WidgetSlot hovered) {
        if (hovered != null) {
            tryAddSlotToDrag(drag, hovered);
        }
    }

    private boolean tryAddSlotToDrag(DragState drag, WidgetSlot slot) {
        if (!drag.active || drag.startStack.isEmpty()) return false;
        if (drag.slots.contains(slot)) return false;
        // Never include more slots than there are items to hand out
        if (drag.slots.size() >= drag.startStack.getCount()) return false;

        ItemStack current = slot.getItem();
        if (!current.isEmpty() && !ItemStackUtil.isSameType(drag.startStack, current)) return false;
        if (current.getCount() >= maxStackFor(slot, drag.startStack)) return false;

        drag.slots.add(slot);
        drag.originalStacks.put(slot, current.copy());
        redistribute(drag);
        return true;
    }

    private void redistribute(DragState drag) {
        if (drag.slots.size() < 2) return;

        int perSlot = drag.oneItemPerSlot ? 1 : drag.startStack.getCount() / drag.slots.size();
        int used = 0;

        for (WidgetSlot slot : drag.slots) {
            ItemStack original = drag.originalStacks.get(slot);
            int add = Math.min(perSlot, maxStackFor(slot, drag.startStack) - original.getCount());

            if (add > 0) {
                ItemStack updated = drag.startStack.copy();
                updated.setCount(original.getCount() + add);
                slot.set(updated);
                used += add;
            } else {
                slot.set(original.copy());
            }
        }

        mouseStack = drag.startStack.copy();
        mouseStack.shrink(used);
    }

    private void quickMove(WidgetSlot source) {
        ItemStack remaining = source.getItem().copy();
        finishSwapsTo(source);

        if (isSlotInHotbar(source.getContainerSlot())) {
            insertIntoRange(source, remaining, 0, hotbarStart(), false);
        } else {
            insertIntoRange(source, remaining, hotbarStart(), slots.size(), true);
        }

        source.set(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
    }

    private void insertIntoRange(WidgetSlot source, ItemStack stack, int start, int end, boolean reverse) {
        int count = end - start;

        for (int pass = 0; pass < 2; pass++) {
            boolean intoEmpty = pass == 1;
            for (int n = 0; n < count && !stack.isEmpty(); n++) {
                WidgetSlot slot = slots.get(reverse ? end - 1 - n : start + n);
                insertIntoSlot(source, stack, slot, intoEmpty);
            }
        }
    }

    private void insertIntoSlot(WidgetSlot sourceSlot, ItemStack source, WidgetSlot slot, boolean intoEmpty) {
        ItemStack target = slot.getItem();

        if (intoEmpty) {
            if (!target.isEmpty()) return;
        } else if (target.isEmpty() || !ItemStackUtil.isSameType(source, target)) {
            return;
        }

        int amount = Math.min(source.getCount(), maxStackFor(slot, source) - target.getCount());
        if (amount <= 0) return;

        animateSlotToSlot(sourceSlot, slot, ItemStackUtil.copyWithCount(source, amount));

        ItemStack updated = source.copy();
        updated.setCount(target.getCount() + amount);
        slot.set(updated);
        source.shrink(amount);
    }

    private void animateSlotToSlot(WidgetSlot from, WidgetSlot to, ItemStack moved) {
        if (moved.isEmpty() || from == to) return;
        Swap swap = Swap.of(SwapPoint.slot(from.getContainerSlot()), SwapPoint.slot(to.getContainerSlot()), moved,
                from.x, from.y, to.x, to.y, frameTime);
        slotSwaps.computeIfAbsent(to, s -> new ArrayList<>()).add(swap);
    }

    private void animateSlotToCursor(WidgetSlot from, ItemStack moved) {
        if (!animateCursorClicks()) return;
        addSlotToCursorSwap(from, moved);
    }

    // No setting check: the double-click collect is always animated, like in-game
    private void addSlotToCursorSwap(WidgetSlot from, ItemStack moved) {
        if (moved.isEmpty()) return;
        Swap swap = Swap.of(SwapPoint.slot(from.getContainerSlot()), SwapPoint.CURSOR, moved,
                from.x, from.y, lastMouseX - 8, lastMouseY - 8, frameTime);
        cursorSwaps.add(swap);
    }

    private void animateCursorToSlot(WidgetSlot to, ItemStack moved) {
        if (moved.isEmpty() || !animateCursorClicks()) return;
        Swap swap = Swap.of(SwapPoint.CURSOR, SwapPoint.slot(to.getContainerSlot()), moved,
                lastMouseX - 8, lastMouseY - 8, to.x, to.y, frameTime);
        slotSwaps.computeIfAbsent(to, s -> new ArrayList<>()).add(swap);
    }

    private static boolean animateCursorClicks() {
        return SmoothSwapping.CONFIG.animateCursorClicks.getValue();
    }

    // a slot that loses items: whatever was still flying into it lands instantly
    private void finishSwapsTo(WidgetSlot slot) {
        slotSwaps.remove(slot);
    }

    private void finishCursorSwaps() {
        cursorSwaps.clear();
    }

    private void removeFinishedSwaps() {
        slotSwaps.values().removeIf(swaps -> {
            swaps.removeIf(swap -> swap.isFinished(frameTime));
            return swaps.isEmpty();
        });
        cursorSwaps.removeIf(swap -> swap.isFinished(frameTime));
    }

    private static int inFlight(List<Swap> swaps) {
        if (swaps == null) return 0;
        int amount = 0;
        for (Swap swap : swaps) amount += swap.amount();
        return amount;
    }

    // monotonic milliseconds, independent of SwapManager's in-game frame time
    private static long now() {
        return System.nanoTime() / 1_000_000L;
    }

    // --- Layout

    private WidgetSlot getSlotAt(double mouseX, double mouseY) {
        for (WidgetSlot slot : slots) {
            if (isPointOverSlot(slot, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private boolean isPointOverSlot(WidgetSlot slot, double mouseX, double mouseY) {
        return mouseX >= slot.x && mouseX < slot.x + SLOT_SIZE
                && mouseY >= slot.y && mouseY < slot.y + SLOT_SIZE;
    }

    private int getSlotX(int index) {
        return getX() + 1 + BORDER_WIDTH + (index % columns) * SLOT_SIZE;
    }

    private int getSlotY(int index) {
        int row = index / columns;
        return getY() + 1 + BORDER_WIDTH_TOP + row * SLOT_SIZE + (isSlotInHotbar(index) ? SPLITTER_HEIGHT : 0);
    }

    private int hotbarStart() {
        return (rows - 1) * columns;
    }

    private boolean isSlotInHotbar(int index) {
        return index >= hotbarStart();
    }

    private static int maxStackFor(Slot slot, ItemStack stack) {
        return Math.min(stack.getMaxStackSize(), slot.getMaxStackSize(stack));
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        for (int i = 0; i < slots.size(); i++) {
            slots.get(i).x = getSlotX(i);
        }
        clearSwaps();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        for (int i = 0; i < slots.size(); i++) {
            slots.get(i).y = getSlotY(i);
        }
        clearSwaps();
    }

    // swaps store absolute start positions, which are wrong once the widget moves
    private void clearSwaps() {
        slotSwaps.clear();
        cursorSwaps.clear();
    }

    private static final class DragState {

        private final boolean oneItemPerSlot;
        private final List<WidgetSlot> slots = new ArrayList<>();
        private final Map<WidgetSlot, ItemStack> originalStacks = new HashMap<>();
        private ItemStack startStack = ItemStack.EMPTY;
        private boolean active;

        private DragState(boolean oneItemPerSlot) {
            this.oneItemPerSlot = oneItemPerSlot;
        }

        private void reset() {
            active = false;
            slots.clear();
            originalStacks.clear();
            startStack = ItemStack.EMPTY;
        }
    }

    // shadows Slot's final x/y, only correct when accessed through the WidgetSlot type
    public static class WidgetSlot extends Slot {
        public int x;
        public int y;

        public WidgetSlot(final Container container, final int slot, final int x, final int y) {
            super(container, slot, x, y);
            this.x = x;
            this.y = y;
        }
    }

    public static Builder builder(Component title) {
        return new Builder(title);
    }

    public static final class Builder extends GenericAbstractWidget.Builder<InventoryWidget, Builder> {

        private int rows = 3;
        private int columns = 3;
        private final Map<Integer, ItemStack> items = new HashMap<>();

        public Builder(Component title) {
            super(title);
        }

        @Override
        public Builder size(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;
            return self();
        }

        @Override
        public Builder bounds(int x, int y, int rows, int columns) {
            this.x = x;
            this.y = y;
            this.rows = rows;
            this.columns = columns;
            return self();
        }

        @Override
        public Builder width(int columns) {
            this.columns = columns;
            return self();
        }

        @Override
        public Builder height(int rows) {
            this.rows = rows;
            return self();
        }

        public Builder rows(int rows) {
            this.rows = rows;
            return self();
        }

        public Builder columns(int columns) {
            this.columns = columns;
            return self();
        }

        public Builder addItem(Item item, int count, int slot) {
            items.put(slot, DisplayItemStack.getStackSimple(item, count));
            return self();
        }

        public Builder addItem(Item item, int slot) {
            items.put(slot, DisplayItemStack.getStackSimple(item, 1));
            return self();
        }

        @Override
        protected InventoryWidget create() {
            int maxSlots = rows * columns - 1;
            items.forEach((slot, stack) -> {
                if (slot > maxSlots) {
                    throw new IllegalArgumentException("Can't put items into slot: " + slot + " highest slot is: " + maxSlots);
                }
            });
            return new InventoryWidget(x, y, rows, columns, message, items);
        }
    }
}
