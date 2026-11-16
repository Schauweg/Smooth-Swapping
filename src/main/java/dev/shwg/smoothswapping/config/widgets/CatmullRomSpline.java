package dev.shwg.smoothswapping.config.widgets;

import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

public class CatmullRomSpline {

    static final float tension = 0f;
    static final float alpha = 0f;

    // Binary search steps to find the curve parameter for an x value; 20 steps is far below a pixel
    private static final int SOLVE_STEPS = 20;

    final float x, oldX;
    private final Vec2 a, b, c, d;

    public CatmullRomSpline(Vec2 p0, Vec2 p1, Vec2 p2, Vec2 p3) {
        x = p2.x;
        oldX = p1.x;

        double t01 = Math.pow(distance(p0, p1), alpha);
        double t12 = Math.pow(distance(p1, p2), alpha);
        double t23 = Math.pow(distance(p2, p3), alpha);

        Vec2 m1 = scale(add(sub(p2, p1), scale(sub(div(sub(p1, p0), t01), div(sub(p2, p0), t01 + t12)), (float) t12)), 1.0f - tension);
        Vec2 m2 = scale(add(sub(p2, p1), scale(sub(div(sub(p3, p2), t23), div(sub(p3, p1), t12 + t23)), (float) t12)), 1.0f - tension);

        a = add(scale(sub(p1, p2), 2.0f), add(m1, m2));
        b = sub(sub(scale(sub(p1, p2), -3.0f), m1), add(m1, m2));
        c = m1;
        d = p1;
    }

    // Point on this segment for a curve parameter u from 0 (p1) to 1 (p2)
    Vec2 getPoint(float u) {
        return add(scale(a, u * u * u), add(scale(b, u * u), add(scale(c, u), d)));
    }

    /**
     * Builds the full set of splines for a curve, given only its real (interior +
     * fixed endpoint) points — handles tangent padding at the boundaries internally.
     */
    public static List<CatmullRomSpline> buildFrom(List<Vec2> points) {
        List<Vec2> padded = new ArrayList<>(points.size() + 2);
        padded.add(points.get(0));
        padded.addAll(points);
        padded.add(points.get(points.size() - 1));

        List<CatmullRomSpline> splines = new ArrayList<>();
        for (int i = 1; i < padded.size() - 2; i++) {
            splines.add(new CatmullRomSpline(padded.get(i - 1), padded.get(i), padded.get(i + 1), padded.get(i + 2)));
        }
        return splines;
    }

    /**
     * Height of the curve at x = t, for t from 0 to 1.
     * Returns 0 at t = 0 and 1 at t = 1 for curves from (0, 0) to (1, 1).
     */
    public static double ease(double t, List<CatmullRomSpline> segments) {
        if (t <= 0) return segments.get(0).getPoint(0).y;
        if (t >= 1) return segments.get(segments.size() - 1).getPoint(1).y;

        CatmullRomSpline segment = getSegmentForX(t, segments);
        return segment.getPoint(segment.solveForX(t)).y;
    }

    // The curve parameter u whose point has the given x; x along a segment isn't linear in u
    private float solveForX(double targetX) {
        float low = 0f, high = 1f;
        for (int i = 0; i < SOLVE_STEPS; i++) {
            float mid = (low + high) / 2f;
            if (getPoint(mid).x < targetX) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return (low + high) / 2f;
    }

    private static CatmullRomSpline getSegmentForX(double x, List<CatmullRomSpline> segments) {
        for (CatmullRomSpline spline : segments) {
            if (x >= spline.oldX && x < spline.x) return spline;
        }
        return segments.get(segments.size() - 1);
    }

    private static Vec2 add(Vec2 a, Vec2 b) { return new Vec2(a.x + b.x, a.y + b.y); }
    private static Vec2 sub(Vec2 a, Vec2 b) { return new Vec2(a.x - b.x, a.y - b.y); }
    private static Vec2 scale(Vec2 v, float s) { return new Vec2(v.x * s, v.y * s); }
    private static Vec2 div(Vec2 v, double s) { return new Vec2((float) (v.x / s), (float) (v.y / s)); }
    private static double distance(Vec2 a, Vec2 b) {
        double dx = a.x - b.x, dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
