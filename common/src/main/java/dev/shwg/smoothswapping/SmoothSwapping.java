package dev.shwg.smoothswapping;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.shwg.smoothswapping.config.ConfigManager;
import dev.shwg.smoothswapping.swaps.InventorySwap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class SmoothSwapping {
    public static final String MOD_ID = "smoothswapping";
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final int ASSUME_CURSOR_STACK_SLOT_INDEX = -2;
    public static boolean clickSwap;
    public static Short clickSwapStack;
    public static Map<Integer, List<InventorySwap>> swaps;
    public static DefaultedList<ItemStack> oldStacks, currentStacks;
    public static ItemStack oldCursorStack;
    public static AtomicReference<ItemStack> currentCursorStack = new AtomicReference<>(null);
    public static final ReentrantLock currentCursorStackLock = new ReentrantLock();

    public static void init() {
        ConfigManager.initializeConfig();
        swaps = new HashMap<>();
        oldStacks = DefaultedList.of();
        currentStacks = DefaultedList.of();
    }
}