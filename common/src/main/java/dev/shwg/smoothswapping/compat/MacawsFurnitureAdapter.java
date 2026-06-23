package dev.shwg.smoothswapping.compat;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MacawsFurnitureAdapter extends VanillaLikeScreenAdapter {
    private static final String FURNITURE_SCREEN = "net.kikoz.mcwfurnitures.storage.FurnitureScreen";
    private static final String FURNITURE_MENU = "net.kikoz.mcwfurnitures.storage.FurnitureScreenHandler";
    private static final String FORGE_FURNITURE_SCREEN = "com.mcwfurnitures.kikoz.storage.FurnitureStorageScreeen";
    private static final String FORGE_FURNITURE_MENU = "com.mcwfurnitures.kikoz.storage.FurnitureStorageContainer";

    @Override
    public boolean matches(Screen screen, AbstractContainerMenu menu) {
        if (screen == null || menu == null) {
            return false;
        }

        String screenClassName = screen.getClass().getName();
        String menuClassName = menu.getClass().getName();
        return (FURNITURE_SCREEN.equals(screenClassName) && FURNITURE_MENU.equals(menuClassName))
                || (FORGE_FURNITURE_SCREEN.equals(screenClassName) && FORGE_FURNITURE_MENU.equals(menuClassName));
    }

    @Override
    public String name() {
        return "macaws_furniture";
    }
}
