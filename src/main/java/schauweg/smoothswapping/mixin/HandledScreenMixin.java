package schauweg.smoothswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.SmoothSwapping;
import schauweg.smoothswapping.SwapStacks;
import schauweg.smoothswapping.SwapUtil;

import java.util.HashMap;
import java.util.Map;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {


    @Shadow
    @Final
    protected ScreenHandler handler;
    private Screen currentScreen = null;

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo cbi) {
//        MinecraftClient client = MinecraftClient.getInstance();
//
//        if (client.player == null || client.player.currentScreenHandler == null) {
//            return;
//        }
//
//        DefaultedList<ItemStack> stacks = client.player.currentScreenHandler.getStacks();
//
//        Screen screen = client.currentScreen;
//        if (currentScreen != screen) {
//            SmoothSwapping.swaps.clear();
//            addAll(SmoothSwapping.oldStacks, stacks);
//            currentScreen = screen;
//        }
//
////        Map<Integer, ItemStack> changedStacks = getChangedStacks(SmoothSwapping.oldStacks, stacks);
////
////        if (changedStacks.size() > 0){
////
////
////
////            addAll(SmoothSwapping.oldStacks, stacks);
////        }
//
//        if (!areStacksEqual(SmoothSwapping.oldStacks, stacks)) {
//            addAll(SmoothSwapping.oldStacks, stacks);
//        }
    }


    @Inject(method = "handledScreenTick", at = @At("TAIL"))
    public void onTick(CallbackInfo cbi) {

        HandledScreen handledScreen = (HandledScreen) (Object) this;

        if (handledScreen instanceof CreativeInventoryScreen) return;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.player.currentScreenHandler == null) {
            return;
        }

        DefaultedList<ItemStack> stacks = client.player.currentScreenHandler.getStacks();

        Screen screen = client.currentScreen;
        if (currentScreen != screen) {
            SmoothSwapping.swaps.clear();
            addAll(SmoothSwapping.oldStacks, stacks);
            currentScreen = screen;
        }

        Map<Integer, ItemStack> changedStacks = getChangedStacks(SmoothSwapping.oldStacks, stacks);
        if (changedStacks.size() > 0) {
//            System.out.println(changedStacks);

            Map<Integer, SwapStacks> moreStacks = new HashMap<>();
            Map<Integer, SwapStacks> lessStacks = new HashMap<>();

            for (Map.Entry<Integer, ItemStack> stackEntry : changedStacks.entrySet()) {
                int slotID = stackEntry.getKey();
                ItemStack newStack = stackEntry.getValue();
                ItemStack oldStack = SmoothSwapping.oldStacks.get(slotID);

                if (getCount(newStack) > getCount(oldStack)) {
                    moreStacks.put(slotID, new SwapStacks(oldStack, newStack, getCount(newStack) - getCount(oldStack)));
                } else if (getCount(newStack) < getCount(oldStack)) {
                    lessStacks.put(slotID, new SwapStacks(oldStack, newStack, getCount(oldStack) - getCount(newStack)));
                }
            }

//            System.out.println("More Stacks: " + moreStacks);
//            System.out.println("Less Stacks: " + lessStacks);

            for (Map.Entry<Integer, SwapStacks> lessStackEntry : lessStacks.entrySet()) {
                SwapStacks lessSwapStacks = lessStackEntry.getValue();
                Slot lessSlot = handler.getSlot(lessStackEntry.getKey());

                while (lessSwapStacks.itemCountToChange > 0) {
                    for (Map.Entry<Integer, SwapStacks> moreStackEntry : moreStacks.entrySet()) {
                        SwapStacks moreSwapStacks = moreStackEntry.getValue();
                        Slot moreSlot = handler.getSlot(moreStackEntry.getKey());
                        if (moreSwapStacks.itemCountToChange <= 0) {
                            moreStacks.remove(moreStackEntry.getKey());
                            continue;
                        }

                        if (!ItemStack.areItemsEqual(lessSwapStacks.getOldStack(), moreSwapStacks.getNewStack())) {
                            continue;
                        }

                        System.out.println("less: " + lessSwapStacks.itemCountToChange);
                        System.out.println("more: " + moreSwapStacks.itemCountToChange);

                        if (lessSwapStacks.itemCountToChange >= moreSwapStacks.itemCountToChange) {
                            lessSwapStacks.itemCountToChange -= moreSwapStacks.itemCountToChange;
                            moreSwapStacks.itemCountToChange = 0;
                        } else {
                            moreSwapStacks.itemCountToChange -= lessSwapStacks.itemCountToChange;
                            lessSwapStacks.itemCountToChange = 0;
                        }
                        int amount = moreSwapStacks.getNewCount() - moreSwapStacks.getOldCount();
                        SwapUtil.addInventorySwap(moreSlot.getIndex(), lessSlot, moreSlot, false, amount);
                    }
                }


            }


            addAll(SmoothSwapping.oldStacks, stacks);
        }

        if (!areStacksEqual(SmoothSwapping.oldStacks, stacks)) {
            addAll(SmoothSwapping.oldStacks, stacks);
        }
    }

    private Map<Integer, ItemStack> getChangedStacks(DefaultedList<ItemStack> oldStacks, DefaultedList<ItemStack> newStacks) {
        Map<Integer, ItemStack> changedStacks = new HashMap<>();
        for (int slotID = 0; slotID < oldStacks.size(); slotID++) {
            ItemStack newStack = newStacks.get(slotID);
            ItemStack oldStack = oldStacks.get(slotID);
            if (!ItemStack.areEqual(oldStack, newStack)) {
                changedStacks.put(slotID, newStack.copy());
            }
        }
        return changedStacks;
    }

    private boolean areStacksEqual(DefaultedList<ItemStack> oldStacks, DefaultedList<ItemStack> newStacks) {
        if (oldStacks == null || newStacks == null || (oldStacks.size() != newStacks.size())) {
            return false;
        } else {
            for (int slotID = 0; slotID < oldStacks.size(); slotID++) {
                ItemStack newStack = newStacks.get(slotID);
                ItemStack oldStack = oldStacks.get(slotID);
                if (!ItemStack.areEqual(oldStack, newStack)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getCount(ItemStack stack) {
        return ItemStack.areItemsEqual(stack, Items.AIR.getDefaultStack()) ? 0 : stack.getCount();
    }

    private void addAll(DefaultedList<ItemStack> oldStacks, DefaultedList<ItemStack> newStacks) {
        oldStacks.clear();
        newStacks.stream().map(ItemStack::copy).forEach(oldStacks::add);
    }
}
