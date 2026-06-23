package dev.shwg.smoothswapping.compat;

import dev.shwg.smoothswapping.SmoothSwapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class TravelersBackpackAdapter extends VanillaLikeScreenAdapter {
    private static final String BACKPACK_SCREEN = "com.tiviacz.travelersbackpack.client.screens.BackpackScreen";
    private static final String BACKPACK_ITEM_MENU = "com.tiviacz.travelersbackpack.inventory.menu.BackpackItemMenu";
    private static final String BACKPACK_BLOCK_MENU = "com.tiviacz.travelersbackpack.inventory.menu.BackpackBlockEntityMenu";
    private static final String BACKPACK_STORAGE_SLOT = "com.tiviacz.travelersbackpack.inventory.menu.slot.BackpackSlotItemHandler";
    private static final String VANILLA_SLOT = "net.minecraft.world.inventory.Slot";
    private static final long CLICK_ANIMATION_WINDOW_MS = 1000L;
    private static final int MAX_SAFE_CLICK_CHANGED_SLOTS = 12;

    @Override
    public boolean matches(Screen screen, AbstractContainerMenu menu) {
        if (screen == null || menu == null || !BACKPACK_SCREEN.equals(screen.getClass().getName())) {
            return false;
        }

        String menuClassName = menu.getClass().getName();
        return BACKPACK_ITEM_MENU.equals(menuClassName) || BACKPACK_BLOCK_MENU.equals(menuClassName);
    }

    @Override
    public boolean isRealItemSlot(AbstractContainerMenu menu, int slotId) {
        if (slotId < 0 || slotId >= menu.slots.size()) {
            return false;
        }

        Slot slot = menu.getSlot(slotId);
        if (slot == null || slot.x <= -100 || slot.y <= -100) {
            return false;
        }

        String slotClassName = slot.getClass().getName();
        return BACKPACK_STORAGE_SLOT.equals(slotClassName)
                || (VANILLA_SLOT.equals(slotClassName) && slot.container instanceof Inventory);
    }

    @Override
    public boolean shouldAnimateChangedStacks(AbstractContainerMenu menu, Map<Integer, ItemStack> changedStacks) {
        return System.currentTimeMillis() - SmoothSwapping.lastContainerClickTime <= CLICK_ANIMATION_WINDOW_MS
                && changedStacks.size() <= MAX_SAFE_CLICK_CHANGED_SLOTS;
    }

    @Override
    public String name() {
        return "travelers_backpack";
    }
}
