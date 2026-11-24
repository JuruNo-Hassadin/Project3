/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

import java.awt.Rectangle;

/** World coordinates and optional horizontal motion for one platform. */
public final class Platform {
    private double x;
    private final int y;
    private final int width;
    private final boolean moving;
    private final int movementMinX;
    private final int movementMaxX;
    private final double movementSpeed;
    private int movementDirection;

    public Platform(int x, int y, int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Platform width must be positive");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        moving = false;
        movementMinX = x;
        movementMaxX = x;
        movementSpeed = 0.0;
        movementDirection = 0;
    }

    public Platform(int x, int y, int width, int movementMinX, int movementMaxX,
            double movementSpeed, int movementDirection) {
        if (width <= 0) {
            throw new IllegalArgumentException("Platform width must be positive");
        }
        if (movementMinX > x || movementMaxX < x || movementMinX > movementMaxX) {
            throw new IllegalArgumentException("Platform start must be inside its movement range");
        }
        if (movementSpeed <= 0.0 || Math.abs(movementDirection) != 1) {
            throw new IllegalArgumentException("Moving platforms need a positive speed and direction");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        moving = true;
        this.movementMinX = movementMinX;
        this.movementMaxX = movementMaxX;
        this.movementSpeed = movementSpeed;
        this.movementDirection = movementDirection;
    }

    public int getX() { return (int) Math.round(x); }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public boolean isMoving() { return moving; }

    /** Advances horizontally and reverses direction at either endpoint. */
    public void update(double elapsedSeconds) {
        if (!moving || elapsedSeconds <= 0.0) {
            return;
        }
        x += movementDirection * movementSpeed * elapsedSeconds;
        if (x <= movementMinX) {
            x = movementMinX;
            movementDirection = 1;
        } else if (x >= movementMaxX) {
            x = movementMaxX;
            movementDirection = -1;
        }
    }

    public Rectangle getBounds(int height) {
        return new Rectangle(getX(), y, width, height);
    }
}
