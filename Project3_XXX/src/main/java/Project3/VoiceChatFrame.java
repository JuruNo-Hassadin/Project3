/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class VoiceChatFrame extends JPanel {
    private final AppController controller;
    private final String currentUser;
    private final JComboBox<String> contact = new JComboBox<>();
    private final JLabel status = new JLabel("Voice messages are saved on this computer.");

    public VoiceChatFrame(AppController controller, String currentUser) {
        this.controller = controller;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));
        contact.addItem("alex");
        contact.addItem("mina");
        contact.addItem("somchai");
        contact.addItem("nicha");
        contact.addItem("guest");
        contact.setEditable(true);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controls.add(new JLabel("Send voice message to:"));
        controls.add(contact);
        JButton record = new JButton("Record");
        JButton stopAndSend = new JButton("Stop and send");
        JButton playLatest = new JButton("Play latest voice");
        controls.add(record);
        controls.add(stopAndSend);
        controls.add(playLatest);

        JSlider volume = new JSlider(0, 100, 80);
        volume.setBorder(BorderFactory.createTitledBorder("Playback volume"));
        volume.addChangeListener(event -> controller.voiceNotes().setVolume(volume.getValue()));
        JPanel center = new JPanel(new BorderLayout(10, 18));
        center.add(controls, BorderLayout.NORTH);
        center.add(volume, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(12, 2, 12, 2));
        center.add(status, BorderLayout.SOUTH);
        add(center, BorderLayout.NORTH);

        record.addActionListener(event -> startRecording());
        stopAndSend.addActionListener(event -> stopAndSend());
        playLatest.addActionListener(event -> playLatest());
    }

    private String selectedContact() {
        Object value = contact.getEditor().getItem();
        return value == null ? "" : value.toString().trim();
    }

    private void startRecording() {
        try {
            controller.voiceNotes().startRecording(currentUser);
            status.setText("Recording. Speak into the microphone, then choose Stop and send.");
        } catch (Exception ex) {
            controller.showError("Could not start microphone recording", ex);
        }
    }

    private void stopAndSend() {
        String peer = selectedContact();
        if (peer.isBlank()) {
            status.setText("Choose a recipient first.");
            return;
        }
        try {
            Path path = controller.voiceNotes().stopRecording();
            if (path == null) {
                status.setText("Start a recording first.");
                return;
            }
            String relativePath = path.toString().replace('\\', '/');
            controller.messages().sendVoice(currentUser, peer, relativePath);
            status.setText("Voice message sent locally to @" + peer + ".");
        } catch (Exception ex) {
            controller.showError("Could not finish or save the voice message", ex);
        }
    }

    private void playLatest() {
        try {
            controller.voiceNotes().play(controller.latestVoice(selectedContact()));
        } catch (Exception ex) {
            controller.showError("Could not play the voice message", ex);
        }
    }
}
