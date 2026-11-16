package dev.shwg.smoothswapping.mixin;

import org.spongepowered.asm.mixin.Mixin;

/*? if >=1.20 {*/
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.shwg.shwgconfig.render.GenericGraphics;
import dev.shwg.shwgconfig.render.GraphicsAccessor;
import dev.shwg.smoothswapping.swap.SlotRenderContext;
import dev.shwg.smoothswapping.swap.SwapRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiGraphics.class)
public class GraphicsMixin {

    // Fallback for slots whose renderSlot is overridden (vanilla's renderSlot wraps mark their slots as handled)

    /*? if >=26 {*/
    //@WrapMethod(method = "item(Lnet/minecraft/world/item/ItemStack;II)V")
    /*?} else {*/
    @WrapMethod(method = "renderItem(Lnet/minecraft/world/item/ItemStack;II)V")
     /*?}*/
    private void smoothswapping$fallbackItem(ItemStack stack, int x, int y, Operation<Void> original) {
        if (!SlotRenderContext.needsFallback()) {
            original.call(stack, x, y);
            return;
        }
        SwapRenderer.fallbackSlotRenderItem(smoothswapping$graphics(), stack, x, y, s -> original.call(s, x, y));
    }

    /*? if >=26 {*/
    //@WrapMethod(method = "item(Lnet/minecraft/world/item/ItemStack;III)V")
    /*?} else {*/
    @WrapMethod(method = "renderItem(Lnet/minecraft/world/item/ItemStack;III)V")
     /*?}*/
    private void smoothswapping$fallbackItemSeeded(ItemStack stack, int x, int y, int seed, Operation<Void> original) {
        if (!SlotRenderContext.needsFallback()) {
            original.call(stack, x, y, seed);
            return;
        }
        SwapRenderer.fallbackSlotRenderItem(smoothswapping$graphics(), stack, x, y, s -> original.call(s, x, y, seed));
    }

    /*? if >=26 {*/
    //@WrapMethod(method = "itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V")
    /*?} else {*/
    @WrapMethod(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V")
     /*?}*/
    private void smoothswapping$fallbackDecorations(Font font, ItemStack stack, int x, int y, @Nullable String countText, Operation<Void> original) {
        if (!SlotRenderContext.needsFallback()) {
            original.call(font, stack, x, y, countText);
            return;
        }
        SwapRenderer.fallbackSlotRenderItemDecorations(smoothswapping$graphics(), font, stack, x, y, countText,
                (s, text) -> original.call(font, s, x, y, text));
    }

    @Unique
    private GenericGraphics smoothswapping$graphics() {
        return ((GraphicsAccessor) this).shwgConfigs$getGraphics();
    }
}
/*?} else {*/
/*import net.minecraft.client.Minecraft;
@Mixin(Minecraft.class)
public abstract class GraphicsMixin {
}
*//*?}*/
