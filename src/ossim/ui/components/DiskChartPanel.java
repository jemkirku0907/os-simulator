package ossim.ui.components;

import ossim.model.DiskModels.DiskResult;
import ossim.model.DiskModels.SeekStep;
import ossim.ui.Theme;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.List;

/**
 * Custom Java2D visualizer for Disk Scheduling head movement across cylinders.
 */
public class DiskChartPanel extends JPanel {

    private DiskResult result;

    public DiskChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(620, 320));
    }

    public void setResult(DiskResult result) {
        this.result = result;
        int steps = (result == null) ? 0 : result.getPathTracks().size();
        int prefH = Math.max(280, Math.min(2000, 75 + steps * 22));
        setPreferredSize(new Dimension(620, prefH));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (result == null || result.getPathTracks().isEmpty()) {
            g2.setFont(Theme.FONT_BODY);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString("Run Disk Scheduling to visualize head movement trajectory.", 20, 40);
            g2.dispose();
            return;
        }

        int padLeft = 40;
        int padRight = 40;
        int padTop = 44;
        int padBottom = 26;
        int plotW = Math.max(200, getWidth() - padLeft - padRight);
        int plotH = Math.max(120, getHeight() - padTop - padBottom);

        int maxTrack = Math.max(1, result.getMaxTrack());
        List<Integer> tracks = result.getPathTracks();
        List<SeekStep> steps = result.getSteps();

        // Top cylinder axis
        g2.setColor(Theme.BORDER_LIGHT);
        g2.drawLine(padLeft, padTop, padLeft + plotW, padTop);

        g2.setFont(Theme.FONT_METRIC_LABEL);
        FontMetrics fm = g2.getFontMetrics();
        int tickCount = 5;
        for (int i = 0; i <= tickCount; i++) {
            int trackVal = (int) Math.round((maxTrack * i) / (double) tickCount);
            int tx = padLeft + (int) Math.round((trackVal / (double) maxTrack) * plotW);
            g2.setColor(new Color(241, 245, 249));
            g2.drawLine(tx, padTop, tx, padTop + plotH);
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawLine(tx, padTop - 4, tx, padTop);
            String label = String.valueOf(trackVal);
            g2.drawString(label, tx - fm.stringWidth(label) / 2, padTop - 10);
        }

        int points = tracks.size();
        double stepY = points > 1 ? (double) plotH / (points - 1) : 0;

        Stroke solidStroke = new BasicStroke(2.2f);
        Stroke dashedStroke = new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, new float[] { 6.0f, 4.0f }, 0.0f);

        // Draw connecting segments
        for (int i = 0; i < points - 1; i++) {
            int x1 = padLeft + (int) Math.round((tracks.get(i) / (double) maxTrack) * plotW);
            int y1 = padTop + (int) Math.round(i * stepY);
            int x2 = padLeft + (int) Math.round((tracks.get(i + 1) / (double) maxTrack) * plotW);
            int y2 = padTop + (int) Math.round((i + 1) * stepY);

            boolean isJump = i < steps.size() && steps.get(i).isBoundaryJump();
            g2.setStroke(isJump ? dashedStroke : solidStroke);
            g2.setColor(isJump ? Theme.WARNING : Theme.PRIMARY);
            g2.drawLine(x1, y1, x2, y2);
        }

        // Draw points and track labels
        g2.setStroke(solidStroke);
        g2.setFont(Theme.FONT_METRIC_LABEL);
        for (int i = 0; i < points; i++) {
            int track = tracks.get(i);
            int x = padLeft + (int) Math.round((track / (double) maxTrack) * plotW);
            int y = padTop + (int) Math.round(i * stepY);

            g2.setColor(i == 0 ? Theme.SUCCESS : Theme.PRIMARY_DARK);
            g2.fillOval(x - 5, y - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawOval(x - 5, y - 5, 10, 10);

            if (points <= 45 || i == 0 || i == points - 1 || i % Math.max(1, points / 25) == 0) {
                g2.setColor(Theme.TEXT_PRIMARY);
                String lbl = (i == 0 ? "Start:" : "") + track;
                int lblX = x + 8;
                if (lblX + fm.stringWidth(lbl) > padLeft + plotW) {
                    lblX = x - fm.stringWidth(lbl) - 8;
                }
                g2.drawString(lbl, lblX, y + 4);
            }
        }

        g2.dispose();
    }
}
