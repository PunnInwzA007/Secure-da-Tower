public class Tower {
    public static final int SIZE = 80;
    private static final double COLLISION_RADIUS = SIZE / 2.0;
    private static final double DEFAULT_BULLET_SPEED = 400.0;
    private static final int DEFAULT_BULLET_DAMAGE = 1;

    private final double x;
    private final double y;
    private final int maxHealth;
    private int health;
    private double aimAngle;

    public Tower(double x, double y, int maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }

        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void aimAt(double targetX, double targetY) {
        aimAngle = Math.atan2(targetY - y, targetX - x);
    }

    public Bullet fire() {
        if (!isAlive()) {
            throw new IllegalStateException("A destroyed tower cannot fire");
        }

        double bulletX = x + Math.cos(aimAngle) * COLLISION_RADIUS;
        double bulletY = y + Math.sin(aimAngle) * COLLISION_RADIUS;
        return new Bullet(bulletX, bulletY, aimAngle, DEFAULT_BULLET_SPEED, DEFAULT_BULLET_DAMAGE);
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

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public double getAimAngle() {
        return aimAngle;
    }

    public double getCollisionRadius() {
        return COLLISION_RADIUS;
    }
}
