/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ProfilePanel extends JPanel {
    private final AppController controller;
    private final String username;
    private final CircularAvatar avatar;

    public ProfilePanel(AppController controller, String username) {
        this.controller = controller;
        this.username = username;

        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Profile   @" + username);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        add(title, BorderLayout.NORTH);

        avatar = new CircularAvatar(username);
        avatar.setImagePath(controller.profileImagePath(username));

        JButton chooseImage = new JButton("Choose profile picture");
        chooseImage.addActionListener(event -> chooseProfileImage());

        JPanel avatarPanel = new JPanel(new BorderLayout(8, 10));
        avatarPanel.add(avatar, BorderLayout.CENTER);
        avatarPanel.add(chooseImage, BorderLayout.SOUTH);

        JTextArea details = new JTextArea();
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(new Font("SansSerif", Font.PLAIN, 17));

        try {
            details.setText(
                    "Username: " + username
                    + "\nInterests: "
                    + controller.accounts().interestsFor(username)
            );
        } catch (IOException ex) {
            details.setText("Could not load profile: " + ex.getMessage());
        }

        JPanel body = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 20));
        body.add(avatarPanel);
        body.add(new JScrollPane(details));

        add(body, BorderLayout.CENTER);
    }

    private void chooseProfileImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a profile picture");
        chooser.setFileFilter(
                new FileNameExtensionFilter(
                        "Image files (PNG, JPG)",
                        "png", "jpg", "jpeg"
                )
        );

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();

        if (controller.saveProfileImage(username, selectedFile.toPath())) {
            avatar.setImagePath(controller.profileImagePath(username));
        }
    }
}