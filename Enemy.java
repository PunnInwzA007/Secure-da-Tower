import java.util.List;
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

    public void moveToward(Tower tower, double deltaSeconds, List<? extends Obstacle> obstacles) {
        if (tower == null) {
            throw new IllegalArgumentException("tower cannot be null");
        }
        if (obstacles == null) {
            throw new IllegalArgumentException("obstacles cannot be null");
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
        int steps = Math.max(1, (int) Math.ceil(distanceToMove / 4.0));
        double stepX = dx / distance * distanceToMove / steps;
        double stepY = dy / distance * distanceToMove / steps;

        for (int i = 0; i < steps; i++) {
            double nextX = x + stepX;
            if (isPositionClear(nextX, y, obstacles)) {
                x = nextX;
            }

            double nextY = y + stepY;
            if (isPositionClear(x, nextY, obstacles)) {
                y = nextY;
            }
        }
    }

    private boolean isPositionClear(double centerX, double centerY,
            List<? extends Obstacle> obstacles) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle == null) {
                continue;
            }

            double closestX = Math.max(obstacle.getX(),
                    Math.min(centerX, obstacle.getX() + obstacle.getWidth()));
            double closestY = Math.max(obstacle.getY(),
                    Math.min(centerY, obstacle.getY() + obstacle.getHeight()));
            double dx = centerX - closestX;
            double dy = centerY - closestY;
            double collisionRadius = getCollisionRadius();
            if (dx * dx + dy * dy < collisionRadius * collisionRadius) {
                return false;
            }
        }
        return true;
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

    public int getRenderWidth() {
        return image.getWidth();
    }

    public int getRenderHeight() {
        return image.getHeight();
    }

    public static class FastEnemy extends Enemy {
        private static final BufferedImage IMAGE = loadImage("assets/Fast.png");

        public FastEnemy(double x, double y) {
            super(x, y, 90.0, 1, IMAGE);
        }
    }
    public static class MediumEnemy extends Enemy {
        private static final BufferedImage IMAGE = loadImage("assets/Medium.png");

        public MediumEnemy(double x, double y) {
            super(x, y, 60.0, 2, IMAGE);
        }
    }
    public static class TankEnemy extends Enemy {
        private static final BufferedImage IMAGE = loadImage("assets/Tank.png");

        public TankEnemy(double x, double y) {
            super(x, y, 35.0, 6, IMAGE);
        }

        @Override
        public double getCollisionRadius() {
            return 25.0;
        }

        @Override
        public int getRenderWidth() {
            return 50;
        }

        @Override
        public int getRenderHeight() {
            return 50;
        }
    }
}
