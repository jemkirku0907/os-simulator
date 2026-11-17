package ossim.ui.panels;

import ossim.engine.PagingEngine;
import ossim.model.InputValidator;
import ossim.model.PagingModels.PageTableEntry;
import ossim.model.PagingModels.PagingSimulationResult;
import ossim.model.PagingModels.TranslationResult;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * GUI Panel for Module 3: Paging and Logical-to-Physical Address Translation.
 */
public class PagingPanel extends JPanel {

    private final PagingEngine engine = new PagingEngine();

    private final JTextField pageSizeField;
    private final JTextField numFramesField;
    private final JTextArea logicalAddrArea;

    private final DefaultTableModel pageTableModel;
    private final JTable pageTable;

    private final MemoryMapPanel frameMapPanel;
    private final DefaultTableModel resultModel;

    private final JLabel validCountLabel = new JLabel("-");
    private final JLabel faultCountLabel = new JLabel("-");
    private final JLabel ramCapacityLabel = new JLabel("-");

    public PagingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        add(Theme.createModuleHeader(
                "3. Paging & Address Translation Simulator",
                "Define arbitrary-size page tables, frame counts, and variable-length logical addresses to simulate page-to-frame mapping."),
                BorderLayout.NORTH);

        // Top controls
        JPanel paramsCard = Theme.createCard("Paging Configuration & Logical Addresses");
        JPanel paramsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        paramsRow.setOpaque(false);

        pageSizeField = Theme.createTextField("1024", 7);
        numFramesField = Theme.createTextField("8", 6);
        JButton translateBtn = Theme.createPrimaryButton("Translate Addresses");
        JButton smallBtn = Theme.createSecondaryButton("Small Sample");
        JButton largeBtn = Theme.createSecondaryButton("Large Sample (64 pages)");

        paramsRow.add(new JLabel("Page / Frame Size (bytes):"));
        paramsRow.add(pageSizeField);
        paramsRow.add(new JLabel("Physical Frames:"));
        paramsRow.add(numFramesField);
        paramsRow.add(translateBtn);
        paramsRow.add(smallBtn);
        paramsRow.add(largeBtn);

        logicalAddrArea = new JTextArea(2, 40);
        logicalAddrArea.setFont(Theme.FONT_MONO);
        logicalAddrArea.setLineWrap(true);
        logicalAddrArea.setWrapStyleWord(true);
        JScrollPane addrScroll = new JScrollPane(logicalAddrArea);
        addrScroll.setBorder(BorderFactory.createTitledBorder("Logical Addresses (comma/space separated, unlimited count)"));

        paramsCard.add(paramsRow, BorderLayout.NORTH);
        paramsCard.add(addrScroll, BorderLayout.CENTER);

        // Page Table card
        pageTableModel = new DefaultTableModel(new Object[] { "Page #", "Frame # (-1 if unmapped)", "Valid (true/false)" }, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Boolean.class : Object.class;
            }
        };
        pageTable = new JTable(pageTableModel);
        Theme.styleTable(pageTable);

        JPanel ptCard = Theme.createCard("Page Table (Dynamic Rows)");
        JPanel ptBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        ptBtns.setOpaque(false);
        JButton addPageBtn = Theme.createSecondaryButton("+ Add Page");
        JButton removePageBtn = Theme.createSecondaryButton("Remove Selected");
        JButton clearPtBtn = Theme.createDangerButton("Clear");
        ptBtns.add(addPageBtn);
        ptBtns.add(removePageBtn);
        ptBtns.add(clearPtBtn);

        ptCard.add(ptBtns, BorderLayout.NORTH);
        ptCard.add(Theme.wrapTable(pageTable, 340, 170), BorderLayout.CENTER);

        JPanel topSplit = new JPanel(new BorderLayout(8, 8));
        topSplit.setOpaque(false);
        topSplit.add(paramsCard, BorderLayout.NORTH);
        topSplit.add(ptCard, BorderLayout.CENTER);

        // Output card
        JPanel metrics = new JPanel(new GridLayout(1, 3, 8, 8));
        metrics.setOpaque(false);
        metrics.add(Theme.createMetricCard("Valid Translations", validCountLabel, Theme.SUCCESS));
        metrics.add(Theme.createMetricCard("Page Faults / Invalid", faultCountLabel, Theme.DANGER));
        metrics.add(Theme.createMetricCard("Total Physical Memory", ramCapacityLabel, Theme.PRIMARY));

        frameMapPanel = new MemoryMapPanel();
        JScrollPane mapScroll = new JScrollPane(frameMapPanel);
        mapScroll.setBorder(BorderFactory.createTitledBorder("Physical Memory Frame Map"));
        mapScroll.setPreferredSize(new java.awt.Dimension(700, 138));

        resultModel = new DefaultTableModel(
                new Object[] { "Logical Addr", "Page #", "Offset", "Frame #", "Physical Addr", "Status", "Calculation" }, 0) {
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

        JPanel outCard = Theme.createCard("Translation & Physical Frame Results");
        outCard.add(metrics, BorderLayout.NORTH);
        outCard.add(outCenter, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, outCard);
        split.setResizeWeight(0.46);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        addPageBtn.addActionListener(e -> addPageRow());
        removePageBtn.addActionListener(e -> removeSelectedRows());
        clearPtBtn.addActionListener(e -> pageTableModel.setRowCount(0));
        smallBtn.addActionListener(e -> loadSmallSample());
        largeBtn.addActionListener(e -> loadLargeSample());
        translateBtn.addActionListener(e -> runTranslation());

        loadSmallSample();
    }

    private void addPageRow() {
        int nextPage = pageTableModel.getRowCount();
        pageTableModel.addRow(new Object[] { String.valueOf(nextPage), String.valueOf(nextPage), Boolean.TRUE });
    }

    private void removeSelectedRows() {
        Theme.commitTableEdits(pageTable);
        int[] sel = pageTable.getSelectedRows();
        for (int i = sel.length - 1; i >= 0; i--) {
            pageTableModel.removeRow(sel[i]);
        }
    }

    private void loadSmallSample() {
        pageSizeField.setText("1024");
        numFramesField.setText("8");
        pageTableModel.setRowCount(0);
        pageTableModel.addRow(new Object[] { "0", "5", Boolean.TRUE });
        pageTableModel.addRow(new Object[] { "1", "2", Boolean.TRUE });
        pageTableModel.addRow(new Object[] { "2", "6", Boolean.TRUE });
        pageTableModel.addRow(new Object[] { "3", "-1", Boolean.FALSE });
        pageTableModel.addRow(new Object[] { "4", "1", Boolean.TRUE });
        logicalAddrArea.setText("0, 512, 1024, 2500, 3100, 4200, 7000");
        runTranslation();
    }

    private void loadLargeSample() {
        pageSizeField.setText("4096");
        numFramesField.setText("128");
        pageTableModel.setRowCount(0);
        Random rng = new Random(77);
        List<Integer> frames = new ArrayList<>();
        for (int f = 0; f < 128; f++) {
            frames.add(f);
        }
        java.util.Collections.shuffle(frames, rng);
        for (int p = 0; p < 64; p++) {
            boolean valid = p % 9 != 0;
            pageTableModel.addRow(new Object[] {
                    String.valueOf(p),
                    valid ? String.valueOf(frames.get(p)) : "-1",
                    valid
            });
        }
        StringBuilder addrs = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            if (i > 0) {
                addrs.append(", ");
            }
            addrs.append(rng.nextInt(68 * 4096));
        }
        logicalAddrArea.setText(addrs.toString());
        runTranslation();
    }

    private List<Long> parseLogicalAddresses() throws ValidationException {
        String raw = InputValidator.requireNonBlank(logicalAddrArea.getText(), "Logical Addresses");
        String[] tokens = raw.split("[,;\\s]+");
        List<Long> addrs = new ArrayList<>();
        for (String t : tokens) {
            String clean = t.trim();
            if (clean.isEmpty()) {
                continue;
            }
            addrs.add(InputValidator.parseNonNegativeLong(clean, "Logical Address \"" + clean + "\""));
        }
        if (addrs.isEmpty()) {
            throw new ValidationException("Enter at least one logical address.");
        }
        return addrs;
    }

    private void runTranslation() {
        try {
            Theme.commitTableEdits(pageTable);
            int pageSize = InputValidator.parsePositiveInt(pageSizeField.getText(), "Page Size");
            int numFrames = InputValidator.parsePositiveInt(numFramesField.getText(), "Physical Frames");

            int rows = pageTableModel.getRowCount();
            if (rows == 0) {
                throw new ValidationException("Page Table cannot be empty.");
            }
            List<PageTableEntry> entries = new ArrayList<>(rows);
            for (int r = 0; r < rows; r++) {
                int pNum = InputValidator.parseNonNegativeInt(
                        strAt(r, 0), "Row " + (r + 1) + " Page #");
                Object validObj = pageTableModel.getValueAt(r, 2);
                boolean valid = validObj instanceof Boolean ? (Boolean) validObj
                        : Boolean.parseBoolean(String.valueOf(validObj));
                int fNum = valid
                        ? InputValidator.parseNonNegativeInt(strAt(r, 1), "Page " + pNum + " Frame #")
                        : -1;
                entries.add(new PageTableEntry(pNum, fNum, valid));
            }

            List<Long> addrs = parseLogicalAddresses();
            PagingSimulationResult res = engine.translate(pageSize, numFrames, entries, addrs);
            displayResult(res);
        } catch (ValidationException ex) {
            Theme.showError(this, ex.getMessage());
        }
    }

    private String strAt(int r, int c) {
        Object val = pageTableModel.getValueAt(r, c);
        return val == null ? "" : val.toString();
    }

    private void displayResult(PagingSimulationResult res) {
        validCountLabel.setText(String.valueOf(res.getValidCount()));
        faultCountLabel.setText(String.valueOf(res.getFaultCount()));
        long totalRam = (long) res.getPageSize() * res.getNumFrames();
        ramCapacityLabel.setText(totalRam + " B (" + res.getNumFrames() + " frames)");

        resultModel.setRowCount(0);
        for (TranslationResult tr : res.getTranslations()) {
            resultModel.addRow(new Object[] {
                    tr.getLogicalAddress(),
                    tr.getPageNumber(),
                    tr.getOffset(),
                    tr.isValid() ? tr.getFrameNumber() : "-",
                    tr.isValid() ? tr.getPhysicalAddress() : "TRAP / FAULT",
                    tr.getStatus(),
                    tr.getExplanation()
            });
        }

        // Build frame map visual
        Map<Integer, Integer> frameToPage = new HashMap<>();
        for (PageTableEntry e : res.getPageTable()) {
            if (e.isValid()) {
                frameToPage.put(e.getFrameNumber(), e.getPageNumber());
            }
        }
        int framesToShow = Math.min(res.getNumFrames(), 64);
        List<MemoryMapPanel.BlockSegment> segs = new ArrayList<>();
        for (int f = 0; f < framesToShow; f++) {
            Integer page = frameToPage.get(f);
            if (page == null) {
                segs.add(new MemoryMapPanel.BlockSegment("F" + f, "Free", res.getPageSize(), 0, Color.LIGHT_GRAY, true));
            } else {
                segs.add(new MemoryMapPanel.BlockSegment("F" + f, "Page " + page, res.getPageSize(), 0,
                        Theme.colorForIndex(page), false));
            }
        }
        frameMapPanel.setSegments(segs, "No frames configured.");
    }
}
