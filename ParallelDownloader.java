import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelDownloader {

    private final HttpClient client;
    private static final int PART_COUNT = 8;
    private DownloadState downloadState;
    private ProgressTracker progress;

    public ParallelDownloader() {
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void download(String link, long fileSize, Path destination) throws Exception {
        if (fileSize <= 0) {
            throw new RuntimeException("Invalid file size: " + fileSize);
        }

        Path stateFile = destination.resolveSibling(
                destination.getFileName() + ".state"
        );

        downloadState = new DownloadState(stateFile);
        downloadState.load();

        boolean resume = link.equals(downloadState.get("url"))
                && String.valueOf(fileSize).equals(downloadState.get("fileSize"))
                && java.nio.file.Files.exists(destination);

        if (!resume) {
            downloadState = new DownloadState(stateFile);
            downloadState.set("url", link);
            downloadState.set("fileSize", String.valueOf(fileSize));

            for (int i = 0; i < PART_COUNT; i++) {
                downloadState.set("part." + i, "false");
            }

            downloadState.save();
        }

        long partSize = (fileSize + PART_COUNT - 1) / PART_COUNT;
        progress = new ProgressTracker(fileSize, PART_COUNT, partSize);

        StandardOpenOption[] options = resume
                ? new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE}
                : new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};

        Thread printer = Thread.ofVirtual().start(() -> {
            try {
                while (true) {
                    progress.print();
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
            }
        });

        try (FileChannel channel = FileChannel.open(destination, options)) {
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> tasks = new ArrayList<>();

                for (int i = 0; i < PART_COUNT; i++) {
                    long start = i * partSize;
                    if (start >= fileSize) break;
                    long end = Math.min(start + partSize - 1, fileSize - 1);

                    if ("true".equals(downloadState.get("part." + i))) {
                        progress.markSkipped(i);
                        continue;
                    }

                    int partIndex = i;
                    long partStart = start;
                    long partEnd = end;

                    tasks.add(executor.submit(() -> {
                        try {
                            downloadPart(link, partStart, partEnd, channel, partIndex);
                            markPartDone(partIndex);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }

                for (Future<?> task : tasks) {
                    task.get();
                }
            }
        } finally {
            printer.interrupt();
        }

        progress.print();
        java.nio.file.Files.deleteIfExists(stateFile);
        System.out.println("Parallel download complete: " + destination);
    }

    private synchronized void markPartDone(int index) {
        downloadState.set("part." + index, "true");
        try {
            downloadState.save();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadPart(
            String link, long start, long end, FileChannel channel, int partIndex
    ) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                .header("Range", "bytes=" + start + "-" + end)
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
        );

        if (response.statusCode() != 206) {
            response.body().close();
            throw new RuntimeException(
                    "Part " + start + "-" + end
                            + " failed, status " + response.statusCode()
            );
        }

        try (InputStream input = response.body()) {
            byte[] buffer = new byte[8192];
            long position = start;
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                progress.add(partIndex, bytesRead);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);

                while (byteBuffer.hasRemaining()) {
                    position += channel.write(byteBuffer, position);
                }
            }

            if (position != end + 1) {
                throw new RuntimeException(
                        "Part " + start + "-" + end
                                + " was incomplete. Ended at " + position
                );
            }
        }
    }
}