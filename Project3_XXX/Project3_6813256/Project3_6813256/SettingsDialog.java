/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/** Editable settings dialog with player and audio controls. */
public final class SettingsDialog extends JDialog {
    private final JComboBox<PlayerSkin> playerSkinBox;
    private final JCheckBox soundBox;
    private final JCheckBox muteMusicBox;
    private final JSlider volumeSlider;
    private final IntConsumer onVolumePreview;
    private final MenuClickSoundEffect clickSound;
    private GameSettings result;

    public SettingsDialog(JFrame owner, GameSettings current) {
        this(owner, current, null, null);
    }

    public SettingsDialog(JFrame owner, GameSettings current, IntConsumer onVolumePreview) {
        this(owner, current, onVolumePreview, null);
    }

    public SettingsDialog(JFrame owner, GameSettings current, IntConsumer onVolumePreview,
            MenuClickSoundEffect clickSound) {
        super(owner, "Game settings", true);
        this.onVolumePreview = onVolumePreview == null ? volume -> { } : onVolumePreview;
        this.clickSound = clickSound;
        playerSkinBox = new JComboBox<>(PlayerSkin.values());
        playerSkinBox.setSelectedItem(current.getPlayerSkin());
        soundBox = new JCheckBox("Sound effects", current.isSoundEnabled());
        muteMusicBox = new JCheckBox("Mute background music",
                current.isMusicMuted() || current.getVolume() == 0);
        volumeSlider = new JSlider(0, 100, current.getVolume());
        volumeSlider.addChangeListener(event -> {
            if (volumeSlider.getValue() == 0) {
                muteMusicBox.setSelected(true);
            } else if (muteMusicBox.isSelected()) {
                muteMusicBox.setSelected(false);
            }
            this.onVolumePreview.accept(
                    muteMusicBox.isSelected() ? 0 : volumeSlider.getValue());
        });
        muteMusicBox.addActionListener(event -> {
            if (!muteMusicBox.isSelected() && volumeSlider.getValue() == 0) {
                volumeSlider.setValue(65);
            }
            this.onVolumePreview.accept(
                    muteMusicBox.isSelected() ? 0 : volumeSlider.getValue());
        });
        buildUi(current);
        pack();
        setLocationRelativeTo(owner);
    }

    public GameSettings getResult() {
        return result;
    }

    private void buildUi(GameSettings current) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        form.add(new JLabel("Player skin"));
        form.add(playerSkinBox);
        form.add(new JLabel("Music volume"));
        form.add(volumeSlider);
        form.add(soundBox);
        form.add(new JLabel(""));
        form.add(muteMusicBox);
        form.add(new JLabel(""));

        JButton applyButton = new JButton("Apply");
        JButton cancelButton = new JButton("Cancel");
        applyButton.addActionListener(event -> {
            if (clickSound != null) {
                clickSound.play(soundBox.isSelected());
            }
            result = new GameSettings(current.getPlayerName(), current.getDifficulty(),
                    (PlayerSkin) playerSkinBox.getSelectedItem(), soundBox.isSelected(),
                    current.isLandingGuide(), current.isShowStopwatch(),
                    current.isReducedShake(), volumeSlider.getValue(),
                    muteMusicBox.isSelected());
            dispose();
        });
        cancelButton.addActionListener(event -> {
            if (clickSound != null) {
                clickSound.play(current.isSoundEnabled());
            }
            dispose();
        });
        JPanel buttons = new JPanel();
        buttons.add(applyButton);
        buttons.add(cancelButton);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }
}