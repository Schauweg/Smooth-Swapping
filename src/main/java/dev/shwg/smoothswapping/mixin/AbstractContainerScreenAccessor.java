package dev.shwg.smoothswapping.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor("isQuickCrafting")
    boolean smoothswapping$isQuickCrafting();

    @Accessor("quickCraftSlots")
    Set<Slot> smoothswapping$getQuickCraftSlots();

    @Accessor("leftPos")
    int smoothswapping$getLeftPos();

    @Accessor("topPos")
    int smoothswapping$getTopPos();
}
