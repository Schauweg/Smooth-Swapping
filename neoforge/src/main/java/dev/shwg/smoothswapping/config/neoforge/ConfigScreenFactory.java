package dev.shwg.smoothswapping.config.neoforge;

import dev.shwg.smoothswapping.config.ConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModContainer;

public class ConfigScreenFactory implements IConfigScreenFactory {

    @Override
    public Screen createScreen(ModContainer container, Screen parent) {
        return new ConfigScreen(parent);
    }
}