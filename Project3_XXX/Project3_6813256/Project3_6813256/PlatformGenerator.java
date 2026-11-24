package Project3_6813256;

import java.util.Random;

/** Generates alternating tiny and long platforms along the jump route. */
public final class PlatformGenerator {
    private static final int WORLD_WIDTH = 1100;
    private static final int SIDE_MARGIN = 30;
    private static final int PLAYER_WIDTH = 64;
    private static final double GRAVITY = 1450.0;
    private static final double FALL_GRAVITY_MULTIPLIER = 1.30;
    private static final double JUMP_VELOCITY = 600.0;

    private boolean nextPlatformIsShort = true;

    public void reset() {
        nextPlatformIsShort = true;
    }

    /**
     * Creates the next floor near the predicted landing point when the player
     * runs toward an edge and jumps, reversing direction in the air.
     */
    public Platform generateAbove(Platform lower, Difficulty difficulty,
            boolean currentlyFacingLeft, double expectedRunSpeed, Random random) {
        int gap = (int) Math.round((70 + random.nextInt(21)) * difficulty.getGapScale());
        int width = nextPlatformIsShort
                ? 120 + random.nextInt(51)
                : 360 + random.nextInt(261);
        nextPlatformIsShort = !nextPlatformIsShort;

        double takeoffX = currentlyFacingLeft
                ? lower.getX() + 8.0
                : lower.getX() + lower.getWidth() - PLAYER_WIDTH - 8.0;
        boolean jumpLeft = !currentlyFacingLeft;
        double jumpHeight = JUMP_VELOCITY * JUMP_VELOCITY / (2.0 * GRAVITY);
        double descentHeight = Math.max(0.0, jumpHeight - gap);
        double timeToLanding = JUMP_VELOCITY / GRAVITY
                + Math.sqrt(2.0 * descentHeight
                        / (GRAVITY * FALL_GRAVITY_MULTIPLIER));
        double travelDistance = expectedRunSpeed * timeToLanding;
        double landingX = takeoffX + (jumpLeft ? -travelDistance : travelDistance);
        landingX = Math.max(SIDE_MARGIN,
                Math.min(WORLD_WIDTH - SIDE_MARGIN - PLAYER_WIDTH, landingX));

        // Leave some runway in the direction the player will keep running
        // after landing, then add a little horizontal variation.
        double preferredX = jumpLeft
                ? landingX + PLAYER_WIDTH + 16 - width
                : landingX - 16;
        int x = (int) Math.round(preferredX) + random.nextInt(25) - 12;
        x = Math.max(SIDE_MARGIN, Math.min(WORLD_WIDTH - SIDE_MARGIN - width, x));
        return new Platform(x, lower.getY() + gap, width);
    }
}
