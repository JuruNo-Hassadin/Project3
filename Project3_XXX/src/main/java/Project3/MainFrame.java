/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainFrame extends JPanel {
    private static final int NAV_BUTTON_WIDTH = 140;
    private static final int NAV_BUTTON_HEIGHT = 90;

    private final AppController controller;
    private final JPanel content = new JPanel(new BorderLayout());
    private final FriendsPanel friendsPanel;

    public MainFrame(AppController controller, String username) {
        this.controller = controller;
        this.friendsPanel = new FriendsPanel(controller);

        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(135, 165, 190));
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("CHATGAE     @" + username);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);

        JButton logout = new JButton("LOG OUT");
        logout.addActionListener(event -> controller.logout());
        header.add(logout, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(170, 0));
        sidebar.setBackground(new Color(93, 111, 132));
        sidebar.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        sidebar.add(Box.createVerticalGlue());

        addNavigation(sidebar, "Profile", "Profile", controller::openProfile);
        sidebar.add(Box.createVerticalStrut(6));

        addNavigation(sidebar, "Home", "Home", controller::openHome);
        sidebar.add(Box.createVerticalStrut(6));

        addNavigation(sidebar, "DM", "DM", controller::openChat);
        sidebar.add(Box.createVerticalStrut(6));

        addNavigation(sidebar, "Voice", "Voice", controller::openVoice);
        sidebar.add(Box.createVerticalStrut(6));

        addNavigation(sidebar, "Friends", "Friends", controller::openFriends);

        sidebar.add(Box.createVerticalGlue());

        add(sidebar, BorderLayout.WEST);

        content.setBackground(Color.WHITE);
        add(content, BorderLayout.CENTER);

        showPanel(new PostPanel(controller));
    }

    private void addNavigation(
            JPanel sidebar,
            String text,
            String imageName,
            Runnable action
    ) {
        JButton button = new JButton(text);
        Icon icon = loadNavigationIcon(imageName);

        if (icon != null) {
            button.setIcon(icon);
        }

        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setPreferredSize(
                new Dimension(NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT)
        );
        button.setMinimumSize(
                new Dimension(NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT)
        );
        button.setMaximumSize(
                new Dimension(NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT)
        );
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.addActionListener(event -> action.run());

        sidebar.add(button);
    }

    private Icon loadNavigationIcon(String imageName) {
        // ลองโหลดจาก classpath ก่อน
        ImageIcon icon = loadFromClasspath(imageName + ".png");
        if (icon != null) return icon;

        icon = loadFromClasspath(imageName + ".jpg");
        if (icon != null) return icon;

        // โหลดจาก Source Package: src/main/java/Project3/Resource/
        Path resourceFolder = Paths.get(
                "src", "main", "java", "Project3", "Resource"
        );

        icon = loadFromFolder(resourceFolder, imageName + ".png");
        if (icon != null) return icon;

        icon = loadFromFolder(resourceFolder, imageName + ".jpg");
        if (icon != null) return icon;

        System.err.println(
                "Could not find " + imageName + ".png or " + imageName + ".jpg"
        );
        return null;
    }

    private ImageIcon loadFromClasspath(String fileName) {
        URL imageUrl = getClass().getResource(
                "/Project3/Resource/" + fileName
        );

        if (imageUrl == null) {
            return null;
        }

        return scaleIcon(new ImageIcon(imageUrl));
    }

    private ImageIcon loadFromFolder(Path folder, String fileName) {
        Path imagePath = folder.resolve(fileName);

        if (!Files.isRegularFile(imagePath)) {
            return null;
        }

        return scaleIcon(
                new ImageIcon(imagePath.toAbsolutePath().toString())
        );
    }

    private ImageIcon scaleIcon(ImageIcon original) {
        int originalWidth = original.getIconWidth();
        int originalHeight = original.getIconHeight();

        if (originalWidth <= 0 || originalHeight <= 0) {
            return null;
        }

        double scale = Math.min(
                32.0 / originalWidth,
                32.0 / originalHeight
        );

        int width = Math.max(1, (int) (originalWidth * scale));
        int height = Math.max(1, (int) (originalHeight * scale));

        Image scaled = original.getImage().getScaledInstance(
                width,
                height,
                Image.SCALE_SMOOTH
        );

        return new ImageIcon(scaled);
    }

    public void showPanel(JPanel panel) {
        content.removeAll();
        content.add(panel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    public void showFriends() {
        showPanel(friendsPanel);
    }

    public void peerSeen(
        String username,
        String ipAddress,
        int tcpPort
) {
    friendsPanel.peerSeen(username, ipAddress, tcpPort);
}

public int peerPortFor(String username) {
    return friendsPanel.tcpPortForUsername(username);
}

    public void stop() {
        friendsPanel.stop();
    }
    public String peerIpFor(String username) {
        return friendsPanel.ipForUsername(username);
    }
}