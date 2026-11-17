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
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main application window styled after the Operating System Workspace UI:
 * dark OS top bar, warm stone sidebar, and bottom quick-launch dock.
 */
public class MainFrame extends JFrame {

    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("hh:mm a  •  EEE, MMM d");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final Map<String, JButton> dockButtons = new LinkedHashMap<>();
    private final JLabel statusLabel = new JLabel("Active: Overview Dashboard");
    private final JLabel clockLabel = new JLabel();

    public MainFrame() {
        super("Operating System | WORKSPACE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1140, 740));
        setPreferredSize(new Dimension(1300, 840));
        setLayout(new BorderLayout());

        // 1. Dark OS Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG_TOPBAR);
        topBar.setPreferredSize(new Dimension(1200, 44));
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 16));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        topLeft.setOpaque(false);

        JLabel appBadge = new JLabel("OS", SwingConstants.CENTER);
        appBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        appBadge.setOpaque(true);
        appBadge.setBackground(Theme.PRIMARY);
        appBadge.setForeground(Color.WHITE);
        appBadge.setPreferredSize(new Dimension(26, 26));

        JLabel titleLbl = new JLabel("Operating System");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);

        JLabel workspaceLbl = new JLabel("|   WORKSPACE");
        workspaceLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        workspaceLbl.setForeground(new Color(168, 162, 158));

        topLeft.add(appBadge);
        topLeft.add(titleLbl);
        topLeft.add(workspaceLbl);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 3));
        topRight.setOpaque(false);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(168, 162, 158));

        JLabel readyDot = new JLabel("● All systems normal");
        readyDot.setFont(new Font("Segoe UI", Font.BOLD, 11));
        readyDot.setForeground(new Color(74, 222, 128));

        clockLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clockLabel.setForeground(Theme.TEXT_ON_DARK);
        updateClock();
        new Timer(15000, e -> updateClock()).start();

        topRight.add(statusLabel);
        topRight.add(readyDot);
        topRight.add(clockLabel);

        topBar.add(topLeft, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        // 2. Warm Stone Left Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(228, 760));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)));

        JLabel navHeader = new JLabel("WORKSPACE");
        navHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navHeader.setForeground(Theme.TEXT_MUTED);
        navHeader.setBorder(BorderFactory.createEmptyBorder(0, 8, 6, 0));
        navHeader.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(navHeader);

        // Register cards
        contentCards.setBackground(Theme.BG_APP);
        contentCards.add(new OverviewPanel(this::navigateTo), "overview");
        contentCards.add(new CpuSchedulingPanel(), "cpu");
        contentCards.add(new BankerPanelHolder().create(), "banker");
        contentCards.add(new PagingPanel(), "paging");
        contentCards.add(new PageReplacementPanel(), "pagerep");
        contentCards.add(new SegmentationPanel(), "segment");
        contentCards.add(new FragmentationPanel(), "frag");
        contentCards.add(new DiskSchedulingPanel(), "disk");
        contentCards.add(new MultiprogrammingPanel(), "multi");

        addNavButton(sidebar, "overview", "Overview");
        sidebar.add(Box.createVerticalStrut(10));

        JLabel modulesHeader = new JLabel("OS SIMULATOR MODULES");
        modulesHeader.setFont(new Font("Segoe UI", Font.BOLD, 10));
        modulesHeader.setForeground(Theme.TEXT_MUTED);
        modulesHeader.setBorder(BorderFactory.createEmptyBorder(0, 8, 6, 0));
        modulesHeader.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(modulesHeader);

        addNavButton(sidebar, "cpu", "1. CPU Scheduling");
        addNavButton(sidebar, "banker", "2. Banker's Algorithm");
        addNavButton(sidebar, "paging", "3. Paging");
        addNavButton(sidebar, "pagerep", "4. Page Replacement");
        addNavButton(sidebar, "segment", "5. Segmentation");
        addNavButton(sidebar, "frag", "6. Fragmentation");
        addNavButton(sidebar, "disk", "7. Disk Scheduling");
        addNavButton(sidebar, "multi", "8. Multiprogramming");

        sidebar.add(Box.createVerticalGlue());
        JLabel footerInfo = new JLabel("<html><font color='#78716c'>Java Swing • 8 Modules<br>Dynamic Inputs • Validated</font></html>");
        footerInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerInfo.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        footerInfo.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(footerInfo);

        // 3. Bottom Floating-Style Dock
        JPanel dockWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        dockWrapper.setBackground(Theme.BG_APP);
        dockWrapper.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_LIGHT));

        JPanel dockPill = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        dockPill.setBackground(Color.WHITE);
        dockPill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        addDockButton(dockPill, "overview", "Overview");
        addDockButton(dockPill, "cpu", "CPU");
        addDockButton(dockPill, "banker", "Banker");
        addDockButton(dockPill, "paging", "Paging");
        addDockButton(dockPill, "pagerep", "PageRep");
        addDockButton(dockPill, "segment", "Segment");
        addDockButton(dockPill, "frag", "Frag");
        addDockButton(dockPill, "disk", "Disk");
        addDockButton(dockPill, "multi", "Multi");

        dockWrapper.add(dockPill);

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentCards, BorderLayout.CENTER);
        add(dockWrapper, BorderLayout.SOUTH);

        navigateTo("overview");
        pack();
        setLocationRelativeTo(null);
    }

    private void updateClock() {
        clockLabel.setText(LocalDateTime.now().format(CLOCK_FMT));
    }

    private void addNavButton(JPanel sidebar, String key, String label) {
        JButton btn = new JButton(label);
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setPreferredSize(new Dimension(205, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setOpaque(true);
        btn.setBackground(Theme.BG_SIDEBAR);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(LEFT_ALIGNMENT);

        btn.addActionListener(e -> navigateTo(key));
        navButtons.put(key, btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(3));
    }

    private void addDockButton(JPanel dock, String key, String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBackground(Theme.BG_SUBTLE);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 11, 6, 11));
        btn.addActionListener(e -> navigateTo(key));
        dockButtons.put(key, btn);
        dock.add(btn);
    }

    public void navigateTo(String key) {
        cardLayout.show(contentCards, key);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            JButton b = entry.getValue();
            b.setBackground(active ? Theme.BG_SIDEBAR_ACTIVE : Theme.BG_SIDEBAR);
            b.setForeground(active ? Theme.PRIMARY_DARK : Theme.TEXT_PRIMARY);
            b.setBorder(active
                    ? BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.PRIMARY),
                            BorderFactory.createEmptyBorder(7, 10, 7, 10))
                    : BorderFactory.createEmptyBorder(7, 13, 7, 10));
            if (active) {
                statusLabel.setText("Active: " + b.getText());
            }
        }
        for (Map.Entry<String, JButton> entry : dockButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            JButton b = entry.getValue();
            b.setBackground(active ? Theme.PRIMARY : Theme.BG_SUBTLE);
            b.setForeground(active ? Color.WHITE : Theme.TEXT_SECONDARY);
        }
    }

    private static final class BankerPanelHolder {
        BankersPanel create() {
            return new BankersPanel();
        }
    }
}
