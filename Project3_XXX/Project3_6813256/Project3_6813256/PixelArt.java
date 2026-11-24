/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Loads pixel-art PNGs using paths relative to the project directory. */
public final class PixelArt {
    private static final String ASSET_PATH = "src/main/java/Project3_6813256/resources/assets/";
    private static final BufferedImage PLAYER = load(ASSET_PATH + "player.png");
    private static final BufferedImage DOG = load(ASSET_PATH + "dog.png");
    private static final BufferedImage CAT = load(ASSET_PATH + "cat.png");
    private static final BufferedImage SHAYNE = load(ASSET_PATH + "shayne.png");
    private static final BufferedImage STOPWATCH = load(ASSET_PATH + "stopwatch.png");
    private static final BufferedImage PLATFORM = load(ASSET_PATH + "platform.png");
    private static final BufferedImage BACKGROUND = load(ASSET_PATH + "background.png");

    private PixelArt() { }

    public static BufferedImage player() {
        return PLAYER;
    }

    public static BufferedImage player(PlayerSkin skin) {
        PlayerSkin selectedSkin = skin == null ? PlayerSkin.RUNNER : skin;
        switch (selectedSkin) {
            case DOG:
                return DOG;
            case CAT:
                return CAT;
            case SHAYNE:
                return SHAYNE;
            default:
                return PLAYER;
        }
    }

    public static BufferedImage stopwatch() {
        return STOPWATCH;
    }

    public static BufferedImage platform() {
        return PLATFORM;
    }

    public static BufferedImage background() {
        return BACKGROUND;
    }

    private static BufferedImage load(String path) {
        Path imagePath = Path.of(path);
        try (InputStream input = Files.newInputStream(imagePath)) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new IOException("Unsupported image file: " + imagePath);
            }
            return image;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load image file: " + imagePath, exception);
        }
    }
}
