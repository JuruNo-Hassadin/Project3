package Project3_6813256;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/** Placeholder credits window for team names and asset attributions. */
public final class CreditsDialog extends JDialog {
    public CreditsDialog(JFrame owner) {
        super(owner, "Tower Hop - Credits", true);
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(new Color(29, 45, 62));
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(142, 194, 151), 4),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)));

        JLabel title = new JLabel("CREDITS", JLabel.CENTER);
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        title.setForeground(new Color(255, 220, 123));

        JTextArea text = new JTextArea(
                "Tower Hop\n\nTeam members: add names and student IDs here.\n"
                + "Pixel art and audio: add creator/source credits here.");
        text.setEditable(false);
        text.setFocusable(false);
        text.setOpaque(false);
        text.setForeground(Color.WHITE);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        text.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));

        JButton close = new JButton("BACK");
        close.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        close.addActionListener(event -> dispose());

        content.add(title, BorderLayout.NORTH);
        content.add(text, BorderLayout.CENTER);
        JPanel buttonRow = new JPanel();
        buttonRow.setOpaque(false);
        buttonRow.add(close);
        content.add(buttonRow, BorderLayout.SOUTH);

        add(content);
        setPreferredSize(new Dimension(520, 300));
        pack();
        setLocationRelativeTo(owner);
    }
}
