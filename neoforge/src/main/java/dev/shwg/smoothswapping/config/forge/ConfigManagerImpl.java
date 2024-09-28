package dev.shwg.smoothswapping.config.forge;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ConfigManagerImpl {

    public static Path getConfigPath(){
        return FMLPaths.CONFIGDIR.get();
    }

}
