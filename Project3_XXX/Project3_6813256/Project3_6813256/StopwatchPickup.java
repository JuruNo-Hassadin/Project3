package Project3_6813256;

/** A collectible stopwatch placed on a generated platform. */
public final class StopwatchPickup {
    public static final int HITBOX_WIDTH = 72;

    private final Platform platform;
    private final int x;

    public StopwatchPickup(Platform platform, int x) {
        if (platform == null) {
            throw new IllegalArgumentException("A pickup must belong to a platform");
        }
        this.platform = platform;
        this.x = x;
    }

    public Platform getPlatform() { return platform; }
    public int getX() { return x; }
}
