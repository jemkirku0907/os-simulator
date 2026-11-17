package ossim.model;

import java.util.List;

/**
 * Data structures for Module 5: Segmentation.
 */
public final class SegmentationModels {

    private SegmentationModels() {
    }

    public static final class SegmentEntry {
        private final int segmentNumber;
        private final String name;
        private final long base;
        private final long limit;

        public SegmentEntry(int segmentNumber, String name, long base, long limit) {
            this.segmentNumber = segmentNumber;
            this.name = name;
            this.base = base;
            this.limit = limit;
        }

        public int getSegmentNumber() {
            return segmentNumber;
        }

        public String getName() {
            return name;
        }

        public long getBase() {
            return base;
        }

        public long getLimit() {
            return limit;
        }

        public long getEndAddressExclusive() {
            return base + limit;
        }
    }

    public static final class SegmentTranslation {
        private final int segmentNumber;
        private final long offset;
        private final long physicalAddress;
        private final boolean valid;
        private final String status;
        private final String detail;

        public SegmentTranslation(int segmentNumber, long offset, long physicalAddress,
                boolean valid, String status, String detail) {
            this.segmentNumber = segmentNumber;
            this.offset = offset;
            this.physicalAddress = physicalAddress;
            this.valid = valid;
            this.status = status;
            this.detail = detail;
        }

        public int getSegmentNumber() {
            return segmentNumber;
        }

        public long getOffset() {
            return offset;
        }

        public long getPhysicalAddress() {
            return physicalAddress;
        }

        public boolean isValid() {
            return valid;
        }

        public String getStatus() {
            return status;
        }

        public String getDetail() {
            return detail;
        }
    }

    public static final class SegmentationResult {
        private final List<SegmentEntry> segments;
        private final List<SegmentTranslation> translations;
        private final List<String> overlapWarnings;
        private final int validCount;
        private final int errorCount;

        public SegmentationResult(List<SegmentEntry> segments, List<SegmentTranslation> translations,
                List<String> overlapWarnings, int validCount, int errorCount) {
            this.segments = segments;
            this.translations = translations;
            this.overlapWarnings = overlapWarnings;
            this.validCount = validCount;
            this.errorCount = errorCount;
        }

        public List<SegmentEntry> getSegments() {
            return segments;
        }

        public List<SegmentTranslation> getTranslations() {
            return translations;
        }

        public List<String> getOverlapWarnings() {
            return overlapWarnings;
        }

        public int getValidCount() {
            return validCount;
        }

        public int getErrorCount() {
            return errorCount;
        }
    }
}
