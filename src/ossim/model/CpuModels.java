package ossim.model;

import java.util.List;

/**
 * Data structures for Module 1: CPU Scheduling.
 */
public final class CpuModels {

    private CpuModels() {
    }

    public enum Algorithm {
        FCFS("FCFS (First-Come, First-Served)"),
        SJF_NON_PREEMPTIVE("SJF (Non-Preemptive)"),
        SRTF_PREEMPTIVE("SRTF (Preemptive SJF)"),
        PRIORITY_NON_PREEMPTIVE("Priority (Non-Preemptive)"),
        PRIORITY_PREEMPTIVE("Priority (Preemptive)"),
        ROUND_ROBIN("Round Robin (RR)");

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

    public static final class ProcessInput {
        private final String pid;
        private final int arrivalTime;
        private final int burstTime;
        private final int priority;

        public ProcessInput(String pid, int arrivalTime, int burstTime, int priority) {
            this.pid = pid;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.priority = priority;
        }

        public String getPid() {
            return pid;
        }

        public int getArrivalTime() {
            return arrivalTime;
        }

        public int getBurstTime() {
            return burstTime;
        }

        public int getPriority() {
            return priority;
        }
    }

    public static final class GanttSlice {
        private final String label; // PID or "IDLE"
        private final int startTime;
        private final int endTime;
        private final boolean idle;

        public GanttSlice(String label, int startTime, int endTime, boolean idle) {
            this.label = label;
            this.startTime = startTime;
            this.endTime = endTime;
            this.idle = idle;
        }

        public String getLabel() {
            return label;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public boolean isIdle() {
            return idle;
        }

        public int getDuration() {
            return endTime - startTime;
        }
    }

    public static final class ProcessResult {
        private final String pid;
        private final int arrivalTime;
        private final int burstTime;
        private final int priority;
        private final int startTime;
        private final int completionTime;
        private final int turnaroundTime;
        private final int waitingTime;
        private final int responseTime;

        public ProcessResult(String pid, int arrivalTime, int burstTime, int priority,
                int startTime, int completionTime, int turnaroundTime, int waitingTime, int responseTime) {
            this.pid = pid;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.priority = priority;
            this.startTime = startTime;
            this.completionTime = completionTime;
            this.turnaroundTime = turnaroundTime;
            this.waitingTime = waitingTime;
            this.responseTime = responseTime;
        }

        public String getPid() {
            return pid;
        }

        public int getArrivalTime() {
            return arrivalTime;
        }

        public int getBurstTime() {
            return burstTime;
        }

        public int getPriority() {
            return priority;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getCompletionTime() {
            return completionTime;
        }

        public int getTurnaroundTime() {
            return turnaroundTime;
        }

        public int getWaitingTime() {
            return waitingTime;
        }

        public int getResponseTime() {
            return responseTime;
        }
    }

    public static final class ScheduleResult {
        private final Algorithm algorithm;
        private final List<ProcessResult> processResults;
        private final List<GanttSlice> ganttChart;
        private final double averageWaitingTime;
        private final double averageTurnaroundTime;
        private final double averageResponseTime;
        private final double cpuUtilizationPercent;
        private final double throughput;
        private final int totalTime;
        private final int totalIdleTime;

        public ScheduleResult(Algorithm algorithm, List<ProcessResult> processResults, List<GanttSlice> ganttChart,
                double averageWaitingTime, double averageTurnaroundTime, double averageResponseTime,
                double cpuUtilizationPercent, double throughput, int totalTime, int totalIdleTime) {
            this.algorithm = algorithm;
            this.processResults = processResults;
            this.ganttChart = ganttChart;
            this.averageWaitingTime = averageWaitingTime;
            this.averageTurnaroundTime = averageTurnaroundTime;
            this.averageResponseTime = averageResponseTime;
            this.cpuUtilizationPercent = cpuUtilizationPercent;
            this.throughput = throughput;
            this.totalTime = totalTime;
            this.totalIdleTime = totalIdleTime;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public List<ProcessResult> getProcessResults() {
            return processResults;
        }

        public List<GanttSlice> getGanttChart() {
            return ganttChart;
        }

        public double getAverageWaitingTime() {
            return averageWaitingTime;
        }

        public double getAverageTurnaroundTime() {
            return averageTurnaroundTime;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public double getCpuUtilizationPercent() {
            return cpuUtilizationPercent;
        }

        public double getThroughput() {
            return throughput;
        }

        public int getTotalTime() {
            return totalTime;
        }

        public int getTotalIdleTime() {
            return totalIdleTime;
        }
    }
}
