/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

import java.awt.image.BufferedImage;

/** Produces pixel-preserving run and jump poses from a player sprite. */
public final class PlayerAnimator {
    private static final int RUN_FRAME_MILLIS = 115;

    private final BufferedImage standingFrame;
    private final BufferedImage[] runningFrames;
    private final BufferedImage jumpingFrame;
    private final int bottomTransparentRows;
    private final int legStartRow;
    private final int legEndRow;

    public PlayerAnimator(BufferedImage baseSprite) {
        this(baseSprite, baseSprite.getHeight() == 16 ? 9 : baseSprite.getHeight() * 3 / 4);
    }

    public PlayerAnimator(BufferedImage baseSprite, int legStartRow) {
        standingFrame = copy(baseSprite);
        bottomTransparentRows = countBottomTransparentRows(baseSprite);
        this.legStartRow = Math.max(0, Math.min(legStartRow, baseSprite.getHeight() - 1));
        legEndRow = Math.max(this.legStartRow,
                baseSprite.getHeight() - bottomTransparentRows - 1);
        runningFrames = new BufferedImage[] {
            standingFrame,
            makeRunningPose(baseSprite, -1, this.legStartRow, legEndRow),
            standingFrame,
            makeRunningPose(baseSprite, 1, this.legStartRow, legEndRow)
        };
        jumpingFrame = makeJumpPose(baseSprite, this.legStartRow, legEndRow);
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

    private static BufferedImage makeRunningPose(BufferedImage source, int stride,
            int legStartRow, int legEndRow) {
        BufferedImage pose = copy(source);
        clearLegArea(pose, legStartRow);
        for (int y = legStartRow; y <= legEndRow; y++) {
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

    private static BufferedImage makeJumpPose(BufferedImage source,
            int legStartRow, int legEndRow) {
        BufferedImage pose = copy(source);
        clearLegArea(pose, legStartRow);
        int tuckedStartRow = Math.min(legStartRow + 2, legEndRow + 1);
        for (int y = legStartRow; y < tuckedStartRow; y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int color = source.getRGB(x, y);
                if ((color >>> 24) != 0) {
                    pose.setRGB(x, y, color);
                }
            }
        }
        for (int y = tuckedStartRow; y <= legEndRow; y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int color = source.getRGB(x, y);
                if ((color >>> 24) == 0) {
                    continue;
                }
                int tuckedX = x < source.getWidth() / 2 ? x + 1 : x - 1;
                int tuckedY = y - 1;
                if (tuckedX >= 0 && tuckedX < pose.getWidth() && tuckedY >= legStartRow) {
                    pose.setRGB(tuckedX, tuckedY, color);
                }
            }
        }
        return pose;
    }

    private static void clearLegArea(BufferedImage image, int legStartRow) {
        for (int y = legStartRow; y < image.getHeight(); y++) {
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
