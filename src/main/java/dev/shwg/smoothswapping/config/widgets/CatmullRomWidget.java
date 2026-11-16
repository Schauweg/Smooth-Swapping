package dev.shwg.smoothswapping.config.widgets;


import dev.shwg.shwgconfig.gui.components.widget.GenericAbstractWidget;
import dev.shwg.shwgconfig.gui.input.events.GenericMouseButtonEvent;
import dev.shwg.shwgconfig.render.GenericGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class CatmullRomWidget extends GenericAbstractWidget {

    private List<Vec2> points;
    private Integer hoveredPointIndex = null;
    private final int borderSize, gridWidth, gridHeight, verticalLines, horizontalLines;
    private final Consumer<List<Vec2>> onChange;
    private List<CatmullRomSpline> renderSplines;

    public CatmullRomWidget(int x, int y, int gridWidth, int gridHeight, int borderSize,
                            int verticalLines, int horizontalLines, List<Vec2> points, Consumer<List<Vec2>> onChange) {
        super(x, y, gridWidth + 2 * borderSize, gridHeight + 2 * borderSize, Component.empty());
        this.points = points;
        this.onChange = onChange != null ? onChange : p -> {};
        this.borderSize = borderSize;
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        this.verticalLines = verticalLines;
        this.horizontalLines = horizontalLines;
        this.renderSplines = CatmullRomSpline.buildFrom(points);
    }

    @Override
    public void renderWidget(@NotNull GenericGraphics graphics, int mouseX, int mouseY, float partialTick) {
        points.sort(Comparator.comparingDouble(v -> v.x));

        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFA000000);

        for (int i = 0; i < verticalLines; i++) {
            int stepSize = gridWidth / verticalLines;
            graphics.vLine(getX() + borderSize + stepSize + i * stepSize,
                    getY() + borderSize, getY() + borderSize + gridHeight, 0x10FFFFFF);
        }
        for (int i = 0; i < horizontalLines; i++) {
            int stepSize = gridHeight / horizontalLines;
            graphics.hLine(getX() + borderSize, getX() + borderSize + gridWidth,
                    getY() + borderSize + 1 + i * stepSize, 0x10FFFFFF);
        }

        graphics.vLine(getX() + borderSize, getY() + borderSize, getY() + borderSize + gridHeight, 0xFFFFFFFF);
        graphics.hLine(getX() + borderSize, getX() + borderSize + gridWidth, getY() + borderSize + gridHeight, 0xFFFFFFFF);

        for (CatmullRomSpline spline : renderSplines) {
            for (float t = 0; t < 1; t += .5f / gridWidth) {
                Vec2 point = spline.getPoint(t);
                int xC = (int) (getX() + borderSize + (point.x * gridWidth)) + 1;
                int yC = (int) (getY() + borderSize + gridHeight + -point.y * gridHeight) - 1;
                graphics.fillWH(xC, yC, 1, 1, 0xFFFF0000);
            }
        }

        hoveredPointIndex = hoveredPointIndex(mouseX, mouseY);
        for (Vec2 point : points) {
            int xC = (int) (getX() + borderSize + (point.x * gridWidth)) + 1;
            int yC = (int) (getY() + borderSize + gridHeight + -point.y * gridHeight) - 1;
            int color = (hoveredPointIndex != null && points.get(hoveredPointIndex).equals(point)) ? 0xFFFFFF00 : 0xFFC908FF;
            graphics.fillWH(xC - 2, yC - 2, 4, 4, color);
        }

        if (isHovered()) {
            renderDeferredTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected boolean onMouseClicked(GenericMouseButtonEvent event) {
        if (event.isRightClick()) {
            if (hoveredPointIndex != null) {
                points.remove((int) hoveredPointIndex);
            } else if (isMouseInGrid(event.getMouseX(), event.getMouseY())) {
                points.add(new Vec2((float) getPointX(event.getMouseX()), (float) getPointY(event.getMouseY())));
                points.sort(Comparator.comparingDouble(v -> v.x));
            }
            renderSplines = CatmullRomSpline.buildFrom(points);
            notifyChange();
        }
        return isMouseOver(event.getMouseX(), event.getMouseY());
    }

    @Override
    protected boolean onMouseDragged(GenericMouseButtonEvent event, double deltaX, double deltaY) {
        if (hoveredPointIndex == null) return false;

        if (isMouseInGridYExtended(event.getMouseX(), event.getMouseY())) {
            points.set(hoveredPointIndex, new Vec2((float) getPointX(event.getMouseX()), (float) getPointY(event.getMouseY())));
            renderSplines = CatmullRomSpline.buildFrom(points);
            notifyChange();
        }
        return true;
    }

    private static List<Vec2> withTangentPadding(List<Vec2> points) {
        List<Vec2> padded = new ArrayList<>(points.size() + 2);
        padded.add(points.get(0));
        padded.addAll(points);
        padded.add(points.get(points.size() - 1));
        return padded;
    }

    private void notifyChange() {
        onChange.accept(new ArrayList<>(points));
    }

    @Override
    protected void updateGenericWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        // intentionally silent - no meaningful narration text for a curve editor
    }

    public List<Vec2> getPoints() {
        return points;
    }

    public void setPoints(List<Vec2> points) {
        this.points = points;
        this.renderSplines = CatmullRomSpline.buildFrom(points);
    }

    public void reset() {
        List<Vec2> defaults = new ArrayList<>();
        defaults.add(Vec2.ZERO);
        defaults.add(Vec2.ONE);
        setPoints(defaults);
    }

    private double getPointX(double globalX) {
        return (-borderSize + globalX - getX() - 1) / gridWidth;
    }

    private double getPointY(double globalY) {
        return (borderSize - globalY + gridHeight + getY() - 1) / gridHeight;
    }

    private boolean isMouseInGrid(double mouseX, double mouseY) {
        return isVisible() && mouseX > getX() + borderSize + 1 && mouseX < getX() + borderSize + gridWidth - 1
                && mouseY > getY() + borderSize + 1 && mouseY < getY() + borderSize + gridHeight - 1;
    }

    private boolean isMouseInGridYExtended(double mouseX, double mouseY) {
        return isVisible() && mouseX > getX() + borderSize + 1 && mouseX < getX() + borderSize + gridWidth - 1
                && mouseY > getY() + 1 && mouseY < getY() + getHeight() - 1;
    }

    @Nullable
    private Integer hoveredPointIndex(double mouseX, double mouseY) {
        int pointWidth = 4;
        if (!isMouseOver(mouseX, mouseY)) return null;
        for (int i = 1; i < points.size() - 1; i++) {
            Vec2 point = points.get(i);
            int xC = (int) (getX() + borderSize + point.x * gridWidth) - 1;
            int yC = (int) (getY() + borderSize + gridHeight - point.y * gridHeight) - pointWidth;
            if (mouseX >= xC && mouseX < xC + pointWidth && mouseY >= yC && mouseY < yC + pointWidth) {
                return i;
            }
        }
        return null;
    }
}
