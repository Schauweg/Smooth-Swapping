package dev.shwg.smoothswapping.config;

import dev.shwg.shwgconfig.api.*;
import dev.shwg.shwgconfig.api.entries.EntryOptions;
import dev.shwg.shwgconfig.api.entries.display.EnumLabels;
import dev.shwg.shwgconfig.api.entries.display.NumberDisplay;
import dev.shwg.shwgconfig.api.model.ConfigCategory;
import dev.shwg.shwgconfig.api.values.BooleanValue;
import dev.shwg.shwgconfig.api.values.EnumValue;
import dev.shwg.shwgconfig.api.values.numbervalues.IntValue;
import dev.shwg.shwgconfig.gui.deferred.GenericTooltip;
import dev.shwg.smoothswapping.config.widgets.CatmullRomWidget;
import dev.shwg.smoothswapping.config.widgets.InventoryWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;

import java.util.Locale;
import java.util.function.Function;

public class SmoothSwappingConfig extends ConfigBase {

    // baseline for animation speed
    private static final float TYPICAL_DISTANCE = 90f;

    public final BooleanValue modEnabledToggle = BooleanValue.of(true);
    public final EnumValue<Options.AnimationMode> animationMode = EnumValue.of(Options.AnimationMode.FIXED_DURATION);
    public final IntValue animationSpeed = IntValue.of(100, 10, 1000, 5);
    public final BooleanValue animateCursorClicks = BooleanValue.of(true);
    public final CurveValue animationCurve = CurveValue.of(new Vec2(0.38f, 0.11f), new Vec2(0.62f, 0.83f));

    @Override
    public ConfigManager.ScreenManager buildScreen() {
        ConfigCategory category = CategoryBuilder.create(Component.empty())
                .addToggle(modEnabledToggle, Component.translatable("smooth_swapping.config.enabled"))
                .addCycleButton(animationMode,
                        Component.translatable("smooth_swapping.config.animation.mode"),
                        EnumLabels.custom(mode -> SmoothSwappingConfig.animationModeText(mode, "smooth_swapping.option")),
                        EntryOptions.create()
                                .enabledWhen(this::modEnabled)
                                .labelTooltip(GenericTooltip.builder()
                                        .line(Component.translatable("smooth_swapping.config.animation.mode.tooltip.line"))
                                        .bullet(SmoothSwappingConfig.animationModeText(Options.AnimationMode.FIXED_DURATION, "smooth_swapping.option")
                                                .append(": ").append(Component.translatable("smooth_swapping.config.animation.mode.tooltip.fixed_duration"))
                                        )
                                        .bullet(SmoothSwappingConfig.animationModeText(Options.AnimationMode.FIXED_SPEED, "smooth_swapping.option")
                                                .append(": ").append(Component.translatable("smooth_swapping.config.animation.mode.tooltip.fixed_speed"))
                                        )
                                        .build()
                                )
                )
                .addNumberSlider(animationSpeed, Component.translatable("smooth_swapping.config.animation.speed"),
                        NumberDisplay.custom(integer -> {
                            Options.AnimationMode mode = animationMode.getValue();
                            MutableComponent text = SmoothSwappingConfig.animationModeText(animationMode, "smooth_swapping.option.short");
                            text.append(": ");
                            switch (mode) {
                                case FIXED_DURATION -> text.append(integer + "ms");
                                case FIXED_SPEED -> text.append(String.format(Locale.ROOT, "%.2f px/ms", (TYPICAL_DISTANCE / integer)));
                            }
                            return text;
                        }),
                        EntryOptions.create()
                                .enabledWhen(this::modEnabled)
                )
                .addToggle(animateCursorClicks, Component.translatable("smooth_swapping.config.animate_cursor_clicks"),
                        EntryOptions.create()
                                .enabledWhen(this::modEnabled)
                                .labelTooltip(GenericTooltip.builder()
                                        .line(Component.translatable("smooth_swapping.config.animate_cursor_clicks.tooltip"))
                                        .build()))
                .build();

        ConfigCategory customWidgetLayout = CategoryBuilder.create(Component.translatable("smooth_swapping.config.easing"))
                .addCustomWidget(new CatmullRomWidget(0, 0, 100, 100, 2, 4, 4, animationCurve.getValue(), animationCurve::setValue))
                .preview(InventoryWidget.builder(Component.translatable("container.inventory"))
                        .size(4, 3)
                        .addItem(Items.COBBLESTONE, 64, 0)
                        .addItem(Items.DIAMOND, 64, 1)
                        .addItem(Items.DIAMOND, 64, 2)
                        .addItem(Items.STICK, 32, 9)
                        .addItem(Items.STICK, 64, 10)
                        .addItem(Items.STICK, 64, 11)
                        .build())
                .useOwnControllerWidth()
                .centerPreviewWithControls()
                .build();

        return Builder.create(Component.translatable("smooth_swapping.config.title"))
                .addTab(TabBuilder.create(Component.literal("Smooth Swapping"))
                        .addCategory(category)
                        .addCategory(customWidgetLayout)
                        .build())
                .build();
    }

    private static MutableComponent animationModeText(EnumValue<Options.AnimationMode> animationMode, String key) {
        return animationModeText(animationMode.getValue(), key);
    }

    private static MutableComponent animationModeText(Options.AnimationMode mode, String key) {
        Function<Options.AnimationMode, Component> modeLabel = EnumLabels.translated(key);
        MutableComponent text = modeLabel.apply(mode).copy();
        switch (mode) {
            case FIXED_DURATION -> text.withStyle(style -> style.withColor(TextColor.fromRgb(0xFFA630)));
            case FIXED_SPEED -> text.withStyle(style -> style.withColor(TextColor.fromRgb(0x00A7E1)));
        }
        return Component.empty().append(text);
    }

    public boolean modEnabled() {
        return modEnabledToggle.getValue();
    }

    // how many milliseconds a swap takes
    public long swapDuration(float distance) {
        int value = animationSpeed.getValue();
        switch (animationMode.getValue()) {
            case FIXED_SPEED: {
                long duration = (long) (value * distance / TYPICAL_DISTANCE);
                return Math.max(animationSpeed.getMin(), Math.min(animationSpeed.getMax(), duration));
            }
            case FIXED_DURATION:
            default:
                return value;
        }
    }
}
