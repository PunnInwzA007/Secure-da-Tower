import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public abstract class Enemy {
    private static final double COLLISION_RADIUS = 12.0;

    private double x;
    private double y;
    private final double speed;
    private final int maxHealth;
    private final BufferedImage image;
    private int health;

    protected Enemy(double x, double y, double speed, int maxHealth, BufferedImage image) {
        if (speed < 0.0 || !Double.isFinite(speed)) {
            throw new IllegalArgumentException("speed must be finite and non-negative");
        }
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }
        if (image == null) {
            throw new IllegalArgumentException("image cannot be null");
        }

        this.x = x;
        this.y = y;
        this.speed = speed;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.image = image;
    }

    protected static BufferedImage loadImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image == null) {
                throw new IOException("Unsupported or invalid image: " + path);
            }
            return image;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load enemy image: " + path, e);
        }
    }

    public void moveToward(Tower tower, double deltaSeconds) {
        if (tower == null) {
            throw new IllegalArgumentException("tower cannot be null");
        }
        if (deltaSeconds < 0.0 || !Double.isFinite(deltaSeconds)) {
            throw new IllegalArgumentException("deltaSeconds must be finite and non-negative");
        }
        if (!isAlive()) {
            return;
        }

        double dx = tower.getX() - x;
        double dy = tower.getY() - y;
        double distance = Math.hypot(dx, dy);
        if (distance == 0.0) {
            return;
        }

        double distanceToMove = Math.min(distance, speed * deltaSeconds);
        x += dx / distance * distanceToMove;
        y += dy / distance * distanceToMove;
    }

    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("damage cannot be negative");
        }

        health = Math.max(0, health - damage);
    }

    public boolean isAlive() {
        return health > 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSpeed() {
        return speed;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public BufferedImage getImage() {
        return image;
    }

    public double getCollisionRadius() {
        return COLLISION_RADIUS;
    }
    public static class FastEnemy extends Enemy {
        private static final BufferedImage IMAGE = loadImage("assets/fast.png");

        public FastEnemy(double x, double y) {
            super(x, y, 90.0, 1, IMAGE);
        }
    }
    public static class MediumEnemy extends Enemy {
        private static final BufferedImage IMAGE = loadImage("assets/medium.png");

        public MediumEnemy(double x, double y) {
            super(x, y, 60.0, 2, IMAGE);
        }
    }
    public static class TankEnemy extends Enemy {
        private static final BufferedImage IMAGE = loadImage("assets/tank.png");

        public TankEnemy(double x, double y) {
            super(x, y, 35.0, 6, IMAGE);
        }
    }
}
