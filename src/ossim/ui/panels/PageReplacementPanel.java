package ossim.ui.panels;

import ossim.engine.PageReplacementEngine;
import ossim.model.InputValidator;
import ossim.model.PageReplacementModels.Algorithm;
import ossim.model.PageReplacementModels.ReplacementResult;
import ossim.model.PageReplacementModels.StepSnapshot;
import ossim.model.ValidationException;
import ossim.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * GUI Panel for Module 4: Page Replacement (FIFO, LRU, Optimal, Second Chance).
 */
public class PageReplacementPanel extends JPanel {

    private final PageReplacementEngine engine = new PageReplacementEngine();

    private final JTextArea refStringArea;
    private final JTextField framesField;
    private final JComboBox<Algorithm> algoCombo;

    private final DefaultTableModel stepsModel;
    private final JTable stepsTable;
    private final DefaultTableModel compareModel;
    private final JTabbedPane outputTabs;

    private final JLabel totalRefsLabel = new JLabel("-");
    private final JLabel faultsLabel = new JLabel("-");
    private final JLabel hitsLabel = new JLabel("-");
    private final JLabel hitRatioLabel = new JLabel("-");
    private final JLabel faultRatioLabel = new JLabel("-");

    public PageReplacementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        add(Theme.createModuleHeader(
                "4. Page Replacement Simulator",
                "Simulate FIFO, LRU, Optimal, and Clock/Second Chance on variable-length reference strings and arbitrary frame counts."),
                BorderLayout.NORTH);

        JPanel inputCard = Theme.createCard("Reference String & Frame Configuration");
        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controlsRow.setOpaque(false);

        algoCombo = new JComboBox<>(Algorithm.values());
        algoCombo.setFont(Theme.FONT_BODY);
        framesField = Theme.createTextField("3", 5);
        JButton runBtn = Theme.createPrimaryButton("Run Simulation");
        JButton compareBtn = Theme.createSecondaryButton("Compare All Algorithms");
        JButton smallBtn = Theme.createSecondaryButton("Small Sample (20 refs)");
        JButton beladyBtn = Theme.createSecondaryButton("Belady's Demo");
        JButton largeBtn = Theme.createSecondaryButton("Large Dataset (200 refs)");

        controlsRow.add(new JLabel("Algorithm:"));
        controlsRow.add(algoCombo);
        controlsRow.add(new JLabel("Frames:"));
        controlsRow.add(framesField);
        controlsRow.add(runBtn);
        controlsRow.add(compareBtn);
        controlsRow.add(smallBtn);
        controlsRow.add(beladyBtn);
        controlsRow.add(largeBtn);

        refStringArea = new JTextArea(3, 50);
        refStringArea.setFont(Theme.FONT_MONO);
        refStringArea.setLineWrap(true);
        refStringArea.setWrapStyleWord(true);
        JScrollPane refScroll = new JScrollPane(refStringArea);
        refScroll.setBorder(BorderFactory.createTitledBorder("Page Reference String (comma/space separated non-negative integers)"));

        inputCard.add(controlsRow, BorderLayout.NORTH);
        inputCard.add(refScroll, BorderLayout.CENTER);

        // Metrics
        JPanel metrics = new JPanel(new GridLayout(1, 5, 8, 8));
        metrics.setOpaque(false);
        metrics.add(Theme.createMetricCard("Total References", totalRefsLabel, Theme.PRIMARY));
        metrics.add(Theme.createMetricCard("Page Faults", faultsLabel, Theme.DANGER));
        metrics.add(Theme.createMetricCard("Page Hits", hitsLabel, Theme.SUCCESS));
        metrics.add(Theme.createMetricCard("Hit Ratio", hitRatioLabel, Theme.colorForIndex(5)));
        metrics.add(Theme.createMetricCard("Fault Ratio", faultRatioLabel, Theme.WARNING));

        stepsModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stepsTable = new JTable(stepsModel);
        Theme.styleTable(stepsTable);
        stepsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    int statusCol = table.getColumnCount() - 3;
                    Object statusVal = statusCol >= 0 ? table.getValueAt(row, statusCol) : null;
                    if ("HIT".equals(statusVal) && column == statusCol) {
                        c.setBackground(Theme.SUCCESS_BG);
                        c.setForeground(new Color(20, 83, 45));
                    } else if ("FAULT".equals(statusVal) && column == statusCol) {
                        c.setBackground(Theme.DANGER_BG);
                        c.setForeground(new Color(127, 29, 29));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Theme.TEXT_PRIMARY);
                    }
                }
                return c;
            }
        });

        compareModel = new DefaultTableModel(
                new Object[] { "Algorithm", "Frames", "Total Refs", "Page Hits", "Page Faults", "Hit Ratio", "Fault Ratio" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable compareTable = new JTable(compareModel);
        Theme.styleTable(compareTable);

        outputTabs = new JTabbedPane();
        outputTabs.setFont(Theme.FONT_BODY_BOLD);
        outputTabs.addTab("Step-by-Step Frame Changes", Theme.wrapTable(stepsTable, 700, 260));
        outputTabs.addTab("Algorithm Comparison", Theme.wrapTable(compareTable, 700, 260));

        JPanel outputCard = Theme.createCard("Page Replacement Trace & Statistics");
        outputCard.add(metrics, BorderLayout.NORTH);
        outputCard.add(outputTabs, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputCard, outputCard);
        split.setResizeWeight(0.32);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        runBtn.addActionListener(e -> runSimulation());
        compareBtn.addActionListener(e -> compareAll());
        smallBtn.addActionListener(e -> loadSmallSample());
        beladyBtn.addActionListener(e -> loadBeladySample());
        largeBtn.addActionListener(e -> loadLargeSample());

        loadSmallSample();
    }

    private void loadSmallSample() {
        framesField.setText("3");
        refStringArea.setText("7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1");
        runSimulation();
    }

    private void loadBeladySample() {
        framesField.setText("4");
        algoCombo.setSelectedItem(Algorithm.FIFO);
        refStringArea.setText("1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5");
        runSimulation();
    }

    private void loadLargeSample() {
        framesField.setText("8");
        Random rng = new Random(99);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(rng.nextInt(16));
        }
        refStringArea.setText(sb.toString());
        runSimulation();
    }

    private void runSimulation() {
        try {
            List<Integer> refs = InputValidator.parseIntList(
                    refStringArea.getText(), "Page Reference String", 0, Integer.MAX_VALUE);
            int frames = InputValidator.parsePositiveInt(framesField.getText(), "Frame Count");
            Algorithm algo = (Algorithm) algoCombo.getSelectedItem();

            ReplacementResult res = engine.simulate(refs, frames, algo);
            displayResult(res);
            outputTabs.setSelectedIndex(0);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private void compareAll() {
        try {
            List<Integer> refs = InputValidator.parseIntList(
                    refStringArea.getText(), "Page Reference String", 0, Integer.MAX_VALUE);
            int frames = InputValidator.parsePositiveInt(framesField.getText(), "Frame Count");
            Algorithm selected = (Algorithm) algoCombo.getSelectedItem();

            compareModel.setRowCount(0);
            ReplacementResult selectedRes = null;
            for (Algorithm algo : Algorithm.values()) {
                ReplacementResult r = engine.simulate(refs, frames, algo);
                if (algo == selected) {
                    selectedRes = r;
                }
                compareModel.addRow(new Object[] {
                        algo.getDisplayName(),
                        r.getFrameCount(),
                        r.getReferenceString().size(),
                        r.getPageHits(),
                        r.getPageFaults(),
                        String.format(Locale.US, "%.2f%%", r.getHitRatioPercent()),
                        String.format(Locale.US, "%.2f%%", r.getFaultRatioPercent())
                });
            }
            if (selectedRes != null) {
                displayResult(selectedRes);
            }
            outputTabs.setSelectedIndex(1);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private void displayResult(ReplacementResult res) {
        totalRefsLabel.setText(String.valueOf(res.getReferenceString().size()));
        faultsLabel.setText(String.valueOf(res.getPageFaults()));
        hitsLabel.setText(String.valueOf(res.getPageHits()));
        hitRatioLabel.setText(String.format(Locale.US, "%.2f%%", res.getHitRatioPercent()));
        faultRatioLabel.setText(String.format(Locale.US, "%.2f%%", res.getFaultRatioPercent()));

        int fCount = res.getFrameCount();
        Object[] cols = new Object[fCount + 5];
        cols[0] = "Step";
        cols[1] = "Page";
        for (int f = 0; f < fCount; f++) {
            cols[2 + f] = "Frame " + f;
        }
        cols[2 + fCount] = "Result";
        cols[3 + fCount] = "Evicted";
        cols[4 + fCount] = "Details";

        stepsModel.setDataVector(new Object[0][0], cols);
        for (StepSnapshot snap : res.getSteps()) {
            Object[] row = new Object[fCount + 5];
            row[0] = snap.getStepIndex();
            row[1] = snap.getReferencedPage();
            int[] frames = snap.getFramesSnapshot();
            for (int f = 0; f < fCount; f++) {
                row[2 + f] = frames[f] == -1 ? "-" : String.valueOf(frames[f]);
            }
            row[2 + fCount] = snap.isHit() ? "HIT" : "FAULT";
            row[3 + fCount] = snap.getEvictedPage() == null ? "-" : String.valueOf(snap.getEvictedPage());
            row[4 + fCount] = snap.getNote();
            stepsModel.addRow(row);
        }
    }
}
