package schauweg.smoothswapping;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

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
        ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
        DefaultedList<ItemStack> stacks = handler.getStacks();
        return stacks.indexOf(stack);
    }

    public static void setRenderToTrue(List<InventorySwap> swapList) {
        for (InventorySwap swap : swapList) {
            swap.setRenderToSlot(true);
        }
    }

    private static int getQuadrant(float angle) {
        return (int) (Math.floor(2 * angle / PI) % 4 + 4) % 4;
    }

    public static float easeInOut(float time, float startValue, float change, float duration) {
        time /= duration / 2;
        if (time < 1) {
            return change / 2 * time * time + startValue;
        }

        time--;
        return -change / 2 * (time * (time - 2) - 1) + startValue;
    }

    public static float bezierBlend(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    public static float map(float in, float inMin, float inMax, float outMax, float outMin) {
        return (in - inMin) / (inMax - inMin) * (outMax - outMin) + outMin;
    }
}
