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
 * Welcome / System Overview dashboard panel styled after the Operating System Workspace UI.
 */
public class OverviewPanel extends JPanel {

    private static final String[][] MODULE_CARDS = {
            { "cpu", "CPU", "1. CPU Scheduling",
                    "FCFS, SJF, SRTF, Priority (Preemptive & Non-Preemptive), Round Robin. Interactive Gantt Chart, WT, TAT, RT & comparison." },
            { "banker", "BNK", "2. Banker's Algorithm",
                    "Dynamic P x R matrices, Need matrix computation, step-by-step Safety check, safe sequence & Resource Request." },
            { "paging", "PAG", "3. Paging",
                    "Dynamic Page Table, frame mapping visualization, single & batch Logical-to-Physical translation & fault detection." },
            { "pagerep", "REP", "4. Page Replacement",
                    "FIFO, LRU, Optimal (OPT), and Clock/Second Chance. Variable-length reference strings, frame grid & comparison." },
            { "segment", "SEG", "5. Segmentation",
                    "Dynamic Segment Table (Base & Limit), physical memory overlap detection, bounds validation & trap diagnostics." },
            { "frag", "FRG", "6. Fragmentation",
                    "Fixed & Dynamic partitioning with First Fit, Best Fit, Worst Fit, Next Fit. Visualizes internal & external fragmentation." },
            { "disk", "DSK", "7. Disk Scheduling",
                    "FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK. Interactive head-movement trajectory chart, seek distances & comparison." },
            { "multi", "MLT", "8. Multiprogramming",
                    "Multi-core RAM + CPU/I/O overlap simulator with live step/play controls, utilization dashboard & baseline comparison." }
    };

    public OverviewPanel(Consumer<String> navigator) {
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        // System Overview Header matching the Workspace aesthetic
        JPanel header = new JPanel(new BorderLayout(8, 4));
        header.setOpaque(false);

        JPanel titleStack = new JPanel(new GridLayout(3, 1, 0, 2));
        titleStack.setOpaque(false);

        JLabel eyebrow = new JLabel("SYSTEM OVERVIEW");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(Theme.PRIMARY);

        JLabel headline = new JLabel("Operating System Simulator.");
        headline.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headline.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("All 8 core OS modules are loaded with unlimited dynamic inputs, validation, and visual charts.");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        titleStack.add(eyebrow);
        titleStack.add(headline);
        titleStack.add(subtitle);

        RoundedPanel statusPill = new RoundedPanel(new BorderLayout(4, 2), 14, Color.WHITE, Theme.BORDER_LIGHT);
        statusPill.setBorder(BorderFactory.createEmptyBorder(8, 14, 10, 14));
        JLabel statusTitle = new JLabel("●  All systems normal");
        statusTitle.setFont(Theme.FONT_BODY_BOLD);
        statusTitle.setForeground(Theme.SUCCESS);
        JLabel statusSub = new JLabel("8/8 modules verified");
        statusSub.setFont(Theme.FONT_METRIC_LABEL);
        statusSub.setForeground(Theme.TEXT_MUTED);
        statusPill.add(statusTitle, BorderLayout.NORTH);
        statusPill.add(statusSub, BorderLayout.SOUTH);

        JPanel pillWrap = new JPanel(new BorderLayout());
        pillWrap.setOpaque(false);
        pillWrap.add(statusPill, BorderLayout.NORTH);

        header.add(titleStack, BorderLayout.CENTER);
        header.add(pillWrap, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(2, 2, 12, 6));

        for (int i = 0; i < MODULE_CARDS.length; i++) {
            final String key = MODULE_CARDS[i][0];
            final String badgeCode = MODULE_CARDS[i][1];
            final String title = MODULE_CARDS[i][2];
            final String desc = MODULE_CARDS[i][3];
            Color accent = Theme.colorForIndex(i);

            RoundedPanel card = new RoundedPanel(new BorderLayout(10, 6), 14, Theme.BG_CARD, Theme.BORDER_LIGHT);
            card.setBottomAccent(accent);
            card.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

            // Card Top: soft-tinted icon badge + title
            JPanel cardTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            cardTop.setOpaque(false);

            RoundedPanel iconBadge = new RoundedPanel(new BorderLayout(), 10, Theme.softTint(accent), null);
            iconBadge.setPreferredSize(new Dimension(34, 34));
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
                    "Open Module  ->", 10, accent, accent.darker(), Color.WHITE, null);
            openBtn.setFont(Theme.FONT_BODY_BOLD);
            openBtn.setBorder(BorderFactory.createEmptyBorder(6, 13, 6, 13));
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
