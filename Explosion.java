import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

public class Explosion {
    private static final double DURATION_SECONDS = 0.45;
    private static final double SECOND_RING_DELAY_SECONDS = 0.07;
    private static final double FIRST_RING_MAX_RADIUS = 24.0;
    private static final double SECOND_RING_MAX_RADIUS = 40.0;

    private final double x;
    private final double y;
    private final double durationSeconds;
    private final double secondRingDelaySeconds;
    private final double firstRingMaxRadius;
    private final double secondRingMaxRadius;
    private double age;

    public Explosion(double x, double y) {
        this(x, y, FIRST_RING_MAX_RADIUS, SECOND_RING_MAX_RADIUS, DURATION_SECONDS);
    }

    public Explosion(double x, double y, double maxRadius, double durationSeconds) {
        this(x, y, maxRadius * 0.6, maxRadius, durationSeconds);
    }

    private Explosion(double x, double y, double firstRingMaxRadius, double secondRingMaxRadius,
            double durationSeconds) {
        if (firstRingMaxRadius <= 0.0 || secondRingMaxRadius < firstRingMaxRadius
                || durationSeconds <= 0.0 || !Double.isFinite(firstRingMaxRadius)
                || !Double.isFinite(secondRingMaxRadius) || !Double.isFinite(durationSeconds)) {
            throw new IllegalArgumentException("Explosion size and duration must be finite and positive");
        }
        this.x = x;
        this.y = y;
        this.durationSeconds = durationSeconds;
        this.secondRingDelaySeconds = Math.min(SECOND_RING_DELAY_SECONDS, durationSeconds * 0.2);
        this.firstRingMaxRadius = firstRingMaxRadius;
        this.secondRingMaxRadius = secondRingMaxRadius;
    }

    public void update(double deltaSeconds) {
        if (deltaSeconds < 0.0 || !Double.isFinite(deltaSeconds)) {
            throw new IllegalArgumentException("deltaSeconds must be finite and non-negative");
        }

        age = Math.min(age + deltaSeconds, durationSeconds);
    }

    public boolean isFinished() {
        return age >= durationSeconds;
    }

    public void draw(Graphics2D g2d) {
        Stroke originalStroke = g2d.getStroke();
        double firstProgress = Math.min(age / durationSeconds, 1.0);
        drawRing(g2d, 2.0 + firstProgress * firstRingMaxRadius,
                (int) (255 * (1.0 - firstProgress)), new Color(255, 190, 45), 3.0f);

        if (age >= secondRingDelaySeconds) {
            double secondDuration = durationSeconds - secondRingDelaySeconds;
            double secondProgress = Math.min((age - secondRingDelaySeconds) / secondDuration, 1.0);
            drawRing(g2d, 10.0 + secondProgress * (secondRingMaxRadius - 10.0),
                    (int) (255 * (1.0 - secondProgress)), new Color(255, 90, 25), 2.0f);
        }
        g2d.setStroke(originalStroke);
    }

    private void drawRing(Graphics2D g2d, double radius, int alpha, Color color, float strokeWidth) {
        if (alpha <= 0) {
            return;
        }

        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.draw(new Ellipse2D.Double(x - radius, y - radius, radius * 2.0, radius * 2.0));
    }
}
