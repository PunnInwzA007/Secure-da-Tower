import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LevelManager {
    private static final int MAP_WIDTH = 800;
    private static final int MAP_HEIGHT = 600;
    private static final int BASE_ENEMY_QUOTA = 10;
    private static final int ENEMY_QUOTA_INCREASE = 5;

    private final List<Level> levels;
    private int currentLevelIndex;

    public LevelManager() {
        levels = new ArrayList<>();
        levels.add(createLevel(1,
                new StaticObstacle(120.0, 100.0, 120.0, 30.0),
                new StaticObstacle(560.0, 110.0, 100.0, 40.0),
                new StaticObstacle(170.0, 430.0, 80.0, 100.0),
                new StaticObstacle(500.0, 410.0, 140.0, 30.0)));
        levels.add(createLevel(2,
                new StaticObstacle(90.0, 230.0, 170.0, 30.0),
                new StaticObstacle(540.0, 320.0, 170.0, 30.0),
                new StaticObstacle(360.0, 80.0, 40.0, 120.0),
                new StaticObstacle(390.0, 420.0, 40.0, 120.0),
                new StaticObstacle(280.0, 500.0, 70.0, 35.0)));
        levels.add(createLevel(3,
                new StaticObstacle(80.0, 130.0, 180.0, 35.0),
                new StaticObstacle(540.0, 100.0, 35.0, 160.0),
                new StaticObstacle(90.0, 430.0, 35.0, 100.0),
                new StaticObstacle(500.0, 430.0, 190.0, 35.0),
                new StaticObstacle(300.0, 80.0, 35.0, 90.0)));
        levels.add(createLevel(4,
                new StaticObstacle(100.0, 180.0, 110.0, 35.0),
                new StaticObstacle(590.0, 180.0, 110.0, 35.0),
                new StaticObstacle(100.0, 385.0, 110.0, 35.0),
                new StaticObstacle(590.0, 385.0, 110.0, 35.0),
                new StaticObstacle(350.0, 70.0, 100.0, 30.0),
                new StaticObstacle(350.0, 500.0, 100.0, 30.0)));
        levels.add(createLevel(5,
                new StaticObstacle(70.0, 100.0, 35.0, 180.0),
                new StaticObstacle(200.0, 70.0, 35.0, 130.0),
                new StaticObstacle(695.0, 320.0, 35.0, 180.0),
                new StaticObstacle(565.0, 400.0, 35.0, 130.0),
                new StaticObstacle(290.0, 100.0, 80.0, 30.0),
                new StaticObstacle(430.0, 470.0, 80.0, 30.0)));
        levels.add(createLevel(6,
                new StaticObstacle(100.0, 90.0, 140.0, 30.0),
                new StaticObstacle(560.0, 90.0, 140.0, 30.0),
                new StaticObstacle(100.0, 480.0, 140.0, 30.0),
                new StaticObstacle(560.0, 480.0, 140.0, 30.0),
                new StaticObstacle(120.0, 260.0, 35.0, 100.0),
                new StaticObstacle(645.0, 260.0, 35.0, 100.0),
                new StaticObstacle(350.0, 120.0, 100.0, 30.0)));
        levels.add(createLevel(7,
                new StaticObstacle(80.0, 160.0, 220.0, 30.0),
                new StaticObstacle(500.0, 160.0, 220.0, 30.0),
                new StaticObstacle(80.0, 410.0, 220.0, 30.0),
                new StaticObstacle(500.0, 410.0, 220.0, 30.0),
                new StaticObstacle(330.0, 70.0, 30.0, 100.0),
                new StaticObstacle(440.0, 430.0, 30.0, 100.0),
                new StaticObstacle(370.0, 250.0, 60.0, 30.0)));
        levels.add(createLevel(8,
                new StaticObstacle(90.0, 80.0, 35.0, 180.0),
                new StaticObstacle(675.0, 340.0, 35.0, 180.0),
                new StaticObstacle(200.0, 100.0, 130.0, 30.0),
                new StaticObstacle(470.0, 470.0, 130.0, 30.0),
                new StaticObstacle(180.0, 440.0, 35.0, 80.0),
                new StaticObstacle(585.0, 80.0, 35.0, 80.0),
                new StaticObstacle(330.0, 100.0, 35.0, 100.0),
                new StaticObstacle(435.0, 400.0, 35.0, 100.0)));
        levels.add(createLevel(9,
                new StaticObstacle(70.0, 120.0, 180.0, 30.0),
                new StaticObstacle(550.0, 120.0, 180.0, 30.0),
                new StaticObstacle(70.0, 450.0, 180.0, 30.0),
                new StaticObstacle(550.0, 450.0, 180.0, 30.0),
                new StaticObstacle(280.0, 70.0, 30.0, 120.0),
                new StaticObstacle(490.0, 410.0, 30.0, 120.0),
                new StaticObstacle(330.0, 220.0, 140.0, 25.0),
                new StaticObstacle(330.0, 355.0, 140.0, 25.0)));
        levels.add(createLevel(10,
                new StaticObstacle(80.0, 90.0, 35.0, 160.0),
                new StaticObstacle(685.0, 350.0, 35.0, 160.0),
                new StaticObstacle(150.0, 80.0, 140.0, 30.0),
                new StaticObstacle(510.0, 490.0, 140.0, 30.0),
                new StaticObstacle(130.0, 400.0, 35.0, 120.0),
                new StaticObstacle(635.0, 80.0, 35.0, 120.0),
                new StaticObstacle(270.0, 150.0, 30.0, 80.0),
                new StaticObstacle(500.0, 370.0, 30.0, 80.0),
                new StaticObstacle(360.0, 80.0, 80.0, 25.0),
                new StaticObstacle(360.0, 495.0, 80.0, 25.0)));
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

    private Level createLevel(int levelNumber, Obstacle... obstacles) {
        List<Obstacle> obstacleList = new ArrayList<>();
        Collections.addAll(obstacleList, obstacles);
        int enemyQuota = BASE_ENEMY_QUOTA + (levelNumber - 1) * ENEMY_QUOTA_INCREASE;
        return new Level(levelNumber, MAP_WIDTH, MAP_HEIGHT, enemyQuota, obstacleList);
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
