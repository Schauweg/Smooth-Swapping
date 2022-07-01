package schauweg.smoothswapping.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.Function;

public class ConfigScreen {

    public static Screen getScreen(Screen parent) {
        Config config = ConfigManager.getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("smoothswapping.config.menu"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("smoothswapping.config.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startIntSlider(Text.translatable("smoothswapping.config.option.animationspeed"), config.getAnimationSpeed(), 10, 500)
                .setDefaultValue(100)
                .setSaveConsumer(config::setAnimationSpeed)
                .setTextGetter(getIntSlider("smoothswapping.config.option.animationspeed.speed"))
                .build());

        SelectionListEntry<String> easeMode = entryBuilder.startSelector(Text.translatable("smoothswapping.config.option.ease"), new String[]{"linear", "ease-in", "ease-out", "ease-in-out"}, config.getEaseMode())
                .setDefaultValue("linear")
                .setSaveConsumer(config::setEaseMode)
                .setNameProvider(getString("smoothswapping.config.option.ease."))
                .build();

        general.addEntry(easeMode);

        general.addEntry(entryBuilder.startIntSlider(Text.translatable("smoothswapping.config.option.easespeed"), config.getEaseSpeed(), 50, 1000)
                .setDefaultValue(400)
                .setSaveConsumer(config::setEaseSpeed)
                .setTextGetter(getIntSlider("smoothswapping.config.option.easespeed.speed"))
                .build());


        builder.setSavingRunnable(ConfigManager::save);

        return builder.build();
    }


    private static Function<Boolean, Text> getYesNoSupplier(String keyYes, String keyNo) {
        return x -> {
            if (x)
                return Text.translatable(keyYes);
            else
                return Text.translatable(keyNo);
        };
    }

    private static Function<String, Text> getString(String key) {
        return x -> Text.translatable(key + x);
    }

    private static Function<Integer, Text> getIntSlider(String key) {
        return x -> Text.translatable(key).append(": " + x / 100F);
    }
}
