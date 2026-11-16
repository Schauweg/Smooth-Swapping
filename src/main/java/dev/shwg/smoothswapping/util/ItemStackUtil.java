package dev.shwg.smoothswapping.util;

import net.minecraft.world.item.ItemStack;

public class ItemStackUtil {

    public static boolean isSameType(ItemStack a, ItemStack b) {
        /*? if >=1.20.5 {*/
        return ItemStack.isSameItemSameComponents(a, b);
        /*?} else {*/
        //return ItemStack.isSameItemSameTags(a, b);
        /*?}*/
    }

    public static ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }
}
