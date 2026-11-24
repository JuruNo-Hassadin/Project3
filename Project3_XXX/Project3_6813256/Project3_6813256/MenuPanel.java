/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import javax.swing.JPanel;

/** Pixel-art main menu with four clickable menu actions. */
public final class MenuPanel extends JPanel {
    public interface Actions {
        void onPlay();
        void onLeaderboard();
        void onSettings();
        void onCredits();
    }

    private enum MenuButton {
        PLAY("PLAY"), LEADERBOARD("LEADERBOARD"), SETTING("SETTING"), CREDIT("CREDIT");

        private final String label;

        MenuButton(String label) {
            this.label = label;
        }
    }

    private final Actions actions;
    private final MenuClickSoundEffect clickSound;
    private final BooleanSupplier soundEnabled;
    private PlayerSkin playerSkin = PlayerSkin.RUNNER;
    private final BufferedImage platformTile = PixelArt.platform();
    private MenuButton hoveredButton;

    public MenuPanel(Actions actions, MenuClickSoundEffect clickSound,
            BooleanSupplier soundEnabled) {
        this.actions = actions;
        this.clickSound = clickSound;
        this.soundEnabled = soundEnabled == null ? () -> true : soundEnabled;
        setFocusable(true);
        setPreferredSize(new java.awt.Dimension(1100, 620));
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent event) {
                MenuButton previous = hoveredButton;
                hoveredButton = buttonAt(event.getPoint());
                setCursor(Cursor.getPredefinedCursor(hoveredButton == null
                        ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                if (previous != hoveredButton) {
                    repaint();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                MenuButton button = buttonAt(event.getPoint());
                if (button == null) {
                    return;
                }
                clickSound.play(MenuPanel.this.soundEnabled.getAsBoolean());
                switch (button) {
                    case PLAY -> actions.onPlay();
                    case LEADERBOARD -> actions.onLeaderboard();
                    case SETTING -> actions.onSettings();
                    case CREDIT -> actions.onCredits();
                    default -> throw new IllegalStateException("Unexpected menu button");
                }
            }
        });
    }

    public void setPlayerSkin(PlayerSkin selectedSkin) {
        playerSkin = selectedSkin == null ? PlayerSkin.RUNNER : selectedSkin;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.drawImage(PixelArt.background(), 0, 0, getWidth(), getHeight(), null);
        g.setColor(new Color(13, 28, 45, 82));
        g.fillRect(0, 0, getWidth(), getHeight());
        drawGround(g);
        drawTitle(g);
        for (Map.Entry<MenuButton, Rectangle> entry : buttonBounds().entrySet()) {
            drawButton(g, entry.getValue(), entry.getKey().label,
                    entry.getKey() == MenuButton.PLAY,
                    entry.getKey() == hoveredButton);
        }
        drawMenuCharacter(g);
        g.dispose();
    }

    private void drawTitle(Graphics2D g) {
        String title = "TOWER HOP";
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 48));
        FontMetrics metrics = g.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(title)) / 2;
        g.setColor(new Color(18, 30, 45));
        g.drawString(title, x + 5, 126 + 5);
        g.setColor(new Color(255, 221, 123));
        g.drawString(title, x, 126);

        String subtitle = "RUN  •  JUMP  •  CLIMB";
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        metrics = g.getFontMetrics();
        x = (getWidth() - metrics.stringWidth(subtitle)) / 2;
        g.setColor(new Color(238, 247, 251));
        g.drawString(subtitle, x, 157);
    }

    private void drawButton(Graphics2D g, Rectangle bounds, String label,
            boolean primary, boolean hovered) {
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        g.setColor(new Color(19, 30, 44));
        g.fillRect(x + 6, y + 6, width, height);

        Color fill = primary ? new Color(66, 116, 94) : new Color(53, 72, 93);
        if (hovered) {
            fill = primary ? new Color(86, 148, 111) : new Color(72, 100, 126);
        }
        g.setColor(new Color(23, 36, 51));
        g.fillRect(x + 5, y, width - 10, height);
        g.fillRect(x, y + 5, width, height - 10);
        g.setColor(fill);
        g.fillRect(x + 7, y + 4, width - 14, height - 8);
        g.setColor(new Color(139, 197, 159));
        g.fillRect(x + 11, y + 8, width - 22, 4);
        g.setColor(new Color(224, 238, 235));
        g.drawRect(x + 7, y + 4, width - 15, height - 9);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, primary ? 22 : 17));
        FontMetrics metrics = g.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(label)) / 2;
        int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent() + 3;
        g.setColor(new Color(20, 31, 44));
        g.drawString(label, textX + 2, textY + 2);
        g.setColor(Color.WHITE);
        g.drawString(label, textX, textY);
    }

    private void drawGround(Graphics2D g) {
        int groundY = getHeight() - 58;
        for (int x = 0; x < getWidth(); x += 32) {
            g.drawImage(platformTile, x, groundY, 32, 22, null);
        }
        g.setColor(new Color(14, 29, 42, 110));
        g.fillRect(0, groundY + 22, getWidth(), getHeight() - groundY - 22);
    }

    private void drawMenuCharacter(Graphics2D g) {
        BufferedImage player = PixelArt.player(playerSkin);
        int scale = Math.max(1, 64 / player.getHeight());
        int width = player.getWidth() * scale;
        int height = player.getHeight() * scale;
        int groundY = getHeight() - 58;
        int x = 76;
        int y = groundY - height + 12;
        g.drawImage(player, x, y, x + width, y + height,
                0, 0, player.getWidth(), player.getHeight(), null);
    }

    private Map<MenuButton, Rectangle> buttonBounds() {
        Map<MenuButton, Rectangle> bounds = new LinkedHashMap<>();
        int width = 250;
        int height = 52;
        int gap = 10;
        int totalHeight = height * MenuButton.values().length + gap * (MenuButton.values().length - 1);
        int x = (getWidth() - width) / 2;
        int startY = (getHeight() - totalHeight) / 2 + 24;
        int index = 0;
        for (MenuButton button : MenuButton.values()) {
            bounds.put(button, new Rectangle(x, startY + index * (height + gap), width, height));
            index++;
        }
        return bounds;
    }

    private MenuButton buttonAt(java.awt.Point point) {
        for (Map.Entry<MenuButton, Rectangle> entry : buttonBounds().entrySet()) {
            if (entry.getValue().contains(point)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
