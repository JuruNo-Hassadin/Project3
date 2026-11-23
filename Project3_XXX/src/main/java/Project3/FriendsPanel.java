/* Group members (English names and student IDs): [FILL IN] */
package Project3;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;

public class FriendsPanel extends JPanel {
    private final AppController controller;
    private final JComboBox<String> searchMode = new JComboBox<>();
    private final JTextField searchText = new JTextField(16);
    private final JPanel peerList = new JPanel();
    private final JLabel status = new JLabel(
            "Waiting for other CHATGAE users on this LAN...");
    private final Timer expiryTimer;

    public FriendsPanel(AppController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        searchMode.addItem("All online");
        searchMode.addItem("Username contains");
        searchMode.addItem("Username starts with");
        searchMode.addItem("Exact username");
        searchMode.addItem("IP address contains");

        JPanel searchBar = new JPanel();
        searchBar.add(new JLabel("Search LAN users:"));
        searchBar.add(searchText);
        searchBar.add(searchMode);

        peerList.setLayout(new BoxLayout(peerList, BoxLayout.Y_AXIS));
        peerList.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.add(status, BorderLayout.NORTH);
        center.add(new JScrollPane(peerList), BorderLayout.CENTER);

        add(new JLabel("Friends - online users discovered on this LAN"), BorderLayout.NORTH);
        add(searchBar, BorderLayout.CENTER);
        add(center, BorderLayout.SOUTH);

        searchText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                filterPeers();
            }
        });
        searchMode.addActionListener(event -> filterPeers());

        expiryTimer = new Timer(2500, event -> removeExpiredPeers());
        expiryTimer.start();
    }

    public void peerSeen(String username, String ipAddress, int tcpPort) {
    if (username.equalsIgnoreCase(controller.currentUser())) return;

    for (int i = 0; i < peerList.getComponentCount(); i++) {
        Component component = peerList.getComponent(i);

        if (component instanceof JButton button) {
            Object savedName = button.getClientProperty("username");

            if (username.equalsIgnoreCase(String.valueOf(savedName))) {
                button.putClientProperty("ipAddress", ipAddress);
                button.putClientProperty("tcpPort", tcpPort);
                button.putClientProperty("lastSeen", System.currentTimeMillis());
                button.setText(
                        "@" + username + "     " + ipAddress + "     Open DM"
                );
                filterPeers();
                return;
            }
        }
    }

    JButton peer = new JButton(
            "@" + username + "     " + ipAddress + "     Open DM"
    );
    peer.setAlignmentX(Component.LEFT_ALIGNMENT);
    peer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    peer.putClientProperty("username", username);
    peer.putClientProperty("ipAddress", ipAddress);
    peer.putClientProperty("tcpPort", tcpPort);
    peer.putClientProperty("lastSeen", System.currentTimeMillis());

    peer.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent event) {
            controller.openChatWith(username);
        }
    });

    peerList.add(peer);
    filterPeers();
    }

    private void filterPeers() {
        String query = searchText.getText().trim().toLowerCase();
        int mode = searchMode.getSelectedIndex();
        int visibleCount = 0;

        for (int i = 0; i < peerList.getComponentCount(); i++) {
            Component component = peerList.getComponent(i);
            if (!(component instanceof JButton button)) continue;

            String username = String.valueOf(button.getClientProperty("username"));
            String ipAddress = String.valueOf(button.getClientProperty("ipAddress"));
            boolean matches = query.isEmpty() || switch (mode) {
                case 1 -> username.toLowerCase().contains(query);
                case 2 -> username.toLowerCase().startsWith(query);
                case 3 -> username.equalsIgnoreCase(query);
                case 4 -> ipAddress.toLowerCase().contains(query);
                default -> true;
            };

            button.setVisible(matches);
            if (matches) visibleCount++;
        }

        if (peerList.getComponentCount() == 0) {
            status.setText("No other CHATGAE users found yet. Keep both apps open on the same LAN.");
        } else if (visibleCount == 0) {
            status.setText("No online users match this search.");
        } else {
            status.setText(visibleCount + " online user(s) found. Click a name to open DM.");
        }

        peerList.revalidate();
        peerList.repaint();
    }

    private void removeExpiredPeers() {
        long now = System.currentTimeMillis();

        for (int i = peerList.getComponentCount() - 1; i >= 0; i--) {
            Component component = peerList.getComponent(i);
            if (!(component instanceof JButton button)) continue;

            Object value = button.getClientProperty("lastSeen");
            if (value instanceof Long lastSeen && now - lastSeen > 8000) {
                peerList.remove(i);
            }
        }

        filterPeers();
    }

    public void stop() {
        expiryTimer.stop();
    }
        public String ipForUsername(String username) {
            for (int i = 0; i < peerList.getComponentCount(); i++) {
            Component component = peerList.getComponent(i);

            if (component instanceof JButton button) {
                Object savedName = button.getClientProperty("username");

                if (username.equalsIgnoreCase(String.valueOf(savedName))) {
                    Object savedIp = button.getClientProperty("ipAddress");
                    return savedIp == null ? "" : savedIp.toString();
            }
        }
    }

    return "";
    }
    public int tcpPortForUsername(String username) {
    for (int i = 0; i < peerList.getComponentCount(); i++) {
        Component component = peerList.getComponent(i);

        if (component instanceof JButton button) {
            Object savedName = button.getClientProperty("username");

            if (username.equalsIgnoreCase(String.valueOf(savedName))) {
                Object savedPort = button.getClientProperty("tcpPort");

                if (savedPort instanceof Integer port) {
                    return port;
                }
            }
        }
    }

    return 0;
    }    
}