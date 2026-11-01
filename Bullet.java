import java.util.List;

public class Bullet {
    private double x;
    private double y;
    private final double velocityX;
    private final double velocityY;
    private final int damage;
    private boolean active = true;

    public Bullet(double x, double y, double angle, double speed, int damage) {
        if (speed <= 0.0 || !Double.isFinite(speed)) {
            throw new IllegalArgumentException("speed must be finite and positive");
        }
        if (damage <= 0) {
            throw new IllegalArgumentException("damage must be positive");
        }

        this.x = x;
        this.y = y;
        this.velocityX = Math.cos(angle) * speed;
        this.velocityY = Math.sin(angle) * speed;
        this.damage = damage;
    }

    public void update(double deltaSeconds) {
        if (deltaSeconds < 0.0 || !Double.isFinite(deltaSeconds)) {
            throw new IllegalArgumentException("deltaSeconds must be finite and non-negative");
        }
        if (!active) {
            return;
        }

        x += velocityX * deltaSeconds;
        y += velocityY * deltaSeconds;
    }

    public boolean checkCollisions(List<Enemy> enemies, List<? extends Obstacle> obstacles) {
        if (enemies == null || obstacles == null) {
            throw new IllegalArgumentException("collision lists cannot be null");
        }
        if (!active) {
            return false;
        }

        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                double dx = x - enemy.getX();
                double dy = y - enemy.getY();
                double radius = enemy.getCollisionRadius();
                if (dx * dx + dy * dy <= radius * radius) {
                    enemy.takeDamage(damage);
                    active = false;
                    return true;
                }
            }
        }

        for (Obstacle obstacle : obstacles) {
            if (obstacle != null
                    && x >= obstacle.getX()
                    && x <= obstacle.getX() + obstacle.getWidth()
                    && y >= obstacle.getY()
                    && y <= obstacle.getY() + obstacle.getHeight()) {
                active = false;
                return true;
            }
        }

        return false;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isActive() {
        return active;
    }
}
