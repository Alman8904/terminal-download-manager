import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class SequentialDownloader {

    private final HttpClient client;

    public SequentialDownloader() {
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void download(String link, Path destination) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
        );

        if (response.statusCode() != 200) {
            response.body().close();
            throw new RuntimeException(
                    "Download failed, status " + response.statusCode()
            );
        }

        long total = response.headers().firstValueAsLong("Content-Length").orElse(-1);
        ProgressTracker progress = total > 0 ? new ProgressTracker(total, 1, total) : null;

        long startTime = System.nanoTime();
        long lastPrint = 0;
        long done = 0;

        try (InputStream input = response.body();
             OutputStream output = Files.newOutputStream(destination)) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                done += bytesRead;

                if (progress != null) {
                    progress.add(0, bytesRead);
                }

                long now = System.nanoTime();
                if (now - lastPrint > 200_000_000L) {
                    lastPrint = now;
                    if (progress != null) {
                        progress.print();
                    } else {
                        printUnknownSize(done, startTime);
                    }
                }
            }
        }

        if (progress != null) {
            progress.print();
        } else {
            printUnknownSize(done, startTime);
            System.out.println();
        }

        System.out.println("Sequential download complete: " + destination);
    }

    private void printUnknownSize(long done, long startTime) {
        double seconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
        double speed = seconds > 0 ? done / seconds : 0;
        System.out.printf("\rDownloaded %.1f MB  %.1f MB/s   ", done / 1_048_576.0, speed / 1_048_576);
    }
}