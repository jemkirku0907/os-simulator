# Operating System Simulator (Java Swing GUI)

An interactive, dependency-free Java desktop application implementing **8 core Operating System modules** with dynamic, unlimited user inputs, full input validation, visual diagrams/charts, and side-by-side algorithm comparisons.

## Requirements
- **Java Development Kit (JDK) 11 or newer** (tested on OpenJDK 25; uses only the standard Java SE `javax.swing` / `java.awt` libraries — zero third-party JARs required).

## Quick Start

### Option 1: Run Pre-Built Runnable JAR
```powershell
java -jar OSSimulator.jar
```
Or double-click `run.bat` on Windows.

### Option 2: Compile, Test, and Repackage from Source
```powershell
powershell -ExecutionPolicy Bypass -File build.ps1
```
This compiles `src/`, builds `OSSimulator.jar`, and runs `ossim.EngineTestSuite` to verify all 8 modules.

## Project Structure
```text
OS/
├── src/ossim/
│   ├── Main.java                       # Application entry point
│   ├── model/                          # Immutable models & InputValidator
│   ├── engine/                         # 8 pure OS algorithm engines
│   └── ui/
│       ├── Theme.java                  # Consistent UI palette & helpers
│       ├── MainFrame.java              # Sidebar navigation & CardLayout shell
│       ├── components/                 # GanttChartPanel, DiskChartPanel, MemoryMapPanel
│       └── panels/                     # Overview + 8 interactive module panels
├── test/ossim/EngineTestSuite.java     # Automated small, large & boundary test suite
├── docs/MODULES_AND_TESTS.md           # Algorithm formulas & sample I/O documentation
├── build.ps1                           # Clean build, package JAR & test runner
├── run.bat                             # Windows launcher
└── OSSimulator.jar                     # Runnable executable JAR
```

## Implemented Modules
1. **CPU Scheduling** (`CpuSchedulingPanel` / `CpuSchedulerEngine`): FCFS, SJF (Non-Preemptive), SRTF (Preemptive), Priority (Non-Preemptive & Preemptive), and Round Robin; Gantt chart, WT, TAT, RT, CPU utilization, throughput, and algorithm comparison.
2. **Banker's Algorithm** (`BankersPanel` / `BankerAlgorithmEngine`): Dynamic `P x R` Allocation & Max matrices, Available vector, Need computation, step-by-step Safety trace, Safe Sequence detection, and Resource Request simulation.
3. **Paging** (`PagingPanel` / `PagingEngine`): Dynamic Page Table, physical frame map, single & batch Logical-to-Physical address translation, page fault & invalid page detection.
4. **Page Replacement** (`PageReplacementPanel` / `PageReplacementEngine`): FIFO, LRU, Optimal (OPT), and Clock/Second Chance on variable-length reference strings and arbitrary frame counts, with frame-by-frame trace and comparison.
5. **Segmentation** (`SegmentationPanel` / `SegmentationEngine`): Dynamic Segment Table (Base & Limit), physical overlap detection, bounds checking (`Offset < Limit`), and trap diagnostics.
6. **Fragmentation** (`FragmentationPanel` / `FragmentationEngine`): Fixed and Dynamic partitioning with First Fit, Best Fit, Worst Fit, and Next Fit; visual memory block map showing internal and external fragmentation.
7. **Disk Scheduling** (`DiskSchedulingPanel` / `DiskSchedulerEngine`): FCFS, SSTF, SCAN, C-SCAN, LOOK, and C-LOOK with interactive head-movement trajectory plot, seek breakdown, and comparison.
8. **Multiprogramming** (`MultiprogrammingPanel` / `MultiprogrammingEngine`): Multi-core RAM + CPU/I/O overlap simulator with live Play/Pause/Step scrubber, RAM map, degree of multiprogramming, and uniprogramming baseline comparison.
