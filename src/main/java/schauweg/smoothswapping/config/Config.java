package schauweg.smoothswapping.config;

import schauweg.smoothswapping.Vec2;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private boolean toggleMod = true;
    private int animationSpeed = 100;
    private float[][] curvePoints = new float[][]{};
    public int getAnimationSpeed() {
        return animationSpeed;
    }
    public void setAnimationSpeed(int animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
    public float getAnimationSpeedFormatted() {
        return animationSpeed / 100F;
    }

    public List<CatmullRomWidget.CatmullRomSpline> getSplines() {
        return CatmullRomWidget.splinesFromPoints(getCurvePoints());
    }

    public void setCurvePoints(List<Vec2> points) {
        float[][] curvePoints = new float[points.size() - 4][2];

        for (int i = 2; i < points.size() - 2; i++) {
            Vec2 p = points.get(i);
            curvePoints[i - 2][0] = (float) p.v[0];
            curvePoints[i - 2][1] = (float) p.v[1];
        }
        this.curvePoints = curvePoints;
    }

    public List<Vec2> getCurvePoints() {

        List<Vec2> points = new ArrayList<>();

        points.add(new Vec2(0, 0));
        points.add(new Vec2(0, 0));

        for (float[] curvePoint : curvePoints) {
            points.add(new Vec2(curvePoint[0], curvePoint[1]));
        }

        points.add(new Vec2(1, 1));
        points.add(new Vec2(1, 1));

        return points;
    }

    public boolean getToggleMod(){
        return toggleMod;
    }

    public void setToggleMod(Boolean value) {
        toggleMod = value;
    }
}
