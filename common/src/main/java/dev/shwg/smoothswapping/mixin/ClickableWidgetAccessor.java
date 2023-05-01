package dev.shwg.smoothswapping.mixin;

import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClickableWidget.class)
public interface ClickableWidgetAccessor {
    @Accessor("lastHoveredTime")
    void setLastHoveredTime(long lastHoveredTime);
}
