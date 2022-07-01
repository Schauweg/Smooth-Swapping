package schauweg.smoothswapping;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import schauweg.smoothswapping.config.Config;
import schauweg.smoothswapping.swaps.InventorySwap;

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
}
