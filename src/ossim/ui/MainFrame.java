package ossim.ui;

import ossim.ui.panels.BankersPanel;
import ossim.ui.panels.CpuSchedulingPanel;
import ossim.ui.panels.DiskSchedulingPanel;
import ossim.ui.panels.FragmentationPanel;
import ossim.ui.panels.MultiprogrammingPanel;
import ossim.ui.panels.OverviewPanel;
import ossim.ui.panels.PageReplacementPanel;
import ossim.ui.panels.PagingPanel;
import ossim.ui.panels.SegmentationPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main application window housing the sidebar navigation and all 8 OS Simulator modules.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final JLabel statusLabel = new JLabel("Ready — Select a module from the sidebar or dashboard.");

    public MainFrame() {
        super("Operating System Simulator — Java Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setPreferredSize(new Dimension(1280, 820));
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(235, 800));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

        JLabel brandTitle = new JLabel("OS SIMULATOR");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel brandSub = new JLabel("Interactive Java Swing Suite");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        brandSub.setForeground(new Color(148, 163, 184));
        brandSub.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(brandTitle);
        sidebar.add(brandSub);
        sidebar.add(Box.createVerticalStrut(18));

        // Register cards
        contentCards.add(new OverviewPanel(this::navigateTo), "overview");
        contentCards.add(new CpuSchedulingPanel(), "cpu");
        contentCards.add(new BankerPanelHolder().create(), "banker");
        contentCards.add(new PagingPanel(), "paging");
        contentCards.add(new PageReplacementPanel(), "pagerep");
        contentCards.add(new SegmentationPanel(), "segment");
        contentCards.add(new FragmentationPanel(), "frag");
        contentCards.add(new DiskSchedulingPanel(), "disk");
        contentCards.add(new MultiprogrammingPanel(), "multi");

        addNavButton(sidebar, "overview", "Overview Dashboard");
        sidebar.add(Box.createVerticalStrut(10));
        JLabel modulesHeader = new JLabel("REQUIRED MODULES");
        modulesHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        modulesHeader.setForeground(new Color(100, 116, 139));
        modulesHeader.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(modulesHeader);
        sidebar.add(Box.createVerticalStrut(6));

        addNavButton(sidebar, "cpu", "1. CPU Scheduling");
        addNavButton(sidebar, "banker", "2. Banker's Algorithm");
        addNavButton(sidebar, "paging", "3. Paging");
        addNavButton(sidebar, "pagerep", "4. Page Replacement");
        addNavButton(sidebar, "segment", "5. Segmentation");
        addNavButton(sidebar, "frag", "6. Fragmentation");
        addNavButton(sidebar, "disk", "7. Disk Scheduling");
        addNavButton(sidebar, "multi", "8. Multiprogramming");

        sidebar.add(Box.createVerticalGlue());
        JLabel footerInfo = new JLabel("<html><font color='#94a3b8'>Dynamic Inputs • Validated<br>Java Swing • 8 Modules</font></html>");
        footerInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerInfo.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(footerInfo);

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.BG_CARD);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        statusLabel.setFont(Theme.FONT_METRIC_LABEL);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        statusBar.add(statusLabel, BorderLayout.WEST);

        add(sidebar, BorderLayout.WEST);
        add(contentCards, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        navigateTo("overview");
        pack();
        setLocationRelativeTo(null);
    }

    private void addNavButton(JPanel sidebar, String key, String label) {
        JButton btn = new JButton(label);
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setPreferredSize(new Dimension(210, 38));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBackground(Theme.BG_SIDEBAR);
        btn.setForeground(Theme.TEXT_ON_DARK);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setAlignmentX(LEFT_ALIGNMENT);

        btn.addActionListener(e -> navigateTo(key));
        navButtons.put(key, btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(4));
    }

    public void navigateTo(String key) {
        cardLayout.show(contentCards, key);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            JButton b = entry.getValue();
            b.setBackground(active ? Theme.BG_SIDEBAR_ACTIVE : Theme.BG_SIDEBAR);
            b.setForeground(active ? Color.WHITE : Theme.TEXT_ON_DARK);
            if (active) {
                statusLabel.setText("Active Module: " + b.getText());
            }
        }
    }

    private static final class BankerPanelHolder {
        BankersPanel create() {
            return new BankersPanel();
        }
    }
}
