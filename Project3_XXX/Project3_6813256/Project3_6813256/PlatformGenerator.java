/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

import java.util.Random;

/** Generates reachable platforms along the predicted jump route. */
public final class PlatformGenerator {
    private static final int WORLD_WIDTH = 1100;
    private static final int SIDE_MARGIN = 30;
    private static final int MOVING_PLATFORM_TRAVEL = 100;
    private static final double MOVING_PLATFORM_SPEED = 105.0;
    private static final int PLAYER_WIDTH = 64;
    private static final double GRAVITY = 1450.0;
    private static final double FALL_GRAVITY_MULTIPLIER = 1.30;
    private static final double JUMP_VELOCITY = 600.0;

    private boolean nextPlatformIsShort = true;
    private boolean nextShortPlatformShiftRight = true;
    private boolean nextLongPlatformOffsetRight;
    private int generatedPlatformCount;

    public void reset() {
        nextPlatformIsShort = true;
        nextShortPlatformShiftRight = true;
        nextLongPlatformOffsetRight = false;
        generatedPlatformCount = 0;
    }

    /**
     * Places each next platform around the predicted landing point and leaves
     * enough width for small timing differences at the platform edge.
     */
    public Platform generateAbove(Platform lower, Difficulty difficulty,
            boolean currentlyFacingLeft, double expectedRunSpeed, Random random) {
        generatedPlatformCount++;
        boolean movingPlatform = generatedPlatformCount >= 3
                && (generatedPlatformCount - 3) % 3 == 0;
        int gap = (int) Math.round((50 + random.nextInt(21)) * difficulty.getGapScale());
        boolean shortPlatform = nextPlatformIsShort || movingPlatform;
        int width = shortPlatform
                ? 180 + random.nextInt(41)
                : 360 + random.nextInt(261);
        nextPlatformIsShort = !shortPlatform;

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

        double platformCenter = landingX + PLAYER_WIDTH / 2.0;
        int minimumX = SIDE_MARGIN;
        int maximumX = WORLD_WIDTH - SIDE_MARGIN - width;
        int predictedX = (int) Math.round(platformCenter - width / 2.0);
        predictedX = Math.max(minimumX, Math.min(maximumX, predictedX));

        int x;
        if (shortPlatform) {
            int offset = nextShortPlatformShiftRight
                    ? 55 + random.nextInt(31)
                    : random.nextInt(21);
            x = Math.min(maximumX, predictedX + offset);
            nextShortPlatformShiftRight = !nextShortPlatformShiftRight;
        } else {
            int offset = 35 + random.nextInt(31);
            int leftX = Math.max(minimumX, predictedX - offset);
            int rightX = Math.min(maximumX, predictedX + offset);
            if (nextLongPlatformOffsetRight) {
                x = rightX != predictedX ? rightX : leftX;
            } else {
                x = leftX != predictedX ? leftX : rightX;
            }
            nextLongPlatformOffsetRight = !nextLongPlatformOffsetRight;
        }
        int platformY = lower.getY() + gap;
        if (!movingPlatform) {
            return new Platform(x, platformY, width);
        }

        int movementMinX = Math.max(minimumX, x - MOVING_PLATFORM_TRAVEL);
        int movementMaxX = Math.min(maximumX, x + MOVING_PLATFORM_TRAVEL);
        if (movementMinX == movementMaxX) {
            return new Platform(x, platformY, width);
        }
        int movingIndex = (generatedPlatformCount - 3) / 3;
        int direction = movingIndex % 2 == 0 ? 1 : -1;
        return new Platform(x, platformY, width, movementMinX, movementMaxX,
                MOVING_PLATFORM_SPEED, direction);
    }
}
