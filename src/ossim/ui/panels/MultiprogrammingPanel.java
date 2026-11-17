package ossim.ui.panels;

import ossim.engine.MultiprogrammingEngine;
import ossim.model.InputValidator;
import ossim.model.MultiprogrammingModels.MultiprogrammingResult;
import ossim.model.MultiprogrammingModels.ProcessSnapshot;
import ossim.model.MultiprogrammingModels.ProcessState;
import ossim.model.MultiprogrammingModels.ProgramInput;
import ossim.model.MultiprogrammingModels.TickSnapshot;
import ossim.model.ValidationException;
import ossim.ui.Theme;
import ossim.ui.components.MemoryMapPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * GUI Panel for Module 8: Multiprogramming Simulator.
 */
public class MultiprogrammingPanel extends JPanel {

    private final MultiprogrammingEngine engine = new MultiprogrammingEngine();

    private final JTextField totalRamField;
    private final JTextField osRamField;
    private final JTextField coresField;
    private final JTextField quantumField;
    private final JTextField maxDegreeField;

    private final DefaultTableModel inputModel;
    private final JTable inputTable;

    private final JSlider tickSlider;
    private final JLabel tickStatusLabel;
    private final JButton playBtn;
    private final Timer playTimer;

    private final MemoryMapPanel ramMapPanel;
    private final DefaultTableModel stateModel;
    private final JTextArea eventLogArea;

    private final JLabel cpuUtilLabel = new JLabel("-");
    private final JLabel uniUtilLabel = new JLabel("-");
    private final JLabel ramUtilLabel = new JLabel("-");
    private final JLabel degreeLabel = new JLabel("-");
    private final JLabel completedLabel = new JLabel("-");

    private MultiprogrammingResult currentResult;
    private boolean updatingSlider = false;

    public MultiprogrammingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        add(Theme.createModuleHeader(
                "8. Multiprogramming & Resource Sharing Simulator",
                "Simulate multiple programs sharing RAM and CPU cores with CPU/I/O overlap, live state inspection, and utilization comparison."),
                BorderLayout.NORTH);

        // System Config Card
        JPanel sysCard = Theme.createCard("System Resources & Controls");
        JPanel sysRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        sysRow.setOpaque(false);

        totalRamField = Theme.createTextField("1024", 5);
        osRamField = Theme.createTextField("256", 5);
        coresField = Theme.createTextField("2", 3);
        quantumField = Theme.createTextField("3", 3);
        maxDegreeField = Theme.createTextField("0", 3);

        sysRow.add(new JLabel("Total RAM (MB):"));
        sysRow.add(totalRamField);
        sysRow.add(new JLabel("OS Reserved (MB):"));
        sysRow.add(osRamField);
        sysRow.add(new JLabel("CPU Cores:"));
        sysRow.add(coresField);
        sysRow.add(new JLabel("Quantum:"));
        sysRow.add(quantumField);
        sysRow.add(new JLabel("Max Degree (0=auto):"));
        sysRow.add(maxDegreeField);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        actionRow.setOpaque(false);
        JButton runBtn = Theme.createPrimaryButton("Run Simulation");
        playBtn = Theme.createSecondaryButton("Play Live");
        JButton stepBtn = Theme.createSecondaryButton("Step +1 Tick");
        JButton smallBtn = Theme.createSecondaryButton("Small Sample (5)");
        JButton largeBtn = Theme.createSecondaryButton("Large Dataset (35)");

        actionRow.add(runBtn);
        actionRow.add(playBtn);
        actionRow.add(stepBtn);
        actionRow.add(smallBtn);
        actionRow.add(largeBtn);

        JPanel sysStack = new JPanel(new GridLayout(2, 1, 2, 2));
        sysStack.setOpaque(false);
        sysStack.add(sysRow);
        sysStack.add(actionRow);
        sysCard.add(sysStack, BorderLayout.CENTER);

        // Program Input Table
        inputModel = new DefaultTableModel(
                new Object[] { "PID", "Arrival", "CPU Burst", "I/O Time", "Memory (MB)", "Priority" }, 0);
        inputTable = new JTable(inputModel);
        Theme.styleTable(inputTable);

        JPanel progCard = Theme.createCard("Programs / Processes (Dynamic Input)");
        JPanel progBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        progBtns.setOpaque(false);
        JButton addProgBtn = Theme.createSecondaryButton("+ Add Program");
        JButton add10Btn = Theme.createSecondaryButton("+ Add 10 Random");
        JButton remProgBtn = Theme.createSecondaryButton("Remove Selected");
        JButton clrProgBtn = Theme.createDangerButton("Clear All");
        progBtns.add(addProgBtn);
        progBtns.add(add10Btn);
        progBtns.add(remProgBtn);
        progBtns.add(clrProgBtn);

        progCard.add(progBtns, BorderLayout.NORTH);
        progCard.add(Theme.wrapTable(inputTable, 680, 140), BorderLayout.CENTER);

        JPanel topArea = new JPanel(new BorderLayout(8, 8));
        topArea.setOpaque(false);
        topArea.add(sysCard, BorderLayout.NORTH);
        topArea.add(progCard, BorderLayout.CENTER);

        // Output Dashboard
        JPanel metrics = new JPanel(new GridLayout(1, 5, 8, 8));
        metrics.setOpaque(false);
        metrics.add(Theme.createMetricCard("Multiprogramming CPU Util", cpuUtilLabel, Theme.SUCCESS));
        metrics.add(Theme.createMetricCard("Uniprogramming Baseline", uniUtilLabel, Theme.WARNING));
        metrics.add(Theme.createMetricCard("Avg / Peak RAM Util", ramUtilLabel, Theme.PRIMARY));
        metrics.add(Theme.createMetricCard("Avg / Peak Degree", degreeLabel, Theme.colorForIndex(3)));
        metrics.add(Theme.createMetricCard("Completed / Ticks", completedLabel, Theme.colorForIndex(5)));

        // Scrubber bar
        JPanel scrubberPanel = new JPanel(new BorderLayout(8, 4));
        scrubberPanel.setOpaque(false);
        tickSlider = new JSlider(0, 0, 0);
        tickSlider.setOpaque(false);
        tickStatusLabel = new JLabel("Tick: 0 / 0");
        tickStatusLabel.setFont(Theme.FONT_BODY_BOLD);
        scrubberPanel.add(new JLabel("Timeline Scrubber:"), BorderLayout.WEST);
        scrubberPanel.add(tickSlider, BorderLayout.CENTER);
        scrubberPanel.add(tickStatusLabel, BorderLayout.EAST);

        ramMapPanel = new MemoryMapPanel();
        JScrollPane ramScroll = new JScrollPane(ramMapPanel);
        ramScroll.setBorder(BorderFactory.createTitledBorder("Live Physical RAM Allocation at Selected Tick"));
        ramScroll.setPreferredSize(new java.awt.Dimension(700, 134));

        stateModel = new DefaultTableModel(
                new Object[] { "PID", "State", "RAM (MB)", "Rem CPU", "Rem I/O", "Completion Tick", "Waiting Ticks" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable stateTable = new JTable(stateModel);
        Theme.styleTable(stateTable);

        eventLogArea = new JTextArea(8, 50);
        eventLogArea.setFont(Theme.FONT_MONO);
        eventLogArea.setEditable(false);

        JTabbedPane outTabs = new JTabbedPane();
        outTabs.setFont(Theme.FONT_BODY_BOLD);
        outTabs.addTab("Process States at Tick", Theme.wrapTable(stateTable, 700, 165));
        outTabs.addTab("Simulation Event Log", new JScrollPane(eventLogArea));

        JPanel liveBody = new JPanel(new BorderLayout(6, 6));
        liveBody.setOpaque(false);
        liveBody.add(scrubberPanel, BorderLayout.NORTH);
        liveBody.add(ramScroll, BorderLayout.CENTER);
        liveBody.add(outTabs, BorderLayout.SOUTH);

        JPanel outCard = Theme.createCard("Multiprogramming Utilization & Live State");
        outCard.add(metrics, BorderLayout.NORTH);
        outCard.add(liveBody, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topArea, outCard);
        split.setResizeWeight(0.40);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        playTimer = new Timer(350, e -> advancePlayback());

        addProgBtn.addActionListener(e -> inputModel.addRow(
                new Object[] { "P" + (inputModel.getRowCount() + 1), "0", "6", "4", "160", "1" }));
        add10Btn.addActionListener(e -> addRandomPrograms(10));
        remProgBtn.addActionListener(e -> removeSelected());
        clrProgBtn.addActionListener(e -> inputModel.setRowCount(0));
        smallBtn.addActionListener(e -> loadSmallSample());
        largeBtn.addActionListener(e -> loadLargeSample());
        runBtn.addActionListener(e -> runSimulation(true));
        playBtn.addActionListener(e -> togglePlay());
        stepBtn.addActionListener(e -> stepForward());
        tickSlider.addChangeListener(e -> {
            if (!updatingSlider && currentResult != null && !currentResult.getTimeline().isEmpty()) {
                renderTick(tickSlider.getValue());
            }
        });

        loadSmallSample();
    }

    private void addRandomPrograms(int count) {
        Random rng = new Random();
        int start = inputModel.getRowCount() + 1;
        for (int i = 0; i < count; i++) {
            inputModel.addRow(new Object[] {
                    "P" + (start + i),
                    String.valueOf(rng.nextInt(15)),
                    String.valueOf(2 + rng.nextInt(10)),
                    String.valueOf(rng.nextInt(8)),
                    String.valueOf(64 + rng.nextInt(4) * 64),
                    String.valueOf(1 + rng.nextInt(5))
            });
        }
    }

    private void removeSelected() {
        Theme.commitTableEdits(inputTable);
        int[] sel = inputTable.getSelectedRows();
        for (int i = sel.length - 1; i >= 0; i--) {
            inputModel.removeRow(sel[i]);
        }
    }

    private void loadSmallSample() {
        stopPlayback();
        totalRamField.setText("1024");
        osRamField.setText("256");
        coresField.setText("1");
        quantumField.setText("3");
        maxDegreeField.setText("0");

        inputModel.setRowCount(0);
        inputModel.addRow(new Object[] { "P1", "0", "6", "8", "256", "1" });
        inputModel.addRow(new Object[] { "P2", "0", "5", "6", "256", "2" });
        inputModel.addRow(new Object[] { "P3", "1", "8", "4", "192", "1" });
        inputModel.addRow(new Object[] { "P4", "2", "4", "7", "256", "3" });
        inputModel.addRow(new Object[] { "P5", "3", "5", "5", "128", "2" });
        runSimulation(true);
    }

    private void loadLargeSample() {
        stopPlayback();
        totalRamField.setText("4096");
        osRamField.setText("512");
        coresField.setText("4");
        quantumField.setText("4");
        maxDegreeField.setText("0");

        Random rng = new Random(314);
        inputModel.setRowCount(0);
        for (int i = 1; i <= 35; i++) {
            inputModel.addRow(new Object[] {
                    "P" + i,
                    String.valueOf(rng.nextInt(25)),
                    String.valueOf(3 + rng.nextInt(12)),
                    String.valueOf(2 + rng.nextInt(10)),
                    String.valueOf(128 + rng.nextInt(5) * 128),
                    String.valueOf(1 + rng.nextInt(5))
            });
        }
        runSimulation(true);
    }

    private boolean runSimulation(boolean jumpToEnd) {
        stopPlayback();
        try {
            Theme.commitTableEdits(inputTable);
            int totalRam = InputValidator.parsePositiveInt(totalRamField.getText(), "Total RAM");
            int osRam = InputValidator.parseNonNegativeInt(osRamField.getText(), "OS Reserved RAM");
            int cores = InputValidator.parsePositiveInt(coresField.getText(), "CPU Cores");
            int quantum = InputValidator.parsePositiveInt(quantumField.getText(), "Time Quantum");
            int maxDeg = InputValidator.parseNonNegativeInt(maxDegreeField.getText(), "Max Degree");

            int rows = inputModel.getRowCount();
            if (rows == 0) {
                throw new ValidationException("Add at least one program/process.");
            }
            List<ProgramInput> list = new ArrayList<>(rows);
            for (int r = 0; r < rows; r++) {
                String pid = InputValidator.requireNonBlank(str(r, 0), "Row " + (r + 1) + " PID");
                int arr = InputValidator.parseNonNegativeInt(str(r, 1), pid + " Arrival");
                int cpu = InputValidator.parsePositiveInt(str(r, 2), pid + " CPU Burst");
                int io = InputValidator.parseNonNegativeInt(str(r, 3), pid + " I/O Time");
                int mem = InputValidator.parsePositiveInt(str(r, 4), pid + " Memory (MB)");
                int prio = InputValidator.parseInt(str(r, 5), pid + " Priority");
                list.add(new ProgramInput(pid, arr, cpu, io, mem, prio));
            }

            currentResult = engine.simulate(list, totalRam, osRam, cores, quantum, maxDeg);
            displaySummary(currentResult);

            int maxIdx = Math.max(0, currentResult.getTimeline().size() - 1);
            updatingSlider = true;
            tickSlider.setMinimum(0);
            tickSlider.setMaximum(maxIdx);
            int targetIdx = jumpToEnd ? maxIdx : 0;
            tickSlider.setValue(targetIdx);
            updatingSlider = false;

            if (!currentResult.getTimeline().isEmpty()) {
                renderTick(targetIdx);
            }
            return true;
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
            return false;
        }
    }

    private String str(int r, int c) {
        Object v = inputModel.getValueAt(r, c);
        return v == null ? "" : v.toString();
    }

    private void displaySummary(MultiprogrammingResult res) {
        cpuUtilLabel.setText(String.format(Locale.US, "%.1f%%", res.getCpuUtilizationPercent()));
        uniUtilLabel.setText(String.format(Locale.US, "%.1f%%", res.getUniprogrammingCpuUtilPercent()));
        ramUtilLabel.setText(String.format(Locale.US, "%.1f%% / %.1f%%",
                res.getAvgRamUtilizationPercent(), res.getPeakRamUtilizationPercent()));
        degreeLabel.setText(String.format(Locale.US, "%.2f / %d",
                res.getAvgDegreeOfMultiprogramming(), res.getPeakDegreeOfMultiprogramming()));
        completedLabel.setText(res.getCompletedCount() + " / " + res.getTotalTicks() + " ticks");

        StringBuilder log = new StringBuilder();
        for (TickSnapshot snap : res.getTimeline()) {
            if (!snap.getEvents().isEmpty()) {
                log.append("Tick ").append(snap.getTick()).append(": ")
                        .append(String.join(" | ", snap.getEvents())).append("\n");
            }
        }
        if (log.length() == 0) {
            log.append("Simulation completed in ").append(res.getTotalTicks()).append(" ticks.");
        }
        eventLogArea.setText(log.toString());
        eventLogArea.setCaretPosition(0);
    }

    private void renderTick(int idx) {
        if (currentResult == null || currentResult.getTimeline().isEmpty()) {
            return;
        }
        int clamped = Math.max(0, Math.min(idx, currentResult.getTimeline().size() - 1));
        TickSnapshot snap = currentResult.getTimeline().get(clamped);

        tickStatusLabel.setText("Tick: " + snap.getTick() + " / " + (currentResult.getTimeline().size() - 1)
                + " | Cores: " + String.join(", ", snap.getRunningOnCores())
                + " | In RAM: " + snap.getActiveInMemoryCount()
                + " | I/O: " + snap.getIoWaitCount()
                + " | JobQ: " + snap.getJobQueueCount());

        stateModel.setRowCount(0);
        List<MemoryMapPanel.BlockSegment> ramSegs = new ArrayList<>();
        ramSegs.add(new MemoryMapPanel.BlockSegment("OS Kernel", snap.getOsReservedMb() + " MB",
                snap.getOsReservedMb(), 0, new Color(51, 65, 85), false));

        int colorIdx = 0;
        for (ProcessSnapshot ps : snap.getProcessSnapshots()) {
            stateModel.addRow(new Object[] {
                    ps.getPid(),
                    ps.getState().getLabel(),
                    ps.getMemoryMb(),
                    ps.getRemainingCpu(),
                    ps.getRemainingIo(),
                    ps.getCompletionTime() >= 0 ? ps.getCompletionTime() : "-",
                    ps.getWaitingTime()
            });
            if (ps.getState() == ProcessState.READY
                    || ps.getState() == ProcessState.RUNNING
                    || ps.getState() == ProcessState.IO_WAIT) {
                ramSegs.add(new MemoryMapPanel.BlockSegment(
                        ps.getPid(),
                        ps.getState().name() + " (" + ps.getMemoryMb() + "M)",
                        ps.getMemoryMb(),
                        0,
                        Theme.colorForIndex(colorIdx++),
                        false));
            }
        }

        int freeRam = snap.getTotalRamMb() - snap.getOsReservedMb() - snap.getUsedByProcessesMb();
        if (freeRam > 0) {
            ramSegs.add(new MemoryMapPanel.BlockSegment("Free RAM", freeRam + " MB", freeRam, 0, Color.LIGHT_GRAY, true));
        }
        ramMapPanel.setSegments(ramSegs, "RAM map unavailable.");
    }

    private void togglePlay() {
        if (playTimer.isRunning()) {
            stopPlayback();
            return;
        }
        if (currentResult == null || tickSlider.getValue() >= tickSlider.getMaximum()) {
            if (!runSimulation(false)) {
                return;
            }
        }
        playBtn.setText("Pause");
        playTimer.start();
    }

    private void stopPlayback() {
        if (playTimer.isRunning()) {
            playTimer.stop();
        }
        if (playBtn != null) {
            playBtn.setText("Play Live");
        }
    }

    private void advancePlayback() {
        if (currentResult == null) {
            stopPlayback();
            return;
        }
        int next = tickSlider.getValue() + 1;
        if (next > tickSlider.getMaximum()) {
            stopPlayback();
            return;
        }
        tickSlider.setValue(next);
    }

    private void stepForward() {
        stopPlayback();
        if (currentResult == null) {
            if (!runSimulation(false)) {
                return;
            }
        }
        int next = Math.min(tickSlider.getMaximum(), tickSlider.getValue() + 1);
        tickSlider.setValue(next);
    }
}
