package ossim.engine;

import ossim.model.PagingModels.PageTableEntry;
import ossim.model.PagingModels.PagingSimulationResult;
import ossim.model.PagingModels.TranslationResult;
import ossim.model.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Engine for Module 3: Paging & Logical-to-Physical Address Translation.
 */
public final class PagingEngine {

    public PagingSimulationResult translate(
            int pageSize,
            int numFrames,
            List<PageTableEntry> pageTable,
            List<Long> logicalAddresses) throws ValidationException {

        if (pageSize <= 0) {
            throw new ValidationException("Page Size must be greater than 0 bytes.");
        }
        if (numFrames <= 0) {
            throw new ValidationException("Number of Physical Frames must be greater than 0.");
        }
        if (pageTable == null || pageTable.isEmpty()) {
            throw new ValidationException("Page Table must contain at least one page entry.");
        }
        if (logicalAddresses == null || logicalAddresses.isEmpty()) {
            throw new ValidationException("Provide at least one Logical Address to translate.");
        }

        Map<Integer, PageTableEntry> pageMap = new HashMap<>();
        Set<Integer> usedFrames = new HashSet<>();

        for (PageTableEntry entry : pageTable) {
            if (entry.getPageNumber() < 0) {
                throw new ValidationException("Page Number cannot be negative (got " + entry.getPageNumber() + ").");
            }
            if (pageMap.containsKey(entry.getPageNumber())) {
                throw new ValidationException("Duplicate Page Number " + entry.getPageNumber() + " in Page Table.");
            }
            if (entry.isValid()) {
                if (entry.getFrameNumber() < 0 || entry.getFrameNumber() >= numFrames) {
                    throw new ValidationException(
                            "Page " + entry.getPageNumber() + " maps to invalid Frame " + entry.getFrameNumber()
                                    + " (valid range: 0 to " + (numFrames - 1) + ").");
                }
                if (!usedFrames.add(entry.getFrameNumber())) {
                    throw new ValidationException(
                            "Frame " + entry.getFrameNumber() + " is mapped to multiple valid pages.");
                }
            }
            pageMap.put(entry.getPageNumber(), entry);
        }

        List<TranslationResult> results = new ArrayList<>(logicalAddresses.size());
        int validCount = 0;
        int faultCount = 0;

        for (long logicalAddr : logicalAddresses) {
            if (logicalAddr < 0) {
                throw new ValidationException("Logical Address cannot be negative (got " + logicalAddr + ").");
            }
            long pageLong = logicalAddr / pageSize;
            int offset = (int) (logicalAddr % pageSize);
            int pageNum = pageLong > Integer.MAX_VALUE ? -1 : (int) pageLong;

            PageTableEntry entry = pageNum >= 0 ? pageMap.get(pageNum) : null;
            if (entry == null) {
                faultCount++;
                results.add(new TranslationResult(
                        logicalAddr,
                        pageNum >= 0 ? pageNum : (int) Math.min(pageLong, Integer.MAX_VALUE),
                        offset,
                        -1,
                        -1,
                        false,
                        "INVALID PAGE",
                        "Page " + pageLong + " is not in the Page Table (out of logical address space)."));
            } else if (!entry.isValid()) {
                faultCount++;
                results.add(new TranslationResult(
                        logicalAddr,
                        pageNum,
                        offset,
                        -1,
                        -1,
                        false,
                        "PAGE FAULT (Not in RAM)",
                        "Page " + pageNum + " valid bit is 0 (page is on disk / not loaded in a frame)."));
            } else {
                validCount++;
                int frame = entry.getFrameNumber();
                long physicalAddr = (long) frame * pageSize + offset;
                results.add(new TranslationResult(
                        logicalAddr,
                        pageNum,
                        offset,
                        frame,
                        physicalAddr,
                        true,
                        "VALID",
                        "Page " + pageNum + " -> Frame " + frame
                                + "; Physical = (" + frame + " * " + pageSize + ") + " + offset + " = " + physicalAddr));
            }
        }

        return new PagingSimulationResult(pageSize, numFrames, new ArrayList<>(pageTable), results, validCount, faultCount);
    }
}
