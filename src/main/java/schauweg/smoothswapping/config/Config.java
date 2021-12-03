package schauweg.smoothswapping.config;

public class Config {

    private int animationSpeed = 100;
    private String easeMode = "linear";
    private int easeSpeed = 400;

    public int getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(int animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public float getAnimationSpeedFormatted() {
        return animationSpeed / 100F;
    }

    public int getEaseSpeed() {
        return easeSpeed;
    }

    public void setEaseSpeed(int easeSpeed) {
        this.easeSpeed = easeSpeed;
    }

    public float getEaseSpeedFormatted() {
        return easeSpeed / 100F;
    }

    public String getEaseMode() {
        return easeMode;
    }

    public void setEaseMode(String easeMode) {
        this.easeMode = easeMode;
    }
}
