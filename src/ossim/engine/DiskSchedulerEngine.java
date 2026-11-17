package ossim.engine;

import ossim.model.DiskModels.Algorithm;
import ossim.model.DiskModels.Direction;
import ossim.model.DiskModels.DiskResult;
import ossim.model.DiskModels.SeekStep;
import ossim.model.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Engine for Module 7: Disk Scheduling.
 * Implements FCFS, SSTF, SCAN, C-SCAN, LOOK, and C-LOOK.
 */
public final class DiskSchedulerEngine {

    public DiskResult schedule(
            List<Integer> requests,
            int initialHead,
            int maxTrack,
            Algorithm algorithm,
            Direction direction) throws ValidationException {

        if (maxTrack <= 0) {
            throw new ValidationException("Max Cylinder / Track must be greater than 0.");
        }
        if (initialHead < 0 || initialHead > maxTrack) {
            throw new ValidationException(
                    "Initial Head Position (" + initialHead + ") must be between 0 and " + maxTrack + ".");
        }
        if (requests == null || requests.isEmpty()) {
            throw new ValidationException("Disk request queue must contain at least one track number.");
        }
        for (int i = 0; i < requests.size(); i++) {
            Integer track = requests.get(i);
            if (track == null || track < 0 || track > maxTrack) {
                throw new ValidationException(
                        "Disk request #" + (i + 1) + " (" + track + ") must be between 0 and " + maxTrack + ".");
            }
        }

        List<Integer> pathTracks = new ArrayList<>();
        pathTracks.add(initialHead);
        List<Integer> servicedRequests = new ArrayList<>();
        List<SeekStep> steps = new ArrayList<>();

        switch (algorithm) {
            case FCFS:
                runFcfs(requests, initialHead, pathTracks, servicedRequests, steps);
                break;
            case SSTF:
                runSstf(requests, initialHead, pathTracks, servicedRequests, steps);
                break;
            case SCAN:
                runScanOrLook(requests, initialHead, maxTrack, direction, true, pathTracks, servicedRequests, steps);
                break;
            case LOOK:
                runScanOrLook(requests, initialHead, maxTrack, direction, false, pathTracks, servicedRequests, steps);
                break;
            case C_SCAN:
                runCircular(requests, initialHead, maxTrack, direction, true, pathTracks, servicedRequests, steps);
                break;
            case C_LOOK:
                runCircular(requests, initialHead, maxTrack, direction, false, pathTracks, servicedRequests, steps);
                break;
            default:
                throw new ValidationException("Unsupported disk algorithm: " + algorithm);
        }

        long totalMovement = 0;
        for (SeekStep step : steps) {
            totalMovement += step.getDistance();
        }
        double avgSeek = requests.isEmpty() ? 0.0 : (double) totalMovement / requests.size();

        return new DiskResult(
                algorithm,
                initialHead,
                0,
                maxTrack,
                direction,
                pathTracks,
                servicedRequests,
                steps,
                totalMovement,
                avgSeek);
    }

    private void appendMove(List<Integer> pathTracks, List<SeekStep> steps, int from, int to,
            boolean boundaryJump, String note) {
        int dist = Math.abs(to - from);
        pathTracks.add(to);
        steps.add(new SeekStep(steps.size() + 1, from, to, dist, boundaryJump, note));
    }

    private void runFcfs(List<Integer> requests, int initialHead,
            List<Integer> pathTracks, List<Integer> serviced, List<SeekStep> steps) {
        int head = initialHead;
        for (int req : requests) {
            serviced.add(req);
            appendMove(pathTracks, steps, head, req, false, "Service request " + req);
            head = req;
        }
    }

    private void runSstf(List<Integer> requests, int initialHead,
            List<Integer> pathTracks, List<Integer> serviced, List<SeekStep> steps) {
        List<Integer> remaining = new ArrayList<>(requests);
        int head = initialHead;

        while (!remaining.isEmpty()) {
            int bestIdx = 0;
            int bestDist = Math.abs(remaining.get(0) - head);
            for (int i = 1; i < remaining.size(); i++) {
                int d = Math.abs(remaining.get(i) - head);
                if (d < bestDist) {
                    bestDist = d;
                    bestIdx = i;
                }
            }
            int next = remaining.remove(bestIdx);
            serviced.add(next);
            appendMove(pathTracks, steps, head, next, false, "Closest request " + next);
            head = next;
        }
    }

    private void runScanOrLook(List<Integer> requests, int initialHead, int maxTrack, Direction direction,
            boolean hitBoundary, List<Integer> pathTracks, List<Integer> serviced, List<SeekStep> steps) {

        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();
        for (int r : requests) {
            if (r < initialHead) {
                left.add(r);
            } else {
                right.add(r);
            }
        }
        Collections.sort(left, Collections.reverseOrder()); // descending from head toward 0
        Collections.sort(right); // ascending from head toward maxTrack

        int head = initialHead;
        if (direction == Direction.UP) {
            for (int r : right) {
                serviced.add(r);
                appendMove(pathTracks, steps, head, r, false, "Service request " + r + " (moving UP)");
                head = r;
            }
            if (!left.isEmpty()) {
                if (hitBoundary && head != maxTrack) {
                    appendMove(pathTracks, steps, head, maxTrack, true, "SCAN reaches upper disk end (" + maxTrack + ")");
                    head = maxTrack;
                }
                for (int r : left) {
                    serviced.add(r);
                    appendMove(pathTracks, steps, head, r, false, "Service request " + r + " (reversing DOWN)");
                    head = r;
                }
            }
        } else {
            // Note: requests equal to initialHead belong in the initial direction
            List<Integer> downInitial = new ArrayList<>();
            List<Integer> upAfter = new ArrayList<>();
            for (int r : requests) {
                if (r <= initialHead) {
                    downInitial.add(r);
                } else {
                    upAfter.add(r);
                }
            }
            Collections.sort(downInitial, Collections.reverseOrder());
            Collections.sort(upAfter);

            for (int r : downInitial) {
                serviced.add(r);
                appendMove(pathTracks, steps, head, r, false, "Service request " + r + " (moving DOWN)");
                head = r;
            }
            if (!upAfter.isEmpty()) {
                if (hitBoundary && head != 0) {
                    appendMove(pathTracks, steps, head, 0, true, "SCAN reaches lower disk end (0)");
                    head = 0;
                }
                for (int r : upAfter) {
                    serviced.add(r);
                    appendMove(pathTracks, steps, head, r, false, "Service request " + r + " (reversing UP)");
                    head = r;
                }
            }
        }
    }

    private void runCircular(List<Integer> requests, int initialHead, int maxTrack, Direction direction,
            boolean hitBoundary, List<Integer> pathTracks, List<Integer> serviced, List<SeekStep> steps) {

        int head = initialHead;
        if (direction == Direction.UP) {
            List<Integer> right = new ArrayList<>();
            List<Integer> left = new ArrayList<>();
            for (int r : requests) {
                if (r >= initialHead) {
                    right.add(r);
                } else {
                    left.add(r);
                }
            }
            Collections.sort(right);
            Collections.sort(left); // ascending from 0 upward

            for (int r : right) {
                serviced.add(r);
                appendMove(pathTracks, steps, head, r, false, "Service request " + r + " (moving UP)");
                head = r;
            }
            if (!left.isEmpty()) {
                if (hitBoundary) {
                    if (head != maxTrack) {
                        appendMove(pathTracks, steps, head, maxTrack, true, "C-SCAN reaches upper end (" + maxTrack + ")");
                        head = maxTrack;
                    }
                    appendMove(pathTracks, steps, head, 0, true, "C-SCAN circular return to 0");
                    head = 0;
                }
                for (int r : left) {
                    serviced.add(r);
                    appendMove(pathTracks, steps, head, r, !hitBoundary && head > r,
                            "Service request " + r + " (new UP sweep)");
                    head = r;
                }
            }
        } else {
            List<Integer> left = new ArrayList<>();
            List<Integer> right = new ArrayList<>();
            for (int r : requests) {
                if (r <= initialHead) {
                    left.add(r);
                } else {
                    right.add(r);
                }
            }
            Collections.sort(left, Collections.reverseOrder());
            Collections.sort(right, Collections.reverseOrder()); // descending from maxTrack downward

            for (int r : left) {
                serviced.add(r);
                appendMove(pathTracks, steps, head, r, false, "Service request " + r + " (moving DOWN)");
                head = r;
            }
            if (!right.isEmpty()) {
                if (hitBoundary) {
                    if (head != 0) {
                        appendMove(pathTracks, steps, head, 0, true, "C-SCAN reaches lower end (0)");
                        head = 0;
                    }
                    appendMove(pathTracks, steps, head, maxTrack, true, "C-SCAN circular return to " + maxTrack);
                    head = maxTrack;
                }
                for (int r : right) {
                    serviced.add(r);
                    appendMove(pathTracks, steps, head, r, !hitBoundary && head < r,
                            "Service request " + r + " (new DOWN sweep)");
                    head = r;
                }
            }
        }
    }
}
