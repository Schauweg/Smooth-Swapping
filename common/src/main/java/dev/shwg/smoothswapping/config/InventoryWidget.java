package dev.shwg.smoothswapping.config;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.shwg.smoothswapping.SwapUtil;
import dev.shwg.smoothswapping.mixin.SimpleInventoryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import static dev.shwg.smoothswapping.SmoothSwapping.*;
//import static net.minecraft.client.gui.screen.ingame.HandledScreen.;

@SuppressWarnings("SuspiciousNameCombination")
public class InventoryWidget extends ClickableWidget {

    private static final Identifier TEXTURE = Identifier.of("textures/gui/container/generic_54.png");
    private static final Identifier SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.ofVanilla("container/slot_highlight_back");

    private static final int textureWidth = 176;
    private static final int textureHeight = 222;
    private static final int borderWidth = 7;
    private static final int slotHeight = 18;
    private static final int borderWidthTop = 17;
    private static final int splitterHeight = 14;
    private final int columns, rows;
    public final DefaultedList<Slot> slots = DefaultedList.of();
    private final Text title;
    private Slot focusedSlot;
    private ItemStack mouseStack;
    SimpleInventory inventory;

    public InventoryWidget(int x, int y, int columns, int rows, Text title) {
        super(x, y, columns * slotHeight + 2 * borderWidth, rows * slotHeight + borderWidthTop + borderWidth + splitterHeight, ScreenTexts.EMPTY);
        this.title = title;
        this.columns = columns;
        this.rows = rows;
        inventory = new SimpleInventory(columns * rows);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Slot slot = new Slot(inventory, slots.size(), x + 1 + borderWidth + column * slotHeight, y + 1 + borderWidthTop + row * slotHeight + (row == rows - 1 ? splitterHeight : 0));
                slot.setStack(ItemStack.EMPTY);
                slots.add(slot);
            }
        }
        inventory.setStack(0, new ItemStack(Items.COBBLESTONE, 32));
        mouseStack = ItemStack.EMPTY;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {

        //Render Border
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), 0, 0, borderWidth, height - borderWidth, 256, 256); //left border
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY() + height - borderWidth, 0, textureHeight - borderWidth, borderWidth, borderWidth, 256, 256); //bottom left corner
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + borderWidth, this.getY(), borderWidth, 0, width - 2 * borderWidth, borderWidthTop, 256, 256); //top border
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + width - borderWidth, this.getY(), textureWidth - borderWidth, 0, borderWidth, height - borderWidth, 256, 256); //right border
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + width - borderWidth, this.getY() + height - borderWidth, textureWidth - borderWidth, textureHeight - borderWidth, borderWidth, borderWidth, 256, 256); //bottom right corner
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + borderWidth, this.getY() + height - borderWidth, borderWidth, textureHeight - borderWidth, width - 2 * borderWidth, borderWidth, 256, 256); //bottom border
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + borderWidth, this.getY() + borderWidthTop + (rows - 1) * slotHeight, borderWidth, 125, width - 2 * borderWidth, splitterHeight, 256, 256); //splitter

        //Render slots texture
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + borderWidth + column * slotHeight, this.getY() + borderWidthTop + row * slotHeight + (row == rows - 1 ? splitterHeight : 0), borderWidth, borderWidthTop, slotHeight, slotHeight, 256, 256);
            }
        }

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int maxNameWidth = this.width - 2 * borderWidth - 2;
        StringVisitable trimmedName = title;
        if (textRenderer.getWidth(title) > maxNameWidth) {
            trimmedName = StringVisitable.concat(textRenderer.trimToWidth(title, maxNameWidth - textRenderer.getWidth(ScreenTexts.ELLIPSIS)), ScreenTexts.ELLIPSIS);
        }

        context.drawText(textRenderer, trimmedName.getString(), this.getX() + 8, this.getY() + 6, 4210752, false);

        for (Slot slot : this.slots) {
            if (slot.isEnabled()) {
                this.drawSlot(context, slot);
            }

            if (isPointOverSlot(slot, mouseX, mouseY) && slot.isEnabled()) {
                this.focusedSlot = slot;
                if (this.focusedSlot != null && this.focusedSlot.canBeHighlighted()) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_TEXTURE, this.focusedSlot.x - 4, this.focusedSlot.y - 4, 24, 24);
                }
            }
        }

        if (!mouseStack.isEmpty()) {
            int x = mouseX - 8;
            int y = mouseY - 8;
            context.drawItem(mouseStack, x, y);
            context.drawStackOverlay(MinecraftClient.getInstance().textRenderer, mouseStack, x, y);
        }
    }

    private boolean isPointOverSlot(Slot slot, double mouseX, double mouseY) {
        if (slot == null) return false;
        int x = slot.x;
        int y = slot.y;
        return mouseX >= (double) x && mouseX < (double) (x + slotHeight) && mouseY >= (double) y && mouseY < (double) (y + slotHeight);
    }

    private void drawSlot(DrawContext context, Slot slot) {
        ItemStack itemStack = slot.getStack();
        context.drawItem(itemStack, slot.x, slot.y);
        context.drawStackOverlay(MinecraftClient.getInstance().textRenderer, itemStack, slot.x, slot.y);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isPointOverSlot(focusedSlot, mouseX, mouseY)) {
                if (Screen.hasShiftDown()) {
                    //Focused Slot is in last row
                    if (focusedSlot.getIndex() >= slots.size() - columns) {
                        for (int i = 0; i < slots.size() - columns; i++) {
                            if (moveItems(i)) return true;
                        }
                    } else {
                        for (int i = slots.size() - 1; i >= slots.size() - columns; i--) {
                            if (moveItems(i)) return true;
                        }
                    }
                } else {
                    if (mouseStack.isEmpty() && !focusedSlot.getStack().isEmpty()) {
                        mouseStack = focusedSlot.getStack().copy();
                        focusedSlot.setStack(ItemStack.EMPTY);
                        return true;
                    } else if (!mouseStack.isEmpty() && focusedSlot.getStack().isEmpty()) {
                        focusedSlot.setStack(mouseStack.copy());
                        mouseStack = ItemStack.EMPTY;
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private boolean moveItems(int index) {
        Slot slot = slots.get(index);
        if (slot.getStack().isEmpty()) {
            currentStacks = ((SimpleInventoryAccessor) inventory).getStacks();
            SwapUtil.copyStacks(currentStacks, oldStacks);
            slot.setStack(focusedSlot.getStack());
            focusedSlot.setStack(ItemStack.EMPTY);
            SwapUtil.addI2IInventorySwap(slot.getIndex(), focusedSlot, slot, false, slot.getStack().getCount());
            return true;
        }
        return false;
    }
}
