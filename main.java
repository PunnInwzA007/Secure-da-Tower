import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class main extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    private static final double AIRSTRIKE_PREPARATION_SECONDS = 1.2;
    private static final double AIRSTRIKE_COOLDOWN_SECONDS = 20.0;
    private static final double AIRSTRIKE_BLAST_RADIUS = 150.0;
    private static final double AIRPLANE_SPEED = 420.0;

    private Timer timer;
    private BufferedImage bgImage;
    private BufferedImage gunBaseImage;
    private BufferedImage gunImage;
    private BufferedImage airplaneImage;
    private LevelManager levelManager;
    private Tower tower;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private Random random;
    private double mouseX = 400.0;
    private double mouseY = 300.0;
    private int enemySpawnTimer = 0;
    private int enemiesSpawned = 0;
    private int enemiesDefeated = 0;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private double airstrikePreparationRemaining;
    private double airstrikeCooldownRemaining;
    private double airstrikeTargetX = 400.0;
    private double airstrikeTargetY = 300.0;
    private double airplaneY = 650.0;
    private boolean airplaneFlying;
    private boolean airstrikeBombDropped;

    public main() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(40, 120, 60)); // สีพื้นหลังสนามหญ้า
        bgImage = loadImage("assets/background.png");
        gunBaseImage = loadImage("assets/Gunbase.png");
        gunImage = loadImage("assets/Gun.png");
        airplaneImage = loadImage("assets/airplane.png");
        loadCustomCursor();
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "restartGame");
        getActionMap().put("restartGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initGame();
                repaint();
            }
        });
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "airstrike");
        getActionMap().put("airstrike", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activateAirstrike();
            }
        });
        installDeveloperTestKeyBindings();

        initGame();

        // Game loop (ประมาณ 60 FPS -> 16ms บ)
        timer = new Timer(16, this);
        timer.start();
    }

    private void initGame() {
        levelManager = new LevelManager();
        // เริ่มต้นที่ด่าน 1 (ขนาดแผนที่ 800x600, ป้อมอยู่ตรงกลาง 400, 300, HP 10)
        tower = new Tower(400, 300, 5);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        random = new Random();
        gameOver = false;
        gameWon = false;
        enemySpawnTimer = 0;
        enemiesSpawned = 0;
        enemiesDefeated = 0;
        airstrikePreparationRemaining = 0.0;
        airstrikeCooldownRemaining = 0.0;
        airplaneFlying = false;
        airplaneY = 650.0;
        airstrikeBombDropped = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        double deltaSeconds = 0.016; // เวลาผ่านไปต่อเฟรม (ประมาณ 16ms)
        updateExplosions(deltaSeconds);
        updateAirstrike(deltaSeconds);

        if (gameOver || gameWon) {
            airstrikePreparationRemaining = 0.0;
            repaint();
            return;
        }

        // 1. อัปเดตตำแหน่งกระสุนและการชน
        List<Obstacle> currentObstacles = levelManager.getCurrentLevel().getObstacles();
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            b.update(deltaSeconds);
            
            // เช็คชนศัตรูหรือสิ่งกีดขวาง
            boolean collided = b.checkCollisions(enemies, currentObstacles);
            if (collided) {
                explosions.add(new Explosion(b.getX(), b.getY()));
            }
            if (collided || !b.isActive()) {
                bulletIter.remove();
            } else {
                // เช็คออกนอกจอไหม
                if (b.getX() < 0 || b.getX() > 800 || b.getY() < 0 || b.getY() > 600) {
                    bulletIter.remove();
                }
            }
        }

        // 2. Spawn only up to the current level's fixed enemy quota.
        int enemyQuota = levelManager.getCurrentLevel().getEnemyQuota();
        if (enemiesSpawned < enemyQuota) {
            enemySpawnTimer++;
            if (enemySpawnTimer > 90) { // Spawn approximately every 1.5 seconds.
                spawnEnemy();
                enemiesSpawned++;
                enemySpawnTimer = 0;
            }
        }

        // 3. อัปเดตศัตรู
        Iterator<Enemy> enemyIter = enemies.iterator();
        while (enemyIter.hasNext()) {
            Enemy enemy = enemyIter.next();
            if (enemy.isAlive()) {
                enemy.moveToward(tower, deltaSeconds,
                        levelManager.getCurrentLevel().getObstacles());
                
                // เช็คว่าศัตรูชนป้อมตรงกลางหรือยัง
                double dx = enemy.getX() - tower.getX();
                double dy = enemy.getY() - tower.getY();
                if (dx * dx + dy * dy < 20 * 20) {
                    tower.takeDamage(1);
                    enemy.takeDamage(999); // ตายเมื่อถึงป้อม
                    if (!tower.isAlive()) {
                        gameOver = true;
                    }
                }
            } else {
                enemyIter.remove();
                enemiesDefeated++;
            }
        }

        // 4. Advance only after every enemy in the quota has been defeated.
        if (!gameOver
                && enemiesSpawned == enemyQuota
                && enemiesDefeated == enemyQuota
                && enemies.isEmpty()) {
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

        repaint();
    }

    private void spawnEnemy() {
        double x = 0, y = 0;
        int edge = random.nextInt(4);
        switch (edge) {
            case 0: x = random.nextInt(800); y = -20; break; // บน
            case 1: x = random.nextInt(800); y = 620; break; // ล่าง
            case 2: x = -20; y = random.nextInt(600); break; // ซ้าย
            case 3: x = 820; y = random.nextInt(600); break; // ขวา
        }

        int levelNumber = levelManager.getCurrentLevel().getLevelNumber();
        int typeRoll = random.nextInt(100);
        int tankChance = Math.min(5 + (levelNumber - 1) * 5, 50);
        int mediumChance = Math.min(25 + (levelNumber - 1) * 4, 45);
        Enemy enemy;
        if (typeRoll < tankChance) {
            enemy = new Enemy.TankEnemy(x, y);
        } else if (typeRoll < tankChance + mediumChance) {
            enemy = new Enemy.MediumEnemy(x, y);
        } else {
            enemy = new Enemy.FastEnemy(x, y);
        }

        enemies.add(enemy);
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

    private void activateAirstrike() {
        if (gameOver || gameWon || airstrikeCooldownRemaining > 0.0
                || airstrikePreparationRemaining > 0.0 || airplaneFlying) {
            return;
        }

        airstrikeTargetX = Math.max(0.0, Math.min(800.0, mouseX));
        airstrikeTargetY = Math.max(0.0, Math.min(600.0, mouseY));
        airstrikePreparationRemaining = AIRSTRIKE_PREPARATION_SECONDS;
        airstrikeCooldownRemaining = AIRSTRIKE_COOLDOWN_SECONDS;
        airplaneY = getAirplaneHalfHeight() + 600.0;
        airstrikeBombDropped = false;
    }

    private void updateAirstrike(double deltaSeconds) {
        airstrikeCooldownRemaining = Math.max(0.0, airstrikeCooldownRemaining - deltaSeconds);
        if (airstrikePreparationRemaining > 0.0) {
            airstrikePreparationRemaining = Math.max(0.0, airstrikePreparationRemaining - deltaSeconds);
            if (airstrikePreparationRemaining == 0.0) {
                airplaneFlying = true;
            }
        }

        if (!airplaneFlying) {
            return;
        }

        double previousY = airplaneY;
        airplaneY -= AIRPLANE_SPEED * deltaSeconds;
        if (!airstrikeBombDropped && previousY >= airstrikeTargetY && airplaneY <= airstrikeTargetY) {
            airstrikeBombDropped = true;
            detonateAirstrike();
        }

        if (airplaneY + getAirplaneHalfHeight() < 0.0) {
            airplaneFlying = false;
        }
    }

    private double getAirplaneHalfHeight() {
        return airplaneImage == null ? 18.0 : airplaneImage.getHeight() / 2.0;
    }

    private void detonateAirstrike() {
        explosions.add(new Explosion(airstrikeTargetX, airstrikeTargetY,
                AIRSTRIKE_BLAST_RADIUS, 0.9));

        Iterator<Enemy> enemyIter = enemies.iterator();
        while (enemyIter.hasNext()) {
            Enemy enemy = enemyIter.next();
            double dx = enemy.getX() - airstrikeTargetX;
            double dy = enemy.getY() - airstrikeTargetY;
            double hitRadius = AIRSTRIKE_BLAST_RADIUS + enemy.getCollisionRadius();
            if (enemy.isAlive() && dx * dx + dy * dy <= hitRadius * hitRadius) {
                enemy.takeDamage(enemy.getHealth());
                enemyIter.remove();
                enemiesDefeated++;
            }
        }
    }

    private void loadCustomCursor() {
        try {
            BufferedImage cursorImage = ImageIO.read(new File("assets/cursor.png"));
            if (cursorImage == null) {
                System.err.println("Unable to load custom cursor: assets/cursor.png is not a supported image.");
                return;
            }

            Point hotspot = new Point(cursorImage.getWidth() / 2, cursorImage.getHeight() / 2);
            Cursor cursor = Toolkit.getDefaultToolkit()
                    .createCustomCursor(cursorImage, hotspot, "Secure da Tower cursor");
            setCursor(cursor);
        } catch (IOException | HeadlessException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void installDeveloperTestKeyBindings() {
        // DEV ONLY: remove this method call and method when screen testing is complete.
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0), "testGameOver");
        getActionMap().put("testGameOver", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameOver = true;
                gameWon = false;
                repaint();
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "testGameWon");
        getActionMap().put("testGameWon", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameWon = true;
                gameOver = false;
                repaint();
            }
        });
    }

    private BufferedImage loadImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image == null) {
                throw new IOException("Unsupported or invalid image: " + path);
            }
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, 800, 600, null);
        } else {
            g2d.setColor(new Color(40, 120, 60));
            g2d.fillRect(0, 0, 800, 600);
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // วาดสิ่งกีดขวาง (Obstacles ในด่านปัจจุบัน)
        g2d.setColor(new Color(100, 70, 40));
        for (Obstacle obs : levelManager.getCurrentLevel().getObstacles()) {
            g2d.fillRect((int) obs.getX(), (int) obs.getY(), (int) obs.getWidth(), (int) obs.getHeight());
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect((int) obs.getX(), (int) obs.getY(), (int) obs.getWidth(), (int) obs.getHeight());
            g2d.setColor(new Color(100, 70, 40));
        }

        // Draw the fixed base first, then rotate the gun around the tower center.
        int towerX = (int) tower.getX();
        int towerY = (int) tower.getY();
        if (gunBaseImage != null) {
            g2d.drawImage(gunBaseImage, towerX - gunBaseImage.getWidth() / 2,
                    towerY - gunBaseImage.getHeight() / 2, null);
        } else {
            g2d.setColor(Color.GRAY);
            g2d.fillOval(towerX - 20, towerY - 20, 40, 40);
        }

        if (gunImage != null) {
            AffineTransform gunTransform = new AffineTransform();
            gunTransform.translate(tower.getX(), tower.getY());
            gunTransform.rotate(tower.getAimAngle());
            gunTransform.translate(-gunImage.getWidth() / 2.0, -gunImage.getHeight() / 2.0);
            g2d.drawImage(gunImage, gunTransform, null);
        } else {
            double angle = tower.getAimAngle();
            int barrelX = (int) (tower.getX() + Math.cos(angle) * 25);
            int barrelY = (int) (tower.getY() + Math.sin(angle) * 25);
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(towerX, towerY, barrelX, barrelY);
        }

        // วาดกระสุน
        g2d.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            g2d.fillOval((int) b.getX() - 4, (int) b.getY() - 4, 8, 8);
        }

        // วาดศัตรู
        for (Enemy en : enemies) {
            if (en.isAlive()) {
                BufferedImage enemyImage = en.getImage();
                double dx = tower.getX() - en.getX();
                double dy = tower.getY() - en.getY();
                double angle = Math.atan2(dy, dx);
                AffineTransform enemyTransform = new AffineTransform();
                enemyTransform.translate(en.getX(), en.getY());
                enemyTransform.rotate(angle);
                enemyTransform.translate(-enemyImage.getWidth() / 2.0,
                        -enemyImage.getHeight() / 2.0);
                g2d.drawImage(enemyImage, enemyTransform, null);
                drawEnemyHealthBar(g2d, en);
            }
        }

        for (Explosion explosion : explosions) {
            explosion.draw(g2d);
        }

        if (airstrikePreparationRemaining > 0.0) {
            g2d.setColor(Color.YELLOW);
            int warningRadius = (int) AIRSTRIKE_BLAST_RADIUS;
            int targetX = (int) airstrikeTargetX;
            int targetY = (int) airstrikeTargetY;
            g2d.drawOval(targetX - warningRadius, targetY - warningRadius,
                    warningRadius * 2, warningRadius * 2);
            g2d.drawLine(targetX - 8, targetY, targetX + 8, targetY);
            g2d.drawLine(targetX, targetY - 8, targetX, targetY + 8);
        }

        if (airplaneFlying) {
            if (airplaneImage != null) {
                g2d.drawImage(airplaneImage,
                        (int) airstrikeTargetX - airplaneImage.getWidth() / 2,
                        (int) airplaneY - airplaneImage.getHeight() / 2, null);
            } else {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillOval((int) airstrikeTargetX - 10, (int) airplaneY - 18, 20, 36);
            }
        }

        drawTacticalHud(g2d);

        if (gameOver || gameWon) {
            drawMissionEndOverlay(g2d, gameWon);
        }
    }

    private void drawMissionEndOverlay(Graphics2D g2d, boolean victory) {
        Graphics2D overlay = (Graphics2D) g2d.create();
        overlay.setColor(new Color(2, 8, 14, 190));
        overlay.fillRect(0, 0, getWidth(), getHeight());

        int panelWidth = 460;
        int panelHeight = 250;
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = (getHeight() - panelHeight) / 2;
        Color accent = victory ? new Color(65, 230, 190) : new Color(245, 70, 85);
        drawHudPanel(overlay, panelX, panelY, panelWidth, panelHeight, accent);

        overlay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        overlay.setColor(new Color(175, 205, 215));
        String header = victory ? "TACTICAL REPORT  //  OPERATION COMPLETE"
                : "TACTICAL REPORT  //  SIGNAL LOST";
        drawCenteredString(overlay, header, panelY + 40);

        overlay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 29));
        overlay.setColor(accent);
        String status = victory ? "SECTOR SECURED!" : "MISSION FAILED";
        drawCenteredString(overlay, status, panelY + 88);

        overlay.setColor(new Color(40, 65, 76));
        overlay.drawLine(panelX + 30, panelY + 108, panelX + panelWidth - 30, panelY + 108);

        overlay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        overlay.setColor(new Color(190, 215, 222));
        drawCenteredString(overlay,
                "SECTOR / LEVEL  " + String.format("%02d", levelManager.getCurrentLevel().getLevelNumber()),
                panelY + 142);
        drawCenteredString(overlay,
                "HOSTILES NEUTRALIZED  " + enemiesDefeated + " / "
                        + levelManager.getCurrentLevel().getEnemyQuota(),
                panelY + 166);

        overlay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        overlay.setColor(Color.WHITE);
        drawCenteredString(overlay, "PRESS [R] TO RESTART / REPLAY", panelY + 212);
        overlay.dispose();
    }

    private void drawCenteredString(Graphics2D g2d, String text, int baselineY) {
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, (getWidth() - textWidth) / 2, baselineY);
    }

    private void drawEnemyHealthBar(Graphics2D g2d, Enemy enemy) {
        int barWidth = 36;
        int barHeight = 5;
        int x = (int) enemy.getX() - barWidth / 2;
        int y = (int) enemy.getY() - enemy.getImage().getHeight() / 2 - 9;
        double healthRatio = Math.max(0.0,
                Math.min(1.0, (double) enemy.getHealth() / enemy.getMaxHealth()));
        Color healthColor = healthRatio > 0.5
                ? blendColors(new Color(255, 190, 55), new Color(65, 225, 140),
                        (healthRatio - 0.5) * 2.0)
                : blendColors(new Color(240, 65, 70), new Color(255, 190, 55),
                        healthRatio * 2.0);

        g2d.setColor(new Color(5, 12, 18, 190));
        g2d.fillRoundRect(x, y, barWidth, barHeight, 4, 4);
        if (healthRatio > 0.0) {
            g2d.setColor(healthColor);
            g2d.fillRoundRect(x + 1, y + 1,
                    Math.max(1, (int) ((barWidth - 2) * healthRatio)), barHeight - 2, 3, 3);
        }
        g2d.setColor(new Color(190, 220, 225, 170));
        g2d.drawRoundRect(x, y, barWidth, barHeight, 4, 4);
    }

    private Color blendColors(Color start, Color end, double amount) {
        double ratio = Math.max(0.0, Math.min(1.0, amount));
        return new Color(
                (int) (start.getRed() + (end.getRed() - start.getRed()) * ratio),
                (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * ratio),
                (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * ratio));
    }

    private void drawTacticalHud(Graphics2D g2d) {
        int x = 16;
        int y = 16;
        int barWidth = 220;
        Color accent = new Color(80, 220, 210);

        // พื้นหลังโปร่งใสแบบบางเฉียบ ไม่บังจอ
        g2d.setColor(new Color(10, 20, 15, 140));
        g2d.fillRoundRect(x - 8, y - 8, barWidth + 16, 74, 8, 8);
        g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 70));
        g2d.drawRoundRect(x - 8, y - 8, barWidth + 16, 74, 8, 8);

        // ข้อความสถานะย่อแบบมินิมอล
        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        g2d.setColor(accent);
        g2d.drawString("LVL: " + levelManager.getCurrentLevel().getLevelNumber(), x, y + 10);
        g2d.setColor(Color.WHITE);
        g2d.drawString("HP: " + tower.getHealth() + "/" + tower.getMaxHealth(), x + 70, y + 10);
        g2d.drawString("KILLS: " + enemiesDefeated + "/" + levelManager.getCurrentLevel().getEnemyQuota(), x + 145, y + 10);

        // หลอดเลือดป้อมแบบแบนๆ
        int healthBarY = y + 18;
        double towerHealthRatio = (double) tower.getHealth() / tower.getMaxHealth();
        g2d.setColor(new Color(30, 45, 40));
        g2d.fillRoundRect(x, healthBarY, barWidth, 4, 2, 2);
        g2d.setColor(towerHealthRatio > 0.5 ? new Color(65, 220, 160)
                : towerHealthRatio > 0.25 ? new Color(255, 190, 55)
                : new Color(245, 75, 75));
        g2d.fillRoundRect(x, healthBarY, (int) (barWidth * towerHealthRatio), 4, 2, 2);

        // ข้อมูลคีย์ลัดด้านล่างแบบเรียบง่าย
        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        g2d.setColor(new Color(200, 220, 215, 180));
        g2d.drawString("[LMB] Shoot   [R] Restart", x, y + 50);

        // วาดหลอด Airstrike แบนๆ ที่มุมขวาล่าง
        drawAirstrikePanel(g2d, 580, 542, 204, 42, accent);

        if (airstrikePreparationRemaining > 0.0) {
            g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            g2d.setColor(new Color(255, 220, 90));
            String warning = "AIRSTRIKE INBOUND";
            int warningWidth = g2d.getFontMetrics().stringWidth(warning);
            g2d.drawString(warning, (800 - warningWidth) / 2, 34);
        }
    }

    private void drawAirstrikePanel(Graphics2D g2d, int x, int y, int width, int height, Color accent) {
        // แผงสกิล E แบบหลอดแบนขนาดเล็ก ชิดมุมขวาล่าง
        g2d.setColor(new Color(10, 20, 15, 150));
        g2d.fillRoundRect(x, y, width, height, 8, 8);
        g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 70));
        g2d.drawRoundRect(x, y, width, height, 8, 8);

        boolean ready = airstrikeCooldownRemaining <= 0.0;
        String status = airstrikePreparationRemaining > 0.0 ? "INBOUND"
                : ready ? "READY [E]" : String.format("RECHARGE %.1fs", airstrikeCooldownRemaining);

        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        g2d.setColor(ready ? new Color(100, 245, 175) : new Color(205, 220, 225));
        g2d.drawString("AIRSTRIKE: " + status, x + 10, y + 16);

        // หลอดคูลดาวน์แบบแบนยาว
        int barX = x + 10;
        int barY = y + 24;
        int barWidth = width - 20;
        double charge = ready ? 1.0 : 1.0 - airstrikeCooldownRemaining / AIRSTRIKE_COOLDOWN_SECONDS;
        charge = Math.max(0.0, Math.min(1.0, charge));

        g2d.setColor(new Color(30, 45, 40));
        g2d.fillRoundRect(barX, barY, barWidth, 6, 3, 3);
        g2d.setColor(ready ? new Color(65, 225, 165) : new Color(65, 190, 205));
        if (charge > 0.0) {
            g2d.fillRoundRect(barX, barY, Math.max(4, (int) (barWidth * charge)), 6, 3, 3);
        }
    }

    private void drawHudPanel(Graphics2D g2d, int x, int y, int width, int height,
            Color accent) {
        g2d.setColor(new Color(5, 14, 22, 218));
        g2d.fillRoundRect(x, y, width, height, 12, 12);
        g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 95));
        g2d.drawRoundRect(x, y, width, height, 12, 12);
        g2d.setColor(accent);
        int corner = 12;
        g2d.drawLine(x + 1, y + corner, x + 1, y + 1);
        g2d.drawLine(x + 1, y + 1, x + corner, y + 1);
        g2d.drawLine(x + width - corner, y + 1, x + width - 1, y + 1);
        g2d.drawLine(x + width - 1, y + 1, x + width - 1, y + corner);
        g2d.drawLine(x + 1, y + height - corner, x + 1, y + height - 1);
        g2d.drawLine(x + 1, y + height - 1, x + corner, y + height - 1);
        g2d.drawLine(x + width - corner, y + height - 1, x + width - 1, y + height - 1);
        g2d.drawLine(x + width - 1, y + height - corner, x + width - 1, y + height - 1);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver) {
            initGame();
            return;
        }
        if (!gameWon && e.getButton() == MouseEvent.BUTTON1) { // คลิกซ้ายยิง
            Bullet b = tower.fire();
            bullets.add(b);
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        tower.aimAt(mouseX, mouseY);
    }

    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Secure da Tower");
        main gamePanel = new main();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}