import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class main extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    private static final double FRAME_DELTA_SECONDS = 0.016;

    private enum GameState {
        STATE_MENU,
        STATE_PLAYING
    }

    private final GameController gameController;
    private final HudRenderer hudRenderer;
    private final Timer timer;
    private GameState gameState = GameState.STATE_MENU;
    private double mouseX = 400.0;
    private double mouseY = 300.0;

    public main() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(new java.awt.Color(40, 120, 60));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        loadCustomCursor();

        gameController = new GameController();
        hudRenderer = new HudRenderer();
        installKeyBindings();

        timer = new Timer(16, this);
        timer.start();
    }

    private void installKeyBindings() {
        bindKey(KeyEvent.VK_R, "restartGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameState == GameState.STATE_PLAYING) {
                    gameController.restart();
                }
                repaint();
            }
        });
        bindKey(KeyEvent.VK_E, "airstrike", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameState == GameState.STATE_PLAYING) {
                    gameController.activateAirstrike(mouseX, mouseY);
                }
            }
        });

        installDeveloperTestKeyBindings();
    }

    private void bindKey(int keyCode, String actionName, AbstractAction action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0), actionName);
        getActionMap().put(actionName, action);
    }

    private void installDeveloperTestKeyBindings() {
        // DEV ONLY: remove this method call and method when screen testing is complete.
        bindKey(KeyEvent.VK_K, "testGameOver", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameState == GameState.STATE_PLAYING) {
                    gameController.triggerGameOverForTesting();
                }
                repaint();
            }
        });
        bindKey(KeyEvent.VK_W, "testGameWon", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameState == GameState.STATE_PLAYING) {
                    gameController.triggerVictoryForTesting();
                }
                repaint();
            }
        });
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.STATE_PLAYING) {
            gameController.update(FRAME_DELTA_SECONDS);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            if (gameState == GameState.STATE_MENU) {
                hudRenderer.drawMainMenu(g2d, getWidth(), getHeight(), mouseX, mouseY);
            } else {
                hudRenderer.drawGame(g2d, getWidth(), getHeight(), gameController);
            }
        } finally {
            g2d.dispose();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }

        if (gameState == GameState.STATE_MENU) {
            if (HudRenderer.getStartButtonBounds(getWidth(), getHeight()).contains(e.getPoint())) {
                gameController.restart();
                gameState = GameState.STATE_PLAYING;
                gameController.aimAt(mouseX, mouseY);
                requestFocusInWindow();
                repaint();
            } else if (HudRenderer.getExitButtonBounds(getWidth(), getHeight())
                    .contains(e.getPoint())) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                }
                System.exit(0);
            }
            return;
        }

        if (gameController.isGameOver()) {
            gameController.restart();
            repaint();
        } else if (!gameController.isGameWon()) {
            gameController.fire();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (gameState == GameState.STATE_PLAYING) {
            gameController.aimAt(mouseX, mouseY);
        } else {
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {
        if (gameState == GameState.STATE_MENU) {
            mouseX = -1.0;
            mouseY = -1.0;
            repaint();
        }
    }

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
