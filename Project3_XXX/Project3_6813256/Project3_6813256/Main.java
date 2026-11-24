package Project3_6813256;

import javax.swing.SwingUtilities;

/** Entry point for the Tower Hop starter. Add the group's names and IDs here. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameFrame().setVisible(true));
    }
}
