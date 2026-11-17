package ossim.ui;

import ossim.ui.Theme.RoundedButton;
import ossim.ui.Theme.RoundedPanel;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main application window styled after the Operating System Workspace UI:
 * dark OS top bar, warm stone sidebar with rounded pills, and floating bottom dock.
 */
public class MainFrame extends JFrame {

    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("hh:mm a  •  EEE, MMM d");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final Map<String, RoundedButton> navButtons = new LinkedHashMap<>();
    private final Map<String, RoundedButton> dockButtons = new LinkedHashMap<>();
    private final JLabel statusLabel = new JLabel("Active: Overview");
    private final JLabel clockLabel = new JLabel();

    public MainFrame() {
        super("Operating System | WORKSPACE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));
        setPreferredSize(new Dimension(1340, 860));
        setLayout(new BorderLayout());

        // 1. Dark OS Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG_TOPBAR);
        topBar.setPreferredSize(new Dimension(1200, 46));
        topBar.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 18));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 1));
        topLeft.setOpaque(false);

        RoundedPanel appBadge = new RoundedPanel(new BorderLayout(), 9, Theme.PRIMARY, null);
        appBadge.setPreferredSize(new Dimension(28, 28));
        JLabel badgeText = new JLabel("OS", SwingConstants.CENTER);
        badgeText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badgeText.setForeground(Color.WHITE);
        appBadge.add(badgeText, BorderLayout.CENTER);

        JLabel titleLbl = new JLabel("Operating System");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);

        JLabel workspaceLbl = new JLabel("|   WORKSPACE");
        workspaceLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        workspaceLbl.setForeground(new Color(168, 162, 158));

        topLeft.add(appBadge);
        topLeft.add(titleLbl);
        topLeft.add(workspaceLbl);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 4));
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
        navHeader.setBorder(BorderFactory.createEmptyBorder(0, 10, 6, 0));
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
        modulesHeader.setBorder(BorderFactory.createEmptyBorder(0, 10, 6, 0));
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
        footerInfo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        footerInfo.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(footerInfo);

        // 3. Floating Rounded Bottom Dock
        JPanel dockWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        dockWrapper.setBackground(Theme.BG_APP);

        RoundedPanel dockPill = new RoundedPanel(new FlowLayout(FlowLayout.CENTER, 6, 4), 20, Color.WHITE, Theme.BORDER_LIGHT);
        dockPill.setBorder(BorderFactory.createEmptyBorder(3, 10, 5, 10));

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
        RoundedButton btn = new RoundedButton(label, 10, Theme.BG_SIDEBAR, Theme.BG_SIDEBAR_HOVER, Theme.TEXT_PRIMARY, null);
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setPreferredSize(new Dimension(205, 36));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 10));
        btn.setAlignmentX(LEFT_ALIGNMENT);

        btn.addActionListener(e -> navigateTo(key));
        navButtons.put(key, btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(3));
    }

    private void addDockButton(JPanel dock, String key, String label) {
        RoundedButton btn = new RoundedButton(label, 12, Theme.BG_SUBTLE, Theme.BG_SIDEBAR_HOVER, Theme.TEXT_SECONDARY, null);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        btn.addActionListener(e -> navigateTo(key));
        dockButtons.put(key, btn);
        dock.add(btn);
    }

    public void navigateTo(String key) {
        cardLayout.show(contentCards, key);
        for (Map.Entry<String, RoundedButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            RoundedButton b = entry.getValue();
            if (active) {
                b.setColors(Theme.BG_SIDEBAR_ACTIVE, Theme.BG_SIDEBAR_ACTIVE, Theme.PRIMARY_DARK, new Color(245, 194, 180));
                statusLabel.setText("Active: " + b.getText());
            } else {
                b.setColors(Theme.BG_SIDEBAR, Theme.BG_SIDEBAR_HOVER, Theme.TEXT_PRIMARY, null);
            }
        }
        for (Map.Entry<String, RoundedButton> entry : dockButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            RoundedButton b = entry.getValue();
            if (active) {
                b.setColors(Theme.PRIMARY, Theme.PRIMARY_DARK, Color.WHITE, null);
            } else {
                b.setColors(Theme.BG_SUBTLE, Theme.BG_SIDEBAR_HOVER, Theme.TEXT_SECONDARY, null);
            }
        }
    }

    private static final class BankerPanelHolder {
        BankersPanel create() {
            return new BankersPanel();
        }
    }
}
