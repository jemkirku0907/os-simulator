package ossim.model;

import java.util.List;

/**
 * Data structures for Module 2: Banker's Algorithm.
 */
public final class BankerModels {

    private BankerModels() {
    }

    public static final class SafetyStep {
        private final int stepNumber;
        private final String processName;
        private final boolean granted;
        private final int[] needSnapshot;
        private final int[] workBefore;
        private final int[] workAfter;
        private final String explanation;

        public SafetyStep(int stepNumber, String processName, boolean granted,
                int[] needSnapshot, int[] workBefore, int[] workAfter, String explanation) {
            this.stepNumber = stepNumber;
            this.processName = processName;
            this.granted = granted;
            this.needSnapshot = needSnapshot;
            this.workBefore = workBefore;
            this.workAfter = workAfter;
            this.explanation = explanation;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public String getProcessName() {
            return processName;
        }

        public boolean isGranted() {
            return granted;
        }

        public int[] getNeedSnapshot() {
            return needSnapshot;
        }

        public int[] getWorkBefore() {
            return workBefore;
        }

        public int[] getWorkAfter() {
            return workAfter;
        }

        public String getExplanation() {
            return explanation;
        }
    }

    public static final class BankerResult {
        private final int processCount;
        private final int resourceCount;
        private final int[][] allocation;
        private final int[][] max;
        private final int[][] need;
        private final int[] available;
        private final boolean safe;
        private final List<String> safeSequence;
        private final List<SafetyStep> steps;
        private final String summaryMessage;

        public BankerResult(int processCount, int resourceCount, int[][] allocation, int[][] max,
                int[][] need, int[] available, boolean safe, List<String> safeSequence,
                List<SafetyStep> steps, String summaryMessage) {
            this.processCount = processCount;
            this.resourceCount = resourceCount;
            this.allocation = allocation;
            this.max = max;
            this.need = need;
            this.available = available;
            this.safe = safe;
            this.safeSequence = safeSequence;
            this.steps = steps;
            this.summaryMessage = summaryMessage;
        }

        public int getProcessCount() {
            return processCount;
        }

        public int getResourceCount() {
            return resourceCount;
        }

        public int[][] getAllocation() {
            return allocation;
        }

        public int[][] getMax() {
            return max;
        }

        public int[][] getNeed() {
            return need;
        }

        public int[] getAvailable() {
            return available;
        }

        public boolean isSafe() {
            return safe;
        }

        public List<String> getSafeSequence() {
            return safeSequence;
        }

        public List<SafetyStep> getSteps() {
            return steps;
        }

        public String getSummaryMessage() {
            return summaryMessage;
        }
    }
}
