package dev.shwg.smoothswapping.compat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class VanillaLikeScreenAdapter implements ScreenCompatibilityAdapter {
    @Override
    public boolean matches(Screen screen, AbstractContainerMenu menu) {
        return true;
    }

    @Override
    public String name() {
        return "vanilla_like";
    }
}
