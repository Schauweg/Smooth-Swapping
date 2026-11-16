package dev.shwg.smoothswapping.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.smoothswapping.swap.SlotRenderContext;
import dev.shwg.smoothswapping.swap.SwapRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*? if >=1.20 { */
import net.minecraft.client.gui.GuiGraphics;
/*?} else { */
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
*//*?} */

/*? if >=1.19.4 { */
import dev.shwg.shwgconfig.render.GraphicsAccessor;
/*?} else { */
//import com.mojang.blaze3d.systems.RenderSystem;
/*?} */

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;

    // funneling render item inside render slot into own rendering method
    /*? if >=26 { */
    /*/^~ if neoforge 'extractSlot' -> 'renderSlotContents'^/
    @WrapOperation(method = "extractSlot", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;item(Lnet/minecraft/world/item/ItemStack;III)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fakeItem(Lnet/minecraft/world/item/ItemStack;III)V")
    })
    private void smoothswapping$renderSlotItem(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y, int seed, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        SlotRenderContext.markHandled();
        GenericGraphics graphics = ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics();
        SwapRenderer.swapSlotRenderItem(graphics, itemStack, x, y, slot, stack -> original.call(guiGraphics, stack, x, y, seed));
    *//*? else if >=1.20 { */
    /*~ if neoforge 'renderSlot' -> 'renderSlotContents'*/
    @WrapOperation(method = "renderSlot", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V")
            /*? if >= 1.20.3 */
            ,@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderFakeItem(Lnet/minecraft/world/item/ItemStack;III)V")
    })
    private void smoothswapping$renderSlotItem(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y, int seed, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        SlotRenderContext.markHandled();
        GenericGraphics graphics = ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics();
        SwapRenderer.swapSlotRenderItem(graphics, itemStack, x, y, slot, stack -> original.call(guiGraphics, stack, x, y, seed));
    /*? else if >=1.19.4 { */
    /*@WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"))
    private void smoothswapping$renderSlotItem(ItemRenderer instance, PoseStack poseStack, LivingEntity player, ItemStack itemStack, int x, int y, int seed, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        SlotRenderContext.markHandled();
        GenericGraphics graphics = ((GraphicsAccessor) poseStack).shwgConfigs$getGraphics();
        SwapRenderer.swapSlotRenderItem(graphics, itemStack, x, y, slot, stack -> original.call(instance, poseStack, player, stack, x, y, seed));
    *//*? else { */
    /*@WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"))
    private void smoothswapping$renderSlotItem(ItemRenderer instance, LivingEntity player, ItemStack itemStack, int x, int y, int seed, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        SlotRenderContext.markHandled();
        PoseStack offsetPose = new PoseStack();
        GenericGraphics graphics = new GenericGraphics(offsetPose);

        SwapRenderer.swapSlotRenderItem(graphics, itemStack, x, y, slot, stack -> {
            PoseStack modelView = RenderSystem.getModelViewStack();
            modelView.pushPose();
            /^~ if >=1.19.3 'multiply' -> 'mul' ^/
            modelView.last().pose().mul(offsetPose.last().pose());
            RenderSystem.applyModelViewMatrix();
            try {
                original.call(instance, player, stack, x, y, seed);
            } finally {
                modelView.popPose();
                RenderSystem.applyModelViewMatrix();
            }
        });
        *//*?} */
    }

    // funneling render item decorations inside render slot into own rendering method
    /*? if >=26 { */
    /*/^~ if neoforge 'extractSlot' -> 'renderSlotContents'^/
    @WrapOperation(method = "extractSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void smoothswapping$renderSlotItemDecoration(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int x, int y, String countText, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        SlotRenderContext.markHandled();
        GenericGraphics graphics = ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics();
        SwapRenderer.swapSlotRenderItemDecorations(graphics, font, itemStack, x, y, countText, slot, (stack, text) -> original.call(guiGraphics, font, stack, x, y, text));
    *//*? else if >=1.20 { */
    /*~ if neoforge 'renderSlot' -> 'renderSlotContents'*/
    @WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void smoothswapping$renderSlotItemDecoration(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int x, int y, String countText, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        SlotRenderContext.markHandled();
        GenericGraphics graphics = ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics();
        SwapRenderer.swapSlotRenderItemDecorations(graphics, font, itemStack, x, y, countText, slot, (stack, text) -> original.call(guiGraphics, font, stack, x, y, text));
    /*? else if >=1.19.4 { */
    /*@WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void smoothswapping$renderSlotItemDecoration(ItemRenderer instance, PoseStack poseStack, Font font, ItemStack itemStack, int x, int y, String countText, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        SlotRenderContext.markHandled();
        GenericGraphics graphics = ((GraphicsAccessor) poseStack).shwgConfigs$getGraphics();
        SwapRenderer.swapSlotRenderItemDecorations(graphics, font, itemStack, x, y, countText, slot, (stack, text) -> original.call(instance, poseStack, font, stack, x, y, text));
    *//*? else { */
    /*@WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void smoothswapping$renderSlotItemDecoration(ItemRenderer instance, Font font, ItemStack itemStack, int x, int y, String countText, Operation<Void> original, @Local(argsOnly = true) Slot slot) {
        SlotRenderContext.markHandled();
        PoseStack offsetPose = new PoseStack();
        GenericGraphics graphics = new GenericGraphics(offsetPose);

        SwapRenderer.swapSlotRenderItemDecorations(graphics, font, itemStack, x, y, countText, slot, (stack, text) -> {
            PoseStack modelView = RenderSystem.getModelViewStack();
            modelView.pushPose();
            /^~ if >=1.19.3 'multiply' -> 'mul' ^/
            modelView.last().pose().mul(offsetPose.last().pose());
            RenderSystem.applyModelViewMatrix();
            try {
                original.call(instance, font, stack, x, y, text);
            } finally {
                modelView.popPose();
                RenderSystem.applyModelViewMatrix();
            }
        });
        *//*?} */
    }

    /*? if >=26.2 {*/
    /*@Unique
    private boolean smoothswapping$isSnapback(ItemStack stack) {
        return false;
    }
    *//*? } else if >=1.21.6 {*/
    @Unique private static boolean smoothswapping$inSnapback;

    /*~ if >=26 'renderSnapbackItem' -> 'extractSnapbackItem' */
    @WrapMethod(method = "renderSnapbackItem")
    private void smoothswapping$markSnapback(GuiGraphics guiGraphics, Operation<Void> original) {
        smoothswapping$inSnapback = true;
        try {
            original.call(guiGraphics);
        } finally {
            smoothswapping$inSnapback = false;
        }
    }

    @Unique
    private boolean smoothswapping$isSnapback(ItemStack stack) {
        return smoothswapping$inSnapback;
    }
    /*?} else {*/
    /*@Shadow private ItemStack snapbackItem;

    @Unique
    private boolean smoothswapping$isSnapback(ItemStack stack) {
        return stack == snapbackItem;
    }
    *//*?}*/


    // funneling render item with decorations inside render floating item into own rendering method
    /*? if >=26 {*/
    /*@WrapMethod(method = "extractFloatingItem")
    private void smoothswapping$renderFloatingItem(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y, String countText, Operation<Void> original) {
        if (smoothswapping$isSnapback(itemStack)) {
            original.call(guiGraphics, itemStack, x, y, countText);
            return;
        }
        GenericGraphics graphics = ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics();
        SwapRenderer.swapMouseRenderItemAndDecorations(graphics, itemStack, x - leftPos, y - topPos, countText,
                (stack, text) -> original.call(guiGraphics, stack, x, y, text));
    }
    *//*?} else if >=1.20 {*/
    @WrapMethod(method = "renderFloatingItem")
    private void smoothswapping$renderFloatingItem(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y, String countText, Operation<Void> original) {
        if (smoothswapping$isSnapback(itemStack)) {
            original.call(guiGraphics, itemStack, x, y, countText);
            return;
        }
        GenericGraphics graphics = ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics();
        /*? if >=1.21.6 {*/
        int localX = x - leftPos, localY = y - topPos;
        /*?} else {*/
        //int localX = x, localY = y;
        /*?}*/
        SwapRenderer.swapMouseRenderItemAndDecorations(graphics, itemStack, localX, localY, countText,
                (stack, text) -> original.call(guiGraphics, stack, x, y, text));
    }
    /*?} else if >=1.19.4 {*/
    /*@WrapMethod(method = "renderFloatingItem")
    private void smoothswapping$renderFloatingItem(PoseStack poseStack, ItemStack itemStack, int x, int y, String countText, Operation<Void> original) {
        if (smoothswapping$isSnapback(itemStack)) {
            original.call(poseStack, itemStack, x, y, countText);
            return;
        }
        GenericGraphics graphics = ((GraphicsAccessor) poseStack).shwgConfigs$getGraphics();
        SwapRenderer.swapMouseRenderItemAndDecorations(graphics, itemStack, x, y, countText,
                (stack, text) -> original.call(poseStack, stack, x, y, text));
    }
    *//*?} else {*/
    /*@WrapMethod(method = "renderFloatingItem")
    private void smoothswapping$renderFloatingItem(ItemStack itemStack, int x, int y, String countText, Operation<Void> original) {
        if (smoothswapping$isSnapback(itemStack)) {
            original.call(itemStack, x, y, countText);
            return;
        }
        PoseStack offsetPose = new PoseStack();
        GenericGraphics graphics = new GenericGraphics(offsetPose);

        SwapRenderer.swapMouseRenderItemAndDecorations(graphics, itemStack, x, y, countText, (stack, text) -> {
            PoseStack modelView = RenderSystem.getModelViewStack();
            modelView.pushPose();
            /^~ if >=1.19.3 'multiply' -> 'mul' ^/
            modelView.last().pose().mul(offsetPose.last().pose());
            RenderSystem.applyModelViewMatrix();
            try {
                original.call(stack, x, y, text);
            } finally {
                modelView.popPose();
                RenderSystem.applyModelViewMatrix();
            }
        });
    }
    *//*?}*/


    // actually rendering flying items
    /*? if >=1.21.6 { */
    /*~ if >=26 'renderCarriedItem' -> 'extractCarriedItem' */
    @Inject(method = "renderCarriedItem", at = @At(value = "HEAD"))
    private void smoothswapping$renderCarriedItemHead(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        GenericGraphics g = ((GraphicsAccessor) guiGraphics).shwgConfigs$getGraphics();
        g.renderDeferred(mouseX, mouseY, 0);
    }
    /*?} */

    /*? if <1.20 {*/
    /*@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lcom/mojang/blaze3d/vertex/PoseStack;II)V", shift = At.Shift.AFTER))
    private void smoothswapping$flushLegacyDeferred(CallbackInfo ci) {
        SwapRenderer.flushLegacyDeferred();
    }
    *//*?} else if <1.21.6 {*/
    /*@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V", shift = At.Shift.AFTER))
    private void smoothswapping$flushLegacyDeferred(CallbackInfo ci) {
        SwapRenderer.flushLegacyDeferred();
    }
    *//*?}*/


    // Slot context for the fallback: wraps the renderSlot call in vanilla's slot loop (reaches renderSlot overrides)
    /*? if >=26 {*/
    /*@WrapOperation(method = "extractSlots", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V"))
    private void smoothswapping$slotContext(AbstractContainerScreen<?> instance, GuiGraphics graphics, Slot slot, int mouseX, int mouseY, Operation<Void> original) {
        SlotRenderContext.run(slot, () -> original.call(instance, graphics, slot, mouseX, mouseY));
    }
    *//*?} else if >=1.21.11 {*/
    @WrapOperation(method = "renderSlots", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V"))
    private void smoothswapping$slotContext(AbstractContainerScreen<?> instance, GuiGraphics graphics, Slot slot, int mouseX, int mouseY, Operation<Void> original) {
        SlotRenderContext.run(slot, () -> original.call(instance, graphics, slot, mouseX, mouseY));
    }
    /*?} else if >=1.21.2 {*/
    /*@WrapOperation(method = "renderSlots", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"))
    private void smoothswapping$slotContext(AbstractContainerScreen<?> instance, GuiGraphics graphics, Slot slot, Operation<Void> original) {
        SlotRenderContext.run(slot, () -> original.call(instance, graphics, slot));
    }
    *//*?} else if >=1.20 {*/
    /*@WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"))
    private void smoothswapping$slotContext(AbstractContainerScreen<?> instance, GuiGraphics graphics, Slot slot, Operation<Void> original) {
        SlotRenderContext.run(slot, () -> original.call(instance, graphics, slot));
    }
    *//*?} else {*/
    /*@WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/inventory/Slot;)V"))
    private void smoothswapping$slotContext(AbstractContainerScreen<?> instance, PoseStack poseStack, Slot slot, Operation<Void> original) {
        SlotRenderContext.run(slot, () -> original.call(instance, poseStack, slot));
    }
    *//*?}*/
}
