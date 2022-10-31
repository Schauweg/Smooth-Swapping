package schauweg.smoothswapping;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import schauweg.smoothswapping.config.Config;
import schauweg.smoothswapping.swaps.InventorySwap;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;

public class SwapUtil {


    public static boolean hasArrived(InventorySwap swap) {
        int quadrant = getQuadrant(swap.getAngle());
        double x = swap.getX();
        double y = swap.getY();
        if (quadrant == 0 && x > 0 && y > 0) {
            return true;
        } else if (quadrant == 1 && x < 0 && y > 0) {
            return true;
        } else if (quadrant == 2 && x < 0 && y < 0) {
            return true;
        } else return quadrant == 3 && x > 0 && y < 0;
    }

    public static int getSlotIndex(ItemStack stack) {
        if (MinecraftClient.getInstance().player == null) return -1;
        ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
        DefaultedList<ItemStack> stacks = handler.getStacks();
        return stacks.indexOf(stack);
    }

    public static void setRenderToTrue(List<InventorySwap> swapList) {
        for (InventorySwap swap : swapList) {
            swap.setRenderDestinationSlot(true);
        }
    }

    private static int getQuadrant(float angle) {
        return (int) (Math.floor(2 * angle / PI) % 4 + 4) % 4;
    }

    public static float bezierBlend(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    public static float map(float in, float inMin, float inMax, float outMax, float outMin) {
        return (in - inMin) / (inMax - inMin) * (outMax - outMin) + outMin;
    }

    public static float getEase(Config config, float progress){
            switch (config.getEaseMode()) {
                case "linear" -> progress = 1f;
                case "ease-in" -> progress = progress - 1;
                case "ease-in-out" -> progress = progress >= 0.5f ? 1f - progress : progress;
                //for "ease-out" do nothing
            }
        return SwapUtil.bezierBlend(progress) * config.getEaseSpeedFormatted();
    }

    public static void addInventorySwap(int index, Slot fromSlot, Slot toSlot, boolean checked, int amount) {
        List<InventorySwap> swaps = SmoothSwapping.swaps.getOrDefault(index, new ArrayList<>());
        swaps.add(new InventorySwap(fromSlot, toSlot, checked, amount));
        SmoothSwapping.swaps.put(index, swaps);
    }

    public static void assignSwaps(List<SwapStacks> moreStacks, List<SwapStacks> lessStacks, ScreenHandler handler){
        for (int i = 0; i < moreStacks.size(); i++) {
            SwapStacks moreStack = moreStacks.get(i);
            if (moreStack.itemCountToChange == 0){
                moreStacks.remove(moreStack);
            }
            Slot moreSlot = handler.getSlot(moreStack.getSlotID());

            int c = 0;
            while (moreStack.itemCountToChange < 0 && c < 64) {
                c++;
                for (int j = 0; j < lessStacks.size(); j++) {
                    SwapStacks lessStack = lessStacks.get(j);

                    int amount = 0;
                    while (lessStack.itemCountToChange != 0 && moreStack.itemCountToChange != 0) {
                        lessStack.itemCountToChange--;
                        moreStack.itemCountToChange++;
                        amount++;
                    }

                    Slot lessSlot = handler.getSlot(lessStack.getSlotID());
                    SwapUtil.addInventorySwap(moreStack.getSlotID(), lessSlot, moreSlot, ItemStack.areItemsEqual(moreStack.getOldStack(), moreStack.getNewStack()), amount);
                    if (lessStack.itemCountToChange == 0){
                        lessStacks.remove(lessStack);
                    }
                    if (moreStack.itemCountToChange == 0){
                            break;
                    }
                }
            }
        }
    }

    public static int getCount(ItemStack stack) {
        return ItemStack.areItemsEqual(stack, Items.AIR.getDefaultStack()) ? 0 : stack.getCount();
    }

}
