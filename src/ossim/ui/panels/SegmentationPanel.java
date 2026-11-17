package ossim.ui.panels;

import ossim.engine.SegmentationEngine;
import ossim.engine.SegmentationEngine.LogicalAddressQuery;
import ossim.model.InputValidator;
import ossim.model.SegmentationModels.SegmentEntry;
import ossim.model.SegmentationModels.SegmentTranslation;
import ossim.model.SegmentationModels.SegmentationResult;
import ossim.model.ValidationException;
import ossim.ui.Theme;
import ossim.ui.components.MemoryMapPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GUI Panel for Module 5: Segmentation.
 */
public class SegmentationPanel extends JPanel {

    private final SegmentationEngine engine = new SegmentationEngine();

    private final DefaultTableModel segmentModel;
    private final JTable segmentTable;
    private final DefaultTableModel queryModel;
    private final JTable queryTable;

    private final MemoryMapPanel memMapPanel;
    private final DefaultTableModel resultModel;
    private final JLabel overlapBanner;
    private final JLabel validCountLabel = new JLabel("-");
    private final JLabel errorCountLabel = new JLabel("-");
    private final JLabel segCountLabel = new JLabel("-");

    public SegmentationPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        add(Theme.createModuleHeader(
                "5. Segmentation Simulator",
                "Define unlimited segments with Base and Limit values, validate logical (Segment, Offset) pairs, and detect out-of-range traps."),
                BorderLayout.NORTH);

        // Top toolbar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.setOpaque(false);
        JButton runBtn = Theme.createPrimaryButton("Validate & Translate");
        JButton smallBtn = Theme.createSecondaryButton("Small Sample (5 segs)");
        JButton largeBtn = Theme.createSecondaryButton("Large Dataset (40 segs)");
        topBar.add(runBtn);
        topBar.add(smallBtn);
        topBar.add(largeBtn);

        // Segment Table
        segmentModel = new DefaultTableModel(new Object[] { "Segment #", "Name", "Base Address", "Limit (Size)" }, 0);
        segmentTable = new JTable(segmentModel);
        Theme.styleTable(segmentTable);

        JPanel segCard = Theme.createCard("Segment Table (Dynamic Rows)");
        JPanel segBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        segBtns.setOpaque(false);
        JButton addSegBtn = Theme.createSecondaryButton("+ Add Segment");
        JButton remSegBtn = Theme.createSecondaryButton("Remove");
        JButton clrSegBtn = Theme.createDangerButton("Clear");
        segBtns.add(addSegBtn);
        segBtns.add(remSegBtn);
        segBtns.add(clrSegBtn);
        segCard.add(segBtns, BorderLayout.NORTH);
        segCard.add(Theme.wrapTable(segmentTable, 400, 180), BorderLayout.CENTER);

        // Logical Address Queries Table
        queryModel = new DefaultTableModel(new Object[] { "Segment #", "Offset" }, 0);
        queryTable = new JTable(queryModel);
        Theme.styleTable(queryTable);

        JPanel qCard = Theme.createCard("Logical Addresses to Translate");
        JPanel qBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        qBtns.setOpaque(false);
        JButton addQBtn = Theme.createSecondaryButton("+ Add Logical Address");
        JButton remQBtn = Theme.createSecondaryButton("Remove");
        JButton clrQBtn = Theme.createDangerButton("Clear");
        qBtns.add(addQBtn);
        qBtns.add(remQBtn);
        qBtns.add(clrQBtn);
        qCard.add(qBtns, BorderLayout.NORTH);
        qCard.add(Theme.wrapTable(queryTable, 260, 180), BorderLayout.CENTER);

        JPanel tablesRow = new JPanel(new GridLayout(1, 2, 10, 10));
        tablesRow.setOpaque(false);
        tablesRow.add(segCard);
        tablesRow.add(qCard);

        JPanel inputArea = new JPanel(new BorderLayout(6, 6));
        inputArea.setOpaque(false);
        inputArea.add(topBar, BorderLayout.NORTH);
        inputArea.add(tablesRow, BorderLayout.CENTER);

        // Output area
        JPanel metrics = new JPanel(new GridLayout(1, 3, 8, 8));
        metrics.setOpaque(false);
        metrics.add(Theme.createMetricCard("Segments Defined", segCountLabel, Theme.PRIMARY));
        metrics.add(Theme.createMetricCard("Valid Translations", validCountLabel, Theme.SUCCESS));
        metrics.add(Theme.createMetricCard("Out-of-Range / Faults", errorCountLabel, Theme.DANGER));

        overlapBanner = new JLabel("No segment overlaps detected.");
        overlapBanner.setFont(Theme.FONT_BODY);
        overlapBanner.setOpaque(true);
        overlapBanner.setBackground(Theme.SUCCESS_BG);
        overlapBanner.setForeground(new Color(20, 83, 45));
        overlapBanner.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JPanel outHeaderStack = new JPanel(new BorderLayout(6, 6));
        outHeaderStack.setOpaque(false);
        outHeaderStack.add(metrics, BorderLayout.NORTH);
        outHeaderStack.add(overlapBanner, BorderLayout.SOUTH);

        memMapPanel = new MemoryMapPanel();
        JScrollPane mapScroll = new JScrollPane(memMapPanel);
        mapScroll.setBorder(BorderFactory.createTitledBorder("Physical Address Space Segment Map"));
        mapScroll.setPreferredSize(new java.awt.Dimension(700, 138));

        resultModel = new DefaultTableModel(
                new Object[] { "Segment #", "Offset", "Physical Address", "Status", "Validation Detail" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable resultTable = new JTable(resultModel);
        Theme.styleTable(resultTable);

        JPanel outCenter = new JPanel(new BorderLayout(8, 8));
        outCenter.setOpaque(false);
        outCenter.add(mapScroll, BorderLayout.NORTH);
        outCenter.add(Theme.wrapTable(resultTable, 700, 180), BorderLayout.CENTER);

        JPanel outCard = Theme.createCard("Segmentation Validation & Translation Results");
        outCard.add(outHeaderStack, BorderLayout.NORTH);
        outCard.add(outCenter, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputArea, outCard);
        split.setResizeWeight(0.45);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        addSegBtn.addActionListener(e -> {
            int n = segmentModel.getRowCount();
            segmentModel.addRow(new Object[] { String.valueOf(n), "Seg-" + n, String.valueOf(n * 1000), "500" });
        });
        remSegBtn.addActionListener(e -> removeSelected(segmentTable, segmentModel));
        clrSegBtn.addActionListener(e -> segmentModel.setRowCount(0));

        addQBtn.addActionListener(e -> queryModel.addRow(new Object[] { "0", "100" }));
        remQBtn.addActionListener(e -> removeSelected(queryTable, queryModel));
        clrQBtn.addActionListener(e -> queryModel.setRowCount(0));

        smallBtn.addActionListener(e -> loadSmallSample());
        largeBtn.addActionListener(e -> loadLargeSample());
        runBtn.addActionListener(e -> runSimulation());

        loadSmallSample();
    }

    private void removeSelected(JTable table, DefaultTableModel model) {
        Theme.commitTableEdits(table);
        int[] sel = table.getSelectedRows();
        for (int i = sel.length - 1; i >= 0; i--) {
            model.removeRow(sel[i]);
        }
    }

    private void loadSmallSample() {
        segmentModel.setRowCount(0);
        segmentModel.addRow(new Object[] { "0", "Code", "1400", "1000" });
        segmentModel.addRow(new Object[] { "1", "Data", "6300", "400" });
        segmentModel.addRow(new Object[] { "2", "Stack", "4300", "400" });
        segmentModel.addRow(new Object[] { "3", "Heap", "3200", "1100" });
        segmentModel.addRow(new Object[] { "4", "Shared", "4700", "1000" });

        queryModel.setRowCount(0);
        queryModel.addRow(new Object[] { "0", "430" });
        queryModel.addRow(new Object[] { "1", "10" });
        queryModel.addRow(new Object[] { "2", "500" });
        queryModel.addRow(new Object[] { "3", "400" });
        queryModel.addRow(new Object[] { "4", "112" });
        queryModel.addRow(new Object[] { "9", "50" });
        runSimulation();
    }

    private void loadLargeSample() {
        segmentModel.setRowCount(0);
        long base = 1000;
        for (int i = 0; i < 40; i++) {
            long limit = 200 + (i % 7) * 150;
            segmentModel.addRow(new Object[] {
                    String.valueOf(i), "Seg-" + i, String.valueOf(base), String.valueOf(limit)
            });
            base += limit + 100;
        }
        queryModel.setRowCount(0);
        for (int i = 0; i < 40; i++) {
            long offset = (i % 6 == 0) ? 1500 : 120; // mix valid and out-of-range
            queryModel.addRow(new Object[] { String.valueOf(i), String.valueOf(offset) });
        }
        runSimulation();
    }

    private void runSimulation() {
        try {
            Theme.commitTableEdits(segmentTable);
            Theme.commitTableEdits(queryTable);

            int segRows = segmentModel.getRowCount();
            if (segRows == 0) {
                throw new ValidationException("Add at least one segment.");
            }
            List<SegmentEntry> segs = new ArrayList<>(segRows);
            for (int r = 0; r < segRows; r++) {
                int segNum = InputValidator.parseNonNegativeInt(str(segmentModel, r, 0), "Row " + (r + 1) + " Segment #");
                String name = str(segmentModel, r, 1);
                long base = InputValidator.parseNonNegativeLong(str(segmentModel, r, 2), "Segment " + segNum + " Base");
                long limit = InputValidator.parsePositiveLong(str(segmentModel, r, 3), "Segment " + segNum + " Limit");
                segs.add(new SegmentEntry(segNum, name, base, limit));
            }

            int qRows = queryModel.getRowCount();
            if (qRows == 0) {
                throw new ValidationException("Add at least one logical address to validate.");
            }
            List<LogicalAddressQuery> queries = new ArrayList<>(qRows);
            for (int r = 0; r < qRows; r++) {
                int segNum = InputValidator.parseNonNegativeInt(str(queryModel, r, 0), "Query " + (r + 1) + " Segment #");
                long offset = InputValidator.parseInt(str(queryModel, r, 1), "Query " + (r + 1) + " Offset");
                queries.add(new LogicalAddressQuery(segNum, offset));
            }

            SegmentationResult res = engine.simulate(segs, queries);
            displayResult(res);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private String str(DefaultTableModel m, int r, int c) {
        Object val = m.getValueAt(r, c);
        return val == null ? "" : val.toString();
    }

    private void displayResult(SegmentationResult res) {
        segCountLabel.setText(String.valueOf(res.getSegments().size()));
        validCountLabel.setText(String.valueOf(res.getValidCount()));
        errorCountLabel.setText(String.valueOf(res.getErrorCount()));

        if (res.getOverlapWarnings().isEmpty()) {
            overlapBanner.setBackground(Theme.SUCCESS_BG);
            overlapBanner.setForeground(new Color(20, 83, 45));
            overlapBanner.setText("Physical memory check: No overlapping segments detected.");
        } else {
            overlapBanner.setBackground(Theme.WARNING_BG);
            overlapBanner.setForeground(new Color(146, 64, 14));
            overlapBanner.setText("<html><b>Warning:</b> " + String.join(" | ", res.getOverlapWarnings()) + "</html>");
        }

        resultModel.setRowCount(0);
        for (SegmentTranslation t : res.getTranslations()) {
            resultModel.addRow(new Object[] {
                    t.getSegmentNumber(),
                    t.getOffset(),
                    t.isValid() ? t.getPhysicalAddress() : "ERROR",
                    t.getStatus(),
                    t.getDetail()
            });
        }

        // Build proportional memory map sorted by Base Address including gaps
        List<SegmentEntry> sorted = new ArrayList<>(res.getSegments());
        sorted.sort(Comparator.comparingLong(SegmentEntry::getBase));
        List<MemoryMapPanel.BlockSegment> blocks = new ArrayList<>();
        long cursor = 0;
        for (SegmentEntry s : sorted) {
            if (s.getBase() > cursor) {
                long gap = s.getBase() - cursor;
                blocks.add(new MemoryMapPanel.BlockSegment("Free", cursor + ".." + (s.getBase() - 1), gap, 0, Color.LIGHT_GRAY, true));
            }
            blocks.add(new MemoryMapPanel.BlockSegment(
                    "S" + s.getSegmentNumber() + " (" + s.getName() + ")",
                    s.getBase() + ".." + (s.getEndAddressExclusive() - 1),
                    s.getLimit(),
                    0,
                    Theme.colorForIndex(s.getSegmentNumber()),
                    false));
            cursor = Math.max(cursor, s.getEndAddressExclusive());
        }
        memMapPanel.setSegments(blocks, "No segments configured.");
    }
}
