import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameController {
    private static final double MAP_WIDTH = 800.0;
    private static final double MAP_HEIGHT = 600.0;
    private static final double GAME_OVER_RADIUS = 20.0;

    private final AirstrikeSystem airstrikeSystem;
    private LevelManager levelManager;
    private Tower tower;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private Random random;
    private int enemySpawnTimer;
    private int enemiesSpawned;
    private int enemiesDefeated;
    private boolean gameOver;
    private boolean gameWon;

    public GameController() {
        airstrikeSystem = new AirstrikeSystem();
        restart();
    }

    public void restart() {
        levelManager = new LevelManager();
        tower = new Tower(400, 300, 5);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        random = new Random();
        enemySpawnTimer = 0;
        enemiesSpawned = 0;
        enemiesDefeated = 0;
        gameOver = false;
        gameWon = false;
        airstrikeSystem.reset();
    }

    public void update(double deltaSeconds) {
        if (deltaSeconds < 0.0 || !Double.isFinite(deltaSeconds)) {
            throw new IllegalArgumentException("deltaSeconds must be finite and non-negative");
        }

        updateExplosions(deltaSeconds);
        enemiesDefeated += airstrikeSystem.update(deltaSeconds, enemies, explosions);

        if (gameOver || gameWon) {
            airstrikeSystem.cancelPreparation();
            return;
        }

        updateBullets(deltaSeconds);
        updateEnemySpawning();
        updateEnemies(deltaSeconds);
        updateLevelProgression();
    }

    private void updateExplosions(double deltaSeconds) {
        Iterator<Explosion> explosionIter = explosions.iterator();
        while (explosionIter.hasNext()) {
            Explosion explosion = explosionIter.next();
            explosion.update(deltaSeconds);
            if (explosion.isFinished()) {
                explosionIter.remove();
            }
        }
    }

    private void updateBullets(double deltaSeconds) {
        List<Obstacle> currentObstacles = levelManager.getCurrentLevel().getObstacles();
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet bullet = bulletIter.next();
            bullet.update(deltaSeconds);

            boolean collided = bullet.checkCollisions(enemies, currentObstacles);
            if (collided) {
                explosions.add(new Explosion(bullet.getX(), bullet.getY()));
            }
            if (collided || !bullet.isActive()
                    || bullet.getX() < 0 || bullet.getX() > MAP_WIDTH
                    || bullet.getY() < 0 || bullet.getY() > MAP_HEIGHT) {
                bulletIter.remove();
            }
        }
    }

    private void updateEnemySpawning() {
        int enemyQuota = levelManager.getCurrentLevel().getEnemyQuota();
        if (enemiesSpawned < enemyQuota) {
            enemySpawnTimer++;
            if (enemySpawnTimer > 90) {
                spawnEnemy();
                enemiesSpawned++;
                enemySpawnTimer = 0;
            }
        }
    }

    private void updateEnemies(double deltaSeconds) {
        List<Obstacle> obstacles = levelManager.getCurrentLevel().getObstacles();
        Iterator<Enemy> enemyIter = enemies.iterator();
        while (enemyIter.hasNext()) {
            Enemy enemy = enemyIter.next();
            if (enemy.isAlive()) {
                enemy.moveToward(tower, deltaSeconds, obstacles);

                double dx = enemy.getX() - tower.getX();
                double dy = enemy.getY() - tower.getY();
                if (dx * dx + dy * dy < GAME_OVER_RADIUS * GAME_OVER_RADIUS) {
                    tower.takeDamage(1);
                    enemy.takeDamage(999);
                    if (!tower.isAlive()) {
                        gameOver = true;
                    }
                }
            } else {
                enemyIter.remove();
                enemiesDefeated++;
            }
        }
    }

    private void updateLevelProgression() {
        int enemyQuota = levelManager.getCurrentLevel().getEnemyQuota();
        if (!gameOver && enemiesSpawned == enemyQuota
                && enemiesDefeated == enemyQuota && enemies.isEmpty()) {
            if (levelManager.advanceToNextLevel()) {
                bullets.clear();
                enemies.clear();
                enemySpawnTimer = 0;
                enemiesSpawned = 0;
                enemiesDefeated = 0;
            } else {
                gameWon = true;
            }
        }
    }

    private void spawnEnemy() {
        double x;
        double y;
        switch (random.nextInt(4)) {
            case 0:
                x = random.nextInt((int) MAP_WIDTH);
                y = -20;
                break;
            case 1:
                x = random.nextInt((int) MAP_WIDTH);
                y = 620;
                break;
            case 2:
                x = -20;
                y = random.nextInt((int) MAP_HEIGHT);
                break;
            default:
                x = 820;
                y = random.nextInt((int) MAP_HEIGHT);
                break;
        }

        int levelNumber = levelManager.getCurrentLevel().getLevelNumber();
        int typeRoll = random.nextInt(100);
        int tankChance = Math.min(5 + (levelNumber - 1) * 5, 50);
        int mediumChance = Math.min(25 + (levelNumber - 1) * 4, 45);
        if (typeRoll < tankChance) {
            enemies.add(new Enemy.TankEnemy(x, y));
        } else if (typeRoll < tankChance + mediumChance) {
            enemies.add(new Enemy.MediumEnemy(x, y));
        } else {
            enemies.add(new Enemy.FastEnemy(x, y));
        }
    }

    public void aimAt(double x, double y) {
        tower.aimAt(x, y);
    }

    public void fire() {
        if (!gameOver && !gameWon) {
            bullets.add(tower.fire());
        }
    }

    public boolean activateAirstrike(double x, double y) {
        return !gameOver && !gameWon && airstrikeSystem.activate(x, y);
    }

    public void triggerGameOverForTesting() {
        gameOver = true;
        gameWon = false;
    }

    public void triggerVictoryForTesting() {
        gameWon = true;
        gameOver = false;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public Tower getTower() {
        return tower;
    }

    public List<Bullet> getBullets() {
        return Collections.unmodifiableList(bullets);
    }

    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public List<Explosion> getExplosions() {
        return Collections.unmodifiableList(explosions);
    }

    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public AirstrikeSystem getAirstrikeSystem() {
        return airstrikeSystem;
    }
}
