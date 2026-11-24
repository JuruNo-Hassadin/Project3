package Project3_6813256;

import java.awt.Rectangle;

/** Immutable world coordinates for one platform. */
public final class Platform {
    private final int x;
    private final int y;
    private final int width;

    public Platform(int x, int y, int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Platform width must be positive");
        }
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }

    public Rectangle getBounds(int height) {
        return new Rectangle(x, y, width, height);
    }
}
