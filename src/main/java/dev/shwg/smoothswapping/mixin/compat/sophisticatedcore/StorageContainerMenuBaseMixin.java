package dev.shwg.smoothswapping.mixin.compat.sophisticatedcore;

import dev.shwg.smoothswapping.swap.ExtraSlotsMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/**
 * Sophisticated keeps its upgrade slots (and the slots inside upgrade tabs) in its own list instead of
 * menu.slots, so the detector would never see them. Their indices continue after menu.slots.
 * The field has the same name and type in every SophisticatedCore branch from 1.19.x to 26.3.
 */
@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase", remap = false)
public abstract class StorageContainerMenuBaseMixin implements ExtraSlotsMenu {

    @Shadow
    @Final
    public List<Slot> upgradeSlots;

    @Override
    public List<Slot> smoothswapping$getExtraSlots() {
        return upgradeSlots;
    }
}
