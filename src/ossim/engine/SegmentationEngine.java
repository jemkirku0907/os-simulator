package ossim.engine;

import ossim.model.SegmentationModels.SegmentEntry;
import ossim.model.SegmentationModels.SegmentTranslation;
import ossim.model.SegmentationModels.SegmentationResult;
import ossim.model.ValidationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Engine for Module 5: Segmentation.
 * Validates segment tables, detects physical memory overlaps, and performs
 * logical (segment, offset) -> physical address translation with bounds checking.
 */
public final class SegmentationEngine {

    public static final class LogicalAddressQuery {
        private final int segmentNumber;
        private final long offset;

        public LogicalAddressQuery(int segmentNumber, long offset) {
            this.segmentNumber = segmentNumber;
            this.offset = offset;
        }

        public int getSegmentNumber() {
            return segmentNumber;
        }

        public long getOffset() {
            return offset;
        }
    }

    public SegmentationResult simulate(List<SegmentEntry> segments, List<LogicalAddressQuery> queries)
            throws ValidationException {
        if (segments == null || segments.isEmpty()) {
            throw new ValidationException("Add at least one segment to the Segment Table.");
        }
        if (queries == null || queries.isEmpty()) {
            throw new ValidationException("Provide at least one logical address (Segment, Offset) to validate.");
        }

        Map<Integer, SegmentEntry> segmentMap = new HashMap<>();
        List<SegmentEntry> cleanSegments = new ArrayList<>(segments.size());

        for (int i = 0; i < segments.size(); i++) {
            SegmentEntry seg = segments.get(i);
            if (seg.getSegmentNumber() < 0) {
                throw new ValidationException("Segment # cannot be negative (row " + (i + 1) + ").");
            }
            if (seg.getBase() < 0) {
                throw new ValidationException("Base address for Segment " + seg.getSegmentNumber() + " cannot be negative.");
            }
            if (seg.getLimit() <= 0) {
                throw new ValidationException("Limit for Segment " + seg.getSegmentNumber() + " must be greater than 0.");
            }
            if (segmentMap.containsKey(seg.getSegmentNumber())) {
                throw new ValidationException("Duplicate Segment # " + seg.getSegmentNumber() + " in Segment Table.");
            }
            String cleanName = (seg.getName() == null || seg.getName().trim().isEmpty())
                    ? "Seg-" + seg.getSegmentNumber()
                    : seg.getName().trim();
            SegmentEntry normalized = new SegmentEntry(seg.getSegmentNumber(), cleanName, seg.getBase(), seg.getLimit());
            segmentMap.put(seg.getSegmentNumber(), normalized);
            cleanSegments.add(normalized);
        }

        // Detect overlapping segments in physical memory
        List<SegmentEntry> sortedByBase = new ArrayList<>(cleanSegments);
        sortedByBase.sort(Comparator.comparingLong(SegmentEntry::getBase).thenComparingInt(SegmentEntry::getSegmentNumber));
        List<String> overlapWarnings = new ArrayList<>();
        for (int i = 0; i < sortedByBase.size() - 1; i++) {
            SegmentEntry curr = sortedByBase.get(i);
            for (int j = i + 1; j < sortedByBase.size(); j++) {
                SegmentEntry next = sortedByBase.get(j);
                if (next.getBase() < curr.getEndAddressExclusive()) {
                    overlapWarnings.add("Overlap: Segment " + curr.getSegmentNumber() + " [" + curr.getBase() + ".."
                            + (curr.getEndAddressExclusive() - 1) + "] overlaps with Segment "
                            + next.getSegmentNumber() + " [" + next.getBase() + ".."
                            + (next.getEndAddressExclusive() - 1) + "].");
                } else {
                    break;
                }
            }
        }

        List<SegmentTranslation> translations = new ArrayList<>(queries.size());
        int validCount = 0;
        int errorCount = 0;

        for (LogicalAddressQuery q : queries) {
            SegmentEntry entry = segmentMap.get(q.getSegmentNumber());
            if (entry == null) {
                errorCount++;
                translations.add(new SegmentTranslation(
                        q.getSegmentNumber(),
                        q.getOffset(),
                        -1,
                        false,
                        "INVALID SEGMENT",
                        "Segment " + q.getSegmentNumber() + " does not exist in the Segment Table."));
            } else if (q.getOffset() < 0) {
                errorCount++;
                translations.add(new SegmentTranslation(
                        q.getSegmentNumber(),
                        q.getOffset(),
                        -1,
                        false,
                        "OUT OF RANGE (Negative)",
                        "Offset " + q.getOffset() + " cannot be negative."));
            } else if (q.getOffset() >= entry.getLimit()) {
                errorCount++;
                translations.add(new SegmentTranslation(
                        q.getSegmentNumber(),
                        q.getOffset(),
                        -1,
                        false,
                        "TRAP: ADDRESSING ERROR",
                        "Offset " + q.getOffset() + " >= Limit " + entry.getLimit()
                                + " for Segment " + entry.getSegmentNumber() + " (" + entry.getName() + ")."));
            } else {
                validCount++;
                long phys = entry.getBase() + q.getOffset();
                translations.add(new SegmentTranslation(
                        q.getSegmentNumber(),
                        q.getOffset(),
                        phys,
                        true,
                        "VALID",
                        "Base " + entry.getBase() + " + Offset " + q.getOffset() + " = " + phys));
            }
        }

        return new SegmentationResult(cleanSegments, translations, overlapWarnings, validCount, errorCount);
    }
}
