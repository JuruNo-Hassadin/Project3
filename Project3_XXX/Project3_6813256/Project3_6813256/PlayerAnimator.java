package Project3_6813256;

import java.awt.image.BufferedImage;

/** Produces small pixel-preserving run and jump poses from the player sprite. */
public final class PlayerAnimator {
    private static final int RUN_FRAME_MILLIS = 115;
    private static final int LEG_START_ROW = 9;

    private final BufferedImage standingFrame;
    private final BufferedImage[] runningFrames;
    private final BufferedImage jumpingFrame;
    private final int bottomTransparentRows;

    public PlayerAnimator(BufferedImage baseSprite) {
        standingFrame = copy(baseSprite);
        bottomTransparentRows = countBottomTransparentRows(baseSprite);
        runningFrames = new BufferedImage[] {
            standingFrame,
            makeRunningPose(baseSprite, -1),
            standingFrame,
            makeRunningPose(baseSprite, 1)
        };
        jumpingFrame = makeJumpPose(baseSprite);
    }

    public BufferedImage frame(boolean airborne, boolean running, long nowNanos) {
        if (airborne) {
            return jumpingFrame;
        }
        if (!running) {
            return standingFrame;
        }
        long frame = nowNanos / (RUN_FRAME_MILLIS * 1_000_000L);
        return runningFrames[(int) (frame % runningFrames.length)];
    }

    public int getBottomTransparentRows() {
        return bottomTransparentRows;
    }

    private static BufferedImage makeRunningPose(BufferedImage source, int stride) {
        BufferedImage pose = copy(source);
        clearLegArea(pose);
        for (int y = LEG_START_ROW; y <= 12; y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int color = source.getRGB(x, y);
                if ((color >>> 24) == 0) {
                    continue;
                }
                int shiftedX = x < source.getWidth() / 2 ? x + stride : x - stride;
                if (shiftedX >= 0 && shiftedX < pose.getWidth()) {
                    pose.setRGB(shiftedX, y, color);
                }
            }
        }
        return pose;
    }

    private static BufferedImage makeJumpPose(BufferedImage source) {
        BufferedImage pose = copy(source);
        clearLegArea(pose);
        for (int y = LEG_START_ROW; y <= 10; y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int color = source.getRGB(x, y);
                if ((color >>> 24) != 0) {
                    pose.setRGB(x, y, color);
                }
            }
        }
        for (int y = 11; y <= 12; y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int color = source.getRGB(x, y);
                if ((color >>> 24) == 0) {
                    continue;
                }
                int tuckedX = x < source.getWidth() / 2 ? x + 1 : x - 1;
                pose.setRGB(tuckedX, y - 1, color);
            }
        }
        return pose;
    }

    private static void clearLegArea(BufferedImage image) {
        for (int y = LEG_START_ROW; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, 0);
            }
        }
    }

    private static int countBottomTransparentRows(BufferedImage image) {
        for (int y = image.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return image.getHeight() - 1 - y;
                }
            }
        }
        return 0;
    }

    private static BufferedImage copy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                copy.setRGB(x, y, source.getRGB(x, y));
            }
        }
        return copy;
    }
}
