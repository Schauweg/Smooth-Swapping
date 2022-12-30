package schauweg.smoothswapping.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import schauweg.smoothswapping.SmoothSwapping;
import schauweg.smoothswapping.SwapUtil;

import static net.minecraft.client.gui.screen.ingame.HandledScreen.drawSlotHighlight;
import static schauweg.smoothswapping.SmoothSwapping.*;
import static schauweg.smoothswapping.SwapUtil.addI2IInventorySwap;

@SuppressWarnings("SuspiciousNameCombination")
public class InventoryWidget extends ClickableWidget {

    private static final Identifier TEXTURE = new Identifier("textures/gui/container/generic_54.png");

    private static final int textureWidth = 176;
    private static final int textureHeight = 222;
    private static final int borderWidth = 7;
    private static final int slotHeight = 18;
    private static final int borderWidthTop = 17;
    private static final int splitterHeight = 14;
    private final int columns, rows;
    ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.renderBackground(matrices, MinecraftClient.getInstance(), mouseX, mouseY);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int maxNameWidth = this.width - 2 * borderWidth - 2;
        StringVisitable trimmedName = title;
        if (textRenderer.getWidth(title) > maxNameWidth) {
            trimmedName = StringVisitable.concat(textRenderer.trimToWidth(title, maxNameWidth - textRenderer.getWidth(ScreenTexts.ELLIPSIS)), ScreenTexts.ELLIPSIS);
        }
        textRenderer.draw(matrices,trimmedName.getString(), this.getX() + 8, this.getY() + 6,4210752);

        for (Slot slot : this.slots) {
            if (slot.isEnabled()) {
                this.drawSlot(slot);
            }

            if (isPointOverSlot(slot, mouseX, mouseY) && slot.isEnabled()) {
                this.focusedSlot = slot;
                drawSlotHighlight(matrices, slot.x, slot.y, this.getZOffset());
            }
        }

        if (!mouseStack.isEmpty()) {
            int x = mouseX - 8;
            int y = mouseY - 8;
            itemRenderer.renderInGuiWithOverrides(mouseStack, x, y);
            itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, mouseStack, x, y);
        }
    }

    @Override
    protected void renderBackground(MatrixStack matrices, MinecraftClient client, int mouseX, int mouseY) {
        //Render Border
        this.drawTexture(matrices, this.getX(), this.getY(), 0, 0, borderWidth, height - borderWidth); //left border
        this.drawTexture(matrices, this.getX(), this.getY() + height - borderWidth, 0, textureHeight - borderWidth, borderWidth, borderWidth); //bottom left corner
        this.drawTexture(matrices, this.getX() + borderWidth, this.getY(), borderWidth, 0, width - 2 * borderWidth, borderWidthTop); //top border
        this.drawTexture(matrices, this.getX() + width - borderWidth, this.getY(), textureWidth - borderWidth, 0, borderWidth, height - borderWidth); //right border
        this.drawTexture(matrices, this.getX() + width - borderWidth, this.getY() + height - borderWidth, textureWidth - borderWidth, textureHeight - borderWidth, borderWidth, borderWidth); //bottom right corner
        this.drawTexture(matrices, this.getX() + borderWidth, this.getY() + height - borderWidth, borderWidth, textureHeight - borderWidth, width - 2 * borderWidth, borderWidth); //bottom border
        this.drawTexture(matrices, this.getX() + borderWidth, this.getY() + borderWidthTop + (rows - 1) * slotHeight, borderWidth, 125, width - 2 * borderWidth, splitterHeight); //splitter

        //Render slots texture
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                this.drawTexture(matrices, this.getX() + borderWidth + column * slotHeight, this.getY() + borderWidthTop + row * slotHeight + (row == rows - 1 ? splitterHeight : 0), borderWidth, borderWidthTop, slotHeight, slotHeight);
            }
        }
    }

    private boolean isPointOverSlot(Slot slot, double mouseX, double mouseY) {
        if (slot == null) return false;
        int x = slot.x;
        int y = slot.y;
        return mouseX >= (double) x && mouseX < (double) (x + slotHeight) && mouseY >= (double) y && mouseY < (double) (y + slotHeight);
    }

    private void drawSlot(Slot slot) {
        ItemStack itemStack = slot.getStack();
        itemRenderer.renderInGuiWithOverrides(itemStack, slot.x, slot.y);
        itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, itemStack, slot.x, slot.y);
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
            SmoothSwapping.currentStacks = inventory.stacks;
            SwapUtil.copyStacks(currentStacks, oldStacks);
            slot.setStack(focusedSlot.getStack());
            focusedSlot.setStack(ItemStack.EMPTY);
            addI2IInventorySwap(slot.getIndex(), focusedSlot, slot, false, slot.getStack().getCount());
            return true;
        }
        return false;
    }
}
