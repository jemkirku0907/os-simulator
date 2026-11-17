package ossim.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Shared modern visual theme styled after the Operating System Workspace UI:
 * rounded cards with soft shadows, pill buttons, thin modern scrollbars,
 * warm stone/cream canvas, dark OS top bar, and coral-orange accents.
 */
public final class Theme {

    private Theme() {
    }

    public static final Color BG_TOPBAR = new Color(28, 27, 26);
    public static final Color BG_APP = new Color(244, 240, 234);
    public static final Color BG_SIDEBAR = new Color(238, 234, 227);
    public static final Color BG_SIDEBAR_HOVER = new Color(228, 223, 214);
    public static final Color BG_SIDEBAR_ACTIVE = new Color(252, 232, 225);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SUBTLE = new Color(249, 247, 244);

    public static final Color BORDER_LIGHT = new Color(226, 220, 211);
    public static final Color BORDER_FOCUS = new Color(232, 93, 63);
    public static final Color SHADOW_SOFT = new Color(28, 25, 23, 16);

    public static final Color TEXT_PRIMARY = new Color(28, 25, 23);
    public static final Color TEXT_SECONDARY = new Color(87, 83, 78);
    public static final Color TEXT_MUTED = new Color(120, 113, 108);
    public static final Color TEXT_ON_DARK = new Color(245, 245, 244);

    public static final Color PRIMARY = new Color(232, 93, 63);       // Coral-Orange
    public static final Color PRIMARY_DARK = new Color(194, 65, 36);  // Deep Coral
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color SUCCESS_BG = new Color(220, 252, 231);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color DANGER_BG = new Color(254, 226, 226);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color WARNING_BG = new Color(254, 243, 199);
    public static final Color INFO_BG = new Color(252, 234, 228);

    public static final Color[] SERIES_COLORS = new Color[] {
            new Color(232, 93, 63),   // Coral-Orange
            new Color(124, 58, 237),  // Violet
            new Color(13, 148, 136),  // Teal
            new Color(59, 130, 246),  // Blue
            new Color(245, 158, 11),  // Amber
            new Color(16, 185, 129),  // Emerald
            new Color(236, 72, 153),  // Pink
            new Color(99, 102, 241),  // Indigo
            new Color(249, 115, 22),  // Orange
            new Color(6, 182, 212)    // Cyan
    };

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_MONO_BOLD = new Font("Consolas", Font.BOLD, 12);
    public static final Font FONT_METRIC_VAL = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_METRIC_LABEL = new Font("Segoe UI", Font.PLAIN, 11);

    public static Color colorForIndex(int idx) {
        return SERIES_COLORS[Math.floorMod(idx, SERIES_COLORS.length)];
    }

    public static Color softTint(Color c) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 28);
    }

    /**
     * Rounded card panel with soft drop-shadow, optional bottom accent stripe, and clean anti-aliased borders.
     */
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private Color fillColor;
        private Color borderColor;
        private Color bottomAccent;

        public RoundedPanel(LayoutManager layout, int radius, Color fillColor, Color borderColor) {
            super(layout);
            this.radius = radius;
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        public void setFillColor(Color fillColor) {
            this.fillColor = fillColor;
            repaint();
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            repaint();
        }

        public void setBottomAccent(Color bottomAccent) {
            this.bottomAccent = bottomAccent;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Soft shadow
            g2.setColor(SHADOW_SOFT);
            g2.fill(new RoundRectangle2D.Double(2, 3, w - 4, h - 4, radius, radius));

            // Card fill
            RoundRectangle2D.Double shape = new RoundRectangle2D.Double(1, 1, w - 4, h - 5, radius, radius);
            g2.setColor(fillColor);
            g2.fill(shape);

            // Optional subtle bottom accent bar clipped to rounded card
            if (bottomAccent != null) {
                g2.setClip(shape);
                g2.setColor(bottomAccent);
                g2.fillRect(1, h - 8, w - 4, 4);
                g2.setClip(null);
            }

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.draw(shape);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Modern rounded button with smooth hover highlight and zero focus rectangle artifacts.
     */
    public static class RoundedButton extends JButton {
        private final int radius;
        private Color baseBg;
        private Color hoverBg;
        private Color borderCol;
        private boolean hovered = false;

        public RoundedButton(String text, int radius, Color bg, Color hoverBg, Color fg, Color borderCol) {
            super(text);
            this.radius = radius;
            this.baseBg = bg;
            this.hoverBg = hoverBg;
            this.borderCol = borderCol;
            setForeground(fg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        public void setColors(Color bg, Color hoverBg, Color fg, Color borderCol) {
            this.baseBg = bg;
            this.hoverBg = hoverBg;
            this.borderCol = borderCol;
            setForeground(fg);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D.Double shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1.5, h - 1.5, radius, radius);
            g2.setColor(hovered ? hoverBg : baseBg);
            g2.fill(shape);
            if (borderCol != null) {
                g2.setColor(borderCol);
                g2.draw(shape);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Thin, modern rounded scrollbar replacing bulky classic Swing arrow bars.
     */
    public static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(196, 189, 179);
            this.thumbHighlightColor = new Color(168, 162, 158);
            this.trackColor = BG_APP;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(BG_APP);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? new Color(168, 162, 158) : new Color(205, 198, 188));
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                    thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }
    }

    public static void styleScrollPane(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
    }

    public static Border cardBorder() {
        return BorderFactory.createEmptyBorder(12, 15, 14, 15);
    }

    public static JPanel createCard(String title) {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(8, 8), 14, BG_CARD, BORDER_LIGHT);
        panel.setBorder(cardBorder());
        if (title != null && !title.isEmpty()) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(FONT_SECTION);
            lbl.setForeground(TEXT_PRIMARY);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            panel.add(lbl, BorderLayout.NORTH);
        }
        return panel;
    }

    public static JPanel createModuleHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(4, 2));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 2));
        JLabel t = new JLabel(title);
        t.setFont(FONT_TITLE);
        t.setForeground(TEXT_PRIMARY);
        JLabel s = new JLabel(subtitle);
        s.setFont(FONT_SUBTITLE);
        s.setForeground(TEXT_SECONDARY);
        header.add(t, BorderLayout.NORTH);
        header.add(s, BorderLayout.SOUTH);
        return header;
    }

    public static JButton createPrimaryButton(String text) {
        RoundedButton btn = new RoundedButton(text, 10, PRIMARY, PRIMARY_DARK, Color.WHITE, null);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        RoundedButton btn = new RoundedButton(text, 10, BG_SUBTLE, BG_SIDEBAR_HOVER, TEXT_PRIMARY, BORDER_LIGHT);
        btn.setFont(FONT_BODY);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        RoundedButton btn = new RoundedButton(
                text, 10, DANGER_BG, new Color(254, 202, 202), DANGER, new Color(252, 165, 165));
        btn.setFont(FONT_BODY);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }

    public static JTextField createTextField(String initialText, int columns) {
        JTextField tf = new JTextField(initialText, columns);
        tf.setFont(FONT_BODY);
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return tf;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(27);
        table.setGridColor(BORDER_LIGHT);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(INFO_BG);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(BG_SUBTLE);
        header.setForeground(TEXT_PRIMARY);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }

    public static JScrollPane wrapTable(JTable table, int prefWidth, int prefHeight) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.setPreferredSize(new Dimension(prefWidth, prefHeight));
        styleScrollPane(scroll);
        return scroll;
    }

    public static JPanel createMetricCard(String label, JLabel valueLabel, Color accent) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(2, 2), 12, BG_CARD, BORDER_LIGHT);
        card.setBottomAccent(accent);
        card.setBorder(BorderFactory.createEmptyBorder(9, 12, 11, 12));
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_METRIC_LABEL);
        lbl.setForeground(TEXT_MUTED);
        valueLabel.setFont(FONT_METRIC_VAL);
        valueLabel.setForeground(TEXT_PRIMARY);
        card.add(lbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public static void commitTableEdits(JTable table) {
        if (table != null && table.isEditing() && table.getCellEditor() != null) {
            table.getCellEditor().stopCellEditing();
        }
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Input Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
