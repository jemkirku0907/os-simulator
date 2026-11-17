package ossim.ui.panels;

import ossim.engine.FragmentationEngine;
import ossim.model.FragmentationModels.FinalRegion;
import ossim.model.FragmentationModels.FitStrategy;
import ossim.model.FragmentationModels.FragmentationResult;
import ossim.model.FragmentationModels.MemoryBlockInput;
import ossim.model.FragmentationModels.MemoryProcessInput;
import ossim.model.FragmentationModels.PartitionMode;
import ossim.model.FragmentationModels.ProcessAllocation;
import ossim.model.InputValidator;
import ossim.model.ValidationException;
import ossim.ui.Theme;
import ossim.ui.components.MemoryMapPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
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
 * GUI Panel for Module 6: Fragmentation (Internal & External).
 */
public class FragmentationPanel extends JPanel {

    private final FragmentationEngine engine = new FragmentationEngine();

    private final DefaultTableModel blockModel;
    private final JTable blockTable;
    private final DefaultTableModel procModel;
    private final JTable procTable;

    private final JComboBox<FitStrategy> strategyCombo;
    private final JComboBox<PartitionMode> modeCombo;

    private final MemoryMapPanel memMapPanel;
    private final DefaultTableModel allocResultModel;
    private final DefaultTableModel compareModel;
    private final JTabbedPane outputTabs;
    private final JLabel diagnosisBanner;

    private final JLabel totalMemLabel = new JLabel("-");
    private final JLabel internalFragLabel = new JLabel("-");
    private final JLabel externalFragLabel = new JLabel("-");
    private final JLabel freeMemLabel = new JLabel("-");
    private final JLabel utilLabel = new JLabel("-");

    public FragmentationPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        add(Theme.createModuleHeader(
                "6. Memory Allocation & Fragmentation Simulator",
                "Demonstrate Fixed & Dynamic partitioning with First Fit, Best Fit, Worst Fit, and Next Fit across unlimited blocks and processes."),
                BorderLayout.NORTH);

        // Controls
        JPanel controlsCard = Theme.createCard("Allocation Strategy & Presets");
        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controlsRow.setOpaque(false);

        strategyCombo = new JComboBox<>(FitStrategy.values());
        strategyCombo.setFont(Theme.FONT_BODY);
        modeCombo = new JComboBox<>(PartitionMode.values());
        modeCombo.setFont(Theme.FONT_BODY);

        JButton runBtn = Theme.createPrimaryButton("Run Allocation");
        JButton compareBtn = Theme.createSecondaryButton("Compare All Strategies");
        JButton smallBtn = Theme.createSecondaryButton("Small Sample");
        JButton extDemoBtn = Theme.createSecondaryButton("External Frag Demo");
        JButton largeBtn = Theme.createSecondaryButton("Large Dataset (35x30)");

        controlsRow.add(new JLabel("Strategy:"));
        controlsRow.add(strategyCombo);
        controlsRow.add(new JLabel("Mode:"));
        controlsRow.add(modeCombo);
        controlsRow.add(runBtn);
        controlsRow.add(compareBtn);
        controlsRow.add(smallBtn);
        controlsRow.add(extDemoBtn);
        controlsRow.add(largeBtn);
        controlsCard.add(controlsRow, BorderLayout.CENTER);

        // Two dynamic input tables
        blockModel = new DefaultTableModel(new Object[] { "Block ID", "Block Size" }, 0);
        blockTable = new JTable(blockModel);
        Theme.styleTable(blockTable);
        JPanel blockCard = Theme.createCard("Memory Blocks (Dynamic)");
        JPanel blkBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        blkBtns.setOpaque(false);
        JButton addBlk = Theme.createSecondaryButton("+ Add Block");
        JButton remBlk = Theme.createSecondaryButton("Remove");
        JButton clrBlk = Theme.createDangerButton("Clear");
        blkBtns.add(addBlk);
        blkBtns.add(remBlk);
        blkBtns.add(clrBlk);
        blockCard.add(blkBtns, BorderLayout.NORTH);
        blockCard.add(Theme.wrapTable(blockTable, 300, 150), BorderLayout.CENTER);

        procModel = new DefaultTableModel(new Object[] { "Process ID", "Requested Memory" }, 0);
        procTable = new JTable(procModel);
        Theme.styleTable(procTable);
        JPanel procCard = Theme.createCard("Processes (Dynamic)");
        JPanel prcBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        prcBtns.setOpaque(false);
        JButton addPrc = Theme.createSecondaryButton("+ Add Process");
        JButton remPrc = Theme.createSecondaryButton("Remove");
        JButton clrPrc = Theme.createDangerButton("Clear");
        prcBtns.add(addPrc);
        prcBtns.add(remPrc);
        prcBtns.add(clrPrc);
        procCard.add(prcBtns, BorderLayout.NORTH);
        procCard.add(Theme.wrapTable(procTable, 300, 150), BorderLayout.CENTER);

        JPanel tablesGrid = new JPanel(new GridLayout(1, 2, 10, 10));
        tablesGrid.setOpaque(false);
        tablesGrid.add(blockCard);
        tablesGrid.add(procCard);

        JPanel topArea = new JPanel(new BorderLayout(8, 8));
        topArea.setOpaque(false);
        topArea.add(controlsCard, BorderLayout.NORTH);
        topArea.add(tablesGrid, BorderLayout.CENTER);

        // Output area
        JPanel metrics = new JPanel(new GridLayout(1, 5, 8, 8));
        metrics.setOpaque(false);
        metrics.add(Theme.createMetricCard("Total Memory", totalMemLabel, Theme.PRIMARY));
        metrics.add(Theme.createMetricCard("Internal Fragmentation", internalFragLabel, Theme.WARNING));
        metrics.add(Theme.createMetricCard("External Fragmentation", externalFragLabel, Theme.DANGER));
        metrics.add(Theme.createMetricCard("Total Free Memory", freeMemLabel, Theme.colorForIndex(5)));
        metrics.add(Theme.createMetricCard("Utilization", utilLabel, Theme.SUCCESS));

        diagnosisBanner = new JLabel("Run allocation to inspect internal and external fragmentation.");
        diagnosisBanner.setFont(Theme.FONT_BODY);
        diagnosisBanner.setOpaque(true);
        diagnosisBanner.setBackground(Theme.INFO_BG);
        diagnosisBanner.setForeground(Theme.TEXT_PRIMARY);
        diagnosisBanner.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JPanel outTop = new JPanel(new BorderLayout(6, 6));
        outTop.setOpaque(false);
        outTop.add(metrics, BorderLayout.NORTH);
        outTop.add(diagnosisBanner, BorderLayout.SOUTH);

        memMapPanel = new MemoryMapPanel();
        JScrollPane mapScroll = new JScrollPane(memMapPanel);
        mapScroll.setBorder(BorderFactory.createTitledBorder("Memory Block Layout (Amber = Internal Fragmentation, Gray = Free Hole)"));
        mapScroll.setPreferredSize(new java.awt.Dimension(700, 138));

        allocResultModel = new DefaultTableModel(
                new Object[] { "Process", "Requested", "Status", "Assigned Block", "Block Size", "Internal Frag", "Details" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable allocTable = new JTable(allocResultModel);
        Theme.styleTable(allocTable);

        JPanel detailTab = new JPanel(new BorderLayout(8, 8));
        detailTab.setOpaque(false);
        detailTab.add(mapScroll, BorderLayout.NORTH);
        detailTab.add(Theme.wrapTable(allocTable, 700, 170), BorderLayout.CENTER);

        compareModel = new DefaultTableModel(
                new Object[] { "Strategy", "Allocated", "Unallocated", "Internal Frag", "External Frag", "Free Memory", "Utilization" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable compareTable = new JTable(compareModel);
        Theme.styleTable(compareTable);

        outputTabs = new JTabbedPane();
        outputTabs.setFont(Theme.FONT_BODY_BOLD);
        outputTabs.addTab("Allocation & Memory Diagram", detailTab);
        outputTabs.addTab("Strategy Comparison", Theme.wrapTable(compareTable, 700, 220));

        JPanel outCard = Theme.createCard("Fragmentation Results");
        outCard.add(outTop, BorderLayout.NORTH);
        outCard.add(outputTabs, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topArea, outCard);
        split.setResizeWeight(0.45);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        addBlk.addActionListener(e -> blockModel.addRow(new Object[] { "B" + (blockModel.getRowCount() + 1), "200" }));
        remBlk.addActionListener(e -> removeSelected(blockTable, blockModel));
        clrBlk.addActionListener(e -> blockModel.setRowCount(0));

        addPrc.addActionListener(e -> procModel.addRow(new Object[] { "P" + (procModel.getRowCount() + 1), "100" }));
        remPrc.addActionListener(e -> removeSelected(procTable, procModel));
        clrPrc.addActionListener(e -> procModel.setRowCount(0));

        smallBtn.addActionListener(e -> loadSmallSample());
        extDemoBtn.addActionListener(e -> loadExternalFragDemo());
        largeBtn.addActionListener(e -> loadLargeSample());
        runBtn.addActionListener(e -> runSimulation());
        compareBtn.addActionListener(e -> compareAll());

        loadSmallSample();
    }

    private void removeSelected(JTable t, DefaultTableModel m) {
        Theme.commitTableEdits(t);
        int[] sel = t.getSelectedRows();
        for (int i = sel.length - 1; i >= 0; i--) {
            m.removeRow(sel[i]);
        }
    }

    private void loadSmallSample() {
        modeCombo.setSelectedItem(PartitionMode.FIXED_PARTITIONS);
        strategyCombo.setSelectedItem(FitStrategy.FIRST_FIT);
        blockModel.setRowCount(0);
        blockModel.addRow(new Object[] { "B1", "100" });
        blockModel.addRow(new Object[] { "B2", "500" });
        blockModel.addRow(new Object[] { "B3", "200" });
        blockModel.addRow(new Object[] { "B4", "300" });
        blockModel.addRow(new Object[] { "B5", "600" });

        procModel.setRowCount(0);
        procModel.addRow(new Object[] { "P1", "212" });
        procModel.addRow(new Object[] { "P2", "417" });
        procModel.addRow(new Object[] { "P3", "112" });
        procModel.addRow(new Object[] { "P4", "426" });
        runSimulation();
    }

    private void loadExternalFragDemo() {
        modeCombo.setSelectedItem(PartitionMode.FIXED_PARTITIONS);
        strategyCombo.setSelectedItem(FitStrategy.FIRST_FIT);
        blockModel.setRowCount(0);
        blockModel.addRow(new Object[] { "B1", "250" });
        blockModel.addRow(new Object[] { "B2", "250" });
        blockModel.addRow(new Object[] { "B3", "250" });

        procModel.setRowCount(0);
        procModel.addRow(new Object[] { "P1", "200" });
        procModel.addRow(new Object[] { "P2", "350" }); // Total free = 500 >= 350, but split across B2(250) + B3(250)!
        runSimulation();
    }

    private void loadLargeSample() {
        Random rng = new Random(55);
        blockModel.setRowCount(0);
        for (int i = 1; i <= 35; i++) {
            blockModel.addRow(new Object[] { "B" + i, String.valueOf(100 + rng.nextInt(700)) });
        }
        procModel.setRowCount(0);
        for (int i = 1; i <= 30; i++) {
            procModel.addRow(new Object[] { "P" + i, String.valueOf(60 + rng.nextInt(550)) });
        }
        runSimulation();
    }

    private List<MemoryBlockInput> readBlocks() throws ValidationException {
        Theme.commitTableEdits(blockTable);
        int rows = blockModel.getRowCount();
        if (rows == 0) {
            throw new ValidationException("Add at least one memory block.");
        }
        List<MemoryBlockInput> list = new ArrayList<>(rows);
        for (int r = 0; r < rows; r++) {
            String id = InputValidator.requireNonBlank(str(blockModel, r, 0), "Block #" + (r + 1) + " ID");
            int size = InputValidator.parsePositiveInt(str(blockModel, r, 1), id + " Size");
            list.add(new MemoryBlockInput(id, size));
        }
        return list;
    }

    private List<MemoryProcessInput> readProcesses() throws ValidationException {
        Theme.commitTableEdits(procTable);
        int rows = procModel.getRowCount();
        if (rows == 0) {
            throw new ValidationException("Add at least one process.");
        }
        List<MemoryProcessInput> list = new ArrayList<>(rows);
        for (int r = 0; r < rows; r++) {
            String id = InputValidator.requireNonBlank(str(procModel, r, 0), "Process #" + (r + 1) + " ID");
            int req = InputValidator.parsePositiveInt(str(procModel, r, 1), id + " Requested Memory");
            list.add(new MemoryProcessInput(id, req));
        }
        return list;
    }

    private String str(DefaultTableModel m, int r, int c) {
        Object v = m.getValueAt(r, c);
        return v == null ? "" : v.toString();
    }

    private void runSimulation() {
        try {
            List<MemoryBlockInput> blocks = readBlocks();
            List<MemoryProcessInput> procs = readProcesses();
            FitStrategy strat = (FitStrategy) strategyCombo.getSelectedItem();
            PartitionMode mode = (PartitionMode) modeCombo.getSelectedItem();

            FragmentationResult res = engine.simulate(blocks, procs, strat, mode);
            displayResult(res);
            outputTabs.setSelectedIndex(0);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private void compareAll() {
        try {
            List<MemoryBlockInput> blocks = readBlocks();
            List<MemoryProcessInput> procs = readProcesses();
            PartitionMode mode = (PartitionMode) modeCombo.getSelectedItem();
            FitStrategy selected = (FitStrategy) strategyCombo.getSelectedItem();

            compareModel.setRowCount(0);
            FragmentationResult selectedRes = null;
            for (FitStrategy strat : FitStrategy.values()) {
                FragmentationResult r = engine.simulate(blocks, procs, strat, mode);
                if (strat == selected) {
                    selectedRes = r;
                }
                compareModel.addRow(new Object[] {
                        strat.getDisplayName(),
                        r.getAllocatedProcessCount(),
                        r.getUnallocatedProcessCount(),
                        r.getTotalInternalFragmentation(),
                        r.getExternalFragmentation(),
                        r.getTotalFreeMemory(),
                        String.format(Locale.US, "%.2f%%", r.getUtilizationPercent())
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

    private void displayResult(FragmentationResult res) {
        totalMemLabel.setText(String.valueOf(res.getTotalMemory()));
        internalFragLabel.setText(String.valueOf(res.getTotalInternalFragmentation()));
        externalFragLabel.setText(String.valueOf(res.getExternalFragmentation()));
        freeMemLabel.setText(String.valueOf(res.getTotalFreeMemory()));
        utilLabel.setText(String.format(Locale.US, "%.2f%%", res.getUtilizationPercent()));

        boolean hasFragWarning = res.getExternalFragmentation() > 0 || res.getUnallocatedProcessCount() > 0;
        diagnosisBanner.setBackground(hasFragWarning ? Theme.WARNING_BG : Theme.SUCCESS_BG);
        diagnosisBanner.setForeground(hasFragWarning ? new Color(146, 64, 14) : new Color(20, 83, 45));
        diagnosisBanner.setText("<html>" + res.getDiagnosis() + "</html>");

        allocResultModel.setRowCount(0);
        for (ProcessAllocation pa : res.getProcessAllocations()) {
            allocResultModel.addRow(new Object[] {
                    pa.getProcessId(),
                    pa.getRequestedSize(),
                    pa.isAllocated() ? "ALLOCATED" : "WAITING / FAILED",
                    pa.getAssignedBlock(),
                    pa.isAllocated() ? pa.getBlockSize() : "-",
                    pa.getInternalFragmentation(),
                    pa.getStatusMessage()
            });
        }

        List<MemoryMapPanel.BlockSegment> segs = new ArrayList<>();
        int idx = 0;
        for (FinalRegion fr : res.getFinalRegions()) {
            if (fr.isFreeHole()) {
                segs.add(new MemoryMapPanel.BlockSegment(fr.getLabel(), "Free Hole", fr.getTotalSize(), 0, Color.LIGHT_GRAY, true));
            } else {
                String sub = fr.getInternalFrag() > 0
                        ? fr.getAssignedProcess() + " (Int:" + fr.getInternalFrag() + ")"
                        : fr.getAssignedProcess();
                segs.add(new MemoryMapPanel.BlockSegment(
                        fr.getLabel(),
                        sub,
                        fr.getTotalSize(),
                        fr.getInternalFrag(),
                        Theme.colorForIndex(idx++),
                        false));
            }
        }
        memMapPanel.setSegments(segs, "No blocks configured.");
    }
}
