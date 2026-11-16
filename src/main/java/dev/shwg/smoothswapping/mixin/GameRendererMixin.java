package dev.shwg.smoothswapping.mixin;

import dev.shwg.smoothswapping.swap.SwapManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /*~ if >=26 'render' -> 'extract' */
    @Inject(method = "render", at = @At("HEAD"))
    private void smoothswapping$frame(CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
            SwapManager.onFrame(screen);
        }
    }
}
