package dev.shwg.smoothswapping.compat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MacawsFurnitureAdapter extends VanillaLikeScreenAdapter {
    private static final String FURNITURE_SCREEN = "net.kikoz.mcwfurnitures.storage.FurnitureScreen";
    private static final String FURNITURE_MENU = "net.kikoz.mcwfurnitures.storage.FurnitureScreenHandler";

    @Override
    public boolean matches(Screen screen, AbstractContainerMenu menu) {
        return screen != null
                && menu != null
                && FURNITURE_SCREEN.equals(screen.getClass().getName())
                && FURNITURE_MENU.equals(menu.getClass().getName());
    }

    @Override
    public String name() {
        return "macaws_furniture";
    }
}
