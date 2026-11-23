/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class PostPanel extends JPanel {
    private final AppController controller;
    private final JTextArea feed = new JTextArea();
    private final JTextArea draft = new JTextArea(3, 30);
    private final JLabel imagePreview = new JLabel("No image selected", JLabel.CENTER);
    private String imageSource = "";

    public PostPanel(AppController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        feed.setEditable(false);
        feed.setLineWrap(true);
        feed.setWrapStyleWord(true);
        feed.setBorder(BorderFactory.createTitledBorder("Home feed"));
        add(new JScrollPane(feed), BorderLayout.CENTER);

        JPanel composer = new JPanel(new BorderLayout(8, 8));
        draft.setLineWrap(true);
        draft.setWrapStyleWord(true);
        draft.setBorder(BorderFactory.createTitledBorder("Write a post"));
        composer.add(new JScrollPane(draft), BorderLayout.CENTER);
        imagePreview.setPreferredSize(new Dimension(150, 90));
        composer.add(imagePreview, BorderLayout.EAST);

        JPanel actions = new JPanel();
        JButton attach = new JButton("Add image");
        JButton publish = new JButton("POST");
        actions.add(attach);
        actions.add(publish);
        composer.add(actions, BorderLayout.SOUTH);
        attach.addActionListener(event -> chooseImage());
        publish.addActionListener(event -> publishPost());
        add(composer, BorderLayout.SOUTH);
        refresh();
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a post image");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        imageSource = file.getAbsolutePath();
        ImageIcon original = new ImageIcon(imageSource);
        Image scaled = original.getImage().getScaledInstance(140, 80, Image.SCALE_SMOOTH);
        imagePreview.setText("");
        imagePreview.setIcon(new ImageIcon(scaled));
    }

    private void publishPost() {
        if (draft.getText().isBlank() && imageSource.isBlank()) {
            JOptionPane.showMessageDialog(this, "Write text or attach an image first.");
            return;
        }
        if (controller.createPost(draft.getText(), imageSource)) {
            draft.setText("");
            imageSource = "";
            imagePreview.setIcon(null);
            imagePreview.setText("No image selected");
            refresh();
        }
    }

    private void refresh() {
        controller.showPosts(feed);
        feed.setCaretPosition(0);
    }
}
