package ossim.model;

import java.util.List;

/**
 * Data structures for Module 3: Paging and Address Translation.
 */
public final class PagingModels {

    private PagingModels() {
    }

    public static final class PageTableEntry {
        private final int pageNumber;
        private final int frameNumber; // -1 if unmapped
        private final boolean valid;

        public PageTableEntry(int pageNumber, int frameNumber, boolean valid) {
            this.pageNumber = pageNumber;
            this.frameNumber = frameNumber;
            this.valid = valid;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getFrameNumber() {
            return frameNumber;
        }

        public boolean isValid() {
            return valid;
        }
    }

    public static final class TranslationResult {
        private final long logicalAddress;
        private final int pageNumber;
        private final int offset;
        private final int frameNumber;
        private final long physicalAddress;
        private final boolean valid;
        private final String status;
        private final String explanation;

        public TranslationResult(long logicalAddress, int pageNumber, int offset, int frameNumber,
                long physicalAddress, boolean valid, String status, String explanation) {
            this.logicalAddress = logicalAddress;
            this.pageNumber = pageNumber;
            this.offset = offset;
            this.frameNumber = frameNumber;
            this.physicalAddress = physicalAddress;
            this.valid = valid;
            this.status = status;
            this.explanation = explanation;
        }

        public long getLogicalAddress() {
            return logicalAddress;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getOffset() {
            return offset;
        }

        public int getFrameNumber() {
            return frameNumber;
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

        public String getExplanation() {
            return explanation;
        }
    }

    public static final class PagingSimulationResult {
        private final int pageSize;
        private final int numFrames;
        private final List<PageTableEntry> pageTable;
        private final List<TranslationResult> translations;
        private final int validCount;
        private final int faultCount;

        public PagingSimulationResult(int pageSize, int numFrames, List<PageTableEntry> pageTable,
                List<TranslationResult> translations, int validCount, int faultCount) {
            this.pageSize = pageSize;
            this.numFrames = numFrames;
            this.pageTable = pageTable;
            this.translations = translations;
            this.validCount = validCount;
            this.faultCount = faultCount;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getNumFrames() {
            return numFrames;
        }

        public List<PageTableEntry> getPageTable() {
            return pageTable;
        }

        public List<TranslationResult> getTranslations() {
            return translations;
        }

        public int getValidCount() {
            return validCount;
        }

        public int getFaultCount() {
            return faultCount;
        }
    }
}
