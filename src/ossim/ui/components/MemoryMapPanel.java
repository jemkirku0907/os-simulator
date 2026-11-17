package ossim.ui.components;

import ossim.ui.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable proportional memory layout diagram used by Fragmentation,
 * Segmentation, Paging, and Multiprogramming modules.
 */
public class MemoryMapPanel extends JPanel {

    public static final class BlockSegment {
        private final String title;
        private final String subLabel;
        private final long size;
        private final long wastedSize; // e.g., internal fragmentation inside a block
        private final Color color;
        private final boolean free;

        public BlockSegment(String title, String subLabel, long size, long wastedSize, Color color, boolean free) {
            this.title = title;
            this.subLabel = subLabel;
            this.size = size;
            this.wastedSize = wastedSize;
            this.color = color;
            this.free = free;
        }
    }

    private List<BlockSegment> segments = new ArrayList<>();
    private String emptyMessage = "Run simulation to view memory layout.";

    public MemoryMapPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(680, 115));
    }

    public void setSegments(List<BlockSegment> segments, String emptyMessage) {
        this.segments = segments != null ? segments : new ArrayList<>();
        if (emptyMessage != null) {
            this.emptyMessage = emptyMessage;
        }
        int minWidth = Math.max(660, this.segments.size() * 68 + 40);
        setPreferredSize(new Dimension(minWidth, 115));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (segments.isEmpty()) {
            g2.setFont(Theme.FONT_BODY);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString(emptyMessage, 18, getHeight() / 2);
            g2.dispose();
            return;
        }

        long total = 0;
        for (BlockSegment seg : segments) {
            total += Math.max(1, seg.size);
        }

        int padX = 18;
        int barY = 20;
        int barH = 54;
        int availW = Math.max(getWidth() - padX * 2, segments.size() * 54);

        int minW = 46;
        int[] widths = new int[segments.size()];
        int sumMin = segments.size() * minW;
        if (availW <= sumMin) {
            for (int i = 0; i < segments.size(); i++) {
                widths[i] = minW;
            }
        } else {
            double extra = availW - sumMin;
            for (int i = 0; i < segments.size(); i++) {
                double frac = Math.max(1, segments.get(i).size) / (double) total;
                widths[i] = minW + (int) Math.round(extra * frac);
            }
        }

        FontMetrics fmBold = g2.getFontMetrics(Theme.FONT_BODY_BOLD);
        FontMetrics fmSmall = g2.getFontMetrics(Theme.FONT_METRIC_LABEL);

        int x = padX;
        for (int i = 0; i < segments.size(); i++) {
            BlockSegment seg = segments.get(i);
            int w = widths[i];

            if (seg.free) {
                g2.setColor(new Color(241, 245, 249));
                g2.fillRect(x, barY, w, barH);
                g2.setColor(Theme.BORDER_LIGHT);
                g2.drawRect(x, barY, w, barH);
            } else {
                // Draw used portion and internal fragmentation portion if any
                int wastedW = 0;
                if (seg.wastedSize > 0 && seg.size > 0) {
                    wastedW = Math.max(6, (int) Math.round((seg.wastedSize / (double) seg.size) * w));
                    wastedW = Math.min(w - 6, wastedW);
                }
                int usedW = w - wastedW;

                g2.setColor(seg.color);
                g2.fillRect(x, barY, usedW, barH);

                if (wastedW > 0) {
                    g2.setColor(new Color(254, 215, 170)); // Amber tint for internal fragmentation
                    g2.fillRect(x + usedW, barY, wastedW, barH);
                    g2.setColor(Theme.WARNING);
                    g2.drawLine(x + usedW, barY, x + usedW, barY + barH);
                }

                g2.setColor(Color.WHITE);
                g2.drawRect(x, barY, w, barH);
            }

            // Title inside box
            g2.setFont(Theme.FONT_BODY_BOLD);
            g2.setColor(seg.free ? Theme.TEXT_SECONDARY : Color.WHITE);
            String title = seg.title;
            int tw = fmBold.stringWidth(title);
            if (tw <= w - 6) {
                g2.drawString(title, x + (w - tw) / 2, barY + 24);
            } else if (w >= 26) {
                g2.drawString(title.substring(0, Math.min(4, title.length())), x + 4, barY + 24);
            }

            // SubLabel below title or beneath bar
            g2.setFont(Theme.FONT_METRIC_LABEL);
            g2.setColor(seg.free ? Theme.TEXT_MUTED : new Color(248, 250, 252));
            String sub = seg.subLabel;
            int sw = fmSmall.stringWidth(sub);
            if (sw <= w - 6) {
                g2.drawString(sub, x + (w - sw) / 2, barY + 42);
            }

            // Size footer beneath bar
            g2.setColor(Theme.TEXT_SECONDARY);
            String sizeStr = String.valueOf(seg.size);
            if (fmSmall.stringWidth(sizeStr) <= w) {
                g2.drawString(sizeStr, x + (w - fmSmall.stringWidth(sizeStr)) / 2, barY + barH + 16);
            }

            x += w;
        }

        g2.dispose();
    }
}
