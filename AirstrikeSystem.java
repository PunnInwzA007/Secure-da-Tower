import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;

public class AirstrikeSystem {
    private static final double PREPARATION_SECONDS = 1.2;
    private static final double COOLDOWN_SECONDS = 20.0;
    private static final double BLAST_RADIUS = 150.0;
    private static final double AIRPLANE_SPEED = 420.0;

    private final BufferedImage airplaneImage;
    private double preparationRemaining;
    private double cooldownRemaining;
    private double targetX = 400.0;
    private double targetY = 300.0;
    private double airplaneY = 650.0;
    private boolean airplaneFlying;
    private boolean bombDropped;

    public AirstrikeSystem() {
        airplaneImage = loadAirplaneImage();
    }

    public boolean activate(double targetX, double targetY) {
        if (cooldownRemaining > 0.0 || preparationRemaining > 0.0 || airplaneFlying) {
            return false;
        }

        this.targetX = Math.max(0.0, Math.min(800.0, targetX));
        this.targetY = Math.max(0.0, Math.min(600.0, targetY));
        preparationRemaining = PREPARATION_SECONDS;
        cooldownRemaining = COOLDOWN_SECONDS;
        airplaneY = 600.0 + getAirplaneHalfHeight();
        bombDropped = false;
        return true;
    }

    public int update(double deltaSeconds, List<Enemy> enemies, List<Explosion> explosions) {
        if (deltaSeconds < 0.0 || !Double.isFinite(deltaSeconds)) {
            throw new IllegalArgumentException("deltaSeconds must be finite and non-negative");
        }
        if (enemies == null || explosions == null) {
            throw new IllegalArgumentException("game object lists cannot be null");
        }

        cooldownRemaining = Math.max(0.0, cooldownRemaining - deltaSeconds);
        if (preparationRemaining > 0.0) {
            preparationRemaining = Math.max(0.0, preparationRemaining - deltaSeconds);
            if (preparationRemaining == 0.0) {
                airplaneFlying = true;
            }
        }

        if (!airplaneFlying) {
            return 0;
        }

        double previousY = airplaneY;
        airplaneY -= AIRPLANE_SPEED * deltaSeconds;
        if (!bombDropped && previousY >= targetY && airplaneY <= targetY) {
            bombDropped = true;
            return detonate(enemies, explosions);
        }

        if (airplaneY + getAirplaneHalfHeight() < 0.0) {
            airplaneFlying = false;
        }
        return 0;
    }

    private int detonate(List<Enemy> enemies, List<Explosion> explosions) {
        explosions.add(new Explosion(targetX, targetY, BLAST_RADIUS, 0.9));

        int defeated = 0;
        Iterator<Enemy> enemyIter = enemies.iterator();
        while (enemyIter.hasNext()) {
            Enemy enemy = enemyIter.next();
            double dx = enemy.getX() - targetX;
            double dy = enemy.getY() - targetY;
            double hitRadius = BLAST_RADIUS + enemy.getCollisionRadius();
            if (enemy.isAlive() && dx * dx + dy * dy <= hitRadius * hitRadius) {
                enemy.takeDamage(enemy.getHealth());
                enemyIter.remove();
                defeated++;
            }
        }
        return defeated;
    }

    public void cancelPreparation() {
        preparationRemaining = 0.0;
    }

    public void reset() {
        preparationRemaining = 0.0;
        cooldownRemaining = 0.0;
        targetX = 400.0;
        targetY = 300.0;
        airplaneY = 600.0 + getAirplaneHalfHeight();
        airplaneFlying = false;
        bombDropped = false;
    }

    private BufferedImage loadAirplaneImage() {
        try {
            BufferedImage image = ImageIO.read(new File("assets/airplane.png"));
            if (image == null) {
                throw new IOException("Unsupported or invalid image: assets/airplane.png");
            }
            return image;
        } catch (IOException e) {
            System.err.println("Unable to load airstrike airplane image.");
            e.printStackTrace();
            return null;
        }
    }

    private double getAirplaneHalfHeight() {
        return airplaneImage == null ? 18.0 : airplaneImage.getHeight() / 2.0;
    }

    public double getPreparationRemaining() {
        return preparationRemaining;
    }

    public double getCooldownRemaining() {
        return cooldownRemaining;
    }

    public double getCooldownDuration() {
        return COOLDOWN_SECONDS;
    }

    public double getBlastRadius() {
        return BLAST_RADIUS;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public double getAirplaneY() {
        return airplaneY;
    }

    public boolean isAirplaneFlying() {
        return airplaneFlying;
    }

    public BufferedImage getAirplaneImage() {
        return airplaneImage;
    }
}
