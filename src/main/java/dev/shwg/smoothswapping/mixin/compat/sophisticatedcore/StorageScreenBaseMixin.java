package dev.shwg.smoothswapping.mixin.compat.sophisticatedcore;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/*? if >=1.20 {*/
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.shwg.shwgconfig.render.GraphicsAccessor;
import dev.shwg.smoothswapping.swap.SlotRenderContext;
import dev.shwg.smoothswapping.swap.SwapManager;
import dev.shwg.smoothswapping.swap.SwapRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase", remap = false)
public abstract class StorageScreenBaseMixin {

    /*? if >=26 {*/
    /*@WrapMethod(method = "extractSlot", require = 0)
    private void smoothswapping$slotContext(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, Operation<Void> original) {
        SlotRenderContext.run(slot, () -> original.call(guiGraphics, slot, mouseX, mouseY));
    }
    *//*?} else if >=1.21.11 {*/
    @WrapMethod(method = "renderSlot", require = 0)
    private void smoothswapping$slotContext(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, Operation<Void> original) {
        SlotRenderContext.run(slot, () -> original.call(guiGraphics, slot, mouseX, mouseY));
    }

    @Inject(method = "renderSuper", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/p3pp3rf1y/sophisticatedcore/client/gui/StorageScreenBase;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            shift = At.Shift.AFTER))
    private void smoothswapping$flushDeferred(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics().renderDeferred(mouseX, mouseY, partialTick);
    }
    /*?} else if >=1.21.6 {*/
    /*@WrapMethod(method = "renderSlot", require = 0)
    private void smoothswapping$slotContext(GuiGraphics guiGraphics, Slot slot, Operation<Void> original) {
        SlotRenderContext.run(slot, () -> original.call(guiGraphics, slot));
    }

    @Inject(method = "renderSuper", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/p3pp3rf1y/sophisticatedcore/client/gui/StorageScreenBase;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            shift = At.Shift.AFTER))
    private void smoothswapping$flushDeferred(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics().renderDeferred(mouseX, mouseY, partialTick);
    }
    *//*?} else {*/
    /*@WrapMethod(method = "renderSlot", require = 0)
    private void smoothswapping$slotContext(GuiGraphics guiGraphics, Slot slot, Operation<Void> original) {
        SlotRenderContext.run(slot, () -> original.call(guiGraphics, slot));
    }

    @Inject(method = "renderSuper", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/p3pp3rf1y/sophisticatedcore/client/gui/StorageScreenBase;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            shift = At.Shift.AFTER))
    private void smoothswapping$flushDeferred(CallbackInfo ci) {
        SwapRenderer.flushLegacyDeferred();
    }
    *//*?}*/

    /*~ if >=26 'renderStack' -> 'extractStack' */
    @WrapOperation(method = "renderStack", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/p3pp3rf1y/sophisticatedcore/util/CountAbbreviator;abbreviate(I)Ljava/lang/String;"))
    private String smoothswapping$ownCountText(int count, Operation<String> original) {
        Slot slot = SlotRenderContext.current();
        if (slot == null || !SwapManager.hasSwapsTo(slot)) {
            return original.call(count);
        }
        int own = SwapRenderer.ownCount(slot, count);
        return own > 0 ? original.call(own) : "";
    }
}
/*?} else {*/
/*@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase", remap = false)
public abstract class StorageScreenBaseMixin {
}
*//*?}*/
