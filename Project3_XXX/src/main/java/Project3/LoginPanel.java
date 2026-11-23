/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginPanel extends JPanel {
    public LoginPanel(AppController controller) {
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.BLACK);

        JPanel card = new JPanel(new GridLayout(0, 1, 8, 8));
        card.setBorder(BorderFactory.createEmptyBorder(35, 38, 35, 38));
        card.setBackground(Color.WHITE);
        card.add(new JLabel("CHATGAE", JLabel.CENTER)).setFont(new Font("SansSerif", Font.BOLD, 30));
        card.add(new JLabel("Username"));
        JTextField username = new JTextField(22);
        card.add(username);
        card.add(new JLabel("Password"));
        JPasswordField password = new JPasswordField(22);
        card.add(password);

        JButton login = new JButton("LOGIN");
        JButton register = new JButton("REGISTER");
        login.setBackground(new Color(65, 185, 245));
        register.setBackground(new Color(65, 185, 245));
        card.add(login);
        card.add(register);
        card.add(new JLabel("Group member names and IDs: edit README.txt", JLabel.CENTER));

        login.addActionListener(event -> controller.login(username.getText(), new String(password.getPassword())));
        register.addActionListener(event -> controller.openRegister());
        add(card, BorderLayout.CENTER);
    }
}
