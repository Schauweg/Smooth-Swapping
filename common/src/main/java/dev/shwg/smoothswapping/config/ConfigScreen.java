package dev.shwg.smoothswapping.config;

import com.google.common.collect.ImmutableList;
import dev.shwg.smoothswapping.Vec2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.CyclingOption;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.List;

public class ConfigScreen extends Screen {

    CatmullRomWidget catmullRomWidget;
    InventoryWidget inventoryWidget;
    Config config;
    DoubleOption animationSpeedOption;
    CyclingOption<Boolean> toggleOption;
    private final int oldAnimationSpeed;
    Screen parentScreen;
    List<Vec2> oldPoints;

    public ConfigScreen(Screen parent) {
        super(new TranslatableText("smoothswapping.config.menu"));
        config = ConfigManager.getConfig();

        this.toggleOption = CyclingOption.create(
                "smoothswapping.config.toggle",
                ImmutableList.of(true, false),
                (value) -> {
                    if (value)
                        return new TranslatableText("smoothswapping.config.toggle.on").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
                    return new TranslatableText("smoothswapping.config.toggle.off").setStyle(Style.EMPTY.withColor(Formatting.RED));
                },
                (gameOptions) -> config.getToggleMod(),
                (gameOptions, option, value) -> config.setToggleMod(value)
        );

        this.animationSpeedOption = new DoubleOption(
                "smoothswapping.config.option.animationspeed",
                10.0D,
                500.0D,
                10.0F,
                (gameOptions) -> (double) config.getAnimationSpeed(),
                (gameOptions, value) -> config.setAnimationSpeed(value.intValue()),
                (gameOptions, option) -> {
                    double val = option.get(gameOptions);
                    return new TranslatableText("smoothswapping.config.option.animationspeed.speed")
                            .append(": ")
                            .append(new LiteralText((int) val + "%"));
                }
        );

        this.oldAnimationSpeed = config.getAnimationSpeed();
        this.parentScreen = parent;
        this.oldPoints = config.getCurvePoints();
    }

    @Override
    protected void init() {
        this.addDrawableChild(toggleOption.createButton(MinecraftClient.getInstance().options, this.width / 2 - 94, height / 5 - 20, 188));
        this.addDrawableChild(animationSpeedOption.createButton(MinecraftClient.getInstance().options, this.width / 2 - 94, this.height / 5 + 5, 188));
        this.catmullRomWidget = new CatmullRomWidget(this.width / 2 - 84 - 10, this.height / 3, 64, 64, 12, 4, 4, config.getCurvePoints());
        this.inventoryWidget = new InventoryWidget(this.width / 2 + 10, this.height / 3, 3, 4, new TranslatableText("smoothswapping.config.testinventory"));
        this.addDrawableChild(this.catmullRomWidget);
        this.addDrawableChild(this.inventoryWidget);

        ButtonWidget resetButton = new ButtonWidget(this.width / 2 - 84 - 10, this.height / 3 + 86 + 4, 88, 20, new TranslatableText("smoothswapping.config.option.animationspeed.reset"), button -> catmullRomWidget.reset());
        this.addDrawableChild(resetButton);

        ButtonWidget saveButton = new ButtonWidget(this.width / 2 + 10, this.height - 30, 88, 20, new TranslatableText("smoothswapping.config.save"), button -> {
            ConfigManager.save();
            MinecraftClient.getInstance().setScreen(parentScreen);
        });
        this.addDrawableChild(saveButton);

        ButtonWidget exitButton = new ButtonWidget(this.width / 2 - 84 - 10, this.height - 30, 88, 20, new TranslatableText("smoothswapping.config.exit"), button -> this.close());
        this.addDrawableChild(exitButton);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        DrawableHelper.drawCenteredText(matrices, textRenderer, title, this.width / 2, 10, 0xFFFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
        this.catmullRomWidget.renderTooltip(matrices, mouseX, mouseY);
        config.setCurvePoints(catmullRomWidget.getPoints());
    }

    @Override
    public void close() {
        config.setCurvePoints(oldPoints);
        config.setAnimationSpeed(oldAnimationSpeed);
        MinecraftClient.getInstance().setScreen(parentScreen);
    }
}
