package Project3_6813256;

import javax.swing.SwingUtilities;

/** Entry point for the Tower Hop starter. Add the group's names and IDs here. */
public final class Main {
    private Main() { }   // standard constructor for this class, means shouldn't be called

    public static void main(String[] args) {   // entry point for JVM
        SwingUtilities.invokeLater(() -> new GameFrame().setVisible(true)); // create UI ( LAMBDA for starting jframe)
    }
}
