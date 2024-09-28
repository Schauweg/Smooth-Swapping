package dev.shwg.smoothswapping.forge;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.config.ConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(SmoothSwapping.MOD_ID)
public class SmoothSwappingForge {
    public SmoothSwappingForge() {
        SmoothSwapping.init();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> new ConfigScreen(parent)));

    }
}