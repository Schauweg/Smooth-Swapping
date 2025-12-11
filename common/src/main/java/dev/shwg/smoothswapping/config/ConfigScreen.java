package dev.shwg.smoothswapping.config;

import com.mojang.serialization.Codec;
import dev.shwg.smoothswapping.Vec2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ConfigScreen extends Screen {

    CatmullRomWidget catmullRomWidget;
    InventoryWidget inventoryWidget;
    Config config;
    SimpleOption<Integer> animationSpeedOption;
    SimpleOption<Boolean> toggleOption;
    private final int oldAnimationSpeed;
    Screen parentScreen;
    List<Vec2> oldPoints;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("smoothswapping.config.menu"));
        config = ConfigManager.getConfig();
        this.toggleOption = SimpleOption.ofBoolean("smoothswapping.config.toggle",
                SimpleOption.emptyTooltip(),
                (optionText, value) -> {
                    if (value)
                        return Text.translatable("smoothswapping.config.toggle.on").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
                    return Text.translatable("smoothswapping.config.toggle.off").setStyle(Style.EMPTY.withColor(Formatting.RED));
                },
                config.getToggleMod(),
                (value) -> config.setToggleMod(value)
        );

        this.animationSpeedOption = new SimpleOption<>("smoothswapping.config.option.animationspeed",
                SimpleOption.emptyTooltip(),
                (optionText, value) -> Text.translatable("smoothswapping.config.option.animationspeed.speed").append(": ").append(Text.literal(value + "%")),
                (new SimpleOption.ValidatingIntSliderCallbacks(1, 50)).withModifier(
                        (value) -> value * 10,
                        (value) -> value / 10),
                Codec.intRange(10, 500),
                config.getAnimationSpeed(),
                (value) -> config.setAnimationSpeed(value));
        this.oldAnimationSpeed = config.getAnimationSpeed();
        this.parentScreen = parent;
        this.oldPoints = config.getCurvePoints();
    }

    @Override
    protected void init() {
        this.addDrawableChild(toggleOption.createWidget(MinecraftClient.getInstance().options, this.width / 2 - 94, height / 5 - 20, 188));
        this.addDrawableChild(animationSpeedOption.createWidget(MinecraftClient.getInstance().options, this.width / 2 - 94, this.height / 5 + 5, 188));
        this.catmullRomWidget = new CatmullRomWidget(this.width / 2 - 84 - 10, this.height / 3, 64, 64, 12, 4, 4, config.getCurvePoints());
        this.inventoryWidget = new InventoryWidget(this.width / 2 + 10, this.height / 3, 3, 4, Text.translatable("smoothswapping.config.testinventory"));
        this.addDrawableChild(this.catmullRomWidget);
        this.addDrawableChild(this.inventoryWidget);

        ButtonWidget resetButton = ButtonWidget.builder(Text.translatable("smoothswapping.config.option.animationspeed.reset"), button ->
                catmullRomWidget.reset()).dimensions(this.width / 2 - 84 - 10, this.height / 3 + 86 + 4, 88, 20).build();
        this.addDrawableChild(resetButton);

        ButtonWidget saveButton = ButtonWidget.builder(Text.translatable("smoothswapping.config.save"), button -> {
            ConfigManager.save();
            MinecraftClient.getInstance().setScreen(parentScreen);
        }).dimensions(this.width / 2 + 10, this.height - 30, 88, 20).build();
        this.addDrawableChild(saveButton);

        ButtonWidget exitButton = ButtonWidget.builder(Text.translatable("smoothswapping.config.exit"), button -> this.close())
                .dimensions(this.width / 2 - 84 - 10, this.height - 30, 88, 20).build();
        this.addDrawableChild(exitButton);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(matrices);
        DrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, title, this.width / 2, 10, 0xFFFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
        config.setCurvePoints(catmullRomWidget.getPoints());
    }

    @Override
    public void close() {
        config.setCurvePoints(oldPoints);
        config.setAnimationSpeed(oldAnimationSpeed);
        MinecraftClient.getInstance().setScreen(parentScreen);
    }
}
