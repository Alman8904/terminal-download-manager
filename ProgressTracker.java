import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public class ProgressTracker {

    private final int parts;
    private final long total;
    private final long[] partTotals;
    private final AtomicLongArray partDone;
    private final AtomicLong skipped = new AtomicLong();
    private final long startTime = System.nanoTime();
    private boolean drawnBefore = false;

    public ProgressTracker(long fileSize, int parts, long partSize) {
        this.total = fileSize;
        this.parts = parts;
        this.partTotals = new long[parts];
        this.partDone = new AtomicLongArray(parts);

        for (int i = 0; i < parts; i++) {
            long start = i * partSize;
            long end = Math.min(start + partSize - 1, fileSize - 1);
            partTotals[i] = start >= fileSize ? 0 : end - start + 1;
        }
    }

    public void add(int part, long bytes) {
        partDone.addAndGet(part, bytes);
    }

    public void markSkipped(int part) {
        partDone.set(part, partTotals[part]);
        skipped.addAndGet(partTotals[part]);
    }

    private String bar(long done, long size) {
        int percent = size == 0 ? 100 : (int) (done * 100 / size);
        int filled = percent * 20 / 100;
        return "[" + "#".repeat(filled) + "-".repeat(20 - filled) + "] "
                + String.format("%3d%%", percent);
    }

    public synchronized void print() {
        boolean showParts = parts > 1;

        if (drawnBefore) {
            Terminal.moveUp(showParts ? parts + 1 : 1);
        }
        drawnBefore = true;

        long sum = 0;
        for (int i = 0; i < parts; i++) {
            long done = partDone.get(i);
            sum += done;
            if (showParts) {
                Terminal.printLine("Part " + i + "  " + bar(done, partTotals[i]));
            }
        }

        double seconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
        double speed = seconds > 0 ? (sum - skipped.get()) / seconds : 0;
        String eta = speed > 0 ? ((long) ((total - sum) / speed)) + "s" : "--";

        Terminal.printLine("Total   " + bar(sum, total)
                + String.format("  %.1f MB/s  ETA %s", speed / 1_048_576, eta));

        System.out.flush();
    }
}