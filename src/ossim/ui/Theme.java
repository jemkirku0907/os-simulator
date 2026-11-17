package ossim.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

/**
 * Shared visual theme styled after the Operating System Workspace UI:
 * dark OS top bar, warm stone/cream canvas, crisp white cards, and coral-orange accents.
 */
public final class Theme {

    private Theme() {
    }

    public static final Color BG_TOPBAR = new Color(28, 27, 26);
    public static final Color BG_APP = new Color(245, 242, 237);
    public static final Color BG_SIDEBAR = new Color(239, 236, 230);
    public static final Color BG_SIDEBAR_HOVER = new Color(229, 224, 216);
    public static final Color BG_SIDEBAR_ACTIVE = new Color(252, 234, 228);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SUBTLE = new Color(250, 248, 245);

    public static final Color BORDER_LIGHT = new Color(226, 221, 213);
    public static final Color BORDER_FOCUS = new Color(232, 93, 63);

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
            new Color(232, 93, 63),   // Coral-Orange (CPU)
            new Color(124, 58, 237),  // Violet (Memory)
            new Color(13, 148, 136),  // Teal (Storage)
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

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14));
    }

    public static JPanel createCard(String title) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(BG_CARD);
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
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
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
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(7, 14, 7, 14));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(BG_SUBTLE);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(DANGER_BG);
        btn.setForeground(DANGER);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(252, 165, 165), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField createTextField(String initialText, int columns) {
        JTextField tf = new JTextField(initialText, columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));
        return tf;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(26);
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
        return scroll;
    }

    public static JPanel createMetricCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(2, 2));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10))));
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
