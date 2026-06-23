package dev.shwg.smoothswapping.compat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public interface ScreenCompatibilityAdapter {
    boolean matches(Screen screen, AbstractContainerMenu menu);

    default boolean shouldHandle(Screen screen, AbstractContainerMenu menu) {
        return true;
    }

    default boolean isRealItemSlot(AbstractContainerMenu menu, int slotId) {
        if (slotId < 0 || slotId >= menu.slots.size()) {
            return false;
        }
        Slot slot = menu.getSlot(slotId);
        return slot != null && slot.x > -100 && slot.y > -100;
    }

    default boolean canAnimateStackChange(AbstractContainerMenu menu, int slotId, Player player) {
        return isRealItemSlot(menu, slotId) && menu.getSlot(slotId).allowModification(player);
    }

    String name();
}
