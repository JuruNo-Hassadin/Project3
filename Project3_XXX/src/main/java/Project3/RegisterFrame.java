/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RegisterFrame extends JFrame {
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JCheckBox music = new JCheckBox("Music");
    private final JCheckBox travel = new JCheckBox("Travel");
    private final JCheckBox games = new JCheckBox("Games");
    private final JCheckBox food = new JCheckBox("Food");
    private final JCheckBox study = new JCheckBox("Study");

    public RegisterFrame(AppController controller) {
        setTitle("CHATGAE - Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(440, 520);
        setLocationRelativeTo(null);
        JPanel form = new JPanel(new GridLayout(0, 1, 7, 7));
        form.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        form.add(new JLabel("Create account"));
        form.add(new JLabel("Username"));
        form.add(username);
        form.add(new JLabel("Password"));
        form.add(password);
        form.add(new JLabel("Choose interests"));
        form.add(music);
        form.add(travel);
        form.add(games);
        form.add(food);
        form.add(study);
        JButton create = new JButton("CREATE ACCOUNT");
        form.add(create);
        create.addActionListener(event -> {
            StringBuilder selected = new StringBuilder();
            if (music.isSelected()) selected.append("Music,");
            if (travel.isSelected()) selected.append("Travel,");
            if (games.isSelected()) selected.append("Games,");
            if (food.isSelected()) selected.append("Food,");
            if (study.isSelected()) selected.append("Study,");
            if (selected.length() > 0) selected.setLength(selected.length() - 1);
            if (controller.register(username.getText(), new String(password.getPassword()), selected.toString())) {
                dispose();
            }
        });
        add(form, BorderLayout.CENTER);
    }
}
