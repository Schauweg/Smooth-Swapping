package schauweg.smoothswapping;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmoothSwapping implements ClientModInitializer {

    public static Map<Integer, List<InventorySwap>> swaps;
    public static List<OldStack> oldStacks;

    @Override
    public void onInitializeClient() {
        swaps = new HashMap<>();
        oldStacks = new ArrayList<>();
    }

    public static void addAllOldItems(List<OldStack> emptyList, List<ItemStack> fullList){
        emptyList.clear();
        fullList.stream().map(stack -> new OldStack(stack.getItem(), stack.getCount())).forEach(emptyList::add);
    }
}
