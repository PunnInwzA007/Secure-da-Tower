import java.awt.Color;

public class Enemy {
    private static final double COLLISION_RADIUS = 12.0;

    public enum Type {
        FAST(90.0, 1, Color.GREEN),
        MEDIUM(60.0, 2, Color.BLUE),
        BOSS(35.0, 6, Color.RED);

        private final double speed;
        private final int maxHealth;
        private final Color color;

        Type(double speed, int maxHealth, Color color) {
            this.speed = speed;
            this.maxHealth = maxHealth;
            this.color = color;
        }

        public double getSpeed() {
            return speed;
        }

        public int getMaxHealth() {
            return maxHealth;
        }

        public Color getColor() {
            return color;
        }
    }

    private double x;
    private double y;
    private final double speed;
    private final int maxHealth;
    private final Type type;
    private int health;

    public Enemy(double x, double y, Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        this.x = x;
        this.y = y;
        this.type = type;
        this.speed = type.getSpeed();
        this.maxHealth = type.getMaxHealth();
        this.health = maxHealth;
    }

    public Enemy(double x, double y, double speed, int health) {
        if (speed < 0.0) {
            throw new IllegalArgumentException("speed cannot be negative");
        }
        if (health <= 0) {
            throw new IllegalArgumentException("health must be positive");
        }

        this.x = x;
        this.y = y;
        this.speed = speed;
        this.maxHealth = health;
        this.health = health;
        this.type = Type.MEDIUM;
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

    public Type getType() {
        return type;
    }

    public Color getColor() {
        return type.getColor();
    }

    public double getCollisionRadius() {
        return COLLISION_RADIUS;
    }
}
