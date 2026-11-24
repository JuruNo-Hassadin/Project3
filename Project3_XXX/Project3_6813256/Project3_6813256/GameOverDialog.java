package Project3_6813256;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Pixel-styled game-over board with a player-name field for the leaderboard. */
public final class GameOverDialog extends JDialog {
    private final JTextField playerNameField;
    private final JLabel errorLabel = new JLabel(" ", SwingConstants.CENTER);
    private final Function<String, String> saveAction;
    private boolean saved;
    private boolean discarded;

    public GameOverDialog(JFrame owner, int score, String defaultName,
            Function<String, String> saveAction) {
        super(owner, "Game Over", true);
        this.saveAction = saveAction;
        playerNameField = new JTextField(defaultName == null ? "Player" : defaultName, 18);
        buildUi(score);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                discardAndClose();
            }
        });
        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
        playerNameField.selectAll();
    }

    public boolean wasSaved() {
        return saved;
    }

    public boolean wasDiscarded() {
        return discarded;
    }

    private void buildUi(int score) {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(new Color(24, 37, 54));
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(137, 194, 154), 5),
                BorderFactory.createEmptyBorder(18, 22, 16, 22)));

        JLabel title = new JLabel("GAME OVER", SwingConstants.CENTER);
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        title.setForeground(new Color(255, 220, 124));

        JPanel form = new JPanel(new BorderLayout(6, 9));
        form.setOpaque(false);
        JLabel scoreLabel = new JLabel("SCORE: " + score + " FLOORS", SwingConstants.CENTER);
        scoreLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        scoreLabel.setForeground(Color.WHITE);
        JLabel nameLabel = new JLabel("PLAYER NAME", SwingConstants.CENTER);
        nameLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        nameLabel.setForeground(new Color(205, 223, 231));
        playerNameField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        errorLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        errorLabel.setForeground(new Color(255, 158, 136));
        form.add(scoreLabel, BorderLayout.NORTH);
        JPanel nameForm = new JPanel(new BorderLayout(0, 6));
        nameForm.setOpaque(false);
        nameForm.add(nameLabel, BorderLayout.NORTH);
        nameForm.add(playerNameField, BorderLayout.CENTER);
        form.add(nameForm, BorderLayout.CENTER);
        form.add(errorLabel, BorderLayout.SOUTH);

        JButton saveButton = makeButton("SAVE SCORE", true);
        JButton quitButton = makeButton("QUIT TO MENU", false);
        saveButton.addActionListener(event -> saveScore());
        quitButton.addActionListener(event -> discardAndClose());
        playerNameField.addActionListener(event -> saveButton.doClick());
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.add(saveButton);
        buttons.add(quitButton);

        content.add(title, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        add(content);
        setPreferredSize(new Dimension(500, 330));
    }

    private JButton makeButton(String label, boolean primary) {
        JButton button = new JButton(label);
        button.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(primary ? new Color(67, 119, 87) : new Color(57, 75, 94));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(16, 26, 40), 3),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return button;
    }

    private void saveScore() {
        String name = playerNameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Enter a player name to continue.");
            playerNameField.requestFocusInWindow();
            return;
        }
        String error = saveAction.apply(name);
        if (error == null) {
            saved = true;
            dispose();
        } else {
            errorLabel.setText(error);
        }
    }

    private void discardAndClose() {
        discarded = true;
        dispose();
    }
}
