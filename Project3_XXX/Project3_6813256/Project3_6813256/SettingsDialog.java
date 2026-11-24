package Project3_6813256;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

/** Editable settings dialog with the requested Swing controls. */
public final class SettingsDialog extends JDialog {
    private final JTextField playerNameField;
    private final JComboBox<Difficulty> difficultyBox;
    private final JCheckBox soundBox;
    private final JCheckBox highContrastBox;
    private final JCheckBox landingGuideBox;
    private final JCheckBox stopwatchBox;
    private final JCheckBox reducedShakeBox;
    private final JSlider volumeSlider;
    private GameSettings result;

    public SettingsDialog(JFrame owner, GameSettings current) {
        super(owner, "Game settings", true);
        playerNameField = new JTextField(current.getPlayerName(), 18);
        difficultyBox = new JComboBox<>(Difficulty.values());
        difficultyBox.setSelectedItem(current.getDifficulty());
        soundBox = new JCheckBox("Sound effects", current.isSoundEnabled());
        highContrastBox = new JCheckBox("High-contrast pixel art", current.isHighContrast());
        landingGuideBox = new JCheckBox("Show landing guide", current.isLandingGuide());
        stopwatchBox = new JCheckBox("Show stopwatch item", current.isShowStopwatch());
        reducedShakeBox = new JCheckBox("Reduce screen shake", current.isReducedShake());
        volumeSlider = new JSlider(0, 100, current.getVolume());
        buildUi();
        pack();
        setLocationRelativeTo(owner);
    }

    public GameSettings getResult() {
        return result;
    }

    private void buildUi() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        form.add(new JLabel("Player name"));
        form.add(playerNameField);
        form.add(new JLabel("Difficulty"));
        form.add(difficultyBox);
        form.add(new JLabel("Music volume"));
        form.add(volumeSlider);
        form.add(soundBox);
        form.add(highContrastBox);
        form.add(landingGuideBox);
        form.add(stopwatchBox);
        form.add(reducedShakeBox);
        form.add(new JLabel("Group members: add names and IDs in this area."));

        JButton applyButton = new JButton("Apply");
        JButton cancelButton = new JButton("Cancel");
        applyButton.addActionListener(event -> {
            result = new GameSettings(playerNameField.getText(),
                    (Difficulty) difficultyBox.getSelectedItem(),
                    soundBox.isSelected(), highContrastBox.isSelected(),
                    landingGuideBox.isSelected(), stopwatchBox.isSelected(),
                    reducedShakeBox.isSelected(), volumeSlider.getValue());
            dispose();
        });
        cancelButton.addActionListener(event -> dispose());
        JPanel buttons = new JPanel();
        buttons.add(applyButton);
        buttons.add(cancelButton);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }
}
