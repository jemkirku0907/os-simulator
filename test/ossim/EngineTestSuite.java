package ossim;

import ossim.engine.BankerAlgorithmEngine;
import ossim.engine.CpuSchedulerEngine;
import ossim.engine.DiskSchedulerEngine;
import ossim.engine.FragmentationEngine;
import ossim.engine.MultiprogrammingEngine;
import ossim.engine.PageReplacementEngine;
import ossim.engine.PagingEngine;
import ossim.engine.SegmentationEngine;
import ossim.engine.SegmentationEngine.LogicalAddressQuery;
import ossim.model.BankerModels.BankerResult;
import ossim.model.CpuModels.Algorithm;
import ossim.model.CpuModels.ProcessInput;
import ossim.model.CpuModels.ProcessResult;
import ossim.model.CpuModels.ScheduleResult;
import ossim.model.DiskModels;
import ossim.model.DiskModels.Direction;
import ossim.model.DiskModels.DiskResult;
import ossim.model.FragmentationModels.FitStrategy;
import ossim.model.FragmentationModels.FragmentationResult;
import ossim.model.FragmentationModels.MemoryBlockInput;
import ossim.model.FragmentationModels.MemoryProcessInput;
import ossim.model.FragmentationModels.PartitionMode;
import ossim.model.InputValidator;
import ossim.model.MultiprogrammingModels.MultiprogrammingResult;
import ossim.model.MultiprogrammingModels.ProgramInput;
import ossim.model.PageReplacementModels;
import ossim.model.PageReplacementModels.ReplacementResult;
import ossim.model.PagingModels.PageTableEntry;
import ossim.model.PagingModels.PagingSimulationResult;
import ossim.model.SegmentationModels.SegmentEntry;
import ossim.model.SegmentationModels.SegmentationResult;
import ossim.model.ValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Comprehensive verification suite for all 8 OS Simulator engines.
 * Tests textbook manual results, boundary/invalid inputs, and large datasets.
 */
public final class EngineTestSuite {

    private static int passed = 0;

    public static void main(String[] args) throws Exception {
        testCpuSchedulingSmallAndLarge();
        testBankersAlgorithmSmallAndLarge();
        testPagingSmallAndLarge();
        testPageReplacementSmallAndLarge();
        testSegmentationSmallAndLarge();
        testFragmentationSmallAndLarge();
        testDiskSchedulingSmallAndLarge();
        testMultiprogrammingSmallAndLarge();
        testValidationAndBoundaryCases();

        System.out.println("ALL TESTS PASSED (" + passed + " assertions verified).");
    }

    private static void check(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError("FAILED: " + msg);
        }
        passed++;
    }

    private static void checkApprox(double actual, double expected, double eps, String msg) {
        if (Math.abs(actual - expected) > eps) {
            throw new AssertionError("FAILED: " + msg + " (expected " + expected + ", got " + actual + ")");
        }
        passed++;
    }

    private static void expectValidationFailure(RunnableThrowing r, String msg) {
        try {
            r.run();
            throw new AssertionError("FAILED: Expected ValidationException for: " + msg);
        } catch (ValidationException expected) {
            passed++;
        } catch (Exception other) {
            throw new AssertionError("FAILED: Unexpected exception for " + msg + ": " + other);
        }
    }

    @FunctionalInterface
    private interface RunnableThrowing {
        void run() throws Exception;
    }

    private static void testCpuSchedulingSmallAndLarge() throws ValidationException {
        CpuSchedulerEngine engine = new CpuSchedulerEngine();
        // Textbook FCFS/SJF/SRTF/RR case:
        // P1(AT=0, BT=8), P2(AT=1, BT=4), P3(AT=2, BT=9), P4(AT=3, BT=5)
        List<ProcessInput> procs = Arrays.asList(
                new ProcessInput("P1", 0, 8, 3),
                new ProcessInput("P2", 1, 4, 1),
                new ProcessInput("P3", 2, 9, 4),
                new ProcessInput("P4", 3, 5, 2));

        ScheduleResult fcfs = engine.schedule(procs, Algorithm.FCFS, 1, true);
        // Completion times: P1=8, P2=12, P3=21, P4=26 -> WT: 0, 7, 10, 18 -> Avg = 8.75
        checkApprox(fcfs.getAverageWaitingTime(), 8.75, 1e-6, "FCFS avg WT");
        checkApprox(fcfs.getAverageTurnaroundTime(), 15.25, 1e-6, "FCFS avg TAT");

        ScheduleResult srtf = engine.schedule(procs, Algorithm.SRTF_PREEMPTIVE, 1, true);
        // Classic Silberschatz SRTF: P1 runs 0..1(rem=7), P2 runs 1..5(done, CT=5), P4 runs 5..10(done, CT=10),
        // P1 runs 10..17(done, CT=17), P3 runs 17..26(done, CT=26)
        // WTs: P1=17-8=9, P2=5-1-4=0, P3=26-2-9=15, P4=10-3-5=2 -> Avg = 26/4 = 6.5
        checkApprox(srtf.getAverageWaitingTime(), 6.5, 1e-6, "SRTF avg WT");

        // Round Robin with idle gap test: P1(AT=2, BT=5), P2(AT=4, BT=3), Q=2
        List<ProcessInput> rrProcs = Arrays.asList(
                new ProcessInput("P1", 2, 5, 1),
                new ProcessInput("P2", 4, 3, 1));
        ScheduleResult rr = engine.schedule(rrProcs, Algorithm.ROUND_ROBIN, 2, true);
        check(rr.getTotalIdleTime() == 2, "RR initial idle time = 2");
        check(rr.getTotalTime() == 10, "RR total time = 10");

        // Priority Preemptive (lower # = higher priority)
        ScheduleResult prio = engine.schedule(procs, Algorithm.PRIORITY_PREEMPTIVE, 1, true);
        ProcessResult p2Res = prio.getProcessResults().get(1);
        check(p2Res.getCompletionTime() == 5, "Priority Preemptive P2 completes at 5");

        // Large dataset: 200 processes across all 6 algorithms
        List<ProcessInput> large = new ArrayList<>();
        Random rng = new Random(1);
        for (int i = 1; i <= 200; i++) {
            large.add(new ProcessInput("P" + i, rng.nextInt(300), 1 + rng.nextInt(30), 1 + rng.nextInt(10)));
        }
        for (Algorithm algo : Algorithm.values()) {
            ScheduleResult r = engine.schedule(large, algo, 4, true);
            check(r.getProcessResults().size() == 200, "Large CPU " + algo + " 200 results");
            check(r.getAverageTurnaroundTime() >= r.getAverageWaitingTime(), "Large CPU TAT >= WT");
        }
    }

    private static void testBankersAlgorithmSmallAndLarge() throws ValidationException {
        BankerAlgorithmEngine engine = new BankerAlgorithmEngine();
        // Silberschatz classic 5x3 safe state
        int[][] alloc = {
                { 0, 1, 0 },
                { 2, 0, 0 },
                { 3, 0, 2 },
                { 2, 1, 1 },
                { 0, 0, 2 }
        };
        int[][] max = {
                { 7, 5, 3 },
                { 3, 2, 2 },
                { 9, 0, 2 },
                { 2, 2, 2 },
                { 4, 3, 3 }
        };
        int[] avail = { 3, 3, 2 };

        BankerResult safeRes = engine.evaluateSafety(alloc, max, avail);
        check(safeRes.isSafe(), "Classic 5x3 Banker's is safe");
        check(safeRes.getSafeSequence().equals(Arrays.asList("P1", "P3", "P4", "P0", "P2")),
                "Classic 5x3 safe sequence matches P1->P3->P4->P0->P2");

        // Request from P1: (1, 0, 2) should be safely granted
        BankerResult reqRes = engine.evaluateRequest(alloc, max, avail, 1, new int[] { 1, 0, 2 });
        check(reqRes.isSafe(), "P1 request (1,0,2) safely granted");

        // Unsafe test
        int[] lowAvail = { 0, 0, 0 };
        BankerResult unsafeRes = engine.evaluateSafety(alloc, max, lowAvail);
        check(!unsafeRes.isSafe(), "Zero available is unsafe");

        // Large dataset: 30x8
        int P = 30, R = 8;
        int[][] lAlloc = new int[P][R];
        int[][] lMax = new int[P][R];
        Random rng = new Random(10);
        for (int i = 0; i < P; i++) {
            for (int j = 0; j < R; j++) {
                lAlloc[i][j] = rng.nextInt(4);
                lMax[i][j] = lAlloc[i][j] + rng.nextInt(5);
            }
        }
        int[] lAvail = { 8, 8, 8, 8, 8, 8, 8, 8 };
        BankerResult largeRes = engine.evaluateSafety(lAlloc, lMax, lAvail);
        check(largeRes.isSafe() && largeRes.getSafeSequence().size() == 30, "Large 30x8 Banker's safe");
    }

    private static void testPagingSmallAndLarge() throws ValidationException {
        PagingEngine engine = new PagingEngine();
        List<PageTableEntry> pt = Arrays.asList(
                new PageTableEntry(0, 5, true),
                new PageTableEntry(1, 2, true),
                new PageTableEntry(2, -1, false));
        PagingSimulationResult res = engine.translate(1024, 8, pt, Arrays.asList(0L, 1025L, 2050L, 5000L));
        check(res.getTranslations().get(0).getPhysicalAddress() == 5120L, "Page 0 offset 0 -> 5120");
        check(res.getTranslations().get(1).getPhysicalAddress() == 2049L, "Page 1 offset 1 -> 2049");
        check(!res.getTranslations().get(2).isValid(), "Page 2 invalid -> Page Fault");
        check(!res.getTranslations().get(3).isValid(), "Page 4 unmapped -> Invalid Page");

        // Large dataset: 256 pages, 200 translations
        List<PageTableEntry> largePt = new ArrayList<>();
        for (int p = 0; p < 256; p++) {
            largePt.add(new PageTableEntry(p, 511 - p, true));
        }
        List<Long> addrs = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            addrs.add((long) i * 4096 + 123);
        }
        PagingSimulationResult largeRes = engine.translate(4096, 512, largePt, addrs);
        check(largeRes.getValidCount() == 200, "Large paging 200 valid translations");
    }

    private static void testPageReplacementSmallAndLarge() throws ValidationException {
        PageReplacementEngine engine = new PageReplacementEngine();
        // Silberschatz reference string: 7,0,1,2,0,3,0,4,2,3,0,3,2,1,2,0,1,7,0,1 with 3 frames
        // Known textbook faults: FIFO=15, LRU=12, Optimal=9
        List<Integer> refs = Arrays.asList(7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1);

        ReplacementResult fifo = engine.simulate(refs, 3, PageReplacementModels.Algorithm.FIFO);
        ReplacementResult lru = engine.simulate(refs, 3, PageReplacementModels.Algorithm.LRU);
        ReplacementResult opt = engine.simulate(refs, 3, PageReplacementModels.Algorithm.OPTIMAL);
        ReplacementResult clk = engine.simulate(refs, 3, PageReplacementModels.Algorithm.SECOND_CHANCE);

        check(fifo.getPageFaults() == 15, "Textbook FIFO faults = 15 (got " + fifo.getPageFaults() + ")");
        check(lru.getPageFaults() == 12, "Textbook LRU faults = 12 (got " + lru.getPageFaults() + ")");
        check(opt.getPageFaults() == 9, "Textbook Optimal faults = 9 (got " + opt.getPageFaults() + ")");
        check(clk.getPageHits() + clk.getPageFaults() == refs.size(), "Clock hits + faults = total");

        // Belady's Anomaly check for FIFO: 1,2,3,4,1,2,5,1,2,3,4,5 (3 frames=9 faults, 4 frames=10 faults)
        List<Integer> belady = Arrays.asList(1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5);
        check(engine.simulate(belady, 3, PageReplacementModels.Algorithm.FIFO).getPageFaults() == 9, "Belady 3 frames = 9");
        check(engine.simulate(belady, 4, PageReplacementModels.Algorithm.FIFO).getPageFaults() == 10, "Belady 4 frames = 10");

        // Large dataset: 500 references across 12 frames
        List<Integer> largeRefs = new ArrayList<>(500);
        Random rng = new Random(7);
        for (int i = 0; i < 500; i++) {
            largeRefs.add(rng.nextInt(30));
        }
        ReplacementResult largeOpt = engine.simulate(largeRefs, 12, PageReplacementModels.Algorithm.OPTIMAL);
        ReplacementResult largeFifo = engine.simulate(largeRefs, 12, PageReplacementModels.Algorithm.FIFO);
        check(largeOpt.getPageFaults() <= largeFifo.getPageFaults(), "Optimal <= FIFO on 500 refs");
    }

    private static void testSegmentationSmallAndLarge() throws ValidationException {
        SegmentationEngine engine = new SegmentationEngine();
        List<SegmentEntry> segs = Arrays.asList(
                new SegmentEntry(0, "Code", 1400, 1000),
                new SegmentEntry(1, "Data", 6300, 400),
                new SegmentEntry(2, "Stack", 4300, 400));
        List<LogicalAddressQuery> queries = Arrays.asList(
                new LogicalAddressQuery(0, 430),
                new LogicalAddressQuery(1, 10),
                new LogicalAddressQuery(2, 500),
                new LogicalAddressQuery(5, 10));
        SegmentationResult res = engine.simulate(segs, queries);
        check(res.getValidCount() == 2 && res.getErrorCount() == 2, "Segmentation 2 valid, 2 errors");
        check(res.getTranslations().get(0).getPhysicalAddress() == 1830L, "Seg 0 offset 430 -> 1830");

        // Large dataset: 100 segments
        List<SegmentEntry> largeSegs = new ArrayList<>();
        List<LogicalAddressQuery> largeQ = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeSegs.add(new SegmentEntry(i, "S" + i, i * 1000L, 500L));
            largeQ.add(new LogicalAddressQuery(i, i % 2 == 0 ? 250L : 600L));
        }
        SegmentationResult largeRes = engine.simulate(largeSegs, largeQ);
        check(largeRes.getValidCount() == 50 && largeRes.getErrorCount() == 50, "Large 100 segments 50/50 split");
    }

    private static void testFragmentationSmallAndLarge() throws ValidationException {
        FragmentationEngine engine = new FragmentationEngine();
        // Silberschatz classic: Blocks 100, 500, 200, 300, 600; Processes 212, 417, 112, 426
        List<MemoryBlockInput> blocks = Arrays.asList(
                new MemoryBlockInput("B1", 100),
                new MemoryBlockInput("B2", 500),
                new MemoryBlockInput("B3", 200),
                new MemoryBlockInput("B4", 300),
                new MemoryBlockInput("B5", 600));
        List<MemoryProcessInput> procs = Arrays.asList(
                new MemoryProcessInput("P1", 212),
                new MemoryProcessInput("P2", 417),
                new MemoryProcessInput("P3", 112),
                new MemoryProcessInput("P4", 426));

        FragmentationResult firstFit = engine.simulate(blocks, procs, FitStrategy.FIRST_FIT, PartitionMode.FIXED_PARTITIONS);
        // In First Fit, P4 (426) fails because only B1(100), B4(300) remain free!
        check(firstFit.getAllocatedProcessCount() == 3 && firstFit.getUnallocatedProcessCount() == 1,
                "First Fit leaves P4 unallocated");

        FragmentationResult bestFit = engine.simulate(blocks, procs, FitStrategy.BEST_FIT, PartitionMode.FIXED_PARTITIONS);
        // Best Fit places all 4: P1->B4(300), P2->B2(500), P3->B3(200), P4->B5(600)
        check(bestFit.getAllocatedProcessCount() == 4 && bestFit.getUnallocatedProcessCount() == 0,
                "Best Fit allocates all 4 processes");

        // Large dataset: 150 blocks, 120 processes
        List<MemoryBlockInput> lBlocks = new ArrayList<>();
        List<MemoryProcessInput> lProcs = new ArrayList<>();
        Random rng = new Random(19);
        for (int i = 1; i <= 150; i++) {
            lBlocks.add(new MemoryBlockInput("B" + i, 100 + rng.nextInt(500)));
        }
        for (int i = 1; i <= 120; i++) {
            lProcs.add(new MemoryProcessInput("P" + i, 50 + rng.nextInt(400)));
        }
        FragmentationResult lRes = engine.simulate(lBlocks, lProcs, FitStrategy.BEST_FIT, PartitionMode.DYNAMIC_PARTITIONS);
        check(lRes.getTotalInternalFragmentation() == 0, "Dynamic partitioning has 0 internal fragmentation");
    }

    private static void testDiskSchedulingSmallAndLarge() throws ValidationException {
        DiskSchedulerEngine engine = new DiskSchedulerEngine();
        // Silberschatz textbook queue: 98, 183, 37, 122, 14, 124, 65, 67, Head=53, Max=199
        // FCFS = 640, SSTF = 236
        List<Integer> queue = Arrays.asList(98, 183, 37, 122, 14, 124, 65, 67);

        DiskResult fcfs = engine.schedule(queue, 53, 199, DiskModels.Algorithm.FCFS, Direction.UP);
        DiskResult sstf = engine.schedule(queue, 53, 199, DiskModels.Algorithm.SSTF, Direction.UP);
        DiskResult scanDown = engine.schedule(queue, 53, 199, DiskModels.Algorithm.SCAN, Direction.DOWN);
        DiskResult lookUp = engine.schedule(queue, 53, 199, DiskModels.Algorithm.LOOK, Direction.UP);

        check(fcfs.getTotalHeadMovement() == 640, "Textbook Disk FCFS = 640 (got " + fcfs.getTotalHeadMovement() + ")");
        check(sstf.getTotalHeadMovement() == 236, "Textbook Disk SSTF = 236 (got " + sstf.getTotalHeadMovement() + ")");
        check(scanDown.getTotalHeadMovement() == 236, "Textbook Disk SCAN DOWN = 236 (got " + scanDown.getTotalHeadMovement() + ")");
        check(lookUp.getTotalHeadMovement() == 299, "Disk LOOK UP = 299 (got " + lookUp.getTotalHeadMovement() + ")");

        // Large dataset: 300 requests
        List<Integer> largeQ = new ArrayList<>();
        Random rng = new Random(88);
        for (int i = 0; i < 300; i++) {
            largeQ.add(rng.nextInt(5000));
        }
        for (DiskModels.Algorithm a : DiskModels.Algorithm.values()) {
            DiskResult r = engine.schedule(largeQ, 2500, 4999, a, Direction.UP);
            check(r.getServicedRequests().size() == 300, "Large Disk " + a + " serviced all 300");
        }
    }

    private static void testMultiprogrammingSmallAndLarge() throws ValidationException {
        MultiprogrammingEngine engine = new MultiprogrammingEngine();
        List<ProgramInput> progs = Arrays.asList(
                new ProgramInput("P1", 0, 6, 8, 256, 1),
                new ProgramInput("P2", 0, 5, 6, 256, 2),
                new ProgramInput("P3", 1, 8, 4, 192, 1),
                new ProgramInput("P4", 2, 4, 7, 256, 3),
                new ProgramInput("P5", 0, 2000, 1, 2048, 1) // exceeds RAM -> should be REJECTED gracefully
        );
        MultiprogrammingResult res = engine.simulate(progs, 1024, 256, 1, 3, 0);
        check(res.getCompletedCount() == 4, "4 programs completed");
        check(res.getRejectedCount() == 1, "Oversized P5 rejected gracefully");
        check(res.getCpuUtilizationPercent() > res.getUniprogrammingCpuUtilPercent() * 0.9,
                "Multiprogramming overlaps CPU & IO");

        // Large dataset: 80 programs across 4 cores
        List<ProgramInput> large = new ArrayList<>();
        Random rng = new Random(33);
        for (int i = 1; i <= 80; i++) {
            large.add(new ProgramInput("P" + i, rng.nextInt(20), 2 + rng.nextInt(10),
                    1 + rng.nextInt(8), 64 + rng.nextInt(4) * 64, 1));
        }
        MultiprogrammingResult largeRes = engine.simulate(large, 4096, 512, 4, 4, 0);
        check(largeRes.getCompletedCount() == 80, "Large 80 programs all completed");
    }

    private static void testValidationAndBoundaryCases() {
        expectValidationFailure(() -> InputValidator.parsePositiveInt("", "Field"), "Blank field");
        expectValidationFailure(() -> InputValidator.parsePositiveInt("-5", "Field"), "Negative positive-only field");
        expectValidationFailure(() -> InputValidator.parseNonNegativeInt("abc", "Field"), "Non-numeric string");
        expectValidationFailure(() -> InputValidator.parseIntList("1, 2, x, 4", "List", 0, 100), "Invalid item in list");
        expectValidationFailure(() -> new CpuSchedulerEngine().schedule(
                Arrays.asList(new ProcessInput("P1", 0, 0, 1)), Algorithm.FCFS, 1, true), "Zero burst time");
        expectValidationFailure(() -> new BankerAlgorithmEngine().evaluateSafety(
                new int[][] { { 5, 0 } }, new int[][] { { 3, 2 } }, new int[] { 1, 1 }), "Allocation > Max");
        expectValidationFailure(() -> new DiskSchedulerEngine().schedule(
                Arrays.asList(250), 50, 199, DiskModels.Algorithm.FCFS, Direction.UP), "Track > MaxCylinder");
    }
}
