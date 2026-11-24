package Project3_6813256;

import javax.swing.SwingUtilities;

public final class MainApplication {
    private MainApplication() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameFrame().setVisible(true));
    }
}