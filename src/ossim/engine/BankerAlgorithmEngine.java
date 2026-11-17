package ossim.engine;

import ossim.model.BankerModels.BankerResult;
import ossim.model.BankerModels.SafetyStep;
import ossim.model.ValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Engine for Module 2: Banker's Algorithm (Deadlock Avoidance).
 * Computes the Need matrix, executes the Safety Algorithm, and optionally
 * evaluates whether a resource request from a specific process can be safely granted.
 */
public final class BankerAlgorithmEngine {

    public BankerResult evaluateSafety(int[][] allocation, int[][] max, int[] available)
            throws ValidationException {
        if (allocation == null || max == null || available == null) {
            throw new ValidationException("Allocation, Max, and Available inputs are required.");
        }
        int pCount = allocation.length;
        if (pCount == 0) {
            throw new ValidationException("Number of processes must be at least 1.");
        }
        int rCount = available.length;
        if (rCount == 0) {
            throw new ValidationException("Number of resource types must be at least 1.");
        }
        if (max.length != pCount) {
            throw new ValidationException("Max matrix row count does not match process count.");
        }

        for (int j = 0; j < rCount; j++) {
            if (available[j] < 0) {
                throw new ValidationException("Available R" + j + " cannot be negative.");
            }
        }

        int[][] allocCopy = new int[pCount][rCount];
        int[][] maxCopy = new int[pCount][rCount];
        int[][] need = new int[pCount][rCount];

        for (int i = 0; i < pCount; i++) {
            if (allocation[i].length != rCount || max[i].length != rCount) {
                throw new ValidationException("Row P" + i + " resource column count mismatch.");
            }
            for (int j = 0; j < rCount; j++) {
                if (allocation[i][j] < 0) {
                    throw new ValidationException("Allocation[P" + i + "][R" + j + "] cannot be negative.");
                }
                if (max[i][j] < 0) {
                    throw new ValidationException("Max[P" + i + "][R" + j + "] cannot be negative.");
                }
                if (allocation[i][j] > max[i][j]) {
                    throw new ValidationException(
                            "Allocation exceeds Max at P" + i + ", R" + j
                                    + " (" + allocation[i][j] + " > " + max[i][j] + ").");
                }
                allocCopy[i][j] = allocation[i][j];
                maxCopy[i][j] = max[i][j];
                need[i][j] = max[i][j] - allocation[i][j];
            }
        }

        int[] availCopy = Arrays.copyOf(available, rCount);
        int[] work = Arrays.copyOf(available, rCount);
        boolean[] finish = new boolean[pCount];
        List<String> safeSequence = new ArrayList<>();
        List<SafetyStep> steps = new ArrayList<>();
        int stepCounter = 1;

        boolean progress = true;
        while (safeSequence.size() < pCount && progress) {
            progress = false;
            for (int i = 0; i < pCount; i++) {
                if (finish[i]) {
                    continue;
                }
                if (canSatisfy(need[i], work)) {
                    int[] workBefore = Arrays.copyOf(work, rCount);
                    for (int j = 0; j < rCount; j++) {
                        work[j] += allocCopy[i][j];
                    }
                    int[] workAfter = Arrays.copyOf(work, rCount);
                    finish[i] = true;
                    String pName = "P" + i;
                    safeSequence.add(pName);
                    steps.add(new SafetyStep(
                            stepCounter++,
                            pName,
                            true,
                            Arrays.copyOf(need[i], rCount),
                            workBefore,
                            workAfter,
                            "Need(" + pName + ") <= Work; executes and releases Allocation(" + pName + ")."));
                    progress = true;
                }
            }
        }

        boolean isSafe = safeSequence.size() == pCount;
        StringBuilder summary = new StringBuilder();
        if (isSafe) {
            summary.append("SYSTEM IS SAFE. Safe sequence: ").append(String.join(" -> ", safeSequence));
        } else {
            List<String> blocked = new ArrayList<>();
            for (int i = 0; i < pCount; i++) {
                if (!finish[i]) {
                    blocked.add("P" + i);
                    steps.add(new SafetyStep(
                            stepCounter++,
                            "P" + i,
                            false,
                            Arrays.copyOf(need[i], rCount),
                            Arrays.copyOf(work, rCount),
                            Arrays.copyOf(work, rCount),
                            "Need(P" + i + ") > Work; cannot proceed with available resources."));
                }
            }
            summary.append("SYSTEM IS UNSAFE. Deadlock risk — unfinished processes: ")
                    .append(String.join(", ", blocked));
        }

        return new BankerResult(
                pCount,
                rCount,
                allocCopy,
                maxCopy,
                need,
                availCopy,
                isSafe,
                safeSequence,
                steps,
                summary.toString());
    }

    /**
     * Simulates a Resource-Request algorithm check for process {@code processIndex}.
     */
    public BankerResult evaluateRequest(int[][] allocation, int[][] max, int[] available,
            int processIndex, int[] request) throws ValidationException {
        if (allocation == null || max == null || available == null || request == null) {
            throw new ValidationException("All matrices and request vector must be provided.");
        }
        int pCount = allocation.length;
        int rCount = available.length;
        if (processIndex < 0 || processIndex >= pCount) {
            throw new ValidationException("Selected requesting process index P" + processIndex + " is out of range.");
        }
        if (request.length != rCount) {
            throw new ValidationException("Request vector length must equal resource count (" + rCount + ").");
        }

        // Validate base state first
        BankerResult baseCheck = evaluateSafety(allocation, max, available);
        int[][] need = baseCheck.getNeed();

        for (int j = 0; j < rCount; j++) {
            if (request[j] < 0) {
                throw new ValidationException("Request[R" + j + "] cannot be negative.");
            }
            if (request[j] > need[processIndex][j]) {
                throw new ValidationException(
                        "Request denied immediately: P" + processIndex + " requested R" + j + "=" + request[j]
                                + " which exceeds its Need (" + need[processIndex][j] + ").");
            }
            if (request[j] > available[j]) {
                throw new ValidationException(
                        "P" + processIndex + " must wait: requested R" + j + "=" + request[j]
                                + " exceeds Available (" + available[j] + ").");
            }
        }

        int[][] newAlloc = new int[pCount][rCount];
        int[][] newMax = new int[pCount][rCount];
        for (int i = 0; i < pCount; i++) {
            newAlloc[i] = Arrays.copyOf(allocation[i], rCount);
            newMax[i] = Arrays.copyOf(max[i], rCount);
        }
        int[] newAvail = Arrays.copyOf(available, rCount);

        for (int j = 0; j < rCount; j++) {
            newAlloc[processIndex][j] += request[j];
            newAvail[j] -= request[j];
        }

        BankerResult postRequest = evaluateSafety(newAlloc, newMax, newAvail);
        String prefix = postRequest.isSafe()
                ? "REQUEST GRANTED for P" + processIndex + ": Resulting state is SAFE. "
                : "REQUEST DENIED for P" + processIndex + " (would cause UNSAFE state): ";
        return new BankerResult(
                postRequest.getProcessCount(),
                postRequest.getResourceCount(),
                postRequest.getAllocation(),
                postRequest.getMax(),
                postRequest.getNeed(),
                postRequest.getAvailable(),
                postRequest.isSafe(),
                postRequest.getSafeSequence(),
                postRequest.getSteps(),
                prefix + postRequest.getSummaryMessage());
    }

    private boolean canSatisfy(int[] needRow, int[] work) {
        for (int j = 0; j < work.length; j++) {
            if (needRow[j] > work[j]) {
                return false;
            }
        }
        return true;
    }
}
