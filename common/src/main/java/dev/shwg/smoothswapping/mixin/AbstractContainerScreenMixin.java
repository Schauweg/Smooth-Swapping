package dev.shwg.smoothswapping.mixin;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.SwapStacks;
import dev.shwg.smoothswapping.SwapUtil;
import dev.shwg.smoothswapping.Vec2;
import dev.shwg.smoothswapping.compat.CompatibilityRegistry;
import dev.shwg.smoothswapping.compat.ScreenCompatibilityAdapter;
import dev.shwg.smoothswapping.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.shwg.smoothswapping.SmoothSwapping.oldCursorStack;
import static dev.shwg.smoothswapping.SwapUtil.getCount;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Shadow
    protected int leftPos, topPos;

    @Unique
    private Screen smooth_Swapping$currentScreen = null;

    @Inject(method = "extractContents", at = @At("HEAD"))
    public void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        try {
            smooth_Swapping$doRender(mouseX, mouseY);
        } catch (Exception e) {
            SwapUtil.reset();
        }
    }

    @Unique
    private void smooth_Swapping$doRender(double mouseX, double mouseY) {
        if (!ConfigManager.getConfig().getToggleMod())
            return;

        @SuppressWarnings({"rawtypes", "DataFlowIssue"})
        AbstractContainerScreen handledScreen = (AbstractContainerScreen) (Object) this;

        if (handledScreen instanceof CreativeModeInventoryScreen) return;

        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.player.containerMenu == null) {
            return;
        }

        SmoothSwapping.currentStacks = client.player.containerMenu.getItems();

        try {
            SmoothSwapping.currentCursorStackLock.lock();
            ItemStack cursorStack = client.player.containerMenu.getCarried();
            ItemStack prevStack = SmoothSwapping.currentCursorStack.get();
            if (
                    prevStack == null
                            || (prevStack.getCount() != cursorStack.getCount()
                            || prevStack.getItem() != cursorStack.getItem())
            ) {
                SmoothSwapping.currentCursorStack.set(cursorStack.copy());
            }
        } finally {
            SmoothSwapping.currentCursorStackLock.unlock();
        }

        Screen screen = client.screen;
        ScreenCompatibilityAdapter adapter = CompatibilityRegistry.getAdapter(screen, menu);
        if (!adapter.shouldHandle(screen, menu)) {
            return;
        }

        if (SmoothSwapping.clickSwap) {
            SmoothSwapping.clickSwap = false;
            SwapUtil.copyStacks(SmoothSwapping.currentStacks, SmoothSwapping.oldStacks);
            return;
        }

        if (smooth_Swapping$currentScreen != screen) {
            SmoothSwapping.swaps.clear();
            SwapUtil.copyStacks(SmoothSwapping.currentStacks, SmoothSwapping.oldStacks);
            smooth_Swapping$currentScreen = screen;
            return;
        }

        Map<Integer, ItemStack> changedStacks = smooth_Swapping$getChangedStacks(SmoothSwapping.oldStacks, SmoothSwapping.currentStacks);
        if (!SmoothSwapping.clickSwap && adapter.shouldAnimateChangedStacks(menu, changedStacks)) {
            int changedStacksSize = changedStacks.size();
            if (changedStacksSize > 1) {
                List<SwapStacks> moreStacks = new ArrayList<>();
                List<SwapStacks> lessStacks = new ArrayList<>();

                int totalAmount = 0;
                for (Map.Entry<Integer, ItemStack> stackEntry : changedStacks.entrySet()) {
                    int slotID = stackEntry.getKey();
                    ItemStack newStack = stackEntry.getValue();
                    ItemStack oldStack = SmoothSwapping.oldStacks.get(slotID);

                    //whether the stack got more items or less and if slot is output slot
                    if (getCount(newStack) > getCount(oldStack)
                            && adapter.canAnimateStackChange(menu, slotID, Minecraft.getInstance().player)) {
                        moreStacks.add(new SwapStacks(slotID, oldStack, newStack, getCount(oldStack) - getCount(newStack)));
                        totalAmount += getCount(newStack) - getCount(oldStack);
                    } else if (getCount(newStack) < getCount(oldStack)
                            && adapter.canAnimateStackChange(menu, slotID, Minecraft.getInstance().player)
                            && SmoothSwapping.clickSwapStack == null) {
                        lessStacks.add(new SwapStacks(slotID, oldStack, newStack, getCount(oldStack) - getCount(newStack)));
                    }
                }
                if (SmoothSwapping.clickSwapStack != null) {
                    lessStacks.clear();
                    int clickSwapSlot = SmoothSwapping.clickSwapStack;
                    if (adapter.isRealItemSlot(menu, clickSwapSlot)) {
                        ItemStack newStack = menu.getSlot(clickSwapSlot).getItem();
                        ItemStack oldStack = SmoothSwapping.oldStacks.get(clickSwapSlot);
                        lessStacks.add(new SwapStacks(clickSwapSlot, oldStack, newStack, totalAmount));
                    }
                    SmoothSwapping.clickSwapStack = null;
                }
                if (moreStacks.isEmpty()) {
                    SwapUtil.assignI2CSwaps(lessStacks, new Vec2(mouseX - leftPos, mouseY - topPos), menu);
                } else {
                    SwapUtil.assignI2ISwaps(moreStacks, lessStacks, menu);
                }
            } else if (changedStacksSize == 1) {
                ItemStack currentCursorStack = SmoothSwapping.currentCursorStack.get();
                ItemStack oldCursorStack = SmoothSwapping.oldCursorStack;
                //LOGGER.info("old cursor stack: " + oldCursorStack + ", current cursor stack: " + currentCursorStack);
                if (
                        currentCursorStack != null && oldCursorStack != null
                                && currentCursorStack.getItem() == oldCursorStack.getItem()
                                && currentCursorStack.getCount() != oldCursorStack.getCount()
                ) {
                    changedStacks.entrySet().stream().findFirst().ifPresent(changedStack -> {
                        if (!adapter.isRealItemSlot(menu, changedStack.getKey())) {
                            return;
                        }
                        ItemStack oldStack = SmoothSwapping.oldStacks.get(changedStack.getKey());
                        ItemStack currentStack = SmoothSwapping.currentStacks.get(changedStack.getKey());
                        int cursorStackCountDiff = currentCursorStack.getCount() - SmoothSwapping.oldCursorStack.getCount();

                        //LOGGER.info("old slot stack: " + oldStack + ", current slot stack: " + currentStack);
                        if (
                                (oldStack.getItem() == currentStack.getItem() && oldStack.getCount() - currentStack.getCount() == cursorStackCountDiff)
                                        || currentStack.getItem() == Items.AIR
                        ) {
                            SwapStacks lessStack = new SwapStacks(changedStack.getKey(), oldStack, currentStack, getCount(oldStack) - getCount(currentStack));
                            SwapUtil.assignI2CSwaps(List.of(lessStack), new Vec2(mouseX - leftPos, mouseY - topPos), menu);
                        }
                    });
                }
            }
        }

        if (!smooth_Swapping$areStacksEqual(SmoothSwapping.oldStacks, SmoothSwapping.currentStacks)) {
            SwapUtil.copyStacks(SmoothSwapping.currentStacks, SmoothSwapping.oldStacks);
            oldCursorStack = SmoothSwapping.currentCursorStack.get();
        }
    }

    @Unique
    private Map<Integer, ItemStack> smooth_Swapping$getChangedStacks(NonNullList<ItemStack> oldStacks, NonNullList<ItemStack> newStacks) {
        Map<Integer, ItemStack> changedStacks = new HashMap<>();
        for (int slotID = 0; slotID < oldStacks.size(); slotID++) {
            ItemStack newStack = newStacks.get(slotID);
            ItemStack oldStack = oldStacks.get(slotID);
            if (!ItemStack.matches(oldStack, newStack)) {
                changedStacks.put(slotID, newStack.copy());
            }
        }
        return changedStacks;
    }

    @Unique
    private boolean smooth_Swapping$areStacksEqual(NonNullList<ItemStack> oldStacks, NonNullList<ItemStack> newStacks) {
        if (oldStacks == null || newStacks == null || (oldStacks.size() != newStacks.size())) {
            return false;
        } else {
            for (int slotID = 0; slotID < oldStacks.size(); slotID++) {
                ItemStack newStack = newStacks.get(slotID);
                ItemStack oldStack = oldStacks.get(slotID);
                if (!ItemStack.matches(oldStack, newStack)) {
                    return false;
                }
            }
        }
        return true;
    }
}
