package dev.shwg.smoothswapping.neoforge;

import dev.shwg.smoothswapping.SmoothSwapping;
import dev.shwg.smoothswapping.config.neoforge.ConfigScreenFactory;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

@Mod(SmoothSwapping.MOD_ID)
public class SmoothSwappingForge {
    public SmoothSwappingForge() {
        SmoothSwapping.init();
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                ConfigScreenFactory::new
        );
    }
}