public abstract class Obstacle {
    public static final int GRID_SIZE = 64;

    protected double x;
    protected double y;
    protected final double width;
    protected final double height;
    private final RockVariant rockVariant;

    protected Obstacle(double x, double y, RockVariant rockVariant) {
        if (rockVariant == null) {
            throw new IllegalArgumentException("rockVariant cannot be null");
        }
        this.x = x;
        this.y = y;
        this.width = GRID_SIZE;
        this.height = GRID_SIZE;
        this.rockVariant = rockVariant;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public RockVariant getRockVariant() {
        return rockVariant;
    }

    public enum RockVariant {
        OBS1,
        OBS2,
        OBS3
    }
}
