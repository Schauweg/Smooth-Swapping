package dev.shwg.smoothswapping.config;

import com.mojang.serialization.Codec;
import dev.shwg.smoothswapping.Vec2;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class ConfigScreen extends Screen {

    CatmullRomWidget catmullRomWidget;
    InventoryWidget inventoryWidget;
    Config config;
    OptionInstance<Integer> animationSpeedOption;
    OptionInstance<Boolean> toggleOption;
    private final int oldAnimationSpeed;
    private static boolean componentsBound = false;
    Screen parentScreen;
    List<Vec2> oldPoints;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("smoothswapping.config.menu"));
        config = ConfigManager.getConfig();
        this.toggleOption = OptionInstance.createBoolean("smoothswapping.config.toggle",
                OptionInstance.noTooltip(),
                (optionText, value) -> {
                    if (value)
                        return Component.translatable("smoothswapping.config.toggle.on").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
                    return Component.translatable("smoothswapping.config.toggle.off").setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                },
                config.getToggleMod(),
                (value) -> config.setToggleMod(value)
        );

        this.animationSpeedOption = new OptionInstance<>("smoothswapping.config.option.animationspeed",
                OptionInstance.noTooltip(),
                (optionText, value) -> Component.translatable("smoothswapping.config.option.animationspeed.speed").append(": ").append(Component.literal(value + "%")),
                (new OptionInstance.IntRange(1, 50)).xmap(
                        (value) -> value * 10,
                        (value) -> value / 10,
                        true),
                Codec.intRange(10, 500),
                config.getAnimationSpeed(),
                (value) -> config.setAnimationSpeed(value));
        this.oldAnimationSpeed = config.getAnimationSpeed();
        this.parentScreen = parent;
        this.oldPoints = config.getCurvePoints();
    }

    @Override
    protected void init() {
        ensureComponentsBound();
        this.addRenderableWidget(toggleOption.createButton(Minecraft.getInstance().options, this.width / 2 - 94, height / 5 - 20, 188));
        this.addRenderableWidget(animationSpeedOption.createButton(Minecraft.getInstance().options, this.width / 2 - 94, this.height / 5 + 5, 188));
        this.catmullRomWidget = new CatmullRomWidget(this.width / 2 - 84 - 10, this.height / 3, 64, 64, 12, 4, 4, config.getCurvePoints());
        this.inventoryWidget = new InventoryWidget(this.width / 2 + 10, this.height / 3, 3, 4, Component.translatable("smoothswapping.config.testinventory"));
        this.addRenderableWidget(this.catmullRomWidget);
        this.addRenderableWidget(this.inventoryWidget);

        Button resetButton = Button.builder(Component.translatable("smoothswapping.config.option.animationspeed.reset"), button ->
                catmullRomWidget.reset()).bounds(this.width / 2 - 84 - 10, this.height / 3 + 86 + 4, 88, 20).build();
        this.addRenderableWidget(resetButton);

        Button saveButton = Button.builder(Component.translatable("smoothswapping.config.save"), button -> {
            ConfigManager.save();
            Minecraft.getInstance().setScreen(parentScreen);
        }).bounds(this.width / 2 + 10, this.height - 30, 88, 20).build();
        this.addRenderableWidget(saveButton);

        Button exitButton = Button.builder(Component.translatable("smoothswapping.config.exit"), button -> this.onClose())
                .bounds(this.width / 2 - 84 - 10, this.height - 30, 88, 20).build();
        this.addRenderableWidget(exitButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(font, title, this.width / 2, 10, 0xFFFFFFFF);
        config.setCurvePoints(catmullRomWidget.getPoints());
    }

    @Override
    public void onClose() {
        config.setCurvePoints(oldPoints);
        config.setAnimationSpeed(oldAnimationSpeed);
        Minecraft.getInstance().setScreen(parentScreen);
    }

    //I'm not sure if this safe but it works :D
    private static synchronized void ensureComponentsBound() {
        if (componentsBound) return;

        try {
            new ItemStack(Items.STONE);
            componentsBound = true;
            return;
        } catch (Exception ignored) {
        }

        try {
            HolderLookup.Provider lookup = VanillaRegistries.createLookup();
            var registry = BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup);
            registry.forEach(DataComponentInitializers.PendingComponents::apply);
            componentsBound = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DataComponents for config GUI", e);
        }
    }
}
