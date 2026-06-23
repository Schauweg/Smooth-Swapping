package dev.shwg.smoothswapping.compat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public class CompatibilityRegistry {
    private static final ScreenCompatibilityAdapter DEFAULT_ADAPTER = new VanillaLikeScreenAdapter();
    private static final List<ScreenCompatibilityAdapter> ADAPTERS = List.of(
            new MacawsFurnitureAdapter(),
            new TravelersBackpackAdapter()
    );

    public static ScreenCompatibilityAdapter getAdapter(Screen screen, AbstractContainerMenu menu) {
        for (ScreenCompatibilityAdapter adapter : ADAPTERS) {
            if (adapter.matches(screen, menu)) {
                return adapter;
            }
        }
        return DEFAULT_ADAPTER;
    }
}
