/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ChatFrame extends JPanel {
    private final AppController controller;
    private final String currentUser;

    private final JComboBox<String> contact = new JComboBox<>();
    private final JTextField contactSearch = new JTextField(14);
    private final JPanel messageList = new JPanel();
    private final JScrollPane messageScroll = new JScrollPane(messageList);
    private final JTextField messageInput = new JTextField();

    public ChatFrame(
            AppController controller,
            String currentUser,
            String initialContact
    ) {
        this.controller = controller;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        contact.setEditable(true);
        if (initialContact != null && !initialContact.isBlank()) {
            contact.setSelectedItem(initialContact);
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search / choose contact:"));
        top.add(contactSearch);
        top.add(contact);

        JButton openChat = new JButton("Open chat");
        top.add(openChat);
        add(top, BorderLayout.NORTH);

        messageList.setLayout(new BoxLayout(messageList, BoxLayout.Y_AXIS));
        messageList.setBackground(Color.WHITE);
        messageList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        messageScroll.setBorder(
                BorderFactory.createTitledBorder("Direct messages")
        );
        add(messageScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 5));
        bottom.add(messageInput, BorderLayout.CENTER);

        JButton playVoice = new JButton("Play latest voice");
        JButton attachImage = new JButton("Image");
        JButton send = new JButton("SEND");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(playVoice);
        buttons.add(attachImage);
        buttons.add(send);

        bottom.add(buttons, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        openChat.addActionListener(event -> refreshConversation());
        contact.addActionListener(event -> refreshConversation());
        send.addActionListener(event -> sendMessage());
        messageInput.addActionListener(event -> sendMessage());
        attachImage.addActionListener(event -> attachImage());
        playVoice.addActionListener(event -> playLatestVoice());

        contactSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    contact.setSelectedItem(contactSearch.getText().trim());
                    refreshConversation();
                }
            }
        });

        refreshConversation();
    }

    private String selectedContact() {
        Object value = contact.getEditor().getItem();
        return value == null ? "" : value.toString().trim();
    }

    public void refreshConversation() {
        messageList.removeAll();

        String peer = selectedContact();
        if (peer.isBlank()) {
            messageList.add(new JLabel("Choose or enter a contact name."));
        } else {
            controller.loadConversationMessages(peer, this::addMessage);

            if (messageList.getComponentCount() == 0) {
                messageList.add(new JLabel("No messages yet."));
            }
        }

        messageList.add(Box.createVerticalGlue());
        messageList.revalidate();
        messageList.repaint();

        SwingUtilities.invokeLater(() -> {
            messageScroll.getVerticalScrollBar().setValue(
                    messageScroll.getVerticalScrollBar().getMaximum()
            );
        });
    }

    private void addMessage(
            String timestamp,
            String sender,
            String kind,
            String content
    ) {
        boolean sentByMe = sender.equalsIgnoreCase(currentUser);

        JPanel row = new JPanel(
                new FlowLayout(
                        sentByMe ? FlowLayout.RIGHT : FlowLayout.LEFT,
                        6,
                        4
                )
        );
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(185, 195, 205)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        bubble.setBackground(
                sentByMe
                        ? new Color(205, 229, 247)
                        : new Color(235, 238, 241)
        );
        bubble.setMaximumSize(new java.awt.Dimension(460, Integer.MAX_VALUE));

        JLabel heading = new JLabel(
                sender + "  \u2022  " + formatTimestamp(timestamp)
        );
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.add(heading);
        bubble.add(Box.createVerticalStrut(5));

        if ("I".equals(kind)) {
            addImageContent(bubble, content);
        } else if ("V".equals(kind)) {
            addVoiceContent(bubble, content);
        } else {
            addTextContent(bubble, content);
        }

        row.add(bubble);
        row.setMaximumSize(new java.awt.Dimension(
                Integer.MAX_VALUE,
                row.getPreferredSize().height
        ));

        messageList.add(row);
        messageList.add(Box.createVerticalStrut(5));
    }

    private String formatTimestamp(String timestamp) {
        if (timestamp.length() >= 10
                && timestamp.charAt(4) == '-'
                && timestamp.charAt(7) == '-') {
            return timestamp.substring(0, 4)
                    + "/"
                    + timestamp.substring(5, 7)
                    + "/"
                    + timestamp.substring(8);
        }

        return timestamp.replace('-', '/');
    }

    private void addTextContent(JPanel bubble, String content) {
        JTextArea text = new JTextArea(content, 0, 30);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setBorder(null);
        text.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.setMaximumSize(new java.awt.Dimension(420, Integer.MAX_VALUE));
        bubble.add(text);
    }

    private void addImageContent(JPanel bubble, String imagePath) {
        String lowerPath = imagePath.toLowerCase(Locale.ROOT);

        if (!lowerPath.endsWith(".png")
                && !lowerPath.endsWith(".jpg")
                && !lowerPath.endsWith(".jpeg")) {
            bubble.add(new JLabel("This attachment is not a supported image."));
            return;
        }

        Path path = Path.of(imagePath);
        if (!Files.isRegularFile(path)) {
            bubble.add(new JLabel("Image file not found: " + imagePath));
            return;
        }

        ImageIcon original = new ImageIcon(path.toAbsolutePath().toString());
        int originalWidth = original.getIconWidth();
        int originalHeight = original.getIconHeight();

        if (originalWidth <= 0 || originalHeight <= 0) {
            bubble.add(new JLabel("Could not display this image."));
            return;
        }

        double scale = Math.min(
                1.0,
                Math.min(300.0 / originalWidth, 220.0 / originalHeight)
        );

        int width = Math.max(1, (int) (originalWidth * scale));
        int height = Math.max(1, (int) (originalHeight * scale));

        Image scaled = original.getImage().getScaledInstance(
                width,
                height,
                Image.SCALE_SMOOTH
        );

        JLabel imageLabel = new JLabel(new ImageIcon(scaled));
        imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.add(imageLabel);
    }

    private void addVoiceContent(JPanel bubble, String voicePath) {
        JButton play = new JButton("Play voice message");
        play.setAlignmentX(Component.LEFT_ALIGNMENT);
        play.addActionListener(event -> {
            try {
                controller.voiceNotes().play(voicePath);
            } catch (Exception ex) {
                controller.showError("Could not play voice message", ex);
            }
        });
        bubble.add(play);
    }

    private void sendMessage() {
        String text = messageInput.getText().trim();
        String peer = selectedContact();

        if (text.isBlank() || peer.isBlank()) {
            return;
        }

        if (controller.sendText(peer, text)) {
            messageInput.setText("");
            refreshConversation();
        }
    }

    private void attachImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose an image to send");
        chooser.setFileFilter(
                new FileNameExtensionFilter(
                        "Image files (PNG, JPG)",
                        "png",
                        "jpg",
                        "jpeg"
                )
        );

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File image = chooser.getSelectedFile();
        String peer = selectedContact();

        if (peer.isBlank()) {
            return;
        }

        if (controller.sendImage(peer, image.getAbsolutePath())) {
            refreshConversation();
        }
    }

    private void playLatestVoice() {
        try {
            String path = controller.latestVoice(selectedContact());
            controller.voiceNotes().play(path);
        } catch (Exception ex) {
            controller.showError("Could not play voice message", ex);
        }
    }
    public String currentContact() {
        return selectedContact();
    }
}