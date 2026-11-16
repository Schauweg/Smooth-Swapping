package dev.shwg.smoothswapping.util;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/*? if >= 1.20.5 {*/
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
/*?} else {*/
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
*//*?}*/


public final class ContainerContents {

    private ContainerContents() {}

    public static boolean hasContents(ItemStack itemStack) {
        /*? if >= 1.20.5 {*/
        BundleContents bundle = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null && !bundle.isEmpty()) return true;

        ItemContainerContents container = itemStack.get(DataComponents.CONTAINER);
        return container != null && !container.equals(ItemContainerContents.EMPTY);
        /*?} else {*/
        /*CompoundTag tag = itemStack.getTag();
        if (tag == null) return false;

        // bundle: items directly in the tag
        if (!tag.getList("Items", Tag.TAG_COMPOUND).isEmpty()) return true;

        // shulker box, chest, ...: items inside the block entity data
        return !tag.getCompound("BlockEntityTag").getList("Items", Tag.TAG_COMPOUND).isEmpty();
        *//*?}*/
    }

    public static boolean isSameContainer(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() == newStack.getItem()
                && oldStack.getCount() == newStack.getCount()
                && (hasContents(oldStack) || hasContents(newStack));
    }

    public static List<ItemStack> getContents(ItemStack itemStack) {
        /*? if >= 1.20.5 {*/
        BundleContents bundle = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null && !bundle.isEmpty()) {
            /*~ if >=26.3 '.itemCopyStream()' -> '.itemCopies()'*/
            return bundle.itemCopyStream().toList();
        }

        ItemContainerContents container = itemStack.get(DataComponents.CONTAINER);
        if (container != null && !container.equals(ItemContainerContents.EMPTY)) {
            /*~ if >=26 '.nonEmptyStream()' -> '.nonEmptyItemCopyStream()'*/
            return container.nonEmptyStream().toList();
        }
        return List.of();
        /*?} else {*/
        /*CompoundTag tag = itemStack.getTag();
        if (tag == null) return List.of();

        // bundle: items directly in the tag, shulker box / chest: inside the block entity data
        ListTag items = tag.contains("Items", Tag.TAG_LIST)
                ? tag.getList("Items", Tag.TAG_COMPOUND)
                : tag.getCompound("BlockEntityTag").getList("Items", Tag.TAG_COMPOUND);

        List<ItemStack> contents = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = ItemStack.of(items.getCompound(i));
            if (!stack.isEmpty()) contents.add(stack);
        }
        return contents;
        *//*?}*/
    }

}
