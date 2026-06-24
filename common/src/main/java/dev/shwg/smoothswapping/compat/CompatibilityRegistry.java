package dev.shwg.smoothswapping.compat;

import dev.shwg.smoothswapping.config.Config;
import dev.shwg.smoothswapping.config.ConfigManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class CompatibilityRegistry {
    private static final ScreenCompatibilityAdapter DEFAULT_ADAPTER = new VanillaLikeScreenAdapter();

    public static ScreenCompatibilityAdapter getAdapter(Screen screen, AbstractContainerMenu menu) {
        Config config = ConfigManager.getConfig();
        if (config.isModdedScreenCompatibilityEnabled()) {
            for (Config.ModdedScreenCompatibilityEntry entry : config.getModdedScreenCompatibilityEntries()) {
                ScreenCompatibilityAdapter adapter = new ConfiguredScreenAdapter(entry);
                if (adapter.matches(screen, menu)) {
                    return adapter;
                }
            }
        }
        return DEFAULT_ADAPTER;
    }
}
