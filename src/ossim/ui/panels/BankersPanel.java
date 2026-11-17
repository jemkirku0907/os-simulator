package ossim.ui.panels;

import ossim.engine.BankerAlgorithmEngine;
import ossim.model.BankerModels.BankerResult;
import ossim.model.BankerModels.SafetyStep;
import ossim.model.InputValidator;
import ossim.model.ValidationException;
import ossim.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * GUI Panel for Module 2: Banker's Algorithm.
 */
public class BankersPanel extends JPanel {

    private final BankerAlgorithmEngine engine = new BankerAlgorithmEngine();

    private final JTextField procCountField;
    private final JTextField resCountField;

    private final DefaultTableModel allocModel;
    private final JTable allocTable;
    private final DefaultTableModel maxModel;
    private final JTable maxTable;
    private final DefaultTableModel availModel;
    private final JTable availTable;

    private final JComboBox<String> requestProcCombo;
    private final JTextField requestVectorField;

    private final JLabel statusBanner;
    private final DefaultTableModel needModel;
    private final DefaultTableModel traceModel;

    private int currentP = 5;
    private int currentR = 3;

    public BankersPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        add(Theme.createModuleHeader(
                "2. Banker's Algorithm (Deadlock Avoidance)",
                "Configure any number of processes and resource types, compute Need, verify system safety, and test resource requests."),
                BorderLayout.NORTH);

        // Dimension & Sample bar
        JPanel configCard = Theme.createCard("System Configuration & Presets");
        JPanel configFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configFlow.setOpaque(false);

        procCountField = Theme.createTextField("5", 4);
        resCountField = Theme.createTextField("3", 4);
        JButton resizeBtn = Theme.createSecondaryButton("Resize Matrices");
        JButton runSafetyBtn = Theme.createPrimaryButton("Check Safety");
        JButton safeSampleBtn = Theme.createSecondaryButton("Safe Sample (5x3)");
        JButton unsafeSampleBtn = Theme.createSecondaryButton("Unsafe Sample (4x3)");
        JButton largeSampleBtn = Theme.createSecondaryButton("Large Dataset (25x6)");

        configFlow.add(new JLabel("Processes (P):"));
        configFlow.add(procCountField);
        configFlow.add(new JLabel("Resources (R):"));
        configFlow.add(resCountField);
        configFlow.add(resizeBtn);
        configFlow.add(runSafetyBtn);
        configFlow.add(safeSampleBtn);
        configFlow.add(unsafeSampleBtn);
        configFlow.add(largeSampleBtn);

        // Request simulation bar
        JPanel reqFlow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        reqFlow.setOpaque(false);
        requestProcCombo = new JComboBox<>();
        requestProcCombo.setFont(Theme.FONT_BODY);
        requestVectorField = Theme.createTextField("1, 0, 2", 14);
        JButton testReqBtn = Theme.createSecondaryButton("Simulate Resource Request");

        reqFlow.add(new JLabel("Optional Resource Request -> Process:"));
        reqFlow.add(requestProcCombo);
        reqFlow.add(new JLabel("Request Vector:"));
        reqFlow.add(requestVectorField);
        reqFlow.add(testReqBtn);

        JPanel configStack = new JPanel(new GridLayout(2, 1, 2, 4));
        configStack.setOpaque(false);
        configStack.add(configFlow);
        configStack.add(reqFlow);
        configCard.add(configStack, BorderLayout.CENTER);

        // Input Matrices
        allocModel = createMatrixModel();
        allocTable = new JTable(allocModel);
        Theme.styleTable(allocTable);

        maxModel = createMatrixModel();
        maxTable = new JTable(maxModel);
        Theme.styleTable(maxTable);

        availModel = createMatrixModel();
        availTable = new JTable(availModel);
        Theme.styleTable(availTable);

        JPanel matricesGrid = new JPanel(new GridLayout(1, 2, 10, 10));
        matricesGrid.setOpaque(false);
        JPanel allocCard = Theme.createCard("Allocation Matrix");
        allocCard.add(Theme.wrapTable(allocTable, 340, 180), BorderLayout.CENTER);
        JPanel maxCard = Theme.createCard("Max Demand Matrix");
        maxCard.add(Theme.wrapTable(maxTable, 340, 180), BorderLayout.CENTER);
        matricesGrid.add(allocCard);
        matricesGrid.add(maxCard);

        JPanel availCard = Theme.createCard("Available Resources Vector");
        availCard.add(Theme.wrapTable(availTable, 680, 62), BorderLayout.CENTER);

        JPanel topInputArea = new JPanel(new BorderLayout(8, 8));
        topInputArea.setOpaque(false);
        topInputArea.add(configCard, BorderLayout.NORTH);
        topInputArea.add(matricesGrid, BorderLayout.CENTER);
        topInputArea.add(availCard, BorderLayout.SOUTH);

        // Output area
        statusBanner = new JLabel("Configure matrices and click 'Check Safety' to run Banker's Algorithm.");
        statusBanner.setFont(Theme.FONT_BODY_BOLD);
        statusBanner.setOpaque(true);
        statusBanner.setBackground(Theme.INFO_BG);
        statusBanner.setForeground(Theme.TEXT_PRIMARY);
        statusBanner.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        needModel = createReadOnlyModel();
        JTable needTable = new JTable(needModel);
        Theme.styleTable(needTable);

        traceModel = new DefaultTableModel(
                new Object[] { "Step", "Process", "Result", "Need", "Work Before", "Work After", "Details" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable traceTable = new JTable(traceModel);
        Theme.styleTable(traceTable);

        JTabbedPane resultTabs = new JTabbedPane();
        resultTabs.setFont(Theme.FONT_BODY_BOLD);
        resultTabs.addTab("Safety Execution Trace", Theme.wrapTable(traceTable, 700, 190));
        resultTabs.addTab("Computed Need Matrix (Max - Allocation)", Theme.wrapTable(needTable, 700, 190));

        JPanel outputCard = Theme.createCard("Banker's Algorithm Output");
        outputCard.add(statusBanner, BorderLayout.NORTH);
        outputCard.add(resultTabs, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topInputArea, outputCard);
        split.setResizeWeight(0.54);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        // Listeners
        resizeBtn.addActionListener(e -> resizeFromFields());
        runSafetyBtn.addActionListener(e -> runSafetyCheck());
        testReqBtn.addActionListener(e -> runRequestCheck());
        safeSampleBtn.addActionListener(e -> loadSafeSample5x3());
        unsafeSampleBtn.addActionListener(e -> loadUnsafeSample4x3());
        largeSampleBtn.addActionListener(e -> loadLargeSample25x6());

        loadSafeSample5x3();
    }

    private DefaultTableModel createMatrixModel() {
        return new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
        };
    }

    private DefaultTableModel createReadOnlyModel() {
        return new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void rebuildTables(int pCount, int rCount, int[][] alloc, int[][] max, int[] avail) {
        currentP = pCount;
        currentR = rCount;
        procCountField.setText(String.valueOf(pCount));
        resCountField.setText(String.valueOf(rCount));

        Object[] cols = new Object[rCount + 1];
        cols[0] = "Process";
        for (int j = 0; j < rCount; j++) {
            cols[j + 1] = "R" + j;
        }

        allocModel.setDataVector(new Object[0][0], cols);
        maxModel.setDataVector(new Object[0][0], cols);
        availModel.setDataVector(new Object[0][0], cols);

        requestProcCombo.removeAllItems();
        for (int i = 0; i < pCount; i++) {
            String pName = "P" + i;
            requestProcCombo.addItem(pName);
            Object[] allocRow = new Object[rCount + 1];
            Object[] maxRow = new Object[rCount + 1];
            allocRow[0] = pName;
            maxRow[0] = pName;
            for (int j = 0; j < rCount; j++) {
                allocRow[j + 1] = alloc != null ? alloc[i][j] : 0;
                maxRow[j + 1] = max != null ? max[i][j] : 0;
            }
            allocModel.addRow(allocRow);
            maxModel.addRow(maxRow);
        }

        Object[] availRow = new Object[rCount + 1];
        availRow[0] = "Available";
        for (int j = 0; j < rCount; j++) {
            availRow[j + 1] = avail != null ? avail[j] : 0;
        }
        availModel.addRow(availRow);
    }

    private void resizeFromFields() {
        try {
            int p = InputValidator.parsePositiveInt(procCountField.getText(), "Process Count (P)");
            int r = InputValidator.parsePositiveInt(resCountField.getText(), "Resource Count (R)");
            rebuildTables(p, r, null, null, null);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private void loadSafeSample5x3() {
        int[][] alloc = {
                { 0, 1, 0 },
                { 2, 0, 0 },
                { 3, 0, 2 },
                { 2, 1, 1 },
                { 0, 0, 2 }
        };
        int[][] max = {
                { 7, 5, 3 },
                { 3, 2, 2 },
                { 9, 0, 2 },
                { 2, 2, 2 },
                { 4, 3, 3 }
        };
        int[] avail = { 3, 3, 2 };
        rebuildTables(5, 3, alloc, max, avail);
        requestVectorField.setText("1, 0, 2");
        runSafetyCheck();
    }

    private void loadUnsafeSample4x3() {
        int[][] alloc = {
                { 1, 1, 0 },
                { 2, 0, 1 },
                { 1, 2, 1 },
                { 0, 1, 2 }
        };
        int[][] max = {
                { 4, 3, 2 },
                { 5, 2, 3 },
                { 3, 4, 4 },
                { 2, 3, 4 }
        };
        int[] avail = { 1, 0, 0 };
        rebuildTables(4, 3, alloc, max, avail);
        requestVectorField.setText("0, 0, 0");
        runSafetyCheck();
    }

    private void loadLargeSample25x6() {
        int p = 25;
        int r = 6;
        Random rng = new Random(123);
        int[][] alloc = new int[p][r];
        int[][] max = new int[p][r];
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < r; j++) {
                alloc[i][j] = rng.nextInt(4);
                max[i][j] = alloc[i][j] + rng.nextInt(5);
            }
        }
        int[] avail = { 6, 5, 6, 5, 7, 6 };
        rebuildTables(p, r, alloc, max, avail);
        requestVectorField.setText("1, 0, 1, 0, 1, 0");
        runSafetyCheck();
    }

    private int[][] readMatrix(DefaultTableModel model, JTable table, String name) throws ValidationException {
        Theme.commitTableEdits(table);
        int[][] mat = new int[currentP][currentR];
        for (int i = 0; i < currentP; i++) {
            for (int j = 0; j < currentR; j++) {
                Object raw = model.getValueAt(i, j + 1);
                String valStr = raw == null ? "" : raw.toString();
                mat[i][j] = InputValidator.parseNonNegativeInt(valStr, name + "[P" + i + "][R" + j + "]");
            }
        }
        return mat;
    }

    private int[] readAvailable() throws ValidationException {
        Theme.commitTableEdits(availTable);
        int[] avail = new int[currentR];
        for (int j = 0; j < currentR; j++) {
            Object raw = availModel.getValueAt(0, j + 1);
            String valStr = raw == null ? "" : raw.toString();
            avail[j] = InputValidator.parseNonNegativeInt(valStr, "Available[R" + j + "]");
        }
        return avail;
    }

    private void runSafetyCheck() {
        try {
            int[][] alloc = readMatrix(allocModel, allocTable, "Allocation");
            int[][] max = readMatrix(maxModel, maxTable, "Max");
            int[] avail = readAvailable();
            BankerResult res = engine.evaluateSafety(alloc, max, avail);
            displayResult(res);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private void runRequestCheck() {
        try {
            int[][] alloc = readMatrix(allocModel, allocTable, "Allocation");
            int[][] max = readMatrix(maxModel, maxTable, "Max");
            int[] avail = readAvailable();
            int procIdx = requestProcCombo.getSelectedIndex();
            List<Integer> reqList = InputValidator.parseIntList(
                    requestVectorField.getText(), "Request Vector", 0, Integer.MAX_VALUE);
            if (reqList.size() != currentR) {
                throw new ValidationException(
                        "Request Vector must have exactly " + currentR + " values (one per resource R0..R" + (currentR - 1) + ").");
            }
            int[] req = reqList.stream().mapToInt(Integer::intValue).toArray();
            BankerResult res = engine.evaluateRequest(alloc, max, avail, procIdx, req);
            displayResult(res);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private void displayResult(BankerResult res) {
        if (res.isSafe()) {
            statusBanner.setBackground(Theme.SUCCESS_BG);
            statusBanner.setForeground(new Color(20, 83, 45));
        } else {
            statusBanner.setBackground(Theme.DANGER_BG);
            statusBanner.setForeground(new Color(127, 29, 29));
        }
        statusBanner.setText("<html>" + res.getSummaryMessage() + "</html>");

        Object[] cols = new Object[res.getResourceCount() + 1];
        cols[0] = "Process";
        for (int j = 0; j < res.getResourceCount(); j++) {
            cols[j + 1] = "R" + j;
        }
        needModel.setDataVector(new Object[0][0], cols);
        for (int i = 0; i < res.getProcessCount(); i++) {
            Object[] row = new Object[res.getResourceCount() + 1];
            row[0] = "P" + i;
            for (int j = 0; j < res.getResourceCount(); j++) {
                row[j + 1] = res.getNeed()[i][j];
            }
            needModel.addRow(row);
        }

        traceModel.setRowCount(0);
        for (SafetyStep s : res.getSteps()) {
            traceModel.addRow(new Object[] {
                    s.getStepNumber(),
                    s.getProcessName(),
                    s.isGranted() ? "EXECUTED" : "BLOCKED",
                    Arrays.toString(s.getNeedSnapshot()),
                    Arrays.toString(s.getWorkBefore()),
                    Arrays.toString(s.getWorkAfter()),
                    s.getExplanation()
            });
        }
    }
}
