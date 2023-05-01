package dev.shwg.smoothswapping.config.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ConfigManagerImpl {

    public static Path getConfigPath(){
        return FabricLoader.getInstance().getConfigDir();
    }

}
