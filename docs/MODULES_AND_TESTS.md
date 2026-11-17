# Module Reference, Algorithms & Sample Inputs/Outputs

## 1. CPU Scheduling
- **Algorithms**: FCFS, SJF (Non-Preemptive), SRTF (Preemptive SJF), Priority (Non-Preemptive & Preemptive, configurable priority order), Round Robin (configurable quantum).
- **Formulas**:
  - `Turnaround Time (TAT) = Completion Time - Arrival Time`
  - `Waiting Time (WT) = Turnaround Time - Burst Time`
  - `Response Time (RT) = First CPU Start Time - Arrival Time`
  - `CPU Utilization = (Total Busy Time / Total Span) * 100%`
- **Sample Input**: `P1(AT=0, BT=8)`, `P2(AT=1, BT=4)`, `P3(AT=2, BT=9)`, `P4(AT=3, BT=5)`
  - FCFS Avg WT = `8.75`, Avg TAT = `15.25`
  - SRTF Avg WT = `6.50`
- **Large Dataset Tested**: 200 randomly generated processes across all 6 algorithms (`EngineTestSuite`) and 60 processes in the GUI preset.

## 2. Banker's Algorithm
- **Formulas & Rules**:
  - `Need[i][j] = Max[i][j] - Allocation[i][j]`
  - Validation: `0 <= Allocation[i][j] <= Max[i][j]`
  - Safety Algorithm finds an execution ordering where `Need[i] <= Work` for each process and updates `Work = Work + Allocation[i]`.
  - Resource Request Algorithm checks `Request <= Need` and `Request <= Available`, tentatively allocates, and runs the Safety check.
- **Sample Input (5x3)**: Classic Silberschatz 5-process, 3-resource dataset (`Available = [3, 3, 2]`) -> **SAFE**, Safe Sequence: `P1 -> P3 -> P4 -> P0 -> P2`.
- **Large Dataset Tested**: `30 x 8` matrix in `EngineTestSuite`; `25 x 6` preset in the GUI.

## 3. Paging
- **Formulas**:
  - `Page Number = Logical Address / Page Size`
  - `Offset = Logical Address % Page Size`
  - If Page is present (`Valid = true`), `Physical Address = (Frame Number * Page Size) + Offset`; otherwise triggers a Page Fault or Invalid Page error.
- **Sample Input**: Page Size = `1024`, Frames = `8`, `Page 0 -> Frame 5`, `Page 1 -> Frame 2`, `Page 3 -> Invalid`
  - Logical `0` -> Physical `5120`
  - Logical `1025` -> Page `1`, Offset `1` -> Physical `2049`
- **Large Dataset Tested**: 256 pages & 200 translations in `EngineTestSuite`; 64 pages & 40 translations in GUI preset.

## 4. Page Replacement
- **Algorithms**: FIFO, LRU, Optimal (OPT), Clock / Second Chance.
- **Sample Input (20 references, 3 frames)**: `7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1`
  - FIFO: `15` faults | LRU: `12` faults | Optimal: `9` faults
- **Large Dataset Tested**: 500 references across 12 frames in `EngineTestSuite`; 200 references across 8 frames in GUI preset.

## 5. Segmentation
- **Formulas & Validation**:
  - Rejects unknown Segment `#`, negative Offset, or `Offset >= Limit` (Addressing Trap).
  - Valid translation: `Physical Address = Base + Offset`.
  - Detects overlapping physical address ranges across segments.
- **Sample Input**: `Seg 0 (Base=1400, Limit=1000)` with `(0, 430)` -> Physical `1830`; `Seg 2 (Base=4300, Limit=400)` with `(2, 500)` -> Trap (`500 >= 400`).
- **Large Dataset Tested**: 100 segments in `EngineTestSuite`; 40 segments in GUI preset.

## 6. Fragmentation
- **Modes & Strategies**: Fixed Partitioning & Dynamic Partitioning with First Fit, Best Fit, Worst Fit, Next Fit.
- **Definitions**:
  - `Internal Fragmentation = Block Size - Requested Process Size` (in Fixed Partitions).
  - `External Fragmentation`: Present when unallocated processes exist whose size `<= Total Free Memory`, because free memory is split across non-contiguous holes.
- **Sample Input**: Blocks `[100, 500, 200, 300, 600]`, Processes `[212, 417, 112, 426]` -> First Fit allocates 3 (P4 fails); Best Fit allocates all 4.
- **Large Dataset Tested**: 150 memory blocks & 120 processes in `EngineTestSuite`; 35 blocks & 30 processes in GUI preset.

## 7. Disk Scheduling
- **Algorithms**: FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK (with configurable Initial Head, Max Cylinder, and Initial Direction).
- **Sample Input**: Queue `[98, 183, 37, 122, 14, 124, 65, 67]`, Head = `53`, Max = `199`
  - FCFS = `640` tracks | SSTF = `236` tracks | SCAN (Down) = `236` tracks | LOOK (Up) = `299` tracks.
- **Large Dataset Tested**: 300 disk requests across all 6 algorithms in `EngineTestSuite`; 120 requests in GUI preset.

## 8. Multiprogramming
- **Simulation Model**:
  - Long-term scheduler admits `NEW` jobs into RAM when free RAM allows; oversized jobs (`Memory > User RAM`) are cleanly `REJECTED`.
  - CPU cores dispatch `READY` processes; when a process performs I/O (`IO_WAIT`), other `READY` processes immediately utilize the CPU, demonstrating higher CPU utilization than uniprogramming.
- **Large Dataset Tested**: 80 concurrent programs on 4 cores in `EngineTestSuite`; 35 programs on 4 cores in GUI preset.
