package ossim.model;

import java.util.List;

/**
 * Data structures for Module 6: Fragmentation.
 */
public final class FragmentationModels {

    private FragmentationModels() {
    }

    public enum FitStrategy {
        FIRST_FIT("First Fit"),
        BEST_FIT("Best Fit"),
        WORST_FIT("Worst Fit"),
        NEXT_FIT("Next Fit");

        private final String displayName;

        FitStrategy(String displayName) {
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

    public enum PartitionMode {
        FIXED_PARTITIONS("Fixed Partitions (Internal + External)"),
        DYNAMIC_PARTITIONS("Dynamic Partitions (Variable-Size / External)");

        private final String displayName;

        PartitionMode(String displayName) {
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

    public static final class MemoryBlockInput {
        private final String blockId;
        private final int size;

        public MemoryBlockInput(String blockId, int size) {
            this.blockId = blockId;
            this.size = size;
        }

        public String getBlockId() {
            return blockId;
        }

        public int getSize() {
            return size;
        }
    }

    public static final class MemoryProcessInput {
        private final String processId;
        private final int requestedSize;

        public MemoryProcessInput(String processId, int requestedSize) {
            this.processId = processId;
            this.requestedSize = requestedSize;
        }

        public String getProcessId() {
            return processId;
        }

        public int getRequestedSize() {
            return requestedSize;
        }
    }

    public static final class FinalRegion {
        private final String label;
        private final int totalSize;
        private final int usedSize;
        private final int internalFrag;
        private final boolean freeHole;
        private final String assignedProcess;

        public FinalRegion(String label, int totalSize, int usedSize, int internalFrag,
                boolean freeHole, String assignedProcess) {
            this.label = label;
            this.totalSize = totalSize;
            this.usedSize = usedSize;
            this.internalFrag = internalFrag;
            this.freeHole = freeHole;
            this.assignedProcess = assignedProcess;
        }

        public String getLabel() {
            return label;
        }

        public int getTotalSize() {
            return totalSize;
        }

        public int getUsedSize() {
            return usedSize;
        }

        public int getInternalFrag() {
            return internalFrag;
        }

        public boolean isFreeHole() {
            return freeHole;
        }

        public String getAssignedProcess() {
            return assignedProcess;
        }
    }

    public static final class ProcessAllocation {
        private final String processId;
        private final int requestedSize;
        private final boolean allocated;
        private final String assignedBlock;
        private final int blockSize;
        private final int internalFragmentation;
        private final String statusMessage;

        public ProcessAllocation(String processId, int requestedSize, boolean allocated,
                String assignedBlock, int blockSize, int internalFragmentation, String statusMessage) {
            this.processId = processId;
            this.requestedSize = requestedSize;
            this.allocated = allocated;
            this.assignedBlock = assignedBlock;
            this.blockSize = blockSize;
            this.internalFragmentation = internalFragmentation;
            this.statusMessage = statusMessage;
        }

        public String getProcessId() {
            return processId;
        }

        public int getRequestedSize() {
            return requestedSize;
        }

        public boolean isAllocated() {
            return allocated;
        }

        public String getAssignedBlock() {
            return assignedBlock;
        }

        public int getBlockSize() {
            return blockSize;
        }

        public int getInternalFragmentation() {
            return internalFragmentation;
        }

        public String getStatusMessage() {
            return statusMessage;
        }
    }

    public static final class FragmentationResult {
        private final FitStrategy strategy;
        private final PartitionMode mode;
        private final List<ProcessAllocation> processAllocations;
        private final List<FinalRegion> finalRegions;
        private final int totalMemory;
        private final int totalUsedByProcesses;
        private final int totalInternalFragmentation;
        private final int totalFreeMemory;
        private final int externalFragmentation;
        private final int allocatedProcessCount;
        private final int unallocatedProcessCount;
        private final double utilizationPercent;
        private final String diagnosis;

        public FragmentationResult(FitStrategy strategy, PartitionMode mode,
                List<ProcessAllocation> processAllocations, List<FinalRegion> finalRegions,
                int totalMemory, int totalUsedByProcesses, int totalInternalFragmentation,
                int totalFreeMemory, int externalFragmentation, int allocatedProcessCount,
                int unallocatedProcessCount, double utilizationPercent, String diagnosis) {
            this.strategy = strategy;
            this.mode = mode;
            this.processAllocations = processAllocations;
            this.finalRegions = finalRegions;
            this.totalMemory = totalMemory;
            this.totalUsedByProcesses = totalUsedByProcesses;
            this.totalInternalFragmentation = totalInternalFragmentation;
            this.totalFreeMemory = totalFreeMemory;
            this.externalFragmentation = externalFragmentation;
            this.allocatedProcessCount = allocatedProcessCount;
            this.unallocatedProcessCount = unallocatedProcessCount;
            this.utilizationPercent = utilizationPercent;
            this.diagnosis = diagnosis;
        }

        public FitStrategy getStrategy() {
            return strategy;
        }

        public PartitionMode getMode() {
            return mode;
        }

        public List<ProcessAllocation> getProcessAllocations() {
            return processAllocations;
        }

        public List<FinalRegion> getFinalRegions() {
            return finalRegions;
        }

        public int getTotalMemory() {
            return totalMemory;
        }

        public int getTotalUsedByProcesses() {
            return totalUsedByProcesses;
        }

        public int getTotalInternalFragmentation() {
            return totalInternalFragmentation;
        }

        public int getTotalFreeMemory() {
            return totalFreeMemory;
        }

        public int getExternalFragmentation() {
            return externalFragmentation;
        }

        public int getAllocatedProcessCount() {
            return allocatedProcessCount;
        }

        public int getUnallocatedProcessCount() {
            return unallocatedProcessCount;
        }

        public double getUtilizationPercent() {
            return utilizationPercent;
        }

        public String getDiagnosis() {
            return diagnosis;
        }
    }
}
