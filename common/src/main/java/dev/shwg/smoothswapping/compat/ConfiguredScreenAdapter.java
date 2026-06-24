package dev.shwg.smoothswapping.compat;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.config.Config;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class ConfiguredScreenAdapter extends VanillaLikeScreenAdapter {
    private static final String CONFIGURED_ITEM_SLOTS = "configured_item_slots";
    private static final String REAL_ITEM_SLOTS_ONLY = "real_item_slots_only";

    private final Config.ModdedScreenCompatibilityEntry entry;

    public ConfiguredScreenAdapter(Config.ModdedScreenCompatibilityEntry entry) {
        this.entry = entry;
    }

    @Override
    public boolean matches(Screen screen, AbstractContainerMenu menu) {
        if (screen == null || menu == null || !entry.isEnabled()) {
            return false;
        }

        return entry.getScreenClasses().contains(screen.getClass().getName())
                && entry.getMenuClasses().contains(menu.getClass().getName());
    }

    @Override
    public boolean isRealItemSlot(AbstractContainerMenu menu, int slotId) {
        String slotPolicy = entry.getSlotPolicy();
        if (!CONFIGURED_ITEM_SLOTS.equals(slotPolicy) && !REAL_ITEM_SLOTS_ONLY.equals(slotPolicy)) {
            return super.isRealItemSlot(menu, slotId);
        }

        if (slotId < 0 || slotId >= menu.slots.size()) {
            return false;
        }

        Slot slot = menu.getSlot(slotId);
        if (slot == null || slot.x <= -100 || slot.y <= -100) {
            return false;
        }

        String slotClassName = slot.getClass().getName();
        if (entry.getItemSlotClasses().contains(slotClassName)) {
            return true;
        }

        return entry.shouldIncludePlayerInventorySlots()
                && slot.container instanceof Inventory
                && entry.getPlayerInventorySlotClasses().contains(slotClassName);
    }

    @Override
    public boolean shouldAnimateChangedStacks(AbstractContainerMenu menu, Map<Integer, ItemStack> changedStacks) {
        long clickAnimationWindowMs = entry.getClickAnimationWindowMs();
        if (clickAnimationWindowMs > 0
                && System.currentTimeMillis() - SmoothSwapping.lastContainerClickTime > clickAnimationWindowMs) {
            return false;
        }

        int maxChangedSlots = entry.getMaxChangedSlots();
        return maxChangedSlots < 0 || changedStacks.size() <= maxChangedSlots;
    }

    @Override
    public String name() {
        return entry.getId();
    }
}
