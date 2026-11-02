import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

public class LevelManager {
    private static final int MAP_WIDTH = 800;
    private static final int MAP_HEIGHT = 600;
    private static final int BASE_ENEMY_QUOTA = 10;
    private static final int ENEMY_QUOTA_INCREASE = 5;
    private static final int BASE_OBSTACLE_COUNT = 4;
    private static final int OBSTACLE_COUNT_INCREASE = 1;
    private static final int GRID_COLUMNS = MAP_WIDTH / Obstacle.GRID_SIZE;
    private static final int GRID_ROWS = MAP_HEIGHT / Obstacle.GRID_SIZE;

    private final List<Level> levels;
    private final BufferedImage[] rockImages;
    private final Random random;
    private int currentLevelIndex;

    public LevelManager() {
        rockImages = new BufferedImage[] {
                loadImage("assets/OBS1.png"),
                loadImage("assets/OBS2.png"),
                loadImage("assets/OBS3.png")
        };
        random = new Random();
        levels = new ArrayList<>();
        for (int levelNumber = 1; levelNumber <= 10; levelNumber++) {
            levels.add(createLevel(levelNumber));
        }
    }

    public Level getCurrentLevel() {
        return levels.get(currentLevelIndex);
    }

    public void loadLevel(int levelNumber) {
        if (levelNumber < 1 || levelNumber > levels.size()) {
            throw new IllegalArgumentException("Unknown level: " + levelNumber);
        }

        currentLevelIndex = levelNumber - 1;
    }

    public boolean advanceToNextLevel() {
        if (currentLevelIndex + 1 >= levels.size()) {
            return false;
        }

        currentLevelIndex++;
        return true;
    }

    public int getLevelCount() {
        return levels.size();
    }

    public BufferedImage getObstacleImage(Obstacle.RockVariant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("variant cannot be null");
        }
        return rockImages[variant.ordinal()];
    }

    private Level createLevel(int levelNumber) {
        List<Integer> availableCells = new ArrayList<>();
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int column = 0; column < GRID_COLUMNS; column++) {
                int x = column * Obstacle.GRID_SIZE;
                int y = row * Obstacle.GRID_SIZE;
                if (!overlapsTower(x, y)) {
                    availableCells.add(row * GRID_COLUMNS + column);
                }
            }
        }

        Collections.shuffle(availableCells, random);
        int obstacleCount = BASE_OBSTACLE_COUNT
                + (levelNumber - 1) * OBSTACLE_COUNT_INCREASE;
        List<Obstacle> obstacleList = new ArrayList<>();
        for (int i = 0; i < obstacleCount; i++) {
            int cell = availableCells.get(i);
            int x = cell % GRID_COLUMNS * Obstacle.GRID_SIZE;
            int y = cell / GRID_COLUMNS * Obstacle.GRID_SIZE;
            Obstacle.RockVariant variant = Obstacle.RockVariant.values()[
                    random.nextInt(Obstacle.RockVariant.values().length)];
            obstacleList.add(new StaticObstacle(x, y, variant));
        }

        int enemyQuota = BASE_ENEMY_QUOTA + (levelNumber - 1) * ENEMY_QUOTA_INCREASE;
        return new Level(levelNumber, MAP_WIDTH, MAP_HEIGHT, enemyQuota, obstacleList);
    }

    private boolean overlapsTower(int x, int y) {
        int towerLeft = (MAP_WIDTH - Tower.SIZE) / 2;
        int towerTop = (MAP_HEIGHT - Tower.SIZE) / 2;
        return x < towerLeft + Tower.SIZE
                && x + Obstacle.GRID_SIZE > towerLeft
                && y < towerTop + Tower.SIZE
                && y + Obstacle.GRID_SIZE > towerTop;
    }

    private BufferedImage loadImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image == null) {
                throw new IOException("Unsupported or invalid image: " + path);
            }
            return image;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load obstacle image: " + path, e);
        }
    }

    public static final class Level {
        private final int levelNumber;
        private final int mapWidth;
        private final int mapHeight;
        private final int enemyQuota;
        private final List<Obstacle> obstacles;

        private Level(int levelNumber, int mapWidth, int mapHeight, int enemyQuota,
                List<Obstacle> obstacles) {
            this.levelNumber = levelNumber;
            this.mapWidth = mapWidth;
            this.mapHeight = mapHeight;
            this.enemyQuota = enemyQuota;
            this.obstacles = Collections.unmodifiableList(new ArrayList<>(obstacles));
        }

        public int getLevelNumber() {
            return levelNumber;
        }

        public int getMapWidth() {
            return mapWidth;
        }

        public int getMapHeight() {
            return mapHeight;
        }

        public int getEnemyQuota() {
            return enemyQuota;
        }

        public List<Obstacle> getObstacles() {
            return obstacles;
        }
    }
}
