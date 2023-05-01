package dev.shwg.smoothswapping.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.shwg.smoothswapping.SmoothSwapping;

import java.io.*;
import java.nio.file.Path;

public class ConfigManager {
    private static File file;
    private static Config config;

    private static void prepareConfigFile() {
        if (file != null) {
            return;
        }
        file = getConfigPath().resolve(SmoothSwapping.MOD_ID+".json").toFile();
    }

    @ExpectPlatform
    public static Path getConfigPath() {throw new AssertionError();}

    public static Config initializeConfig() {
        if (config != null) {
            return config;
        }

        config = new Config();
        load();

        return config;
    }

    private static void load() {
        prepareConfigFile();

        try {
            if (!file.exists()) {
                save();
            }
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));

                Config parsed = SmoothSwapping.GSON.fromJson(br, Config.class);
                if (parsed != null) {
                    config = parsed;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load Login Toast configuration file; reverting to defaults");
            e.printStackTrace();
        }
    }

    public static void save() {
        prepareConfigFile();
        String jsonString = SmoothSwapping.GSON.toJson(config);

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonString);
        } catch (IOException e) {
            System.err.println("Couldn't save Login Toast configuration file");
            e.printStackTrace();
        }
    }

    public static Config getConfig() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }
}
