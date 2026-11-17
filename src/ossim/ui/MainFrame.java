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
 * Main application window with top bar, sidebar navigation, and module cards.
 */
public class MainFrame extends JFrame {

    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("hh:mm a  •  EEE, MMM d");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final Map<String, RoundedButton> navButtons = new LinkedHashMap<>();
    private final JLabel clockLabel = new JLabel();

    public MainFrame() {
        super("Operating System Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 680));
        setPreferredSize(new Dimension(1240, 780));
        setLayout(new BorderLayout());

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG_TOPBAR);
        topBar.setPreferredSize(new Dimension(1200, 44));
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 18));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 1));
        topLeft.setOpaque(false);

        RoundedPanel appBadge = new RoundedPanel(new BorderLayout(), 9, Theme.PRIMARY, null);
        appBadge.setPreferredSize(new Dimension(28, 28));
        JLabel badgeText = new JLabel("OS", SwingConstants.CENTER);
        badgeText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badgeText.setForeground(Color.WHITE);
        appBadge.add(badgeText, BorderLayout.CENTER);

        JLabel titleLbl = new JLabel("Operating System Simulator");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);

        topLeft.add(appBadge);
        topLeft.add(titleLbl);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        topRight.setOpaque(false);
        clockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clockLabel.setForeground(new Color(214, 211, 209));
        updateClock();
        new Timer(15000, e -> updateClock()).start();
        topRight.add(clockLabel);

        topBar.add(topLeft, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        // Left Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 720));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)));

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

        JLabel modulesHeader = new JLabel("MODULES");
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

        add(topBar, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(contentCards, BorderLayout.CENTER);

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
        btn.setPreferredSize(new Dimension(198, 36));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 10));
        btn.setAlignmentX(LEFT_ALIGNMENT);

        btn.addActionListener(e -> navigateTo(key));
        navButtons.put(key, btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(3));
    }

    public void navigateTo(String key) {
        cardLayout.show(contentCards, key);
        for (Map.Entry<String, RoundedButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            RoundedButton b = entry.getValue();
            if (active) {
                b.setColors(Theme.BG_SIDEBAR_ACTIVE, Theme.BG_SIDEBAR_ACTIVE, Theme.PRIMARY_DARK, new Color(245, 194, 180));
            } else {
                b.setColors(Theme.BG_SIDEBAR, Theme.BG_SIDEBAR_HOVER, Theme.TEXT_PRIMARY, null);
            }
        }
    }

    private static final class BankerPanelHolder {
        BankersPanel create() {
            return new BankersPanel();
        }
    }
}
