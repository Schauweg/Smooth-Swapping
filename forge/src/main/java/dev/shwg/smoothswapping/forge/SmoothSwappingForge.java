package dev.shwg.smoothswapping.forge;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.config.ConfigScreen;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

@Mod(SmoothSwapping.MOD_ID)
public class SmoothSwappingForge {
    public SmoothSwappingForge() {
        SmoothSwapping.init();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> new ConfigScreen(parent)));

    }
}