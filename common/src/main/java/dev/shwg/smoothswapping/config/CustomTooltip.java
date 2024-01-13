package dev.shwg.smoothswapping.config;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.Text;

public class CustomTooltip extends Tooltip {
    private final CatmullRomWidget widget;

    public CustomTooltip(CatmullRomWidget widget, Text content) {
        super(content, content);

        this.widget = widget;
    }

    @Override
    protected TooltipPositioner createPositioner(boolean hovered, boolean focused, ScreenRect focus) {
        return new CatmullRomWidget.CMRTooltipPosition(this.widget);
    }
}
