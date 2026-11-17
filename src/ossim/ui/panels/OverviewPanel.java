package ossim.ui.panels;

import ossim.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.function.Consumer;

/**
 * Welcome / Dashboard panel providing quick-launch cards for all 8 OS Simulator modules.
 */
public class OverviewPanel extends JPanel {

    private static final String[][] MODULE_CARDS = {
            { "cpu", "1. CPU Scheduling",
                    "FCFS, SJF, SRTF, Priority (Preemptive & Non-Preemptive), Round Robin. Interactive Gantt Chart, WT, TAT, RT & algorithm comparison." },
            { "banker", "2. Banker's Algorithm",
                    "Dynamic P x R matrices, Need matrix computation, step-by-step Safety check, safe sequence, and Resource Request simulation." },
            { "paging", "3. Paging",
                    "Dynamic Page Table, frame mapping visualization, single & batch Logical-to-Physical address translation, and page fault detection." },
            { "pagerep", "4. Page Replacement",
                    "FIFO, LRU, Optimal (OPT), and Clock/Second Chance. Variable-length reference strings, frame grid, hit/fault ratios & comparison." },
            { "segment", "5. Segmentation",
                    "Dynamic Segment Table (Base & Limit), physical memory overlap detection, bounds validation, and trap/translation diagnostics." },
            { "frag", "6. Fragmentation",
                    "Fixed & Dynamic partitioning with First Fit, Best Fit, Worst Fit, and Next Fit. Visualizes internal & external fragmentation." },
            { "disk", "7. Disk Scheduling",
                    "FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK. Interactive head-movement trajectory chart, seek distances & algorithm comparison." },
            { "multi", "8. Multiprogramming",
                    "Multi-core RAM + CPU/I/O overlap simulator with live step/play controls, utilization dashboard & uniprogramming baseline comparison." }
    };

    public OverviewPanel(Consumer<String> navigator) {
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel header = Theme.createModuleHeader(
                "Operating System Simulator",
                "Interactive Java GUI covering 8 core OS modules with dynamic unlimited inputs, real-time calculations, and visual diagrams.");
        add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);

        for (int i = 0; i < MODULE_CARDS.length; i++) {
            final String key = MODULE_CARDS[i][0];
            final String title = MODULE_CARDS[i][1];
            final String desc = MODULE_CARDS[i][2];
            Color accent = Theme.colorForIndex(i);

            JPanel card = new JPanel(new BorderLayout(8, 8));
            card.setBackground(Theme.BG_CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1),
                            BorderFactory.createEmptyBorder(12, 14, 12, 14))));

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(Theme.FONT_SECTION);
            titleLbl.setForeground(Theme.TEXT_PRIMARY);

            JLabel descLbl = new JLabel("<html><body style='width:340px'>" + desc + "</body></html>");
            descLbl.setFont(Theme.FONT_BODY);
            descLbl.setForeground(Theme.TEXT_SECONDARY);

            JButton openBtn = Theme.createPrimaryButton("Open Module ->");
            openBtn.setBackground(accent);
            openBtn.addActionListener(e -> navigator.accept(key));

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            footer.add(openBtn, BorderLayout.EAST);

            card.add(titleLbl, BorderLayout.NORTH);
            card.add(descLbl, BorderLayout.CENTER);
            card.add(footer, BorderLayout.SOUTH);

            grid.add(card);
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_APP);
        add(scroll, BorderLayout.CENTER);

        JPanel tips = Theme.createCard(null);
        JLabel tipLabel = new JLabel(
                "<html><b>Dynamic Input &amp; Testing Ready:</b> Every module supports arbitrary-size datasets (add/remove rows or paste variable-length lists), "
                        + "includes <i>Load Small Sample</i> &amp; <i>Load Large Dataset</i> buttons, and performs full input validation without crashing.</html>");
        tipLabel.setFont(Theme.FONT_BODY);
        tipLabel.setForeground(Theme.TEXT_SECONDARY);
        tips.add(tipLabel, BorderLayout.CENTER);
        add(tips, BorderLayout.SOUTH);
    }
}
