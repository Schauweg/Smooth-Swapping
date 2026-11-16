package dev.shwg.smoothswapping.swap;

import net.minecraft.world.inventory.Slot;

import java.util.List;

// Menus that keep slots outside menu.slots (e.g. Sophisticated's upgrade slots).
// Indices must continue after menu.slots, as Sophisticated's do.
public interface ExtraSlotsMenu {
    List<Slot> smoothswapping$getExtraSlots();
}
