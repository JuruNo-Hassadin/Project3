/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

/** A collectible stopwatch placed on a generated platform. */
public final class StopwatchPickup {
    public static final int HITBOX_WIDTH = 72;

    private final Platform platform;
    private final int offsetX;

    public StopwatchPickup(Platform platform, int x) {
        if (platform == null) {
            throw new IllegalArgumentException("A pickup must belong to a platform");
        }
        this.platform = platform;
        offsetX = x - platform.getX();
    }

    public Platform getPlatform() { return platform; }
    public int getX() { return platform.getX() + offsetX; }
}
