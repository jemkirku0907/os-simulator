package ossim.engine;

import ossim.model.CpuModels.Algorithm;
import ossim.model.CpuModels.GanttSlice;
import ossim.model.CpuModels.ProcessInput;
import ossim.model.CpuModels.ProcessResult;
import ossim.model.CpuModels.ScheduleResult;
import ossim.model.ValidationException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Engine for Module 1: CPU Scheduling.
 * Implements FCFS, Non-Preemptive SJF, Preemptive SRTF, Non-Preemptive Priority,
 * Preemptive Priority, and Round Robin using event-driven simulation so both
 * small and large datasets (or large burst times) execute instantaneously.
 */
public final class CpuSchedulerEngine {

    private static final class MutableProc {
        final int inputIndex;
        final String pid;
        final int arrival;
        final int burst;
        final int priority;
        int remaining;
        int firstStart = -1;
        int completion = -1;
        boolean queuedOrDone = false;

        MutableProc(int inputIndex, ProcessInput in) {
            this.inputIndex = inputIndex;
            this.pid = in.getPid();
            this.arrival = in.getArrivalTime();
            this.burst = in.getBurstTime();
            this.priority = in.getPriority();
            this.remaining = in.getBurstTime();
        }
    }

    public ScheduleResult schedule(
            List<ProcessInput> inputs,
            Algorithm algorithm,
            int timeQuantum,
            boolean lowerNumberHigherPriority) throws ValidationException {

        if (inputs == null || inputs.isEmpty()) {
            throw new ValidationException("Add at least one process before running CPU Scheduling.");
        }
        if (algorithm == Algorithm.ROUND_ROBIN && timeQuantum <= 0) {
            throw new ValidationException("Round Robin Time Quantum must be greater than 0.");
        }

        Set<String> seenPids = new HashSet<>();
        List<MutableProc> procs = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            ProcessInput in = inputs.get(i);
            if (in.getPid() == null || in.getPid().trim().isEmpty()) {
                throw new ValidationException("Process #" + (i + 1) + " has a blank Process ID.");
            }
            String cleanPid = in.getPid().trim();
            if (!seenPids.add(cleanPid)) {
                throw new ValidationException("Duplicate Process ID \"" + cleanPid + "\" is not allowed.");
            }
            if (in.getArrivalTime() < 0) {
                throw new ValidationException("Arrival Time for " + cleanPid + " cannot be negative.");
            }
            if (in.getBurstTime() <= 0) {
                throw new ValidationException("Burst Time for " + cleanPid + " must be greater than 0.");
            }
            procs.add(new MutableProc(i, new ProcessInput(cleanPid, in.getArrivalTime(), in.getBurstTime(), in.getPriority())));
        }

        List<GanttSlice> rawGantt = new ArrayList<>();
        switch (algorithm) {
            case FCFS:
                runFcfs(procs, rawGantt);
                break;
            case SJF_NON_PREEMPTIVE:
                runNonPreemptive(procs, rawGantt, Comparator
                        .comparingInt((MutableProc p) -> p.burst)
                        .thenComparingInt(p -> p.arrival)
                        .thenComparingInt(p -> p.inputIndex));
                break;
            case PRIORITY_NON_PREEMPTIVE:
                runNonPreemptive(procs, rawGantt, priorityComparator(lowerNumberHigherPriority));
                break;
            case SRTF_PREEMPTIVE:
                runPreemptive(procs, rawGantt, Comparator
                        .comparingInt((MutableProc p) -> p.remaining)
                        .thenComparingInt(p -> p.arrival)
                        .thenComparingInt(p -> p.inputIndex));
                break;
            case PRIORITY_PREEMPTIVE:
                runPreemptive(procs, rawGantt, priorityComparator(lowerNumberHigherPriority));
                break;
            case ROUND_ROBIN:
                runRoundRobin(procs, timeQuantum, rawGantt);
                break;
            default:
                throw new ValidationException("Unsupported CPU scheduling algorithm: " + algorithm);
        }

        List<GanttSlice> mergedGantt = mergeAdjacentSlices(rawGantt);
        return buildResult(algorithm, procs, mergedGantt);
    }

    private Comparator<MutableProc> priorityComparator(boolean lowerNumberHigherPriority) {
        return (a, b) -> {
            int cmp = lowerNumberHigherPriority
                    ? Integer.compare(a.priority, b.priority)
                    : Integer.compare(b.priority, a.priority);
            if (cmp != 0) {
                return cmp;
            }
            cmp = Integer.compare(a.arrival, b.arrival);
            return cmp != 0 ? cmp : Integer.compare(a.inputIndex, b.inputIndex);
        };
    }

    private void runFcfs(List<MutableProc> procs, List<GanttSlice> gantt) {
        List<MutableProc> sorted = new ArrayList<>(procs);
        sorted.sort(Comparator.comparingInt((MutableProc p) -> p.arrival).thenComparingInt(p -> p.inputIndex));

        int currentTime = 0;
        for (MutableProc p : sorted) {
            if (currentTime < p.arrival) {
                gantt.add(new GanttSlice("IDLE", currentTime, p.arrival, true));
                currentTime = p.arrival;
            }
            p.firstStart = currentTime;
            int finish = currentTime + p.burst;
            p.completion = finish;
            p.remaining = 0;
            gantt.add(new GanttSlice(p.pid, currentTime, finish, false));
            currentTime = finish;
        }
    }

    private void runNonPreemptive(List<MutableProc> procs, List<GanttSlice> gantt, Comparator<MutableProc> selector) {
        int n = procs.size();
        int completed = 0;
        int currentTime = 0;

        while (completed < n) {
            MutableProc chosen = null;
            int earliestFutureArrival = Integer.MAX_VALUE;

            for (MutableProc p : procs) {
                if (p.remaining > 0) {
                    if (p.arrival <= currentTime) {
                        if (chosen == null || selector.compare(p, chosen) < 0) {
                            chosen = p;
                        }
                    } else if (p.arrival < earliestFutureArrival) {
                        earliestFutureArrival = p.arrival;
                    }
                }
            }

            if (chosen == null) {
                gantt.add(new GanttSlice("IDLE", currentTime, earliestFutureArrival, true));
                currentTime = earliestFutureArrival;
                continue;
            }

            chosen.firstStart = currentTime;
            int finish = currentTime + chosen.burst;
            chosen.completion = finish;
            chosen.remaining = 0;
            gantt.add(new GanttSlice(chosen.pid, currentTime, finish, false));
            currentTime = finish;
            completed++;
        }
    }

    private void runPreemptive(List<MutableProc> procs, List<GanttSlice> gantt, Comparator<MutableProc> selector) {
        int n = procs.size();
        int completed = 0;
        int currentTime = 0;

        while (completed < n) {
            MutableProc chosen = null;
            int nextArrivalAfterNow = Integer.MAX_VALUE;

            for (MutableProc p : procs) {
                if (p.remaining > 0) {
                    if (p.arrival <= currentTime) {
                        if (chosen == null || selector.compare(p, chosen) < 0) {
                            chosen = p;
                        }
                    } else if (p.arrival < nextArrivalAfterNow) {
                        nextArrivalAfterNow = p.arrival;
                    }
                }
            }

            if (chosen == null) {
                gantt.add(new GanttSlice("IDLE", currentTime, nextArrivalAfterNow, true));
                currentTime = nextArrivalAfterNow;
                continue;
            }

            if (chosen.firstStart == -1) {
                chosen.firstStart = currentTime;
            }

            int finishIfUninterrupted = currentTime + chosen.remaining;
            int stepEnd = Math.min(finishIfUninterrupted, nextArrivalAfterNow);
            int executed = stepEnd - currentTime;

            gantt.add(new GanttSlice(chosen.pid, currentTime, stepEnd, false));
            chosen.remaining -= executed;
            currentTime = stepEnd;

            if (chosen.remaining == 0) {
                chosen.completion = currentTime;
                completed++;
            }
        }
    }

    private void runRoundRobin(List<MutableProc> procs, int quantum, List<GanttSlice> gantt) {
        List<MutableProc> byArrival = new ArrayList<>(procs);
        byArrival.sort(Comparator.comparingInt((MutableProc p) -> p.arrival).thenComparingInt(p -> p.inputIndex));

        Queue<MutableProc> readyQueue = new ArrayDeque<>();
        int nextIdx = 0;
        int n = byArrival.size();
        int completed = 0;
        int currentTime = 0;

        while (completed < n) {
            while (nextIdx < n && byArrival.get(nextIdx).arrival <= currentTime) {
                MutableProc arriving = byArrival.get(nextIdx++);
                arriving.queuedOrDone = true;
                readyQueue.offer(arriving);
            }

            if (readyQueue.isEmpty()) {
                int nextArrival = byArrival.get(nextIdx).arrival;
                gantt.add(new GanttSlice("IDLE", currentTime, nextArrival, true));
                currentTime = nextArrival;
                continue;
            }

            MutableProc current = readyQueue.poll();
            if (current.firstStart == -1) {
                current.firstStart = currentTime;
            }

            int runTime = Math.min(quantum, current.remaining);
            int sliceEnd = currentTime + runTime;
            gantt.add(new GanttSlice(current.pid, currentTime, sliceEnd, false));
            current.remaining -= runTime;
            currentTime = sliceEnd;

            // Enqueue processes that arrive up to currentTime before re-enqueueing current.
            while (nextIdx < n && byArrival.get(nextIdx).arrival <= currentTime) {
                MutableProc arriving = byArrival.get(nextIdx++);
                arriving.queuedOrDone = true;
                readyQueue.offer(arriving);
            }

            if (current.remaining > 0) {
                readyQueue.offer(current);
            } else {
                current.completion = currentTime;
                completed++;
            }
        }
    }

    private List<GanttSlice> mergeAdjacentSlices(List<GanttSlice> raw) {
        List<GanttSlice> merged = new ArrayList<>();
        for (GanttSlice slice : raw) {
            if (slice.getDuration() <= 0) {
                continue;
            }
            if (!merged.isEmpty()) {
                GanttSlice prev = merged.get(merged.size() - 1);
                if (prev.isIdle() == slice.isIdle()
                        && prev.getLabel().equals(slice.getLabel())
                        && prev.getEndTime() == slice.getStartTime()) {
                    merged.set(merged.size() - 1,
                            new GanttSlice(prev.getLabel(), prev.getStartTime(), slice.getEndTime(), prev.isIdle()));
                    continue;
                }
            }
            merged.add(slice);
        }
        return merged;
    }

    private ScheduleResult buildResult(Algorithm algorithm, List<MutableProc> procs, List<GanttSlice> gantt) {
        List<ProcessResult> results = new ArrayList<>(procs.size());
        double sumWt = 0;
        double sumTat = 0;
        double sumRt = 0;
        int minStart = Integer.MAX_VALUE;
        int maxFinish = 0;

        for (MutableProc p : procs) {
            int tat = p.completion - p.arrival;
            int wt = tat - p.burst;
            int rt = p.firstStart - p.arrival;
            sumTat += tat;
            sumWt += wt;
            sumRt += rt;
            if (p.firstStart < minStart) {
                minStart = p.firstStart;
            }
            if (p.completion > maxFinish) {
                maxFinish = p.completion;
            }
            results.add(new ProcessResult(p.pid, p.arrival, p.burst, p.priority,
                    p.firstStart, p.completion, tat, wt, rt));
        }

        int totalIdle = 0;
        int busyTime = 0;
        for (GanttSlice s : gantt) {
            if (s.isIdle()) {
                totalIdle += s.getDuration();
            } else {
                busyTime += s.getDuration();
            }
        }

        int totalSpan = gantt.isEmpty() ? 0 : gantt.get(gantt.size() - 1).getEndTime();
        double cpuUtil = totalSpan > 0 ? (busyTime * 100.0) / totalSpan : 0.0;
        double throughput = totalSpan > 0 ? (procs.size() * 1.0) / totalSpan : 0.0;

        return new ScheduleResult(
                algorithm,
                results,
                gantt,
                sumWt / procs.size(),
                sumTat / procs.size(),
                sumRt / procs.size(),
                cpuUtil,
                throughput,
                totalSpan,
                totalIdle);
    }
}
