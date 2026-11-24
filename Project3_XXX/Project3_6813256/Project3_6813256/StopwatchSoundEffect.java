/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/** Plays the supplied time-stop sound once when the stopwatch is activated. */
public final class StopwatchSoundEffect implements AutoCloseable {
    private static final String ASSET_PATH = "src/main/java/Project3_6813256/resources/assets/";
    private static final Path SOUND_RESOURCE =
            Path.of(ASSET_PATH + "Stopwatch_effect.wav");

    private Clip clip;

    public StopwatchSoundEffect() {
        loadClip();
    }

    public void play(boolean soundEnabled) {
        if (!soundEnabled || clip == null || !clip.isOpen()) {
            return;
        }
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    private void loadClip() {
        try (InputStream resource = Files.newInputStream(SOUND_RESOURCE)) {
            if (resource == null) {
                throw new IOException("Missing sound resource: " + SOUND_RESOURCE);
            }
            try (BufferedInputStream buffered = new BufferedInputStream(resource);
                    AudioInputStream encoded = AudioSystem.getAudioInputStream(buffered)) {
                AudioFormat sourceFormat = encoded.getFormat();
                int channels = sourceFormat.getChannels() > 0 ? sourceFormat.getChannels() : 2;
                float sampleRate = sourceFormat.getSampleRate() > 0
                        ? sourceFormat.getSampleRate() : 44_100.0f;
                AudioFormat pcmFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        sampleRate, 16, channels, channels * 2, sampleRate, false);
                try (AudioInputStream decoded = AudioSystem.getAudioInputStream(pcmFormat, encoded)) {
                    clip = AudioSystem.getClip();
                    clip.open(decoded);
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gain = (FloatControl) clip.getControl(
                                FloatControl.Type.MASTER_GAIN);
                        gain.setValue(Math.max(gain.getMinimum(),
                                Math.min(gain.getMaximum(), -3.0f)));
                    }
                }
            }
        } catch (Exception exception) {
            clip = null;
            System.err.println("Could not initialize stopwatch sound: " + exception.getMessage());
        }
    }

    @Override
    public void close() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
}
