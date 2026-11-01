import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class main extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    private Timer timer;
    private LevelManager levelManager;
    private Tower tower;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private Random random;
    private double mouseX, mouseY;
    private int enemySpawnTimer = 0;
    private int enemiesSpawned = 0;
    private int enemiesDefeated = 0;
    private boolean gameOver = false;
    private boolean gameWon = false;

    public main() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(40, 120, 60)); // สีพื้นหลังสนามหญ้า
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

        initGame();

        // Game loop (ประมาณ 60 FPS -> 16ms ต่อรอบ)
        timer = new Timer(16, this);
        timer.start();
    }

    private void initGame() {
        levelManager = new LevelManager();
        // เริ่มต้นที่ด่าน 1 (ขนาดแผนที่ 800x600, ป้อมอยู่ตรงกลาง 400, 300, HP 10)
        tower = new Tower(400, 300, 5);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        random = new Random();
        gameOver = false;
        gameWon = false;
        enemySpawnTimer = 0;
        enemiesSpawned = 0;
        enemiesDefeated = 0;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver || gameWon) {
            repaint();
            return;
        }

        double deltaSeconds = 0.016; // เวลาผ่านไปต่อเฟรม (ประมาณ 16ms)

        // 1. อัปเดตตำแหน่งกระสุนและการชน
        List<Obstacle> currentObstacles = levelManager.getCurrentLevel().getObstacles();
        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            b.update(deltaSeconds);
            
            // เช็คชนศัตรูหรือสิ่งกีดขวาง
            if (b.checkCollisions(enemies, currentObstacles) || !b.isActive()) {
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
                enemy.moveToward(tower, deltaSeconds);
                
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
        int bossChance = Math.min(5 + (levelNumber - 1) * 5, 50);
        int mediumChance = Math.min(25 + (levelNumber - 1) * 4, 45);
        Enemy.Type type;
        if (typeRoll < bossChance) {
            type = Enemy.Type.BOSS;
        } else if (typeRoll < bossChance + mediumChance) {
            type = Enemy.Type.MEDIUM;
        } else {
            type = Enemy.Type.FAST;
        }

        enemies.add(new Enemy(x, y, type));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // วาดสิ่งกีดขวาง (Obstacles ในด่านปัจจุบัน)
        g2d.setColor(new Color(100, 70, 40));
        for (Obstacle obs : levelManager.getCurrentLevel().getObstacles()) {
            g2d.fillRect((int) obs.getX(), (int) obs.getY(), (int) obs.getWidth(), (int) obs.getHeight());
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect((int) obs.getX(), (int) obs.getY(), (int) obs.getWidth(), (int) obs.getHeight());
            g2d.setColor(new Color(100, 70, 40));
        }

        // วาดป้อมปืน (Tower ตรงกลาง)
        g2d.setColor(Color.GRAY);
        g2d.fillOval((int) tower.getX() - 20, (int) tower.getY() - 20, 40, 40);
        g2d.setColor(Color.RED);
        // วาดลำกล้องปืนตามมุมที่เล็ง
        double angle = tower.getAimAngle();
        int barrelX = (int) (tower.getX() + Math.cos(angle) * 25);
        int barrelY = (int) (tower.getY() + Math.sin(angle) * 25);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine((int) tower.getX(), (int) tower.getY(), barrelX, barrelY);

        // วาดกระสุน
        g2d.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            g2d.fillOval((int) b.getX() - 4, (int) b.getY() - 4, 8, 8);
        }

        // วาดศัตรู
        for (Enemy en : enemies) {
            if (en.isAlive()) {
                g2d.setColor(en.getColor());
                g2d.fillOval((int) en.getX() - 10, (int) en.getY() - 10, 20, 20);
                drawEnemyHealthBar(g2d, en);
            }
        }

        // Display game status.
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Level: " + levelManager.getCurrentLevel().getLevelNumber(), 20, 30);
        g2d.drawString("Tower HP: " + tower.getHealth() + " / " + tower.getMaxHealth(), 20, 55);
        g2d.drawString("Enemies defeated: " + enemiesDefeated + " / "
                + levelManager.getCurrentLevel().getEnemyQuota(), 20, 80);
        g2d.drawString("Enemies alive: " + enemies.size(), 20, 105);
        g2d.drawString("Move Mouse to Aim, Left Click to Shoot, R to Restart.", 20, 570);

        // หน้าจอ Game Over / Win
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString("GAME OVER", 280, 280);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Click to Restart", 340, 330);
        }

        if (gameWon) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.GREEN);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString("ALL LEVELS CLEARED", 190, 280);
        }
    }

    private void drawEnemyHealthBar(Graphics2D g2d, Enemy enemy) {
        int blockWidth = 6;
        int blockHeight = 5;
        int blockGap = 2;
        int maxHealth = enemy.getMaxHealth();
        int barWidth = maxHealth * (blockWidth + blockGap) - blockGap;
        int startX = (int) enemy.getX() - barWidth / 2;
        int y = (int) enemy.getY() - 18;
        double healthRatio = (double) enemy.getHealth() / maxHealth;
        Color healthColor = healthRatio > 0.5
                ? Color.GREEN
                : healthRatio > 0.25 ? Color.YELLOW : Color.RED;

        for (int i = 0; i < maxHealth; i++) {
            int x = startX + i * (blockWidth + blockGap);
            g2d.setColor(i < enemy.getHealth() ? healthColor : Color.DARK_GRAY);
            g2d.fillRect(x, y, blockWidth, blockHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, blockWidth, blockHeight);
        }
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