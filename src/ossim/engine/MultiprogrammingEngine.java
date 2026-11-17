package ossim.engine;

import ossim.model.MultiprogrammingModels.MultiprogrammingResult;
import ossim.model.MultiprogrammingModels.ProcessSnapshot;
import ossim.model.MultiprogrammingModels.ProcessState;
import ossim.model.MultiprogrammingModels.ProgramInput;
import ossim.model.MultiprogrammingModels.TickSnapshot;
import ossim.model.ValidationException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Engine for Module 8: Multiprogramming Simulation.
 * Models job admission into RAM, multi-core CPU dispatching, CPU/I/O overlap,
 * resource release on completion, and CPU/RAM utilization vs uniprogramming baseline.
 */
public final class MultiprogrammingEngine {

    private static final class SimProc {
        final int order;
        final String pid;
        final int arrival;
        final int totalCpu;
        final int totalIo;
        final int memMb;
        final int priority;

        ProcessState state;
        int remainingCpu;
        int remainingIo;
        int cpuExecutedSinceStart = 0;
        int ioTriggerPoint;
        boolean ioTriggered = false;
        int quantumUsed = 0;
        int completionTime = -1;
        int waitingTime = 0;

        SimProc(int order, ProgramInput in) {
            this.order = order;
            this.pid = in.getPid();
            this.arrival = in.getArrivalTime();
            this.totalCpu = in.getCpuBurst();
            this.totalIo = in.getIoTime();
            this.memMb = in.getMemoryRequiredMb();
            this.priority = in.getPriority();
            this.state = ProcessState.NEW;
            this.remainingCpu = in.getCpuBurst();
            this.remainingIo = in.getIoTime();
            // Trigger I/O mid-execution when process has I/O work and CPU >= 2, else after first CPU tick
            this.ioTriggerPoint = (in.getIoTime() > 0) ? Math.max(1, in.getCpuBurst() / 2) : Integer.MAX_VALUE;
        }

        ProcessSnapshot snapshot() {
            return new ProcessSnapshot(pid, state, memMb, remainingCpu, remainingIo,
                    totalCpu, totalIo, completionTime, waitingTime);
        }
    }

    public MultiprogrammingResult simulate(
            List<ProgramInput> programs,
            int totalRamMb,
            int osReservedMb,
            int cpuCores,
            int timeQuantum,
            int maxDegreeLimit) throws ValidationException {

        if (totalRamMb <= 0) {
            throw new ValidationException("Total System RAM must be greater than 0 MB.");
        }
        if (osReservedMb < 0 || osReservedMb >= totalRamMb) {
            throw new ValidationException("OS Reserved RAM must be between 0 and " + (totalRamMb - 1) + " MB.");
        }
        if (cpuCores <= 0) {
            throw new ValidationException("Number of CPU Cores must be at least 1.");
        }
        if (timeQuantum <= 0) {
            throw new ValidationException("Time Quantum must be greater than 0.");
        }
        if (maxDegreeLimit < 0) {
            throw new ValidationException("Max Multiprogramming Degree limit cannot be negative (use 0 for unlimited).");
        }
        if (programs == null || programs.isEmpty()) {
            throw new ValidationException("Add at least one program/process to simulate.");
        }

        Set<String> seen = new HashSet<>();
        List<SimProc> allProcs = new ArrayList<>(programs.size());

        for (int i = 0; i < programs.size(); i++) {
            ProgramInput p = programs.get(i);
            if (p.getPid() == null || p.getPid().trim().isEmpty()) {
                throw new ValidationException("Program #" + (i + 1) + " has a blank Process ID.");
            }
            String cleanPid = p.getPid().trim();
            if (!seen.add(cleanPid)) {
                throw new ValidationException("Duplicate Process ID \"" + cleanPid + "\".");
            }
            if (p.getArrivalTime() < 0) {
                throw new ValidationException("Arrival time for " + cleanPid + " cannot be negative.");
            }
            if (p.getCpuBurst() <= 0) {
                throw new ValidationException("CPU Burst for " + cleanPid + " must be greater than 0.");
            }
            if (p.getIoTime() < 0) {
                throw new ValidationException("I/O Time for " + cleanPid + " cannot be negative.");
            }
            if (p.getMemoryRequiredMb() <= 0) {
                throw new ValidationException("Memory requirement for " + cleanPid + " must be greater than 0 MB.");
            }
            allProcs.add(new SimProc(i, new ProgramInput(
                    cleanPid, p.getArrivalTime(), p.getCpuBurst(), p.getIoTime(),
                    p.getMemoryRequiredMb(), p.getPriority())));
        }

        int userRamTotal = totalRamMb - osReservedMb;
        int freeUserRam = userRamTotal;
        int rejectedCount = 0;

        List<SimProc> notArrived = new ArrayList<>(allProcs);
        notArrived.sort(Comparator.comparingInt((SimProc p) -> p.arrival).thenComparingInt(p -> p.order));

        List<SimProc> jobQueue = new ArrayList<>();
        Queue<SimProc> readyQueue = new ArrayDeque<>();
        List<SimProc> ioList = new ArrayList<>();
        SimProc[] cores = new SimProc[cpuCores];

        List<TickSnapshot> timeline = new ArrayList<>();
        int tick = 0;
        long busyCoreTicks = 0;
        long ramUtilSum = 0;
        double peakRamUtil = 0.0;
        long degreeSum = 0;
        int peakDegree = 0;

        final int maxSafetyTicks = 200_000;
        int activeInMemory = 0;

        while (tick < maxSafetyTicks) {
            List<String> events = new ArrayList<>();

            // 1. Arrivals at this tick
            Iterator<SimProc> arrIt = notArrived.iterator();
            while (arrIt.hasNext()) {
                SimProc p = arrIt.next();
                if (p.arrival <= tick) {
                    arrIt.remove();
                    if (p.memMb > userRamTotal) {
                        p.state = ProcessState.REJECTED;
                        rejectedCount++;
                        events.add(p.pid + " REJECTED (needs " + p.memMb + " MB > available user RAM " + userRamTotal + " MB)");
                    } else {
                        p.state = ProcessState.NEW;
                        jobQueue.add(p);
                        events.add(p.pid + " arrived (" + p.memMb + " MB, CPU=" + p.totalCpu + ", IO=" + p.totalIo + ")");
                    }
                } else {
                    break;
                }
            }

            // 2. Admit jobs from jobQueue into RAM if space and degree cap permit
            Iterator<SimProc> jobIt = jobQueue.iterator();
            while (jobIt.hasNext()) {
                if (maxDegreeLimit > 0 && activeInMemory >= maxDegreeLimit) {
                    break;
                }
                SimProc p = jobIt.next();
                if (p.memMb <= freeUserRam) {
                    jobIt.remove();
                    freeUserRam -= p.memMb;
                    activeInMemory++;
                    p.state = ProcessState.READY;
                    readyQueue.offer(p);
                    events.add(p.pid + " admitted to RAM (" + p.memMb + " MB used, free=" + freeUserRam + " MB)");
                }
            }

            // 3. Assign idle CPU cores from readyQueue
            for (int c = 0; c < cpuCores; c++) {
                if (cores[c] == null && !readyQueue.isEmpty()) {
                    SimProc next = readyQueue.poll();
                    next.state = ProcessState.RUNNING;
                    next.quantumUsed = 0;
                    cores[c] = next;
                    events.add("Core " + c + " dispatches " + next.pid);
                }
            }

            // 4. Advance Waiting time for READY processes
            for (SimProc r : readyQueue) {
                r.waitingTime++;
            }

            // Record snapshot BEFORE advancing 1 tick of work (or at final completion tick)
            boolean allDone = notArrived.isEmpty() && jobQueue.isEmpty() && readyQueue.isEmpty()
                    && ioList.isEmpty() && allCoresIdle(cores);

            int usedByProcs = userRamTotal - freeUserRam;
            if (!allDone) {
                double ramUtilNow = ((osReservedMb + usedByProcs) * 100.0) / totalRamMb;
                ramUtilSum += Math.round(ramUtilNow * 100.0);
                if (ramUtilNow > peakRamUtil) {
                    peakRamUtil = ramUtilNow;
                }
                degreeSum += activeInMemory;
                if (activeInMemory > peakDegree) {
                    peakDegree = activeInMemory;
                }
            } else if (tick > 0) {
                events.add("All admitted processes completed.");
            }

            List<String> coreNames = new ArrayList<>(cpuCores);
            int runningNow = 0;
            for (int c = 0; c < cpuCores; c++) {
                if (cores[c] != null) {
                    coreNames.add(cores[c].pid);
                    runningNow++;
                } else {
                    coreNames.add("IDLE");
                }
            }

            int terminatedNow = 0;
            List<ProcessSnapshot> procSnaps = new ArrayList<>(allProcs.size());
            for (SimProc p : allProcs) {
                if (p.state == ProcessState.TERMINATED) {
                    terminatedNow++;
                }
                procSnaps.add(p.snapshot());
            }

            timeline.add(new TickSnapshot(
                    tick,
                    totalRamMb,
                    osReservedMb,
                    usedByProcs,
                    activeInMemory,
                    readyQueue.size(),
                    runningNow,
                    ioList.size(),
                    jobQueue.size(),
                    terminatedNow,
                    coreNames,
                    procSnaps,
                    events));

            if (allDone) {
                break;
            }

            // 5. Execute 1 tick on I/O devices (overlapped with CPU!)
            Iterator<SimProc> ioIt = ioList.iterator();
            while (ioIt.hasNext()) {
                SimProc p = ioIt.next();
                p.remainingIo--;
                if (p.remainingIo <= 0) {
                    ioIt.remove();
                    if (p.remainingCpu > 0) {
                        p.state = ProcessState.READY;
                        readyQueue.offer(p);
                    } else {
                        p.state = ProcessState.TERMINATED;
                        p.completionTime = tick + 1;
                        freeUserRam += p.memMb;
                        activeInMemory--;
                    }
                }
            }

            // 6. Execute 1 tick on each busy CPU core
            for (int c = 0; c < cpuCores; c++) {
                SimProc p = cores[c];
                if (p == null) {
                    continue;
                }
                busyCoreTicks++;
                p.remainingCpu--;
                p.cpuExecutedSinceStart++;
                p.quantumUsed++;

                if (p.remainingCpu <= 0 && p.remainingIo <= 0) {
                    p.state = ProcessState.TERMINATED;
                    p.completionTime = tick + 1;
                    freeUserRam += p.memMb;
                    activeInMemory--;
                    cores[c] = null;
                } else if (!p.ioTriggered && p.remainingIo > 0
                        && (p.cpuExecutedSinceStart >= p.ioTriggerPoint || p.remainingCpu == 0)) {
                    p.ioTriggered = true;
                    p.state = ProcessState.IO_WAIT;
                    ioList.add(p);
                    cores[c] = null;
                } else if (p.quantumUsed >= timeQuantum && !readyQueue.isEmpty()) {
                    p.state = ProcessState.READY;
                    readyQueue.offer(p);
                    cores[c] = null;
                }
            }

            tick++;
        }

        int completedCount = 0;
        long sumCpuAdmitted = 0;
        long sumIoAdmitted = 0;
        List<ProcessSnapshot> finalStates = new ArrayList<>(allProcs.size());
        for (SimProc p : allProcs) {
            if (p.state == ProcessState.TERMINATED) {
                completedCount++;
            }
            if (p.state != ProcessState.REJECTED) {
                sumCpuAdmitted += p.totalCpu;
                sumIoAdmitted += p.totalIo;
            }
            finalStates.add(p.snapshot());
        }

        int totalTicks = Math.max(tick, 1);
        double cpuUtil = (busyCoreTicks * 100.0) / (totalTicks * (double) cpuCores);
        double uniCpuUtil = (sumCpuAdmitted + sumIoAdmitted) > 0
                ? (sumCpuAdmitted * 100.0) / (sumCpuAdmitted + sumIoAdmitted)
                : 0.0;
        double avgRamUtil = tick > 0 ? (ramUtilSum / 100.0) / tick : 0.0;
        double avgDegree = tick > 0 ? (double) degreeSum / tick : 0.0;
        double throughput = totalTicks > 0 ? (double) completedCount / totalTicks : 0.0;

        return new MultiprogrammingResult(
                totalRamMb,
                osReservedMb,
                cpuCores,
                timeQuantum,
                totalTicks,
                cpuUtil,
                uniCpuUtil,
                avgRamUtil,
                peakRamUtil,
                avgDegree,
                peakDegree,
                completedCount,
                rejectedCount,
                throughput,
                timeline,
                finalStates);
    }

    private boolean allCoresIdle(SimProc[] cores) {
        for (SimProc p : cores) {
            if (p != null) {
                return false;
            }
        }
        return true;
    }
}
