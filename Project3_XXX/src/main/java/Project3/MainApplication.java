/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** The required first application window. Screen details are in separate panel classes. */
public class MainApplication extends JFrame {
    public MainApplication() {
        setTitle("CHATGAE - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setSize(1000, 680);
        setLocationRelativeTo(null);
        new AppController(this).start();
    }

    public void showScreen(JPanel screen) {
        setContentPane(screen);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainApplication window = new MainApplication();
            window.setVisible(true);
        });
    }
}
