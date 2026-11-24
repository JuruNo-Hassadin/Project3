package Project3_6813256;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Displays every saved play session in a non-editable table. */
public final class LeaderboardDialog extends JDialog {
    public LeaderboardDialog(JFrame owner, LeaderboardStore store) {     // store class from another file
        super(owner, "Leaderboard", true);     // freeze other frames
        setLayout(new BorderLayout(8, 8));
        String[] columns = {"Played at", "Player", "Difficulty", "Floors", "Seconds"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        JLabel fileLabel = new JLabel("Saved in: " + store.getFile().normalize());
        try {
            List<LeaderboardEntry> entries = store.loadAll();
            for (LeaderboardEntry entry : entries) {
                model.addRow(new Object[] {entry.playedAt(), entry.playerName(),
                    entry.difficulty(), entry.floorsClimbed(), entry.durationSeconds()});
            }
        } catch (IOException exception) {
            fileLabel.setText("Could not load leaderboard: " + exception.getMessage());
        }
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(event -> dispose());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(fileLabel, BorderLayout.NORTH);
        add(closeButton, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(760, 380));
        pack();
        setLocationRelativeTo(owner);
    }
}
