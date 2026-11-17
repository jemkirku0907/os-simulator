package ossim.ui.panels;

import ossim.ui.Theme;
import ossim.ui.Theme.RoundedButton;
import ossim.ui.Theme.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Consumer;

/**
 * Dashboard overview providing direct access to all 8 OS Simulator modules.
 */
public class OverviewPanel extends JPanel {

    private static final String[][] MODULE_CARDS = {
            { "cpu", "CPU", "1. CPU Scheduling",
                    "FCFS, SJF, SRTF, Priority, and Round Robin with Gantt chart and turnaround/waiting metrics." },
            { "banker", "BNK", "2. Banker's Algorithm",
                    "Need matrix, safety sequence check, and resource request evaluation." },
            { "paging", "PAG", "3. Paging",
                    "Page table mapping, physical frame layout, and logical-to-physical address translation." },
            { "pagerep", "REP", "4. Page Replacement",
                    "FIFO, LRU, Optimal, and Clock with frame trace, hit/fault ratios, and comparison." },
            { "segment", "SEG", "5. Segmentation",
                    "Segment table with base/limit bounds checking, overlap detection, and address translation." },
            { "frag", "FRG", "6. Fragmentation",
                    "First Fit, Best Fit, Worst Fit, and Next Fit for internal and external fragmentation." },
            { "disk", "DSK", "7. Disk Scheduling",
                    "FCFS, SSTF, SCAN, C-SCAN, LOOK, and C-LOOK with head movement chart." },
            { "multi", "MLT", "8. Multiprogramming",
                    "Multi-core RAM allocation, CPU/I/O overlap, and utilization tracking." }
    };

    public OverviewPanel(Consumer<String> navigator) {
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 14, 20));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);

        JLabel headline = new JLabel("Operating System Simulator");
        headline.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headline.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Select a module below or from the sidebar.");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        header.add(headline);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(2, 2, 8, 4));

        for (int i = 0; i < MODULE_CARDS.length; i++) {
            final String key = MODULE_CARDS[i][0];
            final String badgeCode = MODULE_CARDS[i][1];
            final String title = MODULE_CARDS[i][2];
            final String desc = MODULE_CARDS[i][3];
            Color accent = Theme.colorForIndex(i);

            RoundedPanel card = new RoundedPanel(new BorderLayout(8, 6), 14, Theme.BG_CARD, Theme.BORDER_LIGHT);
            card.setBottomAccent(accent);
            card.setBorder(BorderFactory.createEmptyBorder(10, 14, 12, 14));

            JPanel cardTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            cardTop.setOpaque(false);

            RoundedPanel iconBadge = new RoundedPanel(new BorderLayout(), 9, Theme.softTint(accent), null);
            iconBadge.setPreferredSize(new Dimension(32, 32));
            JLabel badgeLbl = new JLabel(badgeCode, SwingConstants.CENTER);
            badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            badgeLbl.setForeground(accent);
            iconBadge.add(badgeLbl, BorderLayout.CENTER);

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(Theme.FONT_SECTION);
            titleLbl.setForeground(Theme.TEXT_PRIMARY);

            cardTop.add(iconBadge);
            cardTop.add(titleLbl);

            JLabel descLbl = new JLabel("<html><body style='width:340px'>" + desc + "</body></html>");
            descLbl.setFont(Theme.FONT_BODY);
            descLbl.setForeground(Theme.TEXT_SECONDARY);
            descLbl.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

            RoundedButton openBtn = new RoundedButton(
                    "Open", 10, accent, accent.darker(), Color.WHITE, null);
            openBtn.setFont(Theme.FONT_BODY_BOLD);
            openBtn.setBorder(BorderFactory.createEmptyBorder(5, 16, 5, 16));
            openBtn.addActionListener(e -> navigator.accept(key));

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            footer.add(openBtn, BorderLayout.EAST);

            card.add(cardTop, BorderLayout.NORTH);
            card.add(descLbl, BorderLayout.CENTER);
            card.add(footer, BorderLayout.SOUTH);

            grid.add(card);
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_APP);
        Theme.styleScrollPane(scroll);
        add(scroll, BorderLayout.CENTER);
    }
}
