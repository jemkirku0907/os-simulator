package ossim.ui.panels;

import ossim.engine.CpuSchedulerEngine;
import ossim.model.CpuModels.Algorithm;
import ossim.model.CpuModels.ProcessInput;
import ossim.model.CpuModels.ProcessResult;
import ossim.model.CpuModels.ScheduleResult;
import ossim.model.InputValidator;
import ossim.model.ValidationException;
import ossim.ui.Theme;
import ossim.ui.components.GanttChartPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * GUI Panel for Module 1: CPU Scheduling.
 */
public class CpuSchedulingPanel extends JPanel {

    private final CpuSchedulerEngine engine = new CpuSchedulerEngine();

    private final DefaultTableModel inputModel;
    private final JTable inputTable;
    private final JComboBox<Algorithm> algoCombo;
    private final JTextField quantumField;
    private final JCheckBox lowerPriorityFirstCheck;

    private final GanttChartPanel ganttPanel;
    private final DefaultTableModel resultModel;
    private final DefaultTableModel compareModel;
    private final JTabbedPane outputTabs;

    private final JLabel avgWtLabel = new JLabel("-");
    private final JLabel avgTatLabel = new JLabel("-");
    private final JLabel avgRtLabel = new JLabel("-");
    private final JLabel cpuUtilLabel = new JLabel("-");
    private final JLabel throughputLabel = new JLabel("-");

    public CpuSchedulingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        add(Theme.createModuleHeader(
                "1. CPU Scheduling Simulator",
                "Supports FCFS, SJF, SRTF, Priority (Non-Preemptive & Preemptive), and Round Robin with unlimited dynamic processes."),
                BorderLayout.NORTH);

        // Input table card
        inputModel = new DefaultTableModel(new Object[] { "Process ID", "Arrival Time", "Burst Time", "Priority" }, 0);
        inputTable = new JTable(inputModel);
        Theme.styleTable(inputTable);

        JPanel inputCard = Theme.createCard("Process Queue (Dynamic Input)");
        JPanel tableBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tableBtns.setOpaque(false);

        JButton addBtn = Theme.createSecondaryButton("+ Add Process");
        JButton add10Btn = Theme.createSecondaryButton("+ Add 10 Random");
        JButton removeBtn = Theme.createSecondaryButton("Remove Selected");
        JButton clearBtn = Theme.createDangerButton("Clear All");
        JButton smallBtn = Theme.createSecondaryButton("Small Sample (5)");
        JButton largeBtn = Theme.createSecondaryButton("Large Dataset (60)");

        tableBtns.add(addBtn);
        tableBtns.add(add10Btn);
        tableBtns.add(removeBtn);
        tableBtns.add(clearBtn);
        tableBtns.add(smallBtn);
        tableBtns.add(largeBtn);

        inputCard.add(tableBtns, BorderLayout.NORTH);
        inputCard.add(Theme.wrapTable(inputTable, 420, 220), BorderLayout.CENTER);

        // Controls bar
        JPanel controlsCard = Theme.createCard("Scheduling Parameters");
        JPanel controlsFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controlsFlow.setOpaque(false);

        algoCombo = new JComboBox<>(Algorithm.values());
        algoCombo.setFont(Theme.FONT_BODY);
        quantumField = Theme.createTextField("3", 4);
        lowerPriorityFirstCheck = new JCheckBox("Lower # = Higher Priority", true);
        lowerPriorityFirstCheck.setOpaque(false);
        lowerPriorityFirstCheck.setFont(Theme.FONT_BODY);

        JButton runBtn = Theme.createPrimaryButton("Run Simulation");
        JButton compareBtn = Theme.createSecondaryButton("Compare All Algorithms");

        controlsFlow.add(new JLabel("Algorithm:"));
        controlsFlow.add(algoCombo);
        controlsFlow.add(new JLabel("Quantum (RR):"));
        controlsFlow.add(quantumField);
        controlsFlow.add(lowerPriorityFirstCheck);
        controlsFlow.add(runBtn);
        controlsFlow.add(compareBtn);
        controlsCard.add(controlsFlow, BorderLayout.CENTER);

        JPanel topSection = new JPanel(new BorderLayout(8, 8));
        topSection.setOpaque(false);
        topSection.add(controlsCard, BorderLayout.NORTH);
        topSection.add(inputCard, BorderLayout.CENTER);

        // Output Section
        JPanel metricsRow = new JPanel(new GridLayout(1, 5, 8, 8));
        metricsRow.setOpaque(false);
        metricsRow.add(Theme.createMetricCard("Avg Waiting Time", avgWtLabel, Theme.PRIMARY));
        metricsRow.add(Theme.createMetricCard("Avg Turnaround Time", avgTatLabel, Theme.SUCCESS));
        metricsRow.add(Theme.createMetricCard("Avg Response Time", avgRtLabel, Theme.colorForIndex(3)));
        metricsRow.add(Theme.createMetricCard("CPU Utilization", cpuUtilLabel, Theme.colorForIndex(2)));
        metricsRow.add(Theme.createMetricCard("Throughput (proc/unit)", throughputLabel, Theme.colorForIndex(5)));

        ganttPanel = new GanttChartPanel();
        JScrollPane ganttScroll = new JScrollPane(ganttPanel);
        ganttScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1));
        ganttScroll.setPreferredSize(new java.awt.Dimension(700, 138));

        resultModel = new DefaultTableModel(
                new Object[] { "PID", "Arrival", "Burst", "Priority", "Start", "Completion", "Turnaround", "Waiting", "Response" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable resultTable = new JTable(resultModel);
        Theme.styleTable(resultTable);

        JPanel detailOutput = new JPanel(new BorderLayout(8, 8));
        detailOutput.setOpaque(false);
        detailOutput.add(ganttScroll, BorderLayout.NORTH);
        detailOutput.add(Theme.wrapTable(resultTable, 700, 200), BorderLayout.CENTER);

        compareModel = new DefaultTableModel(
                new Object[] { "Algorithm", "Avg Waiting Time", "Avg Turnaround Time", "Avg Response Time", "CPU Util (%)", "Total Time" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable compareTable = new JTable(compareModel);
        Theme.styleTable(compareTable);

        outputTabs = new JTabbedPane();
        outputTabs.setFont(Theme.FONT_BODY_BOLD);
        outputTabs.addTab("Schedule & Gantt Chart", detailOutput);
        outputTabs.addTab("Algorithm Comparison", Theme.wrapTable(compareTable, 700, 260));

        JPanel bottomSection = Theme.createCard("Simulation Results");
        bottomSection.add(metricsRow, BorderLayout.NORTH);
        bottomSection.add(outputTabs, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSection, bottomSection);
        split.setResizeWeight(0.45);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        // Event bindings
        addBtn.addActionListener(e -> addSingleRow());
        add10Btn.addActionListener(e -> addRandomRows(10));
        removeBtn.addActionListener(e -> removeSelectedRows());
        clearBtn.addActionListener(e -> inputModel.setRowCount(0));
        smallBtn.addActionListener(e -> loadSmallSample());
        largeBtn.addActionListener(e -> loadLargeSample(60));
        runBtn.addActionListener(e -> runSimulation());
        compareBtn.addActionListener(e -> compareAll());

        loadSmallSample();
    }

    private void addSingleRow() {
        int nextNum = inputModel.getRowCount() + 1;
        inputModel.addRow(new Object[] { "P" + nextNum, "0", "4", "1" });
    }

    private void addRandomRows(int count) {
        Random rng = new Random();
        int start = inputModel.getRowCount() + 1;
        for (int i = 0; i < count; i++) {
            int pidNum = start + i;
            inputModel.addRow(new Object[] {
                    "P" + pidNum,
                    String.valueOf(rng.nextInt(Math.max(10, count))),
                    String.valueOf(1 + rng.nextInt(15)),
                    String.valueOf(1 + rng.nextInt(10))
            });
        }
    }

    private void removeSelectedRows() {
        Theme.commitTableEdits(inputTable);
        int[] selected = inputTable.getSelectedRows();
        for (int i = selected.length - 1; i >= 0; i--) {
            inputModel.removeRow(selected[i]);
        }
    }

    private void loadSmallSample() {
        inputModel.setRowCount(0);
        inputModel.addRow(new Object[] { "P1", "0", "8", "3" });
        inputModel.addRow(new Object[] { "P2", "1", "4", "1" });
        inputModel.addRow(new Object[] { "P3", "2", "9", "4" });
        inputModel.addRow(new Object[] { "P4", "3", "5", "2" });
        inputModel.addRow(new Object[] { "P5", "5", "2", "5" });
    }

    private void loadLargeSample(int count) {
        inputModel.setRowCount(0);
        Random rng = new Random(42);
        for (int i = 1; i <= count; i++) {
            inputModel.addRow(new Object[] {
                    "P" + i,
                    String.valueOf(rng.nextInt(80)),
                    String.valueOf(1 + rng.nextInt(20)),
                    String.valueOf(1 + rng.nextInt(10))
            });
        }
    }

    private List<ProcessInput> readInputs() throws ValidationException {
        Theme.commitTableEdits(inputTable);
        int rows = inputModel.getRowCount();
        if (rows == 0) {
            throw new ValidationException("Add at least one process before running.");
        }
        List<ProcessInput> list = new ArrayList<>(rows);
        for (int r = 0; r < rows; r++) {
            String pid = InputValidator.requireNonBlank(strAt(r, 0), "Row " + (r + 1) + " Process ID");
            int arrival = InputValidator.parseNonNegativeInt(strAt(r, 1), pid + " Arrival Time");
            int burst = InputValidator.parsePositiveInt(strAt(r, 2), pid + " Burst Time");
            int priority = InputValidator.parseInt(strAt(r, 3), pid + " Priority");
            list.add(new ProcessInput(pid, arrival, burst, priority));
        }
        return list;
    }

    private String strAt(int row, int col) {
        Object val = inputModel.getValueAt(row, col);
        return val == null ? "" : val.toString();
    }

    private void runSimulation() {
        try {
            List<ProcessInput> inputs = readInputs();
            Algorithm algo = (Algorithm) algoCombo.getSelectedItem();
            int quantum = algo == Algorithm.ROUND_ROBIN
                    ? InputValidator.parsePositiveInt(quantumField.getText(), "Time Quantum")
                    : 1;
            ScheduleResult res = engine.schedule(inputs, algo, quantum, lowerPriorityFirstCheck.isSelected());
            displayResult(res);
            outputTabs.setSelectedIndex(0);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private void compareAll() {
        try {
            List<ProcessInput> inputs = readInputs();
            int quantum = InputValidator.parsePositiveInt(quantumField.getText(), "Time Quantum");
            boolean lowerFirst = lowerPriorityFirstCheck.isSelected();

            compareModel.setRowCount(0);
            ScheduleResult selectedRes = null;
            Algorithm selectedAlgo = (Algorithm) algoCombo.getSelectedItem();

            for (Algorithm algo : Algorithm.values()) {
                ScheduleResult r = engine.schedule(inputs, algo, quantum, lowerFirst);
                if (algo == selectedAlgo) {
                    selectedRes = r;
                }
                compareModel.addRow(new Object[] {
                        algo.getDisplayName(),
                        fmt(r.getAverageWaitingTime()),
                        fmt(r.getAverageTurnaroundTime()),
                        fmt(r.getAverageResponseTime()),
                        fmt(r.getCpuUtilizationPercent()) + "%",
                        r.getTotalTime()
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

    private void displayResult(ScheduleResult res) {
        avgWtLabel.setText(fmt(res.getAverageWaitingTime()));
        avgTatLabel.setText(fmt(res.getAverageTurnaroundTime()));
        avgRtLabel.setText(fmt(res.getAverageResponseTime()));
        cpuUtilLabel.setText(fmt(res.getCpuUtilizationPercent()) + "%");
        throughputLabel.setText(String.format(Locale.US, "%.4f", res.getThroughput()));

        ganttPanel.setSlices(res.getGanttChart());
        resultModel.setRowCount(0);
        for (ProcessResult pr : res.getProcessResults()) {
            resultModel.addRow(new Object[] {
                    pr.getPid(), pr.getArrivalTime(), pr.getBurstTime(), pr.getPriority(),
                    pr.getStartTime(), pr.getCompletionTime(), pr.getTurnaroundTime(),
                    pr.getWaitingTime(), pr.getResponseTime()
            });
        }
    }

    private String fmt(double val) {
        return String.format(Locale.US, "%.2f", val);
    }
}
