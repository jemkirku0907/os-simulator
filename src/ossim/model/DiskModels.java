package ossim.model;

import java.util.List;

/**
 * Data structures for Module 7: Disk Scheduling.
 */
public final class DiskModels {

    private DiskModels() {
    }

    public enum Algorithm {
        FCFS("FCFS (First-Come, First-Served)"),
        SSTF("SSTF (Shortest Seek Time First)"),
        SCAN("SCAN (Elevator)"),
        C_SCAN("C-SCAN (Circular SCAN)"),
        LOOK("LOOK"),
        C_LOOK("C-LOOK (Circular LOOK)");

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

    public enum Direction {
        UP("Increasing (Toward Max Cylinder)"),
        DOWN("Decreasing (Toward 0)");

        private final String displayName;

        Direction(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static final class SeekStep {
        private final int stepNumber;
        private final int fromTrack;
        private final int toTrack;
        private final int distance;
        private final boolean boundaryJump;
        private final String note;

        public SeekStep(int stepNumber, int fromTrack, int toTrack, int distance, boolean boundaryJump, String note) {
            this.stepNumber = stepNumber;
            this.fromTrack = fromTrack;
            this.toTrack = toTrack;
            this.distance = distance;
            this.boundaryJump = boundaryJump;
            this.note = note;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public int getFromTrack() {
            return fromTrack;
        }

        public int getToTrack() {
            return toTrack;
        }

        public int getDistance() {
            return distance;
        }

        public boolean isBoundaryJump() {
            return boundaryJump;
        }

        public String getNote() {
            return note;
        }
    }

    public static final class DiskResult {
        private final Algorithm algorithm;
        private final int initialHead;
        private final int minTrack;
        private final int maxTrack;
        private final Direction direction;
        private final List<Integer> pathTracks; // starts with initialHead
        private final List<Integer> servicedRequests;
        private final List<SeekStep> steps;
        private final long totalHeadMovement;
        private final double averageSeekLength;

        public DiskResult(Algorithm algorithm, int initialHead, int minTrack, int maxTrack, Direction direction,
                List<Integer> pathTracks, List<Integer> servicedRequests, List<SeekStep> steps,
                long totalHeadMovement, double averageSeekLength) {
            this.algorithm = algorithm;
            this.initialHead = initialHead;
            this.minTrack = minTrack;
            this.maxTrack = maxTrack;
            this.direction = direction;
            this.pathTracks = pathTracks;
            this.servicedRequests = servicedRequests;
            this.steps = steps;
            this.totalHeadMovement = totalHeadMovement;
            this.averageSeekLength = averageSeekLength;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public int getInitialHead() {
            return initialHead;
        }

        public int getMinTrack() {
            return minTrack;
        }

        public int getMaxTrack() {
            return maxTrack;
        }

        public Direction getDirection() {
            return direction;
        }

        public List<Integer> getPathTracks() {
            return pathTracks;
        }

        public List<Integer> getServicedRequests() {
            return servicedRequests;
        }

        public List<SeekStep> getSteps() {
            return steps;
        }

        public long getTotalHeadMovement() {
            return totalHeadMovement;
        }

        public double getAverageSeekLength() {
            return averageSeekLength;
        }
    }
}
