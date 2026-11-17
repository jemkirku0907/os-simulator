package ossim.ui.panels;

import ossim.engine.DiskSchedulerEngine;
import ossim.model.DiskModels.Algorithm;
import ossim.model.DiskModels.Direction;
import ossim.model.DiskModels.DiskResult;
import ossim.model.DiskModels.SeekStep;
import ossim.model.InputValidator;
import ossim.model.ValidationException;
import ossim.ui.Theme;
import ossim.ui.components.DiskChartPanel;

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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * GUI Panel for Module 7: Disk Scheduling.
 */
public class DiskSchedulingPanel extends JPanel {

    private final DiskSchedulerEngine engine = new DiskSchedulerEngine();

    private final JComboBox<Algorithm> algoCombo;
    private final JComboBox<Direction> dirCombo;
    private final JTextField headField;
    private final JTextField maxTrackField;
    private final JTextArea requestsArea;

    private final DiskChartPanel chartPanel;
    private final DefaultTableModel stepsModel;
    private final DefaultTableModel compareModel;
    private final JTabbedPane outputTabs;
    private final JTextArea orderArea;

    private final JLabel totalMoveLabel = new JLabel("-");
    private final JLabel avgSeekLabel = new JLabel("-");
    private final JLabel reqCountLabel = new JLabel("-");

    public DiskSchedulingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        add(Theme.createModuleHeader(
                "7. Disk Scheduling Simulator",
                "Implements FCFS, SSTF, SCAN, C-SCAN, LOOK, and C-LOOK with variable-length request queues and trajectory plotting."),
                BorderLayout.NORTH);

        JPanel inputCard = Theme.createCard("Disk Head Parameters & Request Queue");
        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controlsRow.setOpaque(false);

        algoCombo = new JComboBox<>(Algorithm.values());
        algoCombo.setFont(Theme.FONT_BODY);
        dirCombo = new JComboBox<>(Direction.values());
        dirCombo.setFont(Theme.FONT_BODY);
        headField = Theme.createTextField("53", 5);
        maxTrackField = Theme.createTextField("199", 5);

        JButton runBtn = Theme.createPrimaryButton("Run Simulation");
        JButton compareBtn = Theme.createSecondaryButton("Compare All Algorithms");
        JButton smallBtn = Theme.createSecondaryButton("Small Sample (8)");
        JButton largeBtn = Theme.createSecondaryButton("Large Dataset (120)");

        controlsRow.add(new JLabel("Algorithm:"));
        controlsRow.add(algoCombo);
        controlsRow.add(new JLabel("Direction:"));
        controlsRow.add(dirCombo);
        controlsRow.add(new JLabel("Initial Head:"));
        controlsRow.add(headField);
        controlsRow.add(new JLabel("Max Cylinder:"));
        controlsRow.add(maxTrackField);

        JPanel btnsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        btnsRow.setOpaque(false);
        btnsRow.add(runBtn);
        btnsRow.add(compareBtn);
        btnsRow.add(smallBtn);
        btnsRow.add(largeBtn);

        JPanel topRows = new JPanel(new GridLayout(2, 1, 2, 2));
        topRows.setOpaque(false);
        topRows.add(controlsRow);
        topRows.add(btnsRow);

        requestsArea = new JTextArea(2, 50);
        requestsArea.setFont(Theme.FONT_MONO);
        requestsArea.setLineWrap(true);
        requestsArea.setWrapStyleWord(true);
        JScrollPane reqScroll = new JScrollPane(requestsArea);
        reqScroll.setBorder(BorderFactory.createTitledBorder("Disk Track Requests (comma/space separated, unlimited count)"));

        inputCard.add(topRows, BorderLayout.NORTH);
        inputCard.add(reqScroll, BorderLayout.CENTER);

        // Output
        JPanel metrics = new JPanel(new GridLayout(1, 3, 8, 8));
        metrics.setOpaque(false);
        metrics.add(Theme.createMetricCard("Total Head Movement", totalMoveLabel, Theme.PRIMARY));
        metrics.add(Theme.createMetricCard("Average Seek Distance", avgSeekLabel, Theme.SUCCESS));
        metrics.add(Theme.createMetricCard("Requests Serviced", reqCountLabel, Theme.colorForIndex(3)));

        orderArea = new JTextArea(2, 40);
        orderArea.setFont(Theme.FONT_MONO);
        orderArea.setEditable(false);
        orderArea.setLineWrap(true);
        orderArea.setWrapStyleWord(true);
        JScrollPane orderScroll = new JScrollPane(orderArea);
        orderScroll.setBorder(BorderFactory.createTitledBorder("Head Path / Service Order"));

        JPanel metricsAndOrder = new JPanel(new BorderLayout(6, 6));
        metricsAndOrder.setOpaque(false);
        metricsAndOrder.add(metrics, BorderLayout.NORTH);
        metricsAndOrder.add(orderScroll, BorderLayout.CENTER);

        chartPanel = new DiskChartPanel();
        JScrollPane chartScroll = new JScrollPane(chartPanel);
        chartScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1));

        stepsModel = new DefaultTableModel(
                new Object[] { "Step #", "From Track", "To Track", "Seek Distance", "Note" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable stepsTable = new JTable(stepsModel);
        Theme.styleTable(stepsTable);

        compareModel = new DefaultTableModel(
                new Object[] { "Algorithm", "Total Head Movement", "Avg Seek Distance", "First 8 Serviced" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable compareTable = new JTable(compareModel);
        Theme.styleTable(compareTable);

        outputTabs = new JTabbedPane();
        outputTabs.setFont(Theme.FONT_BODY_BOLD);
        outputTabs.addTab("Head Movement Chart", chartScroll);
        outputTabs.addTab("Seek Step Breakdown", Theme.wrapTable(stepsTable, 700, 240));
        outputTabs.addTab("Algorithm Comparison", Theme.wrapTable(compareTable, 700, 240));

        JPanel outputCard = Theme.createCard("Disk Scheduling Output");
        outputCard.add(metricsAndOrder, BorderLayout.NORTH);
        outputCard.add(outputTabs, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputCard, outputCard);
        split.setResizeWeight(0.32);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        runBtn.addActionListener(e -> runSimulation());
        compareBtn.addActionListener(e -> compareAll());
        smallBtn.addActionListener(e -> loadSmallSample());
        largeBtn.addActionListener(e -> loadLargeSample());

        loadSmallSample();
    }

    private void loadSmallSample() {
        headField.setText("53");
        maxTrackField.setText("199");
        dirCombo.setSelectedItem(Direction.UP);
        requestsArea.setText("98, 183, 37, 122, 14, 124, 65, 67");
        runSimulation();
    }

    private void loadLargeSample() {
        headField.setText("500");
        maxTrackField.setText("4999");
        Random rng = new Random(2026);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(rng.nextInt(5000));
        }
        requestsArea.setText(sb.toString());
        runSimulation();
    }

    private void runSimulation() {
        try {
            int maxTrack = InputValidator.parsePositiveInt(maxTrackField.getText(), "Max Cylinder");
            int head = InputValidator.parseIntInRange(headField.getText(), "Initial Head Position", 0, maxTrack);
            List<Integer> reqs = InputValidator.parseIntList(
                    requestsArea.getText(), "Disk Requests", 0, maxTrack);
            Algorithm algo = (Algorithm) algoCombo.getSelectedItem();
            Direction dir = (Direction) dirCombo.getSelectedItem();

            DiskResult res = engine.schedule(reqs, head, maxTrack, algo, dir);
            displayResult(res);
            outputTabs.setSelectedIndex(0);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private void compareAll() {
        try {
            int maxTrack = InputValidator.parsePositiveInt(maxTrackField.getText(), "Max Cylinder");
            int head = InputValidator.parseIntInRange(headField.getText(), "Initial Head Position", 0, maxTrack);
            List<Integer> reqs = InputValidator.parseIntList(
                    requestsArea.getText(), "Disk Requests", 0, maxTrack);
            Direction dir = (Direction) dirCombo.getSelectedItem();
            Algorithm selected = (Algorithm) algoCombo.getSelectedItem();

            compareModel.setRowCount(0);
            DiskResult selectedRes = null;
            for (Algorithm algo : Algorithm.values()) {
                DiskResult r = engine.schedule(reqs, head, maxTrack, algo, dir);
                if (algo == selected) {
                    selectedRes = r;
                }
                String preview = r.getServicedRequests().stream()
                        .limit(8)
                        .map(String::valueOf)
                        .collect(Collectors.joining(" -> "))
                        + (r.getServicedRequests().size() > 8 ? " -> ..." : "");
                compareModel.addRow(new Object[] {
                        algo.getDisplayName(),
                        r.getTotalHeadMovement(),
                        String.format(Locale.US, "%.2f", r.getAverageSeekLength()),
                        preview
                });
            }
            if (selectedRes != null) {
                displayResult(selectedRes);
            }
            outputTabs.setSelectedIndex(2);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private void displayResult(DiskResult res) {
        totalMoveLabel.setText(res.getTotalHeadMovement() + " tracks");
        avgSeekLabel.setText(String.format(Locale.US, "%.2f tracks", res.getAverageSeekLength()));
        reqCountLabel.setText(String.valueOf(res.getServicedRequests().size()));

        String pathStr = res.getPathTracks().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" -> "));
        orderArea.setText(pathStr);

        chartPanel.setResult(res);

        stepsModel.setRowCount(0);
        for (SeekStep s : res.getSteps()) {
            stepsModel.addRow(new Object[] {
                    s.getStepNumber(),
                    s.getFromTrack(),
                    s.getToTrack(),
                    s.getDistance(),
                    s.getNote()
            });
        }
    }
}
