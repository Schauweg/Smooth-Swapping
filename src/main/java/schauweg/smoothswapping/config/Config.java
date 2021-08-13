package schauweg.smoothswapping.config;

public class Config {

    private int animationSpeed = 100;

    public int getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(int animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public float getAnimationSpeedFormatted() {
        return animationSpeed / 100F;
    }
}
