package ossim.ui.components;

import ossim.model.CpuModels.GanttSlice;
import ossim.ui.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom Java2D Gantt chart visualizer for CPU Scheduling.
 * Automatically scales horizontally and supports scrolling for large datasets.
 */
public class GanttChartPanel extends JPanel {

    private List<GanttSlice> slices = Collections.emptyList();
    private final Map<String, Color> colorMap = new LinkedHashMap<>();

    public GanttChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(700, 120));
    }

    public void setSlices(List<GanttSlice> slices) {
        this.slices = slices != null ? slices : Collections.emptyList();
        this.colorMap.clear();
        int idx = 0;
        for (GanttSlice s : this.slices) {
            if (!s.isIdle() && !colorMap.containsKey(s.getLabel())) {
                colorMap.put(s.getLabel(), Theme.colorForIndex(idx++));
            }
        }
        int totalTime = this.slices.isEmpty() ? 0 : this.slices.get(this.slices.size() - 1).getEndTime();
        int minSliceWidth = this.slices.size() * 44;
        int prefWidth = Math.max(680, Math.max(minSliceWidth, totalTime * 22) + 40);
        setPreferredSize(new Dimension(prefWidth, 120));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (slices.isEmpty()) {
            g2.setFont(Theme.FONT_BODY);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString("Run a CPU scheduling simulation to view the Gantt Chart.", 20, getHeight() / 2);
            g2.dispose();
            return;
        }

        int padX = 24;
        int barY = 26;
        int barH = 48;
        int availW = Math.max(getWidth() - padX * 2, slices.size() * 42);
        int totalDuration = Math.max(1, slices.get(slices.size() - 1).getEndTime());

        // Compute proportional pixel widths while enforcing a readable minimum width per slice
        int[] widths = new int[slices.size()];
        int minW = 36;
        int sumMin = slices.size() * minW;
        if (availW <= sumMin) {
            for (int i = 0; i < slices.size(); i++) {
                widths[i] = minW;
            }
        } else {
            double extra = availW - sumMin;
            for (int i = 0; i < slices.size(); i++) {
                double frac = (double) slices.get(i).getDuration() / totalDuration;
                widths[i] = minW + (int) Math.round(extra * frac);
            }
        }

        FontMetrics fmBold = g2.getFontMetrics(Theme.FONT_BODY_BOLD);
        FontMetrics fmSmall = g2.getFontMetrics(Theme.FONT_METRIC_LABEL);

        int x = padX;
        g2.setFont(Theme.FONT_METRIC_LABEL);
        g2.setColor(Theme.TEXT_SECONDARY);
        String startStr = String.valueOf(slices.get(0).getStartTime());
        g2.drawString(startStr, x - fmSmall.stringWidth(startStr) / 2, barY + barH + 18);

        for (int i = 0; i < slices.size(); i++) {
            GanttSlice s = slices.get(i);
            int w = widths[i];

            Color fill = s.isIdle()
                    ? new Color(226, 232, 240)
                    : colorMap.getOrDefault(s.getLabel(), Theme.PRIMARY);

            g2.setColor(fill);
            g2.fillRect(x, barY, w, barH);
            g2.setColor(Color.WHITE);
            g2.drawRect(x, barY, w, barH);

            // Label inside slice
            g2.setFont(Theme.FONT_BODY_BOLD);
            String label = s.getLabel();
            int labelW = fmBold.stringWidth(label);
            g2.setColor(s.isIdle() ? Theme.TEXT_SECONDARY : Color.WHITE);
            if (labelW <= w - 6) {
                g2.drawString(label, x + (w - labelW) / 2, barY + barH / 2 + 5);
            } else if (w >= 20) {
                g2.drawString(label.substring(0, Math.min(3, label.length())), x + 4, barY + barH / 2 + 5);
            }

            // Timestamp tick at end of slice
            g2.setFont(Theme.FONT_METRIC_LABEL);
            g2.setColor(Theme.TEXT_SECONDARY);
            int endX = x + w;
            g2.drawLine(endX, barY + barH, endX, barY + barH + 4);
            String endStr = String.valueOf(s.getEndTime());
            g2.drawString(endStr, endX - fmSmall.stringWidth(endStr) / 2, barY + barH + 18);

            x = endX;
        }

        g2.dispose();
    }
}
