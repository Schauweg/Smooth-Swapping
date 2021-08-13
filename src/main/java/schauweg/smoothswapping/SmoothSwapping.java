package schauweg.smoothswapping;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmoothSwapping implements ClientModInitializer {

    public static Map<Integer, List<InventorySwap>> swaps;
    public static DefaultedList<ItemStack> oldStacks;

    @Override
    public void onInitializeClient() {
        swaps = new HashMap<>();
        oldStacks = DefaultedList.of();
    }

}
