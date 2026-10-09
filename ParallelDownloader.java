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

    public ParallelDownloader() {
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void download(String link, long fileSize, Path destination) throws Exception {
        if (fileSize <= 0) {
            throw new RuntimeException("Invalid file size: " + fileSize);
        }

        long partSize = (fileSize + PART_COUNT - 1) / PART_COUNT;

        try (FileChannel channel = FileChannel.open(
                destination,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> tasks = new ArrayList<>();

                for (long start = 0; start < fileSize; start += partSize) {
                    long end = Math.min(start + partSize - 1, fileSize - 1);
                    long partStart = start;
                    long partEnd = end;

                    tasks.add(executor.submit(() -> {
                        try {
                            downloadPart(link, partStart, partEnd, channel);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }

                for (Future<?> task : tasks) {
                    task.get();
                }
            }
        }

        System.out.println("Parallel download complete: " + destination);
    }

    private void downloadPart(
            String link, long start, long end, FileChannel channel
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
