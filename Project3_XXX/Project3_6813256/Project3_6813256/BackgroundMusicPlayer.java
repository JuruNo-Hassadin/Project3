package Project3_6813256;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/** Loads and loops the game's MP3 soundtrack without blocking the Swing UI. */
public final class BackgroundMusicPlayer implements AutoCloseable {
    private static final String ASSET_PATH = "src/main/java/Project3_6813256/resources/assets/";
    private static final Path MUSIC_RESOURCE =
            Path.of(ASSET_PATH + "Background_effect.wav");

    private volatile int volume = 65;
    private volatile boolean closed;
    private Clip clip;

    public void startLooping() {
        Thread loader = new Thread(this::loadAndPlay, "tower-hop-background-music"); //override run() by loadAndPlay
        loader.setDaemon(true);  // THread type that's run in the background
        loader.start();
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        synchronized (this) {
            applyVolume();    // method of "this"
        }
    }

    private void loadAndPlay() {          // method that thread actually running on
        try (InputStream resource = Files.newInputStream(MUSIC_RESOURCE)) {
            if (resource == null) {
                throw new IOException("Missing music resource: " + MUSIC_RESOURCE);
            }
            try (BufferedInputStream buffered = new BufferedInputStream(resource);
                    AudioInputStream encoded = AudioSystem.getAudioInputStream(buffered)) {   // audio stream that is wrapped
                AudioFormat sourceFormat = encoded.getFormat();                               // How audio should be read
                int channels = sourceFormat.getChannels() > 0 ? sourceFormat.getChannels() : 2;   // if > 0 ok then 2
                float sampleRate = sourceFormat.getSampleRate() > 0                            // frequency that a sound should be read
                        ? sourceFormat.getSampleRate() : 44_100.0f;
                AudioFormat pcmFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,       // set whole new audio format object
                        sampleRate, 16, channels, channels * 2, sampleRate, false);
                try (AudioInputStream decoded = AudioSystem.getAudioInputStream(pcmFormat, encoded)) {  // reassign modified value
                    Clip loadedClip = AudioSystem.getClip();
                    loadedClip.open(decoded);
                    synchronized (this) {
                        if (closed) {
                            loadedClip.close();
                            return;
                        }
                        clip = loadedClip;
                        applyVolume();
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                    }
                }
            }
        } catch (Exception exception) {
            if (!closed) {
                System.err.println("Could not play background music: " + exception.getMessage());
            }
        }
    }

    private void applyVolume() {
        if (clip == null || !clip.isOpen()) {
            return;
        }
        if (clip.isControlSupported(BooleanControl.Type.MUTE)) {
            BooleanControl mute = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
            mute.setValue(volume == 0);
        }
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float decibels = volume == 0
                    ? gain.getMinimum()
                    : (float) (20.0 * Math.log10(volume / 100.0));
            decibels = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels));
            gain.setValue(decibels);
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
}
