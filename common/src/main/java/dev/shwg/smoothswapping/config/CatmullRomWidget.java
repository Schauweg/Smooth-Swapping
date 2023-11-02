package dev.shwg.smoothswapping.config;

import dev.shwg.smoothswapping.SwapUtil;
import dev.shwg.smoothswapping.Vec2;
import dev.shwg.smoothswapping.mixin.ClickableWidgetAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CatmullRomWidget extends ClickableWidget {

    private List<Vec2> points;
    Integer hoveredPointIndex = null;
    private final int borderSize, gridWidth, gridHeight, verticalLines, horizontalLines;

    private double oldMouseX = 0, oldMouseY = 0;

    public CatmullRomWidget(int x, int y, int gridWidth, int gridHeight, int borderSize, int verticalLines, int horizontalLines, List<Vec2> points) {
        super(x, y, gridWidth + 2 * borderSize, gridHeight + 2 * borderSize, ScreenTexts.EMPTY);
        this.points = points;
        this.borderSize = borderSize;
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        this.verticalLines = verticalLines;
        this.horizontalLines = horizontalLines;
        this.setTooltip(Tooltip.of(Text.translatable("smoothswapping.config.option.animationspeed.tooltip")));
        this.setTooltipDelay(1000);
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {

        //workaround because overriding mouseMoved doesn't work
        //hide tooltip when mouse is moved again
        if (mouseX != oldMouseX || mouseY != oldMouseY){
            oldMouseX = mouseX;
            oldMouseY = mouseY;
            ((ClickableWidgetAccessor) this).setLastHoveredTime(Util.getMeasuringTimeMs());
        }

        Collections.sort(this.points);

        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFA000000);

        for (int i = 0; i < verticalLines; i++) {
            int stepSize = this.gridWidth / verticalLines;
            context.drawVerticalLine(this.getX() + this.borderSize + stepSize + i * stepSize, this.getY() + borderSize, this.getY() + this.borderSize + this.gridHeight, 0x10FFFFFF);
        }

        for (int i = 0; i < horizontalLines; i++) {
            int stepSize = this.gridHeight / horizontalLines;

            context.drawHorizontalLine(this.getX() + this.borderSize, this.getX() + this.borderSize + this.gridWidth, this.getY() + this.borderSize + 1 + i * stepSize, 0x10FFFFFF);
        }

        context.drawVerticalLine(this.getX() + this.borderSize, this.getY() + this.borderSize, this.getY() + this.borderSize + gridHeight, 0xFFFFFFFF);
        context.drawHorizontalLine(this.getX() + this.borderSize, this.getX() + this.borderSize + this.gridWidth, this.getY() + this.borderSize + this.gridHeight, 0xFFFFFFFF);

        for (int i = 1; i < points.size() - 2; i++) {
            Vec2 p0 = points.get(i - 1);
            Vec2 p1 = points.get(i);
            Vec2 p2 = points.get(i + 1);
            Vec2 p3 = points.get(i + 2);

            CatmullRomSpline spline = new CatmullRomSpline(p0, p1, p2, p3);
            for (float t = 0; t < 1; t += 0.005f) {
                Vec2 point = spline.getSegment().getPoint(t);
                int xC = (int) (this.getX() + borderSize + (point.v[0] * gridWidth)) + 1;
                int yC = (int) (this.getY() + borderSize + gridHeight + -point.v[1] * gridHeight) - 1;
                drawPixel(context, xC, yC, 0xFFFF0000);
            }
        }

        hoveredPointIndex = hoveredPointIndex(mouseX, mouseY);
        for (int i = 1; i < points.size() - 1; i++) {
            Vec2 point = points.get(i);
            int xC = (int) (this.getX() + borderSize + (point.v[0] * gridWidth)) + 1;
            int yC = (int) (this.getY() + borderSize + gridHeight + -point.v[1] * gridHeight) - 1;

            if (hoveredPointIndex != null && points.get(hoveredPointIndex).equals(point)) {
                context.fill(xC - 2, yC - 2, xC + 2, yC + 2, 0xFFFFFF00);
            } else {
                context.fill(xC - 2, yC - 2, xC + 2, yC + 2, 0xFFC908FF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            if (hoveredPointIndex != null) {
                this.points.remove((int) hoveredPointIndex);
            } else if (isMouseInGrid(mouseX, mouseY)) {
                this.points.add(new Vec2(getPointX(mouseX), getPointY(mouseY)));
            }
        }
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (hoveredPointIndex != null) {
            Vec2 hoveredPoint = this.points.get(hoveredPointIndex);
            double newX = getPointX(mouseX);
            double newY = getPointY(mouseY);
            if (isMouseInGridYExtended(mouseX, mouseY))
                hoveredPoint.v[0] = newX;
            if (isMouseInGridYExtended(mouseX, mouseY))
                hoveredPoint.v[1] = newY;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (hoveredPointIndex != null) {
            this.onDrag(mouseX, mouseY, deltaX, deltaY);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    protected TooltipPositioner getTooltipPositioner() {
        return new CMRTooltipPosition(this);
    }

    public List<Vec2> getPoints() {
        return this.points;
    }

    public void reset(){
        List<Vec2> points = new ArrayList<>();
        points.add(new Vec2(0, 0));
        points.add(new Vec2(0, 0));
        points.add(new Vec2(1, 1));
        points.add(new Vec2(1, 1));
        setPoints(points);
    }

    public static List<CatmullRomSpline> splinesFromPoints(List<Vec2> points) {

        List<CatmullRomSpline> splines = new ArrayList<>();

        for (int i = 1; i < points.size() - 2; i++) {
            Vec2 p0 = points.get(i - 1);
            Vec2 p1 = points.get(i);
            Vec2 p2 = points.get(i + 1);
            Vec2 p3 = points.get(i + 2);
            splines.add(new CatmullRomSpline(p0, p1, p2, p3));
        }
        return splines;
    }

    public static double getProgress(double t, List<CatmullRomSpline> segments) {
        CatmullRomSpline currentSegment = getSegmentForT(t, segments);

        double progress = SwapUtil.map(t, currentSegment.oldX, currentSegment.x, 1, 0);

        return currentSegment.getSegment().getPoint(progress).v[1];
    }

    private static CatmullRomSpline getSegmentForT(double t, @NotNull List<CatmullRomSpline> segments) {
        for (CatmullRomSpline spline : segments) {
            if (t >= spline.oldX && t < spline.x) {
                return spline;
            }
        }
        return segments.get(0);
    }

    private double getPointX(double globalX) {
        return (-borderSize + globalX - this.getX() - 1) / gridWidth;
    }

    private double getPointY(double globalY) {
        return (borderSize - globalY + gridHeight + this.getY() - 1) / gridHeight;
    }

    private void drawPixel(DrawContext context, int x, int y, int color) {
        context.fill(x, y, x + 1, y + 1, color);
    }

    private boolean isMouseInGrid(double mouseX, double mouseY) {
        return this.visible && mouseX > (double) this.getX() + this.borderSize + 1 && mouseX < (double) (this.getX() + this.borderSize + this.gridWidth - 1) && mouseY > (double) this.getY() + this.borderSize + 1 && mouseY < (double) (this.getY() + this.borderSize + this.gridHeight - 1);
    }

    private boolean isMouseInGridYExtended(double mouseX, double mouseY) {
        return this.visible && mouseX > (double) this.getX() + this.borderSize + 1 && mouseX < (double) (this.getX() + this.borderSize + this.gridWidth - 1) && mouseY > (double) this.getY() + 1 && mouseY < (double) (this.getY() + this.height - 1);
    }

    @Nullable
    private Integer hoveredPointIndex(double mouseX, double mouseY) {

        int pointWidth = 4;

        if (isMouseOver(mouseX, mouseY)) {
            for (int i = 2; i < points.size() - 2; i++) {
                Vec2 point = points.get(i);

                int xC = (int) (this.getX() + this.borderSize + point.v[0] * this.gridWidth) - 1;
                int yC = (int) (this.getY() + this.borderSize + this.gridHeight - point.v[1] * this.gridHeight) - pointWidth;

                if (mouseX >= (double) xC && mouseX < (double) (xC + pointWidth) && mouseY >= (double) yC && mouseY < (double) (yC + pointWidth)) {
                    return i;
                }
            }
        }

        return null;
    }

    public void setPoints(List<Vec2> points) {
        this.points = points;
    }

    public static class CatmullRomSpline {
        static float tension = 0f;
        static float alpha = 0f;

        private final Segment segment;

        float x, oldX;

        public CatmullRomSpline(Vec2 p0, Vec2 p1, Vec2 p2, Vec2 p3) {

            x = (float) p2.v[0];
            oldX = (float) p1.v[0];

            double t01 = Math.pow(Vec2.distance(p0, p1), alpha);
            double t12 = Math.pow(Vec2.distance(p1, p2), alpha);
            double t23 = Math.pow(Vec2.distance(p2, p3), alpha);

            Vec2 m1 = Vec2.sum(Vec2.diff(p2, p1), (Vec2.diff((Vec2.diff(p1, p0).divideScalar(t01)), (Vec2.diff(p2, p0).divideScalar(t01 + t12)))).multiplyScalar(t12)).multiplyScalar(1.0f - tension);
            Vec2 m2 = Vec2.sum(Vec2.diff(p2, p1), (Vec2.diff((Vec2.diff(p3, p2).divideScalar(t23)), (Vec2.diff(p3, p1).divideScalar(t12 + t23)))).multiplyScalar(t12)).multiplyScalar(1.0f - tension);

            segment = new Segment(Vec2.sum(Vec2.diff(p1, p2).multiplyScalar(2.0d), Vec2.sum(m1, m2)),
                    Vec2.diff(Vec2.diff(Vec2.diff(Vec2.diff(p1, p2).multiplyScalar(-3), m1), m1), m2),
                    m1,
                    p1);
        }

        private Segment getSegment() {
            return this.segment;
        }


        private record Segment(Vec2 a, Vec2 b, Vec2 c, Vec2 d) {

            public Vec2 getPoint(double t) {
                return Vec2.sum(a.copy().multiplyScalar(t * t * t).copy(), Vec2.sum(b.copy().multiplyScalar(t * t).copy(), Vec2.sum(c.copy().multiplyScalar(t).copy(), d)));
            }
        }
    }

    public static class CMRTooltipPosition implements TooltipPositioner {

        private final CatmullRomWidget widget;
        private final int xOffset = 10;

        public CMRTooltipPosition(CatmullRomWidget widget){
            this.widget = widget;
        }

        @Override
        public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
            Vector2i vector2i = new Vector2i();
            vector2i.x = x + xOffset;
            vector2i.y = y - height;
            if (vector2i.y + height> screenHeight) {
                vector2i.y = this.widget.getY() - height - 1;
            }

            if (vector2i.x + width > screenWidth) {
                vector2i.x = Math.max(this.widget.getX() + this.widget.getWidth() - width - xOffset, 4);
            }

            return vector2i;
        }
    }
}
