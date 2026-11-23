/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.io.IOException;
import java.nio.file.Path;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.TargetDataLine;

public class VoiceNoteService {
    private static final AudioFormat FORMAT = new AudioFormat(16000.0f, 16, 1, true, false);
    private TargetDataLine recordingLine;
    private Thread writerThread;
    private Path recordingPath;
    private Clip activeClip;
    private IOException writerError;

    public synchronized Path startRecording(String username) throws Exception {
        if (recordingLine != null) throw new IllegalStateException("A recording is already running.");
        DataFiles.prepare();
        recordingPath = DataFiles.VOICE.resolve("voice_" + DataFiles.cleanField(username)
                + "_" + System.currentTimeMillis() + ".wav");
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
        recordingLine = (TargetDataLine) AudioSystem.getLine(info);
        recordingLine.open(FORMAT);
        recordingLine.start();
        AudioInputStream input = new AudioInputStream(recordingLine);
        writerError = null;
        Path target = recordingPath;
        writerThread = new Thread(() -> {
            try {
                AudioSystem.write(input, AudioFileFormat.Type.WAVE, target.toFile());
            } catch (IOException ex) {
                writerError = ex;
            }
        }, "voice-note-writer");
        writerThread.setDaemon(true);
        writerThread.start();
        return recordingPath;
    }

    public synchronized Path stopRecording() throws Exception {
        if (recordingLine == null) return null;
        Path completed = recordingPath;
        recordingLine.stop();
        recordingLine.close();
        recordingLine = null;
        if (writerThread != null) {
            writerThread.join(3000);
            writerThread = null;
        }
        if (writerError != null) throw writerError;
        recordingPath = null;
        return completed;
    }

    public synchronized void play(String path) throws Exception {
        if (path == null || path.isBlank()) throw new IOException("No voice message is selected.");
        Path file = Path.of(path);
        if (!file.isAbsolute()) file = Path.of("").toAbsolutePath().resolve(file).normalize();
        if (activeClip != null) {
            activeClip.stop();
            activeClip.close();
        }
        activeClip = AudioSystem.getClip();
        try (AudioInputStream input = AudioSystem.getAudioInputStream(file.toFile())) {
            activeClip.open(input);
        }
        activeClip.start();
    }

    public synchronized void setVolume(int percent) {
        if (activeClip == null || !activeClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl gain = (FloatControl) activeClip.getControl(FloatControl.Type.MASTER_GAIN);
        float fraction = Math.max(0, Math.min(100, percent)) / 100.0f;
        float value = gain.getMinimum() + (gain.getMaximum() - gain.getMinimum()) * fraction;
        gain.setValue(value);
    }

    public synchronized void stopPlayback() {
        if (activeClip != null) {
            activeClip.stop();
            activeClip.close();
            activeClip = null;
        }
    }
}
