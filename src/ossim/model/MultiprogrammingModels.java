package ossim.model;

import java.util.List;

/**
 * Data structures for Module 8: Multiprogramming Simulation.
 */
public final class MultiprogrammingModels {

    private MultiprogrammingModels() {
    }

    public enum ProcessState {
        NEW("NEW (Job Queue)"),
        READY("READY (In RAM)"),
        RUNNING("RUNNING (On CPU)"),
        IO_WAIT("WAITING (I/O)"),
        TERMINATED("TERMINATED"),
        REJECTED("REJECTED (Exceeds Available RAM)");

        private final String label;

        ProcessState(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static final class ProgramInput {
        private final String pid;
        private final int arrivalTime;
        private final int cpuBurst;
        private final int ioTime;
        private final int memoryRequiredMb;
        private final int priority;

        public ProgramInput(String pid, int arrivalTime, int cpuBurst, int ioTime,
                int memoryRequiredMb, int priority) {
            this.pid = pid;
            this.arrivalTime = arrivalTime;
            this.cpuBurst = cpuBurst;
            this.ioTime = ioTime;
            this.memoryRequiredMb = memoryRequiredMb;
            this.priority = priority;
        }

        public String getPid() {
            return pid;
        }

        public int getArrivalTime() {
            return arrivalTime;
        }

        public int getCpuBurst() {
            return cpuBurst;
        }

        public int getIoTime() {
            return ioTime;
        }

        public int getMemoryRequiredMb() {
            return memoryRequiredMb;
        }

        public int getPriority() {
            return priority;
        }
    }

    public static final class ProcessSnapshot {
        private final String pid;
        private final ProcessState state;
        private final int memoryMb;
        private final int remainingCpu;
        private final int remainingIo;
        private final int totalCpu;
        private final int totalIo;
        private final int completionTime;
        private final int waitingTime;

        public ProcessSnapshot(String pid, ProcessState state, int memoryMb, int remainingCpu,
                int remainingIo, int totalCpu, int totalIo, int completionTime, int waitingTime) {
            this.pid = pid;
            this.state = state;
            this.memoryMb = memoryMb;
            this.remainingCpu = remainingCpu;
            this.remainingIo = remainingIo;
            this.totalCpu = totalCpu;
            this.totalIo = totalIo;
            this.completionTime = completionTime;
            this.waitingTime = waitingTime;
        }

        public String getPid() {
            return pid;
        }

        public ProcessState getState() {
            return state;
        }

        public int getMemoryMb() {
            return memoryMb;
        }

        public int getRemainingCpu() {
            return remainingCpu;
        }

        public int getRemainingIo() {
            return remainingIo;
        }

        public int getTotalCpu() {
            return totalCpu;
        }

        public int getTotalIo() {
            return totalIo;
        }

        public int getCompletionTime() {
            return completionTime;
        }

        public int getWaitingTime() {
            return waitingTime;
        }
    }

    public static final class TickSnapshot {
        private final int tick;
        private final int totalRamMb;
        private final int osReservedMb;
        private final int usedByProcessesMb;
        private final int activeInMemoryCount;
        private final int readyCount;
        private final int runningCount;
        private final int ioWaitCount;
        private final int jobQueueCount;
        private final int terminatedCount;
        private final List<String> runningOnCores;
        private final List<ProcessSnapshot> processSnapshots;
        private final List<String> events;

        public TickSnapshot(int tick, int totalRamMb, int osReservedMb, int usedByProcessesMb,
                int activeInMemoryCount, int readyCount, int runningCount, int ioWaitCount,
                int jobQueueCount, int terminatedCount, List<String> runningOnCores,
                List<ProcessSnapshot> processSnapshots, List<String> events) {
            this.tick = tick;
            this.totalRamMb = totalRamMb;
            this.osReservedMb = osReservedMb;
            this.usedByProcessesMb = usedByProcessesMb;
            this.activeInMemoryCount = activeInMemoryCount;
            this.readyCount = readyCount;
            this.runningCount = runningCount;
            this.ioWaitCount = ioWaitCount;
            this.jobQueueCount = jobQueueCount;
            this.terminatedCount = terminatedCount;
            this.runningOnCores = runningOnCores;
            this.processSnapshots = processSnapshots;
            this.events = events;
        }

        public int getTick() {
            return tick;
        }

        public int getTotalRamMb() {
            return totalRamMb;
        }

        public int getOsReservedMb() {
            return osReservedMb;
        }

        public int getUsedByProcessesMb() {
            return usedByProcessesMb;
        }

        public int getActiveInMemoryCount() {
            return activeInMemoryCount;
        }

        public int getReadyCount() {
            return readyCount;
        }

        public int getRunningCount() {
            return runningCount;
        }

        public int getIoWaitCount() {
            return ioWaitCount;
        }

        public int getJobQueueCount() {
            return jobQueueCount;
        }

        public int getTerminatedCount() {
            return terminatedCount;
        }

        public List<String> getRunningOnCores() {
            return runningOnCores;
        }

        public List<ProcessSnapshot> getProcessSnapshots() {
            return processSnapshots;
        }

        public List<String> getEvents() {
            return events;
        }
    }

    public static final class MultiprogrammingResult {
        private final int totalRamMb;
        private final int osReservedMb;
        private final int cpuCores;
        private final int timeQuantum;
        private final int totalTicks;
        private final double cpuUtilizationPercent;
        private final double uniprogrammingCpuUtilPercent;
        private final double avgRamUtilizationPercent;
        private final double peakRamUtilizationPercent;
        private final double avgDegreeOfMultiprogramming;
        private final int peakDegreeOfMultiprogramming;
        private final int completedCount;
        private final int rejectedCount;
        private final double throughput;
        private final List<TickSnapshot> timeline;
        private final List<ProcessSnapshot> finalStates;

        public MultiprogrammingResult(int totalRamMb, int osReservedMb, int cpuCores, int timeQuantum,
                int totalTicks, double cpuUtilizationPercent, double uniprogrammingCpuUtilPercent,
                double avgRamUtilizationPercent, double peakRamUtilizationPercent,
                double avgDegreeOfMultiprogramming, int peakDegreeOfMultiprogramming,
                int completedCount, int rejectedCount, double throughput,
                List<TickSnapshot> timeline, List<ProcessSnapshot> finalStates) {
            this.totalRamMb = totalRamMb;
            this.osReservedMb = osReservedMb;
            this.cpuCores = cpuCores;
            this.timeQuantum = timeQuantum;
            this.totalTicks = totalTicks;
            this.cpuUtilizationPercent = cpuUtilizationPercent;
            this.uniprogrammingCpuUtilPercent = uniprogrammingCpuUtilPercent;
            this.avgRamUtilizationPercent = avgRamUtilizationPercent;
            this.peakRamUtilizationPercent = peakRamUtilizationPercent;
            this.avgDegreeOfMultiprogramming = avgDegreeOfMultiprogramming;
            this.peakDegreeOfMultiprogramming = peakDegreeOfMultiprogramming;
            this.completedCount = completedCount;
            this.rejectedCount = rejectedCount;
            this.throughput = throughput;
            this.timeline = timeline;
            this.finalStates = finalStates;
        }

        public int getTotalRamMb() {
            return totalRamMb;
        }

        public int getOsReservedMb() {
            return osReservedMb;
        }

        public int getCpuCores() {
            return cpuCores;
        }

        public int getTimeQuantum() {
            return timeQuantum;
        }

        public int getTotalTicks() {
            return totalTicks;
        }

        public double getCpuUtilizationPercent() {
            return cpuUtilizationPercent;
        }

        public double getUniprogrammingCpuUtilPercent() {
            return uniprogrammingCpuUtilPercent;
        }

        public double getAvgRamUtilizationPercent() {
            return avgRamUtilizationPercent;
        }

        public double getPeakRamUtilizationPercent() {
            return peakRamUtilizationPercent;
        }

        public double getAvgDegreeOfMultiprogramming() {
            return avgDegreeOfMultiprogramming;
        }

        public int getPeakDegreeOfMultiprogramming() {
            return peakDegreeOfMultiprogramming;
        }

        public int getCompletedCount() {
            return completedCount;
        }

        public int getRejectedCount() {
            return rejectedCount;
        }

        public double getThroughput() {
            return throughput;
        }

        public List<TickSnapshot> getTimeline() {
            return timeline;
        }

        public List<ProcessSnapshot> getFinalStates() {
            return finalStates;
        }
    }
}
