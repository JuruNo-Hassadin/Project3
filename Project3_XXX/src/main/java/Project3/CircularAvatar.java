/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class CircularAvatar extends JComponent {
    private final String username;
    private Image avatarImage;

    public CircularAvatar(String username) {
        this.username = username;
        setPreferredSize(new Dimension(150, 150));
        setMinimumSize(new Dimension(150, 150));
        setMaximumSize(new Dimension(150, 150));
    }

    public void setImagePath(String imagePath) {
        avatarImage = null;

        if (imagePath != null && !imagePath.isBlank()) {
            ImageIcon icon = new ImageIcon(imagePath);

            if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                avatarImage = icon.getImage();
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int size = Math.min(getWidth(), getHeight());
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        Ellipse2D circle =
                new Ellipse2D.Double(x + 2, y + 2, size - 4, size - 4);
        Shape previousClip = g2.getClip();

        g2.setClip(circle);
        g2.setColor(new Color(225, 232, 239));
        g2.fill(circle);

        if (avatarImage != null) {
            int imageWidth = avatarImage.getWidth(this);
            int imageHeight = avatarImage.getHeight(this);

            if (imageWidth > 0 && imageHeight > 0) {
                double scale = Math.max(
                        (double) size / imageWidth,
                        (double) size / imageHeight
                );

                int drawWidth = (int) (imageWidth * scale);
                int drawHeight = (int) (imageHeight * scale);
                int drawX = x + (size - drawWidth) / 2;
                int drawY = y + (size - drawHeight) / 2;

                g2.drawImage(
                        avatarImage,
                        drawX,
                        drawY,
                        drawWidth,
                        drawHeight,
                        this
                );
            }
        } else {
            String initial = username == null || username.isBlank()
                    ? "?"
                    : username.substring(0, 1).toUpperCase(Locale.ROOT);

            g2.setColor(new Color(88, 108, 128));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));

            FontMetrics metrics = g2.getFontMetrics();
            int textX = x + (size - metrics.stringWidth(initial)) / 2;
            int textY = y + (size - metrics.getHeight()) / 2
                    + metrics.getAscent();

            g2.drawString(initial, textX, textY);
        }

        g2.setClip(previousClip);
        g2.setColor(new Color(83, 105, 126));
        g2.setStroke(new BasicStroke(3.0f));
        g2.draw(circle);
        g2.dispose();
    }
}