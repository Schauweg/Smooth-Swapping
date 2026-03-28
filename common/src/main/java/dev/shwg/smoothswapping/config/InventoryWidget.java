package dev.shwg.smoothswapping.config;

import dev.shwg.smoothswapping.SwapUtil;
import dev.shwg.smoothswapping.mixin.SimpleContainerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static dev.shwg.smoothswapping.SmoothSwapping.currentStacks;
import static dev.shwg.smoothswapping.SmoothSwapping.oldStacks;

public class InventoryWidget extends AbstractWidget {

    private static final Identifier TEXTURE = Identifier.parse("textures/gui/container/generic_54.png");
    private static final Identifier SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.withDefaultNamespace("container/slot_highlight_back");

    private static final int textureWidth = 176;
    private static final int textureHeight = 222;
    private static final int borderWidth = 7;
    private static final int slotHeight = 18;
    private static final int borderWidthTop = 17;
    private static final int splitterHeight = 14;
    private final int columns, rows;
    public final NonNullList<Slot> slots = NonNullList.create();
    private final Component title;
    private Slot focusedSlot;
    private ItemStack mouseStack;
    SimpleContainer inventory;

    public InventoryWidget(int x, int y, int columns, int rows, Component title) {
        super(x, y, columns * slotHeight + 2 * borderWidth, rows * slotHeight + borderWidthTop + borderWidth + splitterHeight, CommonComponents.EMPTY);
        this.title = title;
        this.columns = columns;
        this.rows = rows;
        inventory = new SimpleContainer(columns * rows);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Slot slot = new Slot(inventory, slots.size(), x + 1 + borderWidth + column * slotHeight, y + 1 + borderWidthTop + row * slotHeight + (row == rows - 1 ? splitterHeight : 0));
                slot.setByPlayer(ItemStack.EMPTY);
                slots.add(slot);
            }
        }
        inventory.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
        mouseStack = ItemStack.EMPTY;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {

        //Render Border
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), 0, 0, borderWidth, height - borderWidth, 256, 256); //left border
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY() + height - borderWidth, 0, textureHeight - borderWidth, borderWidth, borderWidth, 256, 256); //bottom left corner
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + borderWidth, this.getY(), borderWidth, 0, width - 2 * borderWidth, borderWidthTop, 256, 256); //top border
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + width - borderWidth, this.getY(), textureWidth - borderWidth, 0, borderWidth, height - borderWidth, 256, 256); //right border
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + width - borderWidth, this.getY() + height - borderWidth, textureWidth - borderWidth, textureHeight - borderWidth, borderWidth, borderWidth, 256, 256); //bottom right corner
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + borderWidth, this.getY() + height - borderWidth, borderWidth, textureHeight - borderWidth, width - 2 * borderWidth, borderWidth, 256, 256); //bottom border
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + borderWidth, this.getY() + borderWidthTop + (rows - 1) * slotHeight, borderWidth, 125, width - 2 * borderWidth, splitterHeight, 256, 256); //splitter

        //Render slots texture
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX() + borderWidth + column * slotHeight, this.getY() + borderWidthTop + row * slotHeight + (row == rows - 1 ? splitterHeight : 0), borderWidth, borderWidthTop, slotHeight, slotHeight, 256, 256);
            }
        }

        Font textRenderer = Minecraft.getInstance().font;
        int maxNameWidth = this.width - 2 * borderWidth - 2;
        FormattedText trimmedName = title;
        if (textRenderer.width(title) > maxNameWidth) {
            trimmedName = FormattedText.composite(textRenderer.substrByWidth(title, maxNameWidth - textRenderer.width(CommonComponents.ELLIPSIS)), CommonComponents.ELLIPSIS);
        }

        context.text(textRenderer, trimmedName.getString(), this.getX() + 8, this.getY() + 6, 4210752, false);

        for (Slot slot : this.slots) {
            if (isPointOverSlot(slot, mouseX, mouseY) && slot.isActive()) {
                this.focusedSlot = slot;
                if (this.focusedSlot != null && this.focusedSlot.isHighlightable()) {
                    context.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_TEXTURE, this.focusedSlot.x - 4, this.focusedSlot.y - 4, 24, 24);
                }
            }

            if (slot.isActive()) {
                this.drawSlot(context, slot);
            }
        }

        if (!mouseStack.isEmpty()) {
            int x = mouseX - 8;
            int y = mouseY - 8;
            context.item(mouseStack, x, y);
            context.itemDecorations(Minecraft.getInstance().font, mouseStack, x, y);
        }
    }

    private boolean isPointOverSlot(Slot slot, double mouseX, double mouseY) {
        if (slot == null) return false;
        int x = slot.x;
        int y = slot.y;
        return mouseX >= (double) x && mouseX < (double) (x + slotHeight) && mouseY >= (double) y && mouseY < (double) (y + slotHeight);
    }

    private void drawSlot(GuiGraphicsExtractor context, Slot slot) {
        ItemStack itemStack = slot.getItem();
        context.item(itemStack, slot.x, slot.y);
        context.itemDecorations(Minecraft.getInstance().font, itemStack, slot.x, slot.y);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == 0) {
            if (isPointOverSlot(focusedSlot, click.x(), click.y())) {
                if (click.hasShiftDown()) {
                    //Focused Slot is in last row
                    if (focusedSlot.getContainerSlot() >= slots.size() - columns) {
                        for (int i = 0; i < slots.size() - columns; i++) {
                            if (moveItems(i)) return true;
                        }
                    } else {
                        for (int i = slots.size() - 1; i >= slots.size() - columns; i--) {
                            if (moveItems(i)) return true;
                        }
                    }
                } else {
                    if (mouseStack.isEmpty() && !focusedSlot.getItem().isEmpty()) {
                        mouseStack = focusedSlot.getItem().copy();
                        focusedSlot.setByPlayer(ItemStack.EMPTY);
                        return true;
                    } else if (!mouseStack.isEmpty() && focusedSlot.getItem().isEmpty()) {
                        focusedSlot.setByPlayer(mouseStack.copy());
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
        if (slot.getItem().isEmpty()) {
            currentStacks = ((SimpleContainerAccessor) inventory).getStacks();
            SwapUtil.copyStacks(currentStacks, oldStacks);
            slot.setByPlayer(focusedSlot.getItem());
            focusedSlot.setByPlayer(ItemStack.EMPTY);
            SwapUtil.addI2IInventorySwap(slot.getContainerSlot(), focusedSlot, slot, false, slot.getItem().getCount());
            return true;
        }
        return false;
    }
}
