/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
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
import javax.swing.RowSorter;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Displays every saved play session in a non-editable table. */
public final class LeaderboardDialog extends JDialog {
    public LeaderboardDialog(JFrame owner, LeaderboardStore store) {
        super(owner, "Leaderboard", true);
        setLayout(new BorderLayout(8, 8));
        String[] columns = {"Player", "Difficulty", "Floors", "Seconds"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(2, SortOrder.DESCENDING)));
        JLabel fileLabel = new JLabel("Saved in: " + store.getFile().normalize());
        try {
            List<LeaderboardEntry> entries = store.loadAll();
            for (LeaderboardEntry entry : entries) {
                model.addRow(new Object[] {entry.playerName(), entry.difficulty(),
                    entry.floorsClimbed(), entry.durationSeconds()});
            }
        } catch (IOException exception) {
            fileLabel.setText("Could not load leaderboard: " + exception.getMessage());
        }
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(event -> dispose());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(fileLabel, BorderLayout.NORTH);
        add(closeButton, BorderLayout.SOUTH);
        setResizable(false);
        setPreferredSize(new Dimension(760, 380));
        pack();
        setLocationRelativeTo(owner);
    }
}
