package dev.shwg.smoothswapping.neoforge;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.config.ConfigScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

@Mod(SmoothSwapping.MOD_ID)
public class SmoothSwappingNeoForge {
    public SmoothSwappingNeoForge() {
        SmoothSwapping.init();
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (modContainer, parent) -> new ConfigScreen(parent)
        );
    }
}