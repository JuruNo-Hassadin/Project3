/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/** Small synthesized retro chirp that plays when a jump begins. */
public final class JumpSoundEffect implements AutoCloseable {
    private static final int SAMPLE_RATE = 22_050;
    private static final double DURATION_SECONDS = 0.14;
    private Clip clip;

    public JumpSoundEffect() {
        try {
            int frameCount = (int) (SAMPLE_RATE * DURATION_SECONDS);
            byte[] pcm = createJumpTone(frameCount);
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            clip = AudioSystem.getClip();
            clip.open(format, pcm, 0, pcm.length);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), -8.0f)));
            }
        } catch (Exception exception) {
            clip = null;
            System.err.println("Could not initialize jump sound: " + exception.getMessage());
        }
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

    private byte[] createJumpTone(int frameCount) {
        byte[] pcm = new byte[frameCount * 2];
        for (int frame = 0; frame < frameCount; frame++) {
            double progress = frame / (double) frameCount;
            double time = frame / (double) SAMPLE_RATE;
            double frequencyStart = 390.0;
            double frequencyEnd = 980.0;
            double phase = 2.0 * Math.PI * (frequencyStart * time
                    + (frequencyEnd - frequencyStart)
                    * time * time / (2.0 * DURATION_SECONDS));
            double envelope = Math.pow(1.0 - progress, 1.6);
            short sample = (short) (Math.sin(phase) * envelope * 14_000);
            pcm[frame * 2] = (byte) (sample & 0xff);
            pcm[frame * 2 + 1] = (byte) ((sample >>> 8) & 0xff);
        }
        return pcm;
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
