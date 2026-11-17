package ossim.model;

import java.util.List;

/**
 * Data structures for Module 4: Page Replacement.
 */
public final class PageReplacementModels {

    private PageReplacementModels() {
    }

    public enum Algorithm {
        FIFO("FIFO (First-In, First-Out)"),
        LRU("LRU (Least Recently Used)"),
        OPTIMAL("Optimal (Belady's OPT)"),
        SECOND_CHANCE("Clock / Second Chance");

        private final String displayName;

        Algorithm(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static final class StepSnapshot {
        private final int stepIndex;
        private final int referencedPage;
        private final int[] framesSnapshot; // -1 represents empty frame
        private final boolean hit;
        private final Integer evictedPage;
        private final String note;

        public StepSnapshot(int stepIndex, int referencedPage, int[] framesSnapshot,
                boolean hit, Integer evictedPage, String note) {
            this.stepIndex = stepIndex;
            this.referencedPage = referencedPage;
            this.framesSnapshot = framesSnapshot;
            this.hit = hit;
            this.evictedPage = evictedPage;
            this.note = note;
        }

        public int getStepIndex() {
            return stepIndex;
        }

        public int getReferencedPage() {
            return referencedPage;
        }

        public int[] getFramesSnapshot() {
            return framesSnapshot;
        }

        public boolean isHit() {
            return hit;
        }

        public Integer getEvictedPage() {
            return evictedPage;
        }

        public String getNote() {
            return note;
        }
    }

    public static final class ReplacementResult {
        private final Algorithm algorithm;
        private final int frameCount;
        private final List<Integer> referenceString;
        private final List<StepSnapshot> steps;
        private final int pageHits;
        private final int pageFaults;
        private final double hitRatioPercent;
        private final double faultRatioPercent;

        public ReplacementResult(Algorithm algorithm, int frameCount, List<Integer> referenceString,
                List<StepSnapshot> steps, int pageHits, int pageFaults,
                double hitRatioPercent, double faultRatioPercent) {
            this.algorithm = algorithm;
            this.frameCount = frameCount;
            this.referenceString = referenceString;
            this.steps = steps;
            this.pageHits = pageHits;
            this.pageFaults = pageFaults;
            this.hitRatioPercent = hitRatioPercent;
            this.faultRatioPercent = faultRatioPercent;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public int getFrameCount() {
            return frameCount;
        }

        public List<Integer> getReferenceString() {
            return referenceString;
        }

        public List<StepSnapshot> getSteps() {
            return steps;
        }

        public int getPageHits() {
            return pageHits;
        }

        public int getPageFaults() {
            return pageFaults;
        }

        public double getHitRatioPercent() {
            return hitRatioPercent;
        }

        public double getFaultRatioPercent() {
            return faultRatioPercent;
        }
    }
}
