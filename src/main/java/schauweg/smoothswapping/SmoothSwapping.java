package schauweg.smoothswapping;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import schauweg.smoothswapping.config.ConfigManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmoothSwapping implements ClientModInitializer {

    public static final String MOD_ID = "smoothswapping";
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

    public static Map<Integer, List<InventorySwap>> swaps;
    public static DefaultedList<ItemStack> oldStacks;

    @Override
    public void onInitializeClient() {
        ConfigManager.initializeConfig();
        swaps = new HashMap<>();
        oldStacks = DefaultedList.of();
    }

}
