import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HudRenderer {
    private final BufferedImage backgroundImage = loadImage("assets/Background.png");
    private final BufferedImage gunBaseImage = loadImage("assets/Gunbase.png");
    private final BufferedImage gunImage = loadImage("assets/Gun.png");

    public void drawGame(Graphics2D g2d, int screenWidth, int screenHeight,
            GameController controller) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, screenWidth, screenHeight, null);
        } else {
            g2d.setColor(new Color(40, 120, 60));
            g2d.fillRect(0, 0, screenWidth, screenHeight);
        }

        for (Obstacle obstacle : controller.getLevelManager().getCurrentLevel().getObstacles()) {
            g2d.setColor(new Color(100, 70, 40));
            g2d.fillRect((int) obstacle.getX(), (int) obstacle.getY(),
                    (int) obstacle.getWidth(), (int) obstacle.getHeight());
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect((int) obstacle.getX(), (int) obstacle.getY(),
                    (int) obstacle.getWidth(), (int) obstacle.getHeight());
        }

        Tower tower = controller.getTower();
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
            AffineTransform transform = new AffineTransform();
            transform.translate(tower.getX(), tower.getY());
            transform.rotate(tower.getAimAngle());
            transform.translate(-gunImage.getWidth() / 2.0, -gunImage.getHeight() / 2.0);
            g2d.drawImage(gunImage, transform, null);
        } else {
            double angle = tower.getAimAngle();
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(towerX, towerY,
                    (int) (tower.getX() + Math.cos(angle) * 25),
                    (int) (tower.getY() + Math.sin(angle) * 25));
        }

        g2d.setColor(Color.YELLOW);
        for (Bullet bullet : controller.getBullets()) {
            g2d.fillOval((int) bullet.getX() - 4, (int) bullet.getY() - 4, 8, 8);
        }

        for (Enemy enemy : controller.getEnemies()) {
            if (!enemy.isAlive()) {
                continue;
            }
            BufferedImage image = enemy.getImage();
            double angle = Math.atan2(tower.getY() - enemy.getY(), tower.getX() - enemy.getX());
            AffineTransform transform = new AffineTransform();
            transform.translate(enemy.getX(), enemy.getY());
            transform.rotate(angle);
            transform.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
            g2d.drawImage(image, transform, null);
            drawEnemyHealthBar(g2d, enemy);
        }

        for (Explosion explosion : controller.getExplosions()) {
            explosion.draw(g2d);
        }

        AirstrikeSystem airstrike = controller.getAirstrikeSystem();
        if (airstrike.getPreparationRemaining() > 0.0) {
            g2d.setColor(Color.YELLOW);
            int radius = (int) airstrike.getBlastRadius();
            int targetX = (int) airstrike.getTargetX();
            int targetY = (int) airstrike.getTargetY();
            g2d.drawOval(targetX - radius, targetY - radius, radius * 2, radius * 2);
            g2d.drawLine(targetX - 8, targetY, targetX + 8, targetY);
            g2d.drawLine(targetX, targetY - 8, targetX, targetY + 8);
        }

        if (airstrike.isAirplaneFlying()) {
            BufferedImage planeImage = airstrike.getAirplaneImage();
            if (planeImage != null) {
                g2d.drawImage(planeImage,
                        (int) airstrike.getTargetX() - planeImage.getWidth() / 2,
                        (int) airstrike.getAirplaneY() - planeImage.getHeight() / 2, null);
            } else {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillOval((int) airstrike.getTargetX() - 10,
                        (int) airstrike.getAirplaneY() - 18, 20, 36);
            }
        }

        drawTacticalHud(g2d, screenWidth, tower, controller.getLevelManager(),
                controller.getEnemiesDefeated(), airstrike.getCooldownRemaining(),
                airstrike.getPreparationRemaining(), airstrike.getCooldownDuration());
        if (controller.isGameOver() || controller.isGameWon()) {
            drawMissionEndOverlay(g2d, controller.isGameWon(), screenWidth, screenHeight,
                    controller.getLevelManager(), controller.getEnemiesDefeated());
        }
    }

    private BufferedImage loadImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image == null) {
                throw new IOException("Unsupported or invalid image: " + path);
            }
            return image;
        } catch (IOException e) {
            System.err.println("Unable to load renderer image: " + path);
            e.printStackTrace();
            return null;
        }
    }

    public void drawEnemyHealthBar(Graphics2D g2d, Enemy enemy) {
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

    public void drawTacticalHud(Graphics2D g2d, int screenWidth, Tower tower,
            LevelManager levelManager, int enemiesDefeated, double airstrikeCooldownRemaining,
            double airstrikePreparationRemaining, double airstrikeCooldownDuration) {
        int x = 16;
        int y = 16;
        int barWidth = 220;
        Color accent = new Color(80, 220, 210);

        g2d.setColor(new Color(10, 20, 15, 140));
        g2d.fillRoundRect(x - 8, y - 8, barWidth + 16, 74, 8, 8);
        g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 70));
        g2d.drawRoundRect(x - 8, y - 8, barWidth + 16, 74, 8, 8);

        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        g2d.setColor(accent);
        g2d.drawString("LVL: " + levelManager.getCurrentLevel().getLevelNumber(), x, y + 10);
        g2d.setColor(Color.WHITE);
        g2d.drawString("HP: " + tower.getHealth() + "/" + tower.getMaxHealth(), x + 70, y + 10);
        g2d.drawString("KILLS: " + enemiesDefeated + "/"
                + levelManager.getCurrentLevel().getEnemyQuota(), x + 145, y + 10);

        int healthBarY = y + 18;
        double towerHealthRatio = (double) tower.getHealth() / tower.getMaxHealth();
        g2d.setColor(new Color(30, 45, 40));
        g2d.fillRoundRect(x, healthBarY, barWidth, 4, 2, 2);
        g2d.setColor(towerHealthRatio > 0.5 ? new Color(65, 220, 160)
                : towerHealthRatio > 0.25 ? new Color(255, 190, 55)
                : new Color(245, 75, 75));
        g2d.fillRoundRect(x, healthBarY, (int) (barWidth * towerHealthRatio), 4, 2, 2);

        g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        g2d.setColor(new Color(200, 220, 215, 180));
        g2d.drawString("[LMB] Shoot   [R] Restart", x, y + 50);

        drawAirstrikePanel(g2d, screenWidth - 220, 542, 204, 42, accent,
                airstrikeCooldownRemaining, airstrikePreparationRemaining,
                airstrikeCooldownDuration);

        if (airstrikePreparationRemaining > 0.0) {
            g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            g2d.setColor(new Color(255, 220, 90));
            String warning = "AIRSTRIKE INBOUND";
            drawCenteredString(g2d, warning, screenWidth, 34);
        }
    }

    private void drawAirstrikePanel(Graphics2D g2d, int x, int y, int width, int height,
            Color accent, double cooldownRemaining, double preparationRemaining,
            double cooldownDuration) {
        g2d.setColor(new Color(10, 20, 15, 150));
        g2d.fillRoundRect(x, y, width, height, 8, 8);
        g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 70));
        g2d.drawRoundRect(x, y, width, height, 8, 8);

        boolean ready = cooldownRemaining <= 0.0;
        String status = preparationRemaining > 0.0 ? "INBOUND"
                : ready ? "READY [E]" : String.format("RECHARGE %.1fs", cooldownRemaining);

        g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        g2d.setColor(ready ? new Color(100, 245, 175) : new Color(205, 220, 225));
        g2d.drawString("AIRSTRIKE: " + status, x + 10, y + 16);

        int barX = x + 10;
        int barY = y + 24;
        int barWidth = width - 20;
        double charge = ready ? 1.0 : 1.0 - cooldownRemaining / cooldownDuration;
        charge = Math.max(0.0, Math.min(1.0, charge));

        g2d.setColor(new Color(30, 45, 40));
        g2d.fillRoundRect(barX, barY, barWidth, 6, 3, 3);
        g2d.setColor(ready ? new Color(65, 225, 165) : new Color(65, 190, 205));
        if (charge > 0.0) {
            g2d.fillRoundRect(barX, barY, Math.max(4, (int) (barWidth * charge)), 6, 3, 3);
        }
    }

    public void drawMissionEndOverlay(Graphics2D g2d, boolean victory, int screenWidth,
            int screenHeight, LevelManager levelManager, int enemiesDefeated) {
        Graphics2D overlay = (Graphics2D) g2d.create();
        try {
            overlay.setColor(new Color(2, 8, 14, 190));
            overlay.fillRect(0, 0, screenWidth, screenHeight);

            int panelWidth = 460;
            int panelHeight = 250;
            int panelX = (screenWidth - panelWidth) / 2;
            int panelY = (screenHeight - panelHeight) / 2;
            Color accent = victory ? new Color(65, 230, 190) : new Color(245, 70, 85);
            drawHudPanel(overlay, panelX, panelY, panelWidth, panelHeight, accent);

            overlay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
            overlay.setColor(new Color(175, 205, 215));
            String header = victory ? "TACTICAL REPORT  //  OPERATION COMPLETE"
                    : "TACTICAL REPORT  //  SIGNAL LOST";
            drawCenteredString(overlay, header, screenWidth, panelY + 40);

            overlay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 29));
            overlay.setColor(accent);
            String status = victory ? "SECTOR SECURED!" : "MISSION FAILED";
            drawCenteredString(overlay, status, screenWidth, panelY + 88);

            overlay.setColor(new Color(40, 65, 76));
            overlay.drawLine(panelX + 30, panelY + 108, panelX + panelWidth - 30, panelY + 108);

            overlay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            overlay.setColor(new Color(190, 215, 222));
            drawCenteredString(overlay,
                    "SECTOR / LEVEL  "
                            + String.format("%02d", levelManager.getCurrentLevel().getLevelNumber()),
                    screenWidth, panelY + 142);
            drawCenteredString(overlay,
                    "HOSTILES NEUTRALIZED  " + enemiesDefeated + " / "
                            + levelManager.getCurrentLevel().getEnemyQuota(),
                    screenWidth, panelY + 166);

            overlay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
            overlay.setColor(Color.WHITE);
            drawCenteredString(overlay, "PRESS [R] TO RESTART / REPLAY",
                    screenWidth, panelY + 212);
        } finally {
            overlay.dispose();
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

    private void drawCenteredString(Graphics2D g2d, String text, int screenWidth, int baselineY) {
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, (screenWidth - textWidth) / 2, baselineY);
    }

    private Color blendColors(Color start, Color end, double amount) {
        double ratio = Math.max(0.0, Math.min(1.0, amount));
        return new Color(
                (int) (start.getRed() + (end.getRed() - start.getRed()) * ratio),
                (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * ratio),
                (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * ratio));
    }
}
