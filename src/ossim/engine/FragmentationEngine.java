package ossim.engine;

import ossim.model.FragmentationModels.FinalRegion;
import ossim.model.FragmentationModels.FitStrategy;
import ossim.model.FragmentationModels.FragmentationResult;
import ossim.model.FragmentationModels.MemoryBlockInput;
import ossim.model.FragmentationModels.MemoryProcessInput;
import ossim.model.FragmentationModels.PartitionMode;
import ossim.model.FragmentationModels.ProcessAllocation;
import ossim.model.ValidationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Engine for Module 6: Memory Allocation and Fragmentation.
 * Supports Fixed and Dynamic partitioning with First Fit, Best Fit, Worst Fit, and Next Fit.
 */
public final class FragmentationEngine {

    private static final class RegionNode {
        String label;
        int size;
        boolean free;
        String assignedProcess;

        RegionNode(String label, int size, boolean free, String assignedProcess) {
            this.label = label;
            this.size = size;
            this.free = free;
            this.assignedProcess = assignedProcess;
        }
    }

    public FragmentationResult simulate(
            List<MemoryBlockInput> blocks,
            List<MemoryProcessInput> processes,
            FitStrategy strategy,
            PartitionMode mode) throws ValidationException {

        if (blocks == null || blocks.isEmpty()) {
            throw new ValidationException("Add at least one memory block.");
        }
        if (processes == null || processes.isEmpty()) {
            throw new ValidationException("Add at least one process to allocate.");
        }

        Set<String> blockIds = new HashSet<>();
        List<RegionNode> regions = new ArrayList<>(blocks.size());
        int totalMemory = 0;

        for (int i = 0; i < blocks.size(); i++) {
            MemoryBlockInput b = blocks.get(i);
            String id = (b.getBlockId() == null || b.getBlockId().trim().isEmpty())
                    ? "B" + (i + 1)
                    : b.getBlockId().trim();
            if (!blockIds.add(id)) {
                throw new ValidationException("Duplicate Memory Block ID \"" + id + "\".");
            }
            if (b.getSize() <= 0) {
                throw new ValidationException("Size for block " + id + " must be greater than 0.");
            }
            totalMemory += b.getSize();
            regions.add(new RegionNode(id, b.getSize(), true, null));
        }

        Set<String> procIds = new HashSet<>();
        List<MemoryProcessInput> cleanProcs = new ArrayList<>(processes.size());
        for (int i = 0; i < processes.size(); i++) {
            MemoryProcessInput p = processes.get(i);
            String id = (p.getProcessId() == null || p.getProcessId().trim().isEmpty())
                    ? "P" + (i + 1)
                    : p.getProcessId().trim();
            if (!procIds.add(id)) {
                throw new ValidationException("Duplicate Process ID \"" + id + "\".");
            }
            if (p.getRequestedSize() <= 0) {
                throw new ValidationException("Requested memory for process " + id + " must be greater than 0.");
            }
            cleanProcs.add(new MemoryProcessInput(id, p.getRequestedSize()));
        }

        List<ProcessAllocation> allocations = new ArrayList<>(cleanProcs.size());
        int nextFitCursor = 0;
        int totalUsed = 0;
        int totalInternalFrag = 0;
        int allocatedCount = 0;
        int unallocatedCount = 0;
        int smallestUnallocatedRequest = Integer.MAX_VALUE;

        for (MemoryProcessInput proc : cleanProcs) {
            int req = proc.getRequestedSize();
            int chosenIdx = selectBlockIndex(regions, req, strategy, nextFitCursor);

            if (chosenIdx == -1) {
                unallocatedCount++;
                if (req < smallestUnallocatedRequest) {
                    smallestUnallocatedRequest = req;
                }
                allocations.add(new ProcessAllocation(
                        proc.getProcessId(), req, false, "-", 0, 0,
                        "Not allocated (no single free block >= " + req + ")"));
                continue;
            }

            allocatedCount++;
            RegionNode target = regions.get(chosenIdx);
            String blockLabel = target.label;
            int blockSize = target.size;

            if (mode == PartitionMode.FIXED_PARTITIONS) {
                int internalFrag = blockSize - req;
                target.free = false;
                target.assignedProcess = proc.getProcessId();
                totalUsed += req;
                totalInternalFrag += internalFrag;
                allocations.add(new ProcessAllocation(
                        proc.getProcessId(), req, true, blockLabel, blockSize, internalFrag,
                        "Allocated in " + blockLabel + " (Internal Frag = " + internalFrag + ")"));
                nextFitCursor = (chosenIdx + 1) % regions.size();
            } else {
                // Dynamic partitioning splits the chosen free hole
                int remainder = blockSize - req;
                RegionNode allocatedNode = new RegionNode(blockLabel, req, false, proc.getProcessId());
                regions.set(chosenIdx, allocatedNode);
                if (remainder > 0) {
                    regions.add(chosenIdx + 1, new RegionNode(blockLabel + "-free", remainder, true, null));
                    nextFitCursor = (chosenIdx + 1) % regions.size();
                } else {
                    nextFitCursor = (chosenIdx + 1) % regions.size();
                }
                totalUsed += req;
                allocations.add(new ProcessAllocation(
                        proc.getProcessId(), req, true, blockLabel, req, 0,
                        "Allocated exact " + req + " from " + blockLabel
                                + (remainder > 0 ? " (remaining hole = " + remainder + ")" : "")));
            }
        }

        // Compute final regions and free/external fragmentation stats
        List<FinalRegion> finalRegions = new ArrayList<>(regions.size());
        int totalFreeMemory = 0;
        int freeHoleCount = 0;

        // Track used size per fixed partition via allocations map
        for (RegionNode r : regions) {
            if (r.free) {
                totalFreeMemory += r.size;
                freeHoleCount++;
                finalRegions.add(new FinalRegion(r.label, r.size, 0, 0, true, null));
            } else {
                int used = r.size;
                int intFrag = 0;
                if (mode == PartitionMode.FIXED_PARTITIONS) {
                    for (ProcessAllocation pa : allocations) {
                        if (pa.isAllocated() && pa.getProcessId().equals(r.assignedProcess)) {
                            used = pa.getRequestedSize();
                            intFrag = pa.getInternalFragmentation();
                            break;
                        }
                    }
                }
                finalRegions.add(new FinalRegion(r.label, r.size, used, intFrag, false, r.assignedProcess));
            }
        }

        // External fragmentation exists when total free memory across non-contiguous holes
        // is sufficient for an unallocated process that still failed to find a single large enough hole.
        boolean externalFragPresent = unallocatedCount > 0 && totalFreeMemory >= smallestUnallocatedRequest;
        int externalFragmentation = externalFragPresent ? totalFreeMemory : 0;
        double utilization = totalMemory > 0 ? (totalUsed * 100.0) / totalMemory : 0.0;

        StringBuilder diagnosis = new StringBuilder();
        if (totalInternalFrag > 0) {
            diagnosis.append("Internal Fragmentation = ").append(totalInternalFrag)
                    .append(" units (unused space inside fixed partitions). ");
        } else {
            diagnosis.append("Internal Fragmentation = 0 units. ");
        }

        if (externalFragPresent) {
            diagnosis.append("External Fragmentation = ").append(externalFragmentation)
                    .append(" units across ").append(freeHoleCount)
                    .append(" free hole(s): total free memory >= smallest waiting process (")
                    .append(smallestUnallocatedRequest).append("), but not contiguous.");
        } else if (unallocatedCount > 0) {
            diagnosis.append("Total Free Memory = ").append(totalFreeMemory)
                    .append(" units (insufficient total free memory for waiting process of size ")
                    .append(smallestUnallocatedRequest).append(").");
        } else {
            diagnosis.append("All processes allocated! Remaining free memory across ")
                    .append(freeHoleCount).append(" hole(s) = ").append(totalFreeMemory).append(" units.");
        }

        return new FragmentationResult(
                strategy,
                mode,
                allocations,
                finalRegions,
                totalMemory,
                totalUsed,
                totalInternalFrag,
                totalFreeMemory,
                externalFragmentation,
                allocatedCount,
                unallocatedCount,
                utilization,
                diagnosis.toString());
    }

    private int selectBlockIndex(List<RegionNode> regions, int req, FitStrategy strategy, int nextFitCursor) {
        int n = regions.size();
        int chosen = -1;

        switch (strategy) {
            case FIRST_FIT:
                for (int i = 0; i < n; i++) {
                    if (regions.get(i).free && regions.get(i).size >= req) {
                        return i;
                    }
                }
                return -1;

            case NEXT_FIT:
                for (int offset = 0; offset < n; offset++) {
                    int idx = (nextFitCursor + offset) % n;
                    if (regions.get(idx).free && regions.get(idx).size >= req) {
                        return idx;
                    }
                }
                return -1;

            case BEST_FIT:
                int bestSize = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    RegionNode r = regions.get(i);
                    if (r.free && r.size >= req && r.size < bestSize) {
                        bestSize = r.size;
                        chosen = i;
                    }
                }
                return chosen;

            case WORST_FIT:
                int worstSize = -1;
                for (int i = 0; i < n; i++) {
                    RegionNode r = regions.get(i);
                    if (r.free && r.size >= req && r.size > worstSize) {
                        worstSize = r.size;
                        chosen = i;
                    }
                }
                return chosen;

            default:
                return -1;
        }
    }
}
