package schauweg.smoothswapping.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.function.Function;

public class ConfigScreen {

    public static Screen getScreen(Screen parent) {
        Config config = ConfigManager.getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableText("smoothswapping.config.menu"));

        ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("smoothswapping.config.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startIntSlider(new TranslatableText("smoothswapping.config.option.animationspeed"), config.getAnimationSpeed(), 10, 500)
                .setDefaultValue(100)
                .setSaveConsumer(config::setAnimationSpeed)
                .setTextGetter(getIntSlider("smoothswapping.config.option.animationspeed.speed"))
                .build());


        builder.setSavingRunnable(ConfigManager::save);

        return builder.build();
    }


    private static Function<Boolean, Text> getYesNoSupplier(String keyYes, String keyNo) {
        return x -> {
            if (x)
                return new TranslatableText(keyYes);
            else
                return new TranslatableText(keyNo);
        };
    }

    private static Function<Integer, Text> getIntSlider(String text) {
        return x -> new TranslatableText(text).append(": " + x / 100F);
    }
}
