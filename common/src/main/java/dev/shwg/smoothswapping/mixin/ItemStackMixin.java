package dev.shwg.smoothswapping.mixin;

import dev.shwg.smoothswapping.ItemStackAccessor;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class ItemStackMixin implements ItemStackAccessor {

    @Unique
    private boolean isSwapStack;

    @Override
    public void smooth_Swapping$setIsSwapStack(boolean isSwapStack) {
        this.isSwapStack = isSwapStack;
    }

    @Override
    public boolean smooth_Swapping$isSwapStack() {
        return this.isSwapStack;
    }
}
