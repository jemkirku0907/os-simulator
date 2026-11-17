package ossim.engine;

import ossim.model.PageReplacementModels.Algorithm;
import ossim.model.PageReplacementModels.ReplacementResult;
import ossim.model.PageReplacementModels.StepSnapshot;
import ossim.model.ValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Engine for Module 4: Page Replacement.
 * Implements FIFO, LRU, Optimal (OPT), and Clock / Second Chance.
 */
public final class PageReplacementEngine {

    public ReplacementResult simulate(List<Integer> referenceString, int frameCount, Algorithm algorithm)
            throws ValidationException {
        if (referenceString == null || referenceString.isEmpty()) {
            throw new ValidationException("Page reference string must contain at least one page number.");
        }
        if (frameCount <= 0) {
            throw new ValidationException("Number of frames must be greater than 0.");
        }
        for (int i = 0; i < referenceString.size(); i++) {
            Integer p = referenceString.get(i);
            if (p == null || p < 0) {
                throw new ValidationException(
                        "Page number at position #" + (i + 1) + " cannot be negative or null.");
            }
        }

        int[] frames = new int[frameCount];
        Arrays.fill(frames, -1);

        // Tracking metadata per frame slot
        int[] loadTime = new int[frameCount];
        int[] lastUsedTime = new int[frameCount];
        boolean[] refBit = new boolean[frameCount];
        int clockHand = 0;
        int filledCount = 0;

        List<StepSnapshot> steps = new ArrayList<>(referenceString.size());
        int hits = 0;
        int faults = 0;

        for (int t = 0; t < referenceString.size(); t++) {
            int page = referenceString.get(t);
            int foundSlot = findSlot(frames, page);

            if (foundSlot != -1) {
                hits++;
                lastUsedTime[foundSlot] = t;
                refBit[foundSlot] = true;
                steps.add(new StepSnapshot(
                        t + 1,
                        page,
                        Arrays.copyOf(frames, frameCount),
                        true,
                        null,
                        "HIT in Frame " + foundSlot));
                continue;
            }

            faults++;
            Integer evicted = null;
            int targetSlot;

            if (filledCount < frameCount) {
                targetSlot = filledCount++;
                frames[targetSlot] = page;
                loadTime[targetSlot] = t;
                lastUsedTime[targetSlot] = t;
                refBit[targetSlot] = true;
                steps.add(new StepSnapshot(
                        t + 1,
                        page,
                        Arrays.copyOf(frames, frameCount),
                        false,
                        null,
                        "FAULT -> Loaded into empty Frame " + targetSlot));
                continue;
            }

            switch (algorithm) {
                case FIFO:
                    targetSlot = selectMinIndex(loadTime);
                    break;
                case LRU:
                    targetSlot = selectMinIndex(lastUsedTime);
                    break;
                case OPTIMAL:
                    targetSlot = selectOptimalVictim(frames, referenceString, t + 1, loadTime);
                    break;
                case SECOND_CHANCE:
                    while (refBit[clockHand]) {
                        refBit[clockHand] = false;
                        clockHand = (clockHand + 1) % frameCount;
                    }
                    targetSlot = clockHand;
                    clockHand = (clockHand + 1) % frameCount;
                    break;
                default:
                    throw new ValidationException("Unsupported algorithm: " + algorithm);
            }

            evicted = frames[targetSlot];
            frames[targetSlot] = page;
            loadTime[targetSlot] = t;
            lastUsedTime[targetSlot] = t;
            refBit[targetSlot] = true;

            steps.add(new StepSnapshot(
                    t + 1,
                    page,
                    Arrays.copyOf(frames, frameCount),
                    false,
                    evicted,
                    "FAULT -> Replaced page " + evicted + " in Frame " + targetSlot));
        }

        int total = referenceString.size();
        double hitRatio = (hits * 100.0) / total;
        double faultRatio = (faults * 100.0) / total;

        return new ReplacementResult(
                algorithm,
                frameCount,
                new ArrayList<>(referenceString),
                steps,
                hits,
                faults,
                hitRatio,
                faultRatio);
    }

    private int findSlot(int[] frames, int page) {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == page) {
                return i;
            }
        }
        return -1;
    }

    private int selectMinIndex(int[] timestamps) {
        int bestIdx = 0;
        for (int i = 1; i < timestamps.length; i++) {
            if (timestamps[i] < timestamps[bestIdx]) {
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private int selectOptimalVictim(int[] frames, List<Integer> refs, int startIdx, int[] loadTime) {
        int victimSlot = 0;
        int farthestNextUse = -1;

        for (int i = 0; i < frames.length; i++) {
            int currentPage = frames[i];
            int nextUse = Integer.MAX_VALUE;
            for (int j = startIdx; j < refs.size(); j++) {
                if (refs.get(j) == currentPage) {
                    nextUse = j;
                    break;
                }
            }
            if (nextUse > farthestNextUse) {
                farthestNextUse = nextUse;
                victimSlot = i;
            } else if (nextUse == farthestNextUse && loadTime[i] < loadTime[victimSlot]) {
                // Deterministic FIFO tie-breaker when multiple pages are never used again
                victimSlot = i;
            }
        }
        return victimSlot;
    }
}
